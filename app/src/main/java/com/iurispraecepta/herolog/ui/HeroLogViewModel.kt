package com.iurispraecepta.herolog.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iurispraecepta.herolog.data.createInitialCharacterState
import com.iurispraecepta.herolog.data.repository.CharacterRepository
import com.iurispraecepta.herolog.data.repository.FocusSessionRepository
import com.iurispraecepta.herolog.data.repository.PendingRewardRepository
import com.iurispraecepta.herolog.logic.CombatLogic
import com.iurispraecepta.herolog.logic.EquipTitleResult
import com.iurispraecepta.herolog.logic.InventoryLogic
import com.iurispraecepta.herolog.logic.character.LevelUpEvent
import com.iurispraecepta.herolog.logic.character.LevelUpLogic
import com.iurispraecepta.herolog.logic.SaveImportOutcome
import com.iurispraecepta.herolog.logic.SaveImportResult
import com.iurispraecepta.herolog.logic.SaveMigrationLogic
import com.iurispraecepta.herolog.logic.TitleLogic
import com.iurispraecepta.herolog.logic.TitlePurchaseResult
import com.iurispraecepta.herolog.logic.SkillLogic
import com.iurispraecepta.herolog.logic.SkillOperationResult
import com.iurispraecepta.herolog.logic.SkillError
import com.iurispraecepta.herolog.logic.DeleteSkillEligibility
import com.iurispraecepta.herolog.model.ChecklistItem
import com.iurispraecepta.herolog.model.Daily
import com.iurispraecepta.herolog.model.Difficulty
import com.iurispraecepta.herolog.model.Habit
import com.iurispraecepta.herolog.model.RepeatInterval
import com.iurispraecepta.herolog.model.Skill
import com.iurispraecepta.herolog.model.Todo
import com.iurispraecepta.herolog.logic.quests.DailyLogic
import com.iurispraecepta.herolog.logic.quests.HabitLogic
import com.iurispraecepta.herolog.logic.quests.QuestApplyLogic
import com.iurispraecepta.herolog.logic.quests.QuestLogic
import com.iurispraecepta.herolog.logic.quests.RolloverLogic
import com.iurispraecepta.herolog.logic.quests.TodoLogic
import com.iurispraecepta.herolog.logic.focus.BreakTimerState
import com.iurispraecepta.herolog.logic.focus.FocusSessionConfig
import com.iurispraecepta.herolog.logic.focus.FocusSessionState
import com.iurispraecepta.herolog.logic.focus.FocusUseCase
import com.iurispraecepta.herolog.logic.focus.PersistedFocusSession
import com.iurispraecepta.herolog.logic.focus.AggregatedCelebrationSummary
import com.iurispraecepta.herolog.logic.focus.WILDERNESS_GRACE_PERIOD_SECONDS
import com.iurispraecepta.herolog.logic.focus.WildernessInfractionOutcome
import com.iurispraecepta.herolog.logic.focus.resolveWildernessInfraction
import com.iurispraecepta.herolog.logic.focus.resolveCognitiveDeath
import com.iurispraecepta.herolog.logic.focus.resolveRespawn
import com.iurispraecepta.herolog.model.CharClass
import com.iurispraecepta.herolog.model.CharacterState
import com.iurispraecepta.herolog.model.DailyReportData
import com.iurispraecepta.herolog.model.InventoryItem
import com.iurispraecepta.herolog.model.LogEntry
import com.iurispraecepta.herolog.model.OrbConcept
import com.iurispraecepta.herolog.logic.achievements.Achievement
import com.iurispraecepta.herolog.logic.achievements.AchievementCatalog
import com.iurispraecepta.herolog.logic.achievements.AchievementDetection
import com.iurispraecepta.herolog.logic.quests.getDifficultyRewards
import com.iurispraecepta.herolog.service.FocusSessionService
import com.iurispraecepta.herolog.service.ServicePhase
import com.iurispraecepta.herolog.service.completeSessionFromBackup
import com.iurispraecepta.herolog.ui.sfx.SfxManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

data class ProcessedQuest(
    val id: String,
    val name: String,
    val desc: String,
    val target: Int,
    val rewardGold: Int,
    val rewardXp: Int,
    val progress: Int,
    val isCompleted: Boolean,
    val isClaimed: Boolean
)

class HeroLogViewModel(
    private val repository: CharacterRepository,
    private val focusSessionRepository: FocusSessionRepository,
    private val sfxManager: SfxManager,
    private val clock: () -> Long = { System.currentTimeMillis() },
    private val pendingRewardRepository: PendingRewardRepository? = null
) : ViewModel() {

    private val _characterState = MutableStateFlow<CharacterState?>(null)
    val characterState: StateFlow<CharacterState?> = _characterState.asStateFlow()

    private val _focusSessionState = MutableStateFlow(FocusSessionState())
    val focusSessionState: StateFlow<FocusSessionState> = _focusSessionState.asStateFlow()

    private val _dungeonSessionsProgress = MutableStateFlow(0)
    val dungeonSessionsProgress: StateFlow<Int> = _dungeonSessionsProgress.asStateFlow()

    private val _breakTimerState = MutableStateFlow(BreakTimerState())
    val breakTimerState: StateFlow<BreakTimerState> = _breakTimerState.asStateFlow()

    // Porte de `levelUpQueue` (useLevelUp.ts). `activeLevelUp`/`hasPendingLevelUps` da fonte sao
    // apenas derivados dessa fila (queue[0] / queue.length > 1) -- nao precisam de StateFlow
    // proprio, quem consome (Composable) deriva direto daqui, igual a fonte deriva do state.
    private val _levelUpQueue = MutableStateFlow<List<LevelUpEvent>>(emptyList())
    val levelUpQueue: StateFlow<List<LevelUpEvent>> = _levelUpQueue.asStateFlow()

    private val _systemLogs = MutableStateFlow<List<LogEntry>>(emptyList())
    val systemLogs: StateFlow<List<LogEntry>> = _systemLogs.asStateFlow()

    // ── Fila global de anúncios de conquistas (FR-009) ────────────────────────
    // Estado efêmero, separado dos IDs persistidos em CharacterState.achievements.
    private val _achievementAnnouncementQueue = MutableStateFlow<List<Achievement>>(emptyList())
    val achievementAnnouncementQueue: StateFlow<List<Achievement>> = _achievementAnnouncementQueue.asStateFlow()

    // Novas conquistas detectadas durante conclusão de foco (para o FocusCompletionFlow).
    // Limpa ao iniciar nova sessão; populada em confirmFocusSession antes do save.
    private val _pendingFocusAchievements = MutableStateFlow<List<Achievement>>(emptyList())
    val pendingFocusAchievements: StateFlow<List<Achievement>> = _pendingFocusAchievements.asStateFlow()

    // Celebrações pendentes vindas de conclusões em background (spec-008, T10–T12,
    // opção A: peek ao abrir + consume dos IDs exibidos no Confirm).
    private val _pendingCelebration = MutableStateFlow<AggregatedCelebrationSummary?>(null)
    val pendingCelebration: StateFlow<AggregatedCelebrationSummary?> = _pendingCelebration.asStateFlow()
    private var shownCelebrationIds: List<String> = emptyList()

    // Retomada de sessão interrompida por morte de processo (recovery caso 3):
    // o VM não tem Context no init — expõe o registro e a MainActivity despacha
    // o Service com o deadline original.
    private val _pendingServiceResume = MutableStateFlow<PersistedFocusSession?>(null)
    val pendingServiceResume: StateFlow<PersistedFocusSession?> = _pendingServiceResume.asStateFlow()

    // Descanso automático (autoStartBreak): o VM decide os minutos, a MainActivity
    // despacha o Service (VM não tem Context).
    private val _pendingBreakStart = MutableStateFlow<Int?>(null)
    val pendingBreakStart: StateFlow<Int?> = _pendingBreakStart.asStateFlow()

    /**
     * Descarta a primeira conquista da fila de anúncios (FR-009).
     * Usado quando o usuário avança no overlay de anúncio fora do foco.
     */
    fun dismissNextAchievementAnnouncement() {
        val queue = _achievementAnnouncementQueue.value
        if (queue.isNotEmpty()) {
            _achievementAnnouncementQueue.value = queue.drop(1)
        }
    }

    /**
     * Descarta a primeira conquista da fila de anúncios de foco.
     * Usado quando o usuário avança na etapa de conquista do FocusCompletionFlow.
     */
    fun dismissNextFocusAchievement() {
        val list = _pendingFocusAchievements.value
        if (list.isNotEmpty()) {
            _pendingFocusAchievements.value = list.drop(1)
        }
    }

    private val _dailyReport = MutableStateFlow<DailyReportData?>(null)
    val dailyReport: StateFlow<DailyReportData?> = _dailyReport.asStateFlow()

    /** Porte de addSystemLog (App.tsx ~1223). Cap 51 (mais recente primeiro). */
    fun addSystemLog(text: String, highlighted: Boolean = false) {
        val timeStr = SimpleDateFormat("HH:mm:ss", Locale.forLanguageTag("pt-BR"))
            .format(Date(clock()))
        _systemLogs.value = (listOf(LogEntry(UUID.randomUUID().toString(), timeStr, text, highlighted)) + _systemLogs.value)
            .take(51)
    }

    private var graceTickJob: Job? = null
    private var graceEndTimeMillis: Long = 0L

    init {
        viewModelScope.launch {
            val existing = repository.getCharacterState()
            val stateToUse = if (existing != null) {
                existing
            } else {
                val initial = createInitialCharacterState(Date(clock()))
                repository.saveCharacterState(initial)
                initial
            }

            val rolloverResult = RolloverLogic.applyRollover(
                state = stateToUse,
                dailies = stateToUse.dailies,
                referenceDate = Date(clock())
            )
            var finalState = rolloverResult.updatedState
            if (rolloverResult.shieldConsumed) {
                if (rolloverResult.missedCount > 0) {
                    addSystemLog("🛡️ Escudo de Streak ativado! Oração do Santuário absorveu o choque de ${rolloverResult.missedCount} diárias negligenciadas de ontem.", true)
                } else {
                    addSystemLog("🛡️ Escudo do Santuário ativado! Sua streak de dias consecutivos está preservada do gelo do esquecimento.", true)
                }
            } else if (rolloverResult.missedCount > 0) {
                val dano = stateToUse.hp - rolloverResult.updatedState.hp
                if (dano > 0) {
                    addSystemLog("💀 Dano Solar da Negligência: Deixaste ${rolloverResult.missedCount} Diárias incompletas ontem! Perdeste -${dano} de vitalidade HP.", false)
                }
            }

            // Relatório Diário: calcula dados a partir do estado pré-update + resultado do rollover.
            // Fonte: React App.tsx:1056-1097 — valores computados antes de mutar state.
            if (stateToUse.todayDate != QuestLogic.toDateStringJs(Date(clock()))) {
                val rewardAmount = if (stateToUse.charClass == CharClass.Warrior) 120 else 100
                _dailyReport.value = DailyReportData(
                    rewardAmount = rewardAmount,
                    currentStreak = stateToUse.streak,
                    streakLost = stateToUse.streak > 0 && rolloverResult.updatedState.streak == 0 && !rolloverResult.shieldConsumed,
                    streakProtected = rolloverResult.shieldConsumed && stateToUse.streak > 0,
                    missedDailiesCount = rolloverResult.missedCount,
                    damageTaken = max(0, stateToUse.hp - rolloverResult.updatedState.hp),
                    allDailiesCompleted = rolloverResult.missedCount == 0 && stateToUse.dailies.isNotEmpty()
                )
            }

            if (!finalState.hasClaimedLogin) {
                val loginGold = if (finalState.charClass == com.iurispraecepta.herolog.model.CharClass.Warrior) 120 else 100
                finalState = finalState.copy(
                    gold = finalState.gold + loginGold,
                    totalGoldEarned = finalState.totalGoldEarned + loginGold,
                    hasClaimedLogin = true
                )
                addSystemLog("💎 Proclamação Diária: Recebeste +${loginGold} GP por adentrar hoje ao Santuário Sagrado!", true)
                sfxManager.playCoins()
            }

            if (finalState != existing) {
                repository.saveCharacterState(finalState)
            }
            _characterState.value = finalState

            if (rolloverResult.died) {
                viewModelScope.launch {
                    delay(100)
                    val cur = _characterState.value ?: return@launch
                    saveCharacterState(cur.copy(isPlayerDead = true))
                    sfxManager.playDeath()
                }
            }

            recoverFocusSession()
            observeServiceState()
            refreshPendingCelebration()
        }
    }

    fun dismissDailyReport() {
        _dailyReport.value = null
    }

    /**
     * Recuperação pós-reabertura (spec-008, T10 — migração total: o Service detém
     * o timer; o VM só espelha e reconcilia o Room).
     *
     * - Caso 1: holder do Service ativo (RUNNING/PAUSED/BREAK_RUNNING) → só
     *   espelhar; a coleta de [observeServiceState] já cuida.
     * - Caso 2: cálculo guardado (`pendingCalculation != null`, morte na janela
     *   calculate→apply) → aplica o cálculo guardado, nunca recalcula.
     * - Caso 3: sessão futura sem cálculo → expõe em [pendingServiceResume] para
     *   a MainActivity retomar o Service com o deadline original (VM sem Context).
     * - Caso 4: sessão expirada sem cálculo → aplica agora (caminho do backup).
     */
    private suspend fun recoverFocusSession() {
        val holderPhase = FocusSessionService.state.value.phase
        if (holderPhase == ServicePhase.RUNNING ||
            holderPhase == ServicePhase.PAUSED ||
            holderPhase == ServicePhase.BREAK_RUNNING
        ) {
            return
        }

        val persisted = focusSessionRepository.getSession() ?: return

        val stored = persisted.pendingCalculation
        if (stored != null) {
            val useCase = buildUseCase() ?: return
            useCase.applyRewards(stored, referenceDate = Date(clock()))
            focusSessionRepository.clearSession()
            refreshPendingCelebration()
            return
        }

        if (persisted.endTimeMillis - clock() <= 0) {
            val repo = pendingRewardRepository ?: return
            completeSessionFromBackup(
                repository,
                focusSessionRepository,
                repo,
                persisted.config,
                persisted.durationMinutes,
                Date(clock())
            )
            refreshPendingCelebration()
        } else {
            _pendingServiceResume.value = persisted
        }
    }

    /** Marca o resume do caso 3 como despachado (chamado pela MainActivity). */
    fun clearServiceResume() {
        _pendingServiceResume.value = null
    }

    /** Consome o pedido de descanso automático (chamado pela MainActivity). */
    fun clearBreakStart() {
        _pendingBreakStart.value = null
    }

    private fun buildUseCase(): FocusUseCase? {
        val repo = pendingRewardRepository ?: return null
        return FocusUseCase(repository, focusSessionRepository, repo)
    }

    // ── Espelho do Service (FR-13) ────────────────────────────────────────────
    // O VM nunca calcula/encerra sessão; apenas reflete ServiceState em
    // FocusSessionState/BreakTimerState para a UI. O ticker abaixo é puramente
    // apresentacional (timeLeft/secondsLeft); sem lógica de conclusão.

    private var mirrorPrevPhase: ServicePhase = ServicePhase.IDLE
    private var mirrorTickJob: Job? = null

    private fun observeServiceState() {
        viewModelScope.launch {
            FocusSessionService.state.collect { serviceState ->
                mapServiceToMirror(serviceState)
                // Ticker apresentacional só existe durante fases ativas — fora
                // delas seria um loop infinito que travaria advanceUntilIdle e
                // gastaria CPU à toa em produção.
                if (serviceState.phase == ServicePhase.RUNNING ||
                    serviceState.phase == ServicePhase.PAUSED ||
                    serviceState.phase == ServicePhase.BREAK_RUNNING
                ) {
                    startMirrorTickJob()
                } else {
                    mirrorTickJob?.cancel()
                }
                if (serviceState.phase == ServicePhase.COMPLETED &&
                    mirrorPrevPhase != ServicePhase.COMPLETED
                ) {
                    handleServiceCompleted()
                }
                mirrorPrevPhase = serviceState.phase
            }
        }
    }

    private fun mapServiceToMirror(serviceState: com.iurispraecepta.herolog.service.ServiceState) {
        val now = clock()
        val grace = _focusSessionState.value.let { it.isGraceActive to it.graceSecondsLeft }
        _focusSessionState.value = when (serviceState.phase) {
            ServicePhase.RUNNING -> FocusSessionState(
                isRunning = true,
                isPaused = false,
                timeLeft = max(0, ((serviceState.endTimeMillis - now) / 1000L).toInt()),
                totalSeconds = serviceState.durationMinutes * 60,
                config = serviceState.sessionConfig,
                durationMinutes = serviceState.durationMinutes
            )
            ServicePhase.PAUSED -> FocusSessionState(
                isRunning = true,
                isPaused = true,
                timeLeft = max(0, (serviceState.pausedRemainingMillis / 1000L).toInt()),
                totalSeconds = serviceState.durationMinutes * 60,
                pauseCount = serviceState.pauseCount,
                config = serviceState.sessionConfig,
                durationMinutes = serviceState.durationMinutes
            )
            else -> FocusSessionState()
        }.copy(isGraceActive = grace.first, graceSecondsLeft = grace.second)

        _breakTimerState.value = when (serviceState.phase) {
            ServicePhase.BREAK_RUNNING -> BreakTimerState(
                isBreakActive = true,
                selectedBreakMins = serviceState.breakDurationMinutes,
                secondsLeft = max(0, ((serviceState.breakEndTimeMillis - now) / 1000L).toInt()),
                totalSeconds = serviceState.breakTotalSeconds,
                wasLastSessionDungeonMode = serviceState.lastSessionDungeonMode,
                wasLastSessionWildernessMode = serviceState.lastSessionWildernessMode,
                lastSessionDungeonSessions = serviceState.lastSessionDungeonSessions
            )
            ServicePhase.IDLE, ServicePhase.SUGGESTING -> BreakTimerState()
            else -> _breakTimerState.value
        }
    }

    private fun startMirrorTickJob() {
        if (mirrorTickJob?.isActive == true) return
        mirrorTickJob?.cancel()
        mirrorTickJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                val serviceState = FocusSessionService.state.value
                if (serviceState.phase == ServicePhase.RUNNING ||
                    serviceState.phase == ServicePhase.PAUSED ||
                    serviceState.phase == ServicePhase.BREAK_RUNNING
                ) {
                    mapServiceToMirror(serviceState)
                } else {
                    return@launch
                }
            }
        }
    }

    /**
     * Pós-processamento de conclusão com app aberto: a recompensa já foi aplicada
     * pelo Service — aqui vão só os efeitos de UX (logs, sons, progresso de
     * masmorra, conquistas p/ o Flow), derivados da celebração enfileirada.
     */
    private suspend fun handleServiceCompleted() {
        val repo = pendingRewardRepository ?: return
        val latest = repo.getPending().lastOrNull()
        val holder = FocusSessionService.state.value
        if (latest != null) {
            addSystemLog("🎉 Sessão de foco concluída: +${latest.xpGained} XP, +${latest.goldGained} ouro!", true)
            sfxManager.playCoins()
            if (latest.leveledUp) {
                addSystemLog("🎉 ${latest.skillName} alcançou o Nível ${latest.newLevel}.", true)
                sfxManager.playLevelUp()
            }
            if (latest.lootedItems != "[]") {
                addSystemLog("✨ Espólio encontrado! Vá ao Inventário para visualizá-lo ou equipá-lo.", true)
            }
            if (latest.droppedTitle != null) {
                addSystemLog("✨ Sorte ancestral: você dropou um TÍTULO RARO!", true)
            }
            val achievements = runCatching {
                com.iurispraecepta.herolog.data.JsonConfig.default
                    .decodeFromString<List<String>>(latest.achievementsUnlocked)
            }.getOrDefault(emptyList())
            val newlyUnlocked = achievements.mapNotNull { id ->
                AchievementCatalog.ACHIEVEMENTS_LIST.firstOrNull { it.id == id }
            }
            _pendingFocusAchievements.value = newlyUnlocked
            newlyUnlocked.forEach { ach ->
                addSystemLog("🏆 Conquista heroica: desbloqueada runa especial [${ach.name}]!", true)
            }
        }
        val charState = _characterState.value
        if (charState != null && holder.lastSessionDungeonMode) {
            val nextSessions = holder.lastSessionDungeonSessions + 1
            _dungeonSessionsProgress.value = if (nextSessions >= 4) 0 else nextSessions
            if (nextSessions >= 4) {
                addSystemLog("🏆 Exploração masmorra sucesso: concluiu as 4 sessões heroicas! Bônus monumental de +2.500 GP adicionado!", true)
            } else {
                addSystemLog("⚔️ Masmorra Progresso: (${nextSessions}/4) focos consecutivos selados.", true)
            }
        }
        if (charState != null) {
            if (charState.pomodoroSettings.autoStartBreak) {
                val breakMins = if (holder.lastSessionDungeonMode &&
                    holder.lastSessionDungeonSessions + 1 >= 4
                ) {
                    charState.pomodoroSettings.longBreakDuration.takeIf { it > 0 } ?: 15
                } else {
                    charState.pomodoroSettings.shortBreakDuration.takeIf { it > 0 } ?: 5
                }
                _pendingBreakStart.value = breakMins
            } else {
                enterBreakPrep(
                    wasDungeonMode = holder.lastSessionDungeonMode,
                    wasWildernessMode = holder.lastSessionWildernessMode,
                    lastDungeonSessions = holder.lastSessionDungeonSessions
                )
            }
        }
        refreshPendingCelebration()
    }

    /** Peek da fila (opção A): agrega sem marcar; o Confirm consome (T12). */
    fun refreshPendingCelebration() {
        viewModelScope.launch {
            val summary = pendingRewardRepository?.peekPendingCelebrations()
            shownCelebrationIds = summary?.sessions?.map { it.id } ?: emptyList()
            _pendingCelebration.value = summary
        }
    }

    /** Confirm do modal (FR-12): consome exatamente os IDs exibidos, atomicamente. */
    fun confirmPendingCelebration() {
        viewModelScope.launch {
            pendingRewardRepository?.consumeIds(shownCelebrationIds)
            shownCelebrationIds = emptyList()
            _pendingCelebration.value = null
        }
    }

    /**
     * Funil único de mutação do personagem. Porte parcial de `useLevelUp.ts`: a cada transição de
     * estado, compara o valor anterior com o novo e enfileira eventos de level up detectados
     * (`LevelUpLogic.detectLevelUps`) -- equivalente ao `useEffect` que a fonte roda a cada
     * mudança de `gameState.combatLevel`/`gameState.skills`.
     *
     * @param suppressLevelUpDetection porte de `isImportingRef.current = true` da fonte -- usado
     *   quando o estado inteiro está sendo substituído por uma fonte externa (import de save) e
     *   uma cascata de popups de "level up" comparando contra o personagem anterior seria
     *   espúria/sem sentido pro jogador.
     */
    fun saveCharacterState(state: CharacterState, suppressLevelUpDetection: Boolean = false) {
        val previous = _characterState.value
        if (!suppressLevelUpDetection && previous != null) {
            val newEvents = LevelUpLogic.detectLevelUps(previous, state)
            if (newEvents.isNotEmpty()) {
                _levelUpQueue.value = _levelUpQueue.value + newEvents
            }
            if (state.combatLevel > previous.combatLevel) {
                addSystemLog("🆙 COMBAT LEVEL UP: Nível de combate subiu para ${state.combatLevel}!", true)
            }
        }
        viewModelScope.launch {
            repository.saveCharacterState(state)
            _characterState.value = state
        }
    }

    /** Porte de `dismissCurrentLevelUp` (useLevelUp.ts) -- remove o evento ativo (topo da fila). */
    fun dismissCurrentLevelUp() {
        _levelUpQueue.value = _levelUpQueue.value.drop(1)
    }

    fun unequipItem(slotIdx: Int) {
        val current = _characterState.value ?: return
        val result = InventoryLogic.unequipItem(current.inventory, current.equippedEquipment, slotIdx)
        saveCharacterState(current.copy(inventory = result.inventory, equippedEquipment = result.equippedEquipment))
    }

    fun equipItem(item: InventoryItem, slotIdx: Int) {
        val current = _characterState.value ?: return
        val result = InventoryLogic.equipItem(current.inventory, current.equippedEquipment, item, slotIdx)
        saveCharacterState(current.copy(inventory = result.inventory, equippedEquipment = result.equippedEquipment))
        sfxManager.playCoins()
    }

    // Bug corrigido em relacao ao scaffolding anterior do MainActivity: o gold precisa ser
    // incrementado com o sellPrice (fonte real confirmada: hook useInventory.ts, funcao
    // sellItem, gold: prev.gold + sellingPrice). A versao anterior so logava o preco sem
    // aplicar ao estado.
    fun sellItem(item: InventoryItem) {
        val current = _characterState.value ?: return
        val (updatedInventory, sellPrice) = InventoryLogic.sellItem(current.inventory, item)
        saveCharacterState(current.copy(inventory = updatedInventory, gold = current.gold + sellPrice))
        sfxManager.playCoins()
    }

    fun buyShopItem(catalogEntry: com.iurispraecepta.herolog.logic.kingdom.ShopCatalogEntry) {
        val current = _characterState.value ?: return
        val result = com.iurispraecepta.herolog.logic.kingdom.ShopLogic.buyItem(
            gold = current.gold,
            inventory = current.inventory,
            catalogEntry = catalogEntry
        ) ?: return
        saveCharacterState(current.copy(gold = result.gold, inventory = result.inventory))
        sfxManager.playCoins()
    }

    fun discardItem(item: InventoryItem) {
        val current = _characterState.value ?: return
        val updatedInventory = InventoryLogic.discardItem(current.inventory, item)
        saveCharacterState(current.copy(inventory = updatedInventory))
    }

    fun triggerHabit(habitId: String, isUp: Boolean) {
        val current = _characterState.value ?: return
        val habit = current.habits.find { it.id == habitId } ?: return
        val result = HabitLogic.trigger(habit, current, isUp, referenceDate = Date(clock()))
        val updatedHabits = current.habits.map { if (it.id == habitId) result.updatedHabit else it }
        val candidateState = result.updatedState.copy(habits = updatedHabits)

        // FR-004/FR-010: detecção central de conquistas para ações fora do foco
        val newAchievements = AchievementDetection.detectNewAchievements(current, candidateState)
        val stateWithAchievements = AchievementDetection.withAchievements(candidateState, newAchievements.map { it.id })
        saveCharacterState(stateWithAchievements)

        // Fila global de anúncios (FR-009)
        if (newAchievements.isNotEmpty()) {
            _achievementAnnouncementQueue.value = _achievementAnnouncementQueue.value + newAchievements
        }

        val rewards = getDifficultyRewards(habit.difficulty)
        if (isUp) {
            val xp = if (current.charClass == CharClass.Mage) floor(rewards.xp * 1.2).toInt() else rewards.xp
            val gold = if (current.charClass == CharClass.Warrior) floor(rewards.gold * 1.2).toInt() else rewards.gold
            addSystemLog("✨ Prática Virtuosa: Completou o hábito positivo \"${habit.title}\"! Ganhou +${gold} GP e +${xp} XP.", true)
            sfxManager.playCoins()
            if (candidateState.combatLevel > current.combatLevel) {
                sfxManager.playLevelUp()
            }
        } else {
            val damage = if (current.charClass == CharClass.Ranger) max(1, floor(rewards.damage * 0.7).toInt()) else rewards.damage
            addSystemLog("⚠️ Desvio Espiritual: Sofreu dano pelo hábito negativo \"${habit.title}\"! Perdeu -${damage} HP de sua integridade.", false)
            sfxManager.playWildernessWarning()
            if (candidateState.isPlayerDead && !current.isPlayerDead) {
                sfxManager.playDeath()
            }
        }
    }

    fun toggleDaily(dailyId: String) {
        val current = _characterState.value ?: return
        val daily = current.dailies.find { it.id == dailyId } ?: return
        val result = DailyLogic.toggle(daily, current)
        val updatedDailies = current.dailies.map { if (it.id == dailyId) result.updatedDaily else it }
        saveCharacterState(result.updatedState.copy(dailies = updatedDailies))

        val rewards = getDifficultyRewards(daily.difficulty)
        val xp = if (current.charClass == CharClass.Mage) floor(rewards.xp * 1.2).toInt() else rewards.xp
        val gold = if (current.charClass == CharClass.Warrior) floor(rewards.gold * 1.2).toInt() else rewards.gold
        if (!daily.completed) {
            val streak = daily.streak
            addSystemLog("📅 Voto Diário Cumprido: Concluiu \"${daily.title}\"! (+${gold} GP, +${xp} XP, Streak: ${streak + 1} dias)", true)
            sfxManager.playCoins()
            if (result.updatedState.combatLevel > current.combatLevel) {
                sfxManager.playLevelUp()
            }
        } else {
            addSystemLog("↩️ Reversão de Voto: Diária \"${daily.title}\" desmarcada. Perdidos -${gold} GP e -${xp} XP.")
        }
    }

    fun toggleTodo(todoId: String) {
        val current = _characterState.value ?: return
        val todo = current.todos.find { it.id == todoId } ?: return
        val result = TodoLogic.toggle(todo, current, referenceDate = Date(clock()))
        val updatedTodos = current.todos.map { if (it.id == todoId) result.updatedTodo else it }
        saveCharacterState(result.updatedState.copy(todos = updatedTodos))

        val rewards = getDifficultyRewards(todo.difficulty)
        val xp = if (current.charClass == CharClass.Mage) floor(rewards.xp * 1.2).toInt() else rewards.xp
        val gold = if (current.charClass == CharClass.Warrior) floor(rewards.gold * 1.2).toInt() else rewards.gold
        if (!todo.completed) {
            addSystemLog("✔️ Afazer Cumprido: Concluiu aventura \"${todo.title}\"! (+${gold} GP, +${xp} XP!)", true)
            sfxManager.playCoins()
            if (result.updatedState.combatLevel > current.combatLevel) {
                sfxManager.playLevelUp()
            }
        } else {
            addSystemLog("↩️ Reversão de Contrato: Afazer \"${todo.title}\" reaberto. Perdidos -${gold} GP e -${xp} XP.")
        }
    }

    fun addHabit(title: String, notes: String, up: Boolean, down: Boolean, difficulty: Difficulty, tags: List<String>) {
        val current = _characterState.value ?: return
        val newHabit = Habit(
            id = UUID.randomUUID().toString(),
            title = title, notes = notes, up = up, down = down, difficulty = difficulty,
            upCount = 0, downCount = 0, streak = 0, tags = tags
        )
        saveCharacterState(current.copy(habits = current.habits + newHabit))
        addSystemLog("🔥 Runas Consagradas: Novo hábito \"${title}\" adicionado à sua capela diária!")
    }

    fun editHabit(edited: Habit) {
        val current = _characterState.value ?: return
        val updated = current.habits.map { if (it.id == edited.id) edited else it }
        saveCharacterState(current.copy(habits = updated))
        addSystemLog("⚙️ Runas Alteradas: Hábito \"${edited.title}\" atualizado.")
    }

    fun deleteHabit(habitId: String) {
        val current = _characterState.value ?: return
        saveCharacterState(current.copy(habits = current.habits.filter { it.id != habitId }))
        addSystemLog("🗑️ Runas Banidas: Hábito removido com sucesso.")
    }

    fun addDaily(title: String, notes: String, difficulty: Difficulty, streak: Int, repeats: RepeatInterval, every: Int, tags: List<String>, checklistTexts: List<String>) {
        val current = _characterState.value ?: return
        val checklist = checklistTexts.map { ChecklistItem(id = UUID.randomUUID().toString(), text = it, completed = false) }
        val newDaily = Daily(
            id = UUID.randomUUID().toString(),
            title = title, notes = notes, difficulty = difficulty, completed = false,
            streak = streak, repeats = repeats, every = every, tags = tags, checklist = checklist,
            value = 0, createdAt = java.time.Instant.now().toString()
        )
        saveCharacterState(current.copy(dailies = current.dailies + newDaily))
        addSystemLog("📅 Novo Voto de Diária Consagrado: \"${newDaily.title}\"!")
    }

    fun editDaily(edited: Daily) {
        val current = _characterState.value ?: return
        val updated = current.dailies.map { if (it.id == edited.id) edited else it }
        saveCharacterState(current.copy(dailies = updated))
        addSystemLog("⚙️ Diária Modificada: \"${edited.title}\" atualizada.")
    }

    fun deleteDaily(dailyId: String) {
        val current = _characterState.value ?: return
        saveCharacterState(current.copy(dailies = current.dailies.filter { it.id != dailyId }))
        addSystemLog("🗑️ Voto de Diária Aniquilado.")
    }

    fun toggleDailyChecklistItem(dailyId: String, itemId: String) {
        val current = _characterState.value ?: return
        val updated = current.dailies.map { d ->
            if (d.id == dailyId) d.copy(checklist = d.checklist.map { if (it.id == itemId) it.copy(completed = !it.completed) else it })
            else d
        }
        saveCharacterState(current.copy(dailies = updated))
    }

    fun addTodo(title: String, notes: String, difficulty: Difficulty, tags: List<String>, checklistTexts: List<String>) {
        val current = _characterState.value ?: return
        val checklist = checklistTexts.map { ChecklistItem(id = UUID.randomUUID().toString(), text = it, completed = false) }
        val newTodo = Todo(
            id = UUID.randomUUID().toString(),
            title = title, notes = notes, difficulty = difficulty, completed = false,
            tags = tags, checklist = checklist, createdAt = java.time.Instant.now().toString()
        )
        saveCharacterState(current.copy(todos = current.todos + newTodo))
        addSystemLog("📜 Novo Contrato / Afazer em mãos: \"${newTodo.title}\"!")
    }

    fun editTodo(edited: Todo) {
        val current = _characterState.value ?: return
        val updated = current.todos.map { if (it.id == edited.id) edited else it }
        saveCharacterState(current.copy(todos = updated))
        addSystemLog("⚙️ Afazer Editado: \"${edited.title}\" atualizado.")
    }

    fun deleteTodo(todoId: String) {
        val current = _characterState.value ?: return
        saveCharacterState(current.copy(todos = current.todos.filter { it.id != todoId }))
        addSystemLog("🗑️ Contrato de Afazer Destruído.")
    }

    fun toggleTodoChecklistItem(todoId: String, itemId: String) {
        val current = _characterState.value ?: return
        val updated = current.todos.map { t ->
            if (t.id == todoId) t.copy(checklist = t.checklist.map { if (it.id == itemId) it.copy(completed = !it.completed) else it })
            else t
        }
        saveCharacterState(current.copy(todos = updated))
    }

    fun dailyQuests(state: CharacterState, referenceDate: Date = Date()): List<ProcessedQuest> {
        return QuestLogic.getRotatingDailyQuests(3, referenceDate).map { quest ->
            val progress = quest.getProgress(state)
            ProcessedQuest(
                id = quest.id,
                name = quest.name,
                desc = quest.desc,
                target = quest.target,
                rewardGold = quest.rewardGold,
                rewardXp = quest.rewardXp,
                progress = progress,
                isCompleted = progress >= quest.target,
                isClaimed = QuestLogic.isQuestClaimed(state, quest.id, referenceDate)
            )
        }
    }

    fun claimQuestReward(questId: String, goldReward: Int, xpReward: Int) {
        val current = _characterState.value ?: return
        val updated = QuestApplyLogic.claimQuestReward(current, questId, goldReward, xpReward)
        saveCharacterState(updated)
        addSystemLog("📜 Contrato da Gilda Resgatado! Moedas +${goldReward} GP e Relíquias +${xpReward} XP depositadas nas sacolas.", true)
        sfxManager.playCoins()
    }

    fun equipTitle(titleId: String?) {
        val current = _characterState.value ?: return
        when (val result = TitleLogic.equipTitle(current.ownedTitles, titleId)) {
            is EquipTitleResult.Success -> {
                saveCharacterState(current.copy(equippedTitle = result.equippedTitle))
                sfxManager.playCoins()
            }
            EquipTitleResult.NotOwned -> { /* no-op: mesma regra da fonte, titulo nao possuido nao equipa */ }
        }
    }

    fun buyTitle(titleId: String, price: Int) {
        val current = _characterState.value ?: return
        when (val result = TitleLogic.buyTitle(current.gold, current.ownedTitles, titleId, price)) {
            is TitlePurchaseResult.Success -> {
                val candidateState = current.copy(gold = result.newGold, ownedTitles = result.newOwnedTitles)
                // FR-004/FR-010: detecção central (Arsenal de Títulos)
                val newAchievements = AchievementDetection.detectNewAchievements(current, candidateState)
                val stateWithAchievements = AchievementDetection.withAchievements(candidateState, newAchievements.map { it.id })
                saveCharacterState(stateWithAchievements)
                if (newAchievements.isNotEmpty()) {
                    _achievementAnnouncementQueue.value = _achievementAnnouncementQueue.value + newAchievements
                }
                sfxManager.playCoins()
            }
            TitlePurchaseResult.InsufficientGold -> { /* no-op: mesma regra da fonte */ }
            TitlePurchaseResult.AlreadyOwned -> { /* no-op: bug já corrigido no Bloco 7 */ }
        }
    }

    fun claimAchievementTitle(titleId: String) {
        val current = _characterState.value ?: return
        val candidateState = current.copy(ownedTitles = TitleLogic.claimAchievementTitle(current.ownedTitles, titleId))
        // FR-004/FR-010: detecção central (Arsenal de Títulos)
        val newAchievements = AchievementDetection.detectNewAchievements(current, candidateState)
        val stateWithAchievements = AchievementDetection.withAchievements(candidateState, newAchievements.map { it.id })
        saveCharacterState(stateWithAchievements)
        if (newAchievements.isNotEmpty()) {
            _achievementAnnouncementQueue.value = _achievementAnnouncementQueue.value + newAchievements
        }
        sfxManager.playLevelUp()
    }

    fun importSaveFromPastedText(rawJson: String): SaveImportOutcome {
        return importSaveFromJson(rawJson)
    }

    /**
     * Importa um save a partir de uma string JSON arbitrária.
     *
     * Centraliza o que já existia no [importSaveFromPastedText] (texto colado)
     * e reusa o mesmo caminho para o import por arquivo. A detecção de
     * `ExportPayload` vs `CharacterState` cru é feita pelo
     * [com.iurispraecepta.herolog.data.export.GameStateImporter.parsePayload]
     * — esta função só persiste o resultado e devolve a [SaveImportOutcome]
     * traduzida pra UI.
     */
    fun importSaveFromJson(rawJson: String): SaveImportOutcome {
        return when (val result = com.iurispraecepta.herolog.data.export.GameStateImporter.parsePayload(rawJson)) {
            is SaveImportResult.Success -> {
                saveCharacterState(result.characterState, suppressLevelUpDetection = true)
                SaveImportOutcome.Restored(result.characterState.charName)
            }
            is SaveImportResult.InvalidJson -> SaveImportOutcome.Failed
        }
    }

    /**
     * Constrói o [com.iurispraecepta.herolog.data.export.ExportPayload] a
     * partir do estado atual (personagem + sessão de foco ativa, se houver).
     *
     * Retorna `null` se o personagem ainda não foi carregado (banco vazio e
     * estado inicial não criado — improvável em produção, mas defensivo).
     */
    suspend fun buildExportPayload(): com.iurispraecepta.herolog.data.export.ExportPayload? {
        val character = _characterState.value ?: return null
        val activeFocus = focusSessionRepository.getSession()
        return com.iurispraecepta.herolog.data.export.GameStateExporter.buildPayload(
            character = character,
            activeFocus = activeFocus
        )
    }

    /**
     * Serializa o save atual em string JSON pretty (mesmo formato do
     * arquivo exportado, sem a parte de `writeToCache`). Usado pelo
     * export por clipboard.
     */
    suspend fun exportSaveAsJsonString(): String? {
        val payload = buildExportPayload() ?: return null
        return com.iurispraecepta.herolog.data.export.GameStateExporter.encodePayload(
            payload,
            com.iurispraecepta.herolog.data.JsonConfig.pretty
        )
    }

    fun addCustomSkill(nameInput: String, emoji: String): SkillOperationResult {
        val current = _characterState.value ?: return SkillOperationResult.Error(SkillError.InvalidIndex)
        val result = SkillLogic.addCustomSkill(current.skills, nameInput, emoji)
        if (result is SkillOperationResult.Success) {
            saveCharacterState(current.copy(skills = result.newSkills))
        }
        return result
    }

    fun addTagToSkill(skillIdx: Int, newTag: String) {
        val current = _characterState.value ?: return
        saveCharacterState(current.copy(skills = SkillLogic.addTagToSkill(current.skills, skillIdx, newTag)))
    }

    fun removeTagFromSkill(skillIdx: Int, tagIdx: Int) {
        val current = _characterState.value ?: return
        saveCharacterState(current.copy(skills = SkillLogic.removeTagFromSkill(current.skills, skillIdx, tagIdx)))
    }

    fun renameSkill(idx: Int, newName: String): SkillOperationResult {
        val current = _characterState.value ?: return SkillOperationResult.Error(SkillError.InvalidIndex)
        val result = SkillLogic.renameSkill(current.skills, idx, newName)
        if (result is SkillOperationResult.Success) {
            saveCharacterState(current.copy(skills = result.newSkills))
        }
        return result
    }

    fun deleteSkill(idx: Int): DeleteSkillEligibility {
        val current = _characterState.value ?: return DeleteSkillEligibility.Blocked
        val isFocusSessionRunning = _focusSessionState.value.isRunning
        val eligibility = SkillLogic.canDeleteSkill(current.skills, isFocusSessionRunning)
        if (eligibility == DeleteSkillEligibility.Eligible) {
            saveCharacterState(current.copy(skills = SkillLogic.deleteSkillAt(current.skills, idx)))
        }
        return eligibility
    }

    fun prestigeSkill(idx: Int) {
        val current = _characterState.value ?: return
        val skill = current.skills.getOrNull(idx) ?: return
        if (SkillLogic.isPrestigeEligible(skill)) {
            val updated = SkillLogic.applyPrestige(skill)
            saveCharacterState(current.copy(skills = current.skills.toMutableList().apply { this[idx] = updated }))
            sfxManager.playLevelUp()
        }
    }

    /**
     * Inicia uma sessão delegando o timer ao Service (spec-008, T10 — migração
     * total). Persiste o registro ativo (sem cálculo) para recovery e dispara o
     * Foreground Service, única fonte de verdade do timer.
     */
    fun startSession(context: Context, config: FocusSessionConfig, durationMinutes: Int) {
        if (_focusSessionState.value.isRunning) return
        cancelAllTimers()
        _breakTimerState.value = BreakTimerState()
        _pendingFocusAchievements.value = emptyList()

        val totalSeconds = durationMinutes * 60
        _focusSessionState.value = FocusSessionState(
            isRunning = true,
            isPaused = false,
            timeLeft = totalSeconds,
            totalSeconds = totalSeconds,
            config = config,
            durationMinutes = durationMinutes
        )

        viewModelScope.launch {
            focusSessionRepository.saveSession(
                PersistedFocusSession(
                    config = config,
                    durationMinutes = durationMinutes,
                    endTimeMillis = clock() + totalSeconds * 1000L,
                    pendingCalculation = null
                )
            )
        }

        FocusSessionService.startSession(context, config, durationMinutes)
        sfxManager.playFocusBell()
    }

    fun togglePauseQuest(context: Context) {
        val phase = FocusSessionService.state.value.phase
        if (phase != ServicePhase.RUNNING && phase != ServicePhase.PAUSED) return
        if (phase == ServicePhase.RUNNING) {
            FocusSessionService.sendAction(context, FocusSessionService.ACTION_PAUSE)
        } else {
            FocusSessionService.sendAction(context, FocusSessionService.ACTION_RESUME)
        }
    }

    fun cancelAllTimers() {
        graceTickJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        graceTickJob?.cancel()
        mirrorTickJob?.cancel()
    }

    fun cancelSession(context: Context) {
        cancelAllTimers()
        FocusSessionService.sendAction(context, FocusSessionService.ACTION_STOP)
    }

    fun abandonSession(context: Context) {
        val wasDungeonMode = _focusSessionState.value.config?.isDungeonMode == true
        cancelSession(context)
        if (wasDungeonMode) {
            _dungeonSessionsProgress.value = 0
            addSystemLog(
                "💀 FRACASSO NA MASMORRA: Ao abandonar, sua expedição na Masmorra colapsou " +
                    "tragicamente e todo o progresso heróico de focos seguidos foi perdido nas cinzas.",
                highlighted = true
            )
            sfxManager.playDeath()
        }
    }

    fun onAppBackgrounded(context: Context) {
        val current = _focusSessionState.value
        val charState = _characterState.value
        if (!current.isRunning || current.isPaused || current.config?.isWildernessChecked != true ||
            current.isGraceActive || charState?.isPlayerDead == true) return

        val equippedTitleId = charState?.equippedTitle
        when (resolveWildernessInfraction(equippedTitleId)) {
            WildernessInfractionOutcome.CONVERTED_TO_PAUSE -> {
                if (!_focusSessionState.value.isPaused) {
                    togglePauseQuest(context)
                }
                sfxManager.playWildernessWarning()
            }
            WildernessInfractionOutcome.GRACE_PERIOD_STARTED -> {
                startGracePeriod(context)
                sfxManager.playWildernessWarning()
            }
        }
    }

    private fun startGracePeriod(context: Context) {
        graceEndTimeMillis = clock() + WILDERNESS_GRACE_PERIOD_SECONDS * 1000L
        _focusSessionState.value = _focusSessionState.value.copy(
            isGraceActive = true,
            graceSecondsLeft = WILDERNESS_GRACE_PERIOD_SECONDS
        )
        graceTickJob?.cancel()
        graceTickJob = viewModelScope.launch {
            while (isActive) {
                val remainingMs = graceEndTimeMillis - clock()
                val remainingSec = ceil(remainingMs / 1000.0).toInt().coerceAtLeast(0)
                _focusSessionState.value = _focusSessionState.value.copy(graceSecondsLeft = remainingSec)
                if (remainingMs <= 0) {
                    triggerCognitiveDeath(context)
                    break
                }
                delay(250)
            }
        }
    }

    /**
     * Morte cognitiva: para o timer no Service (único dono), limpa a sessão e
     * atualiza o espelho. Sem isso o Service concluiria e aplicaria a recompensa
     * de uma sessão já morta.
     */
    private fun triggerCognitiveDeath(context: Context) {
        graceTickJob?.cancel()
        val charState = _characterState.value ?: return
        val result = resolveCognitiveDeath(charState.charClass, charState.streak)
        saveCharacterState(
            charState.copy(
                streak = result.newStreak,
                combo = result.newCombo,
                isPlayerDead = true
            )
        )
        FocusSessionService.sendAction(context, FocusSessionService.ACTION_STOP)
        _focusSessionState.value = _focusSessionState.value.copy(
            isGraceActive = false,
            isRunning = false
        )
        viewModelScope.launch { focusSessionRepository.clearSession() }
        sfxManager.playDeath()
    }

    fun onAppForegrounded() {
        if (!_focusSessionState.value.isGraceActive) return
        returnToFocusFromGrace()
    }

    fun returnToFocusFromGrace() {
        graceTickJob?.cancel()
        _focusSessionState.value = _focusSessionState.value.copy(
            isGraceActive = false,
            graceSecondsLeft = WILDERNESS_GRACE_PERIOD_SECONDS
        )
    }

    fun respawnHero() {
        val charState = _characterState.value ?: return
        val result = resolveRespawn(charState.combatLevel, charState.gold)
        saveCharacterState(
            charState.copy(
                combatLevel = result.newCombatLevel,
                gold = result.newGold,
                combatXP = result.newCombatXp,
                hp = charState.maxHp,
                isPlayerDead = false
            )
        )
        if (_focusSessionState.value.isRunning || _focusSessionState.value.isGraceActive || _focusSessionState.value.isPaused || _focusSessionState.value.config != null) {
            _focusSessionState.value = FocusSessionState()
        }
    }

    fun selectBreakDuration(minutes: Int) {
        _breakTimerState.value = _breakTimerState.value.copy(selectedBreakMins = minutes)
    }

    fun enterBreakPrep(wasDungeonMode: Boolean = false, wasWildernessMode: Boolean = false, lastDungeonSessions: Int = 0) {
        val defaultBreakMins = _characterState.value?.pomodoroSettings?.shortBreakDuration?.takeIf { it > 0 } ?: 5
        _breakTimerState.value = _breakTimerState.value.copy(
            isBreakPrep = true,
            wasLastSessionDungeonMode = wasDungeonMode,
            wasLastSessionWildernessMode = wasWildernessMode,
            lastSessionDungeonSessions = lastDungeonSessions,
            selectedBreakMins = defaultBreakMins
        )
    }

    /**
     * Inicia o descanso delegando ao Service (spec-008, T10). O espelho de
     * [_breakTimerState] é atualizado pela coleta do holder; sem tick próprio.
     * Os antigos `wasLastSession*` continuam viajando no holder do Service.
     */
    fun startBreakTimer(
        context: Context,
        minutes: Int,
        wasDungeonMode: Boolean = false,
        wasWildernessMode: Boolean = false,
        lastDungeonSessions: Int = 0
    ) {
        FocusSessionService.startBreak(context, minutes)
    }

    fun skipBreak(context: Context) {
        FocusSessionService.sendAction(context, FocusSessionService.ACTION_SKIP_BREAK)
    }

    fun changeFocusDuration(minutes: Int) {
        val current = _characterState.value ?: return
        if (_focusSessionState.value.isRunning || _breakTimerState.value.isBreakActive) return
        saveCharacterState(current.copy(
            pomodoroSettings = current.pomodoroSettings.copy(focusDuration = minutes)
        ))
    }

    fun saveCustomTimerSettings(focusMinutes: Int, shortBreakMinutes: Int, longBreakMinutes: Int) {
        val current = _characterState.value ?: return
        if (_focusSessionState.value.isRunning || _breakTimerState.value.isBreakActive) return
        saveCharacterState(current.copy(
            pomodoroSettings = current.pomodoroSettings.copy(
                focusDuration = focusMinutes,
                shortBreakDuration = shortBreakMinutes,
                longBreakDuration = longBreakMinutes
            )
        ))
    }

    fun toggleAutoStartBreak() {
        val current = _characterState.value ?: return
        saveCharacterState(current.copy(
            pomodoroSettings = current.pomodoroSettings.copy(
                autoStartBreak = !current.pomodoroSettings.autoStartBreak
            )
        ))
    }

    fun toggleAutoStartFocus() {
        val current = _characterState.value ?: return
        saveCharacterState(current.copy(
            pomodoroSettings = current.pomodoroSettings.copy(
                autoStartFocus = !current.pomodoroSettings.autoStartFocus
            )
        ))
    }

    fun updateCharacterProfile(name: String, charClass: CharClass) {
        val current = _characterState.value ?: return
        saveCharacterState(current.copy(charName = name, charClass = charClass))
    }

    fun updateOrbConcept(concept: OrbConcept) {
        val current = _characterState.value ?: return
        saveCharacterState(current.copy(orbConcept = concept))
        addSystemLog("🔮 Formato do Núcleo de Foco atualizado: Conceito ${concept.name}", true)
    }
}
