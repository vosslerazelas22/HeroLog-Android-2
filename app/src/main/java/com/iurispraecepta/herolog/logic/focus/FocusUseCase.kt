package com.iurispraecepta.herolog.logic.focus

import com.iurispraecepta.herolog.data.JsonConfig
import com.iurispraecepta.herolog.data.entity.PendingRewardCelebrationEntity
import com.iurispraecepta.herolog.data.repository.CharacterRepository
import com.iurispraecepta.herolog.data.repository.FocusSessionRepository
import com.iurispraecepta.herolog.data.repository.PendingRewardRepository
import com.iurispraecepta.herolog.logic.achievements.AchievementDetection
import com.iurispraecepta.herolog.model.CharacterState
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Date
import kotlin.random.Random

/**
 * Camada compartilhada de conclusão de sessão de foco (spec-008, pré-requisito
 * arquitetural, FR-5).
 *
 * Extrai o fluxo calcular → aplicar → enfileirar celebração para que tanto o
 * ViewModel (app aberto, com notas/tag editadas pelo usuário no último step do
 * FocusCompletionFlow) quanto o futuro `FocusSessionService` (app fechado, com
 * os defaults `editedNotes = ""` / `selectedTag = null` — decisão registrada no
 * plano da spec-008) chamem a mesma lógica sem duplicação.
 *
 * Semântica idêntica a `HeroLogViewModel.confirmFocusSession` + `onFocusSessionCompleted`:
 * cálculo congelado via `pendingCalculation` (nunca recalculado — anti-reroll de loot),
 * detecção de achievements em dois passos (`detectNewAchievementIds` + `withAchievements`,
 * idempotente), e `totalGold = goldEarned + dungeonClearGoldBonus` na celebração
 * (mesmo valor gravado no `HistoryEntry`).
 */
class FocusUseCase(
    private val characterRepository: CharacterRepository,
    private val focusSessionRepository: FocusSessionRepository,
    private val pendingRewardRepository: PendingRewardRepository,
    private val json: Json = JsonConfig.default
) {
    /**
     * Fase 1: calcula a recompensa e a persiste congelada na sessão ativa.
     * Retorna `null` se não houver personagem persistido.
     */
    suspend fun calculateRewards(
        config: FocusSessionConfig,
        studiedMinutes: Int,
        endTimeMillis: Long = System.currentTimeMillis() + studiedMinutes * 60_000L,
        activeEventType: AmbientEventType? = null,
        random: Random = Random.Default
    ): FocusRewardsCalculation? {
        val state = characterRepository.getCharacterState() ?: return null
        val calc = FocusRewardsLogic.calculate(
            state = state,
            config = config,
            studiedMinutes = studiedMinutes,
            activeEventType = activeEventType,
            random = random
        )
        focusSessionRepository.saveSession(
            PersistedFocusSession(
                config = config,
                durationMinutes = studiedMinutes,
                endTimeMillis = endTimeMillis,
                pendingCalculation = calc
            )
        )
        return calc
    }

    /**
     * Fase 2: aplica a recompensa, persiste o personagem e enfileira a celebração.
     * Retorna `null` se não houver personagem persistido.
     */
    suspend fun applyRewards(
        calc: FocusRewardsCalculation,
        editedNotes: String = "",
        selectedTag: String? = null,
        referenceDate: Date = Date()
    ): CharacterState? {
        val previous = characterRepository.getCharacterState() ?: return null
        val candidate = FocusApplyLogic.apply(
            state = previous,
            calc = calc,
            editedNotes = editedNotes,
            selectedTag = selectedTag,
            referenceDate = referenceDate
        )
        val newIds = AchievementDetection.detectNewAchievementIds(previous, candidate)
        val finalState = AchievementDetection.withAchievements(candidate, newIds)
        characterRepository.saveCharacterState(finalState)
        queueCelebration(calc, previous, finalState, newIds)
        return finalState
    }

    /**
     * Registra a celebração pendente (spec-008, FR-10). Nível de skill comparado
     * campo a campo (nunca igualdade de objeto); achievements vindos da detecção
     * central (nunca lista hardcoded).
     */
    suspend fun queueCelebration(
        calc: FocusRewardsCalculation,
        previous: CharacterState,
        newState: CharacterState,
        achievementIds: List<String>
    ) {
        val previousLevel = previous.skills.getOrNull(calc.skillIdx)?.level
        val newLevel = newState.skills.getOrNull(calc.skillIdx)?.level
        val leveledUp = previousLevel != null && newLevel != null && newLevel > previousLevel
        pendingRewardRepository.queue(
            PendingRewardCelebrationEntity(
                skillName = calc.skillName,
                durationMinutes = calc.durationMins,
                xpGained = calc.xpEarned,
                goldGained = calc.goldEarned + calc.dungeonClearGoldBonus,
                lootedItems = json.encodeToString(calc.lootedItems),
                droppedTitle = calc.droppedTitle?.let { json.encodeToString(it) },
                leveledUp = leveledUp,
                previousLevel = previousLevel?.takeIf { leveledUp },
                newLevel = newLevel?.takeIf { leveledUp },
                achievementsUnlocked = json.encodeToString(achievementIds)
            )
        )
    }
}
