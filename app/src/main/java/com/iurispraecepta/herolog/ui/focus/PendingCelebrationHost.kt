package com.iurispraecepta.herolog.ui.focus

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.iurispraecepta.herolog.data.JsonConfig
import com.iurispraecepta.herolog.data.entity.PendingRewardCelebrationEntity
import com.iurispraecepta.herolog.logic.achievements.AchievementCatalog.ACHIEVEMENTS_LIST
import com.iurispraecepta.herolog.logic.focus.AggregatedCelebrationSummary
import com.iurispraecepta.herolog.logic.focus.DroppedTitle
import com.iurispraecepta.herolog.logic.focus.FocusRewardsCalculation
import com.iurispraecepta.herolog.logic.focus.LootItem

/**
 * Hospedeiro fullscreen das celebrações pendentes (plano-focus-completion-flow).
 *
 * Roteamento — `sessionCount` como único critério (sem parâmetro de ciclo de vida):
 * - `sessionCount == 1`: fluxo completo ([FocusCompletionFlow]), alimentado pelos
 *   snapshots gravados na entidade (`streakAfter`, `showStreakCelebration`,
 *   `pauseCount`, `historyId`).
 * - `sessionCount >= 2`: compacto ([MultipleSessionCelebrationContent]) dentro do
 *   mesmo [CompletionShell] do completo (mesmo fundo, insets e IME).
 *
 * Sem `Dialog`/`ModalBottomSheet`/`HeroLogModal`: camada fullscreen como em `710a8036`.
 */
enum class CelebrationMode {
    SINGLE,
    AGGREGATED
}

fun selectCelebrationMode(sessionCount: Int): CelebrationMode =
    if (sessionCount == 1) CelebrationMode.SINGLE else CelebrationMode.AGGREGATED

@Composable
fun PendingCelebrationHost(
    summary: AggregatedCelebrationSummary,
    onConfirmSingle: (historyId: String?, notes: String, tag: String) -> Unit,
    onDismissAggregated: () -> Unit,
    modifier: Modifier = Modifier,
    skills: List<com.iurispraecepta.herolog.model.Skill> = emptyList()
) {
    when (selectCelebrationMode(summary.sessionCount)) {
        CelebrationMode.SINGLE -> {
            val session = summary.sessions.first()
            val rewardsCalculation = rebuildRewardsCalculation(session)
            // Tags resolvidas pela skill real (a entidade não guarda tags).
            val skillTags =
                skills.firstOrNull { it.name == session.skillName }?.tags ?: emptyList()
            val newAchievements = runCatching {
                JsonConfig.default.decodeFromString<List<String>>(session.achievementsUnlocked)
            }.getOrDefault(emptyList()).mapNotNull { id ->
                ACHIEVEMENTS_LIST.firstOrNull { it.id == id }
            }
            // O fluxo calcula `streakPreview = streak + 1`: recebe o streak
            // PRÉ-sessão, como em 710a8036 (`characterState.streak` antes do apply).
            val streakForFlow = if (session.showStreakCelebration) {
                (session.streakAfter - 1).coerceAtLeast(0)
            } else {
                session.streakAfter
            }

            FocusCompletionFlow(
                rewardsCalculation = rewardsCalculation,
                pauseCount = session.pauseCount,
                streak = streakForFlow,
                shouldShowStreakCelebration = session.showStreakCelebration,
                skillTags = skillTags,
                newAchievements = newAchievements,
                initialNotes = "",
                onConfirm = { notes: String, tag: String ->
                    onConfirmSingle(session.historyId, notes, tag)
                },
                modifier = modifier.fillMaxSize()
            )
        }
        CelebrationMode.AGGREGATED -> {
            CompletionShell(
                onNext = onDismissAggregated,
                isLastStep = true,
                modifier = modifier.fillMaxSize()
            ) {
                MultipleSessionCelebrationContent(
                    summary = summary,
                    skills = skills
                )
            }
        }
    }
}

/**
 * Reconstrói FocusRewardsCalculation a partir de PendingRewardCelebrationEntity.
 * Usa os valores persistidos (sem recalcular) — anti-reroll.
 */
private fun rebuildRewardsCalculation(entity: PendingRewardCelebrationEntity): FocusRewardsCalculation {
    val lootedItems = runCatching { JsonConfig.default.decodeFromString<List<LootItem>>(entity.lootedItems) }
        .getOrDefault(emptyList())

    val droppedTitle = entity.droppedTitle?.let {
        runCatching { JsonConfig.default.decodeFromString<DroppedTitle>(it) }.getOrNull()
    }

    return FocusRewardsCalculation(
        skillIdx = 0, // não persistido; 0 é placeholder
        skillName = entity.skillName,
        xpEarned = entity.xpGained,
        goldEarned = entity.goldGained,
        durationMins = entity.durationMinutes,
        dungeonClearGoldBonus = 0, // já somado em goldGained no queue; 0 evita dupla contagem
        hasUsedDoubleLoot = false,
        hasUsedFocusElixir = false,
        hasUsedRuneFortune = false,
        hasUsedCrystalClarity = false,
        usedEquipmentIndicesAndCharges = emptyList(),
        lootedItems = lootedItems,
        droppedTitle = droppedTitle,
        isWildernessChecked = false,
        isDungeonMode = false,
        comboBonusPercent = 0
    )
}
