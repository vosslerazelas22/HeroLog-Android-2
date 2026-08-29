package com.iurispraecepta.herolog.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iurispraecepta.herolog.data.createInitialCharacterState
import com.iurispraecepta.herolog.data.repository.CharacterRepository
import com.iurispraecepta.herolog.data.repository.FocusSessionRepository
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
import com.iurispraecepta.herolog.logic.quests.QuestCatalog
import com.iurispraecepta.herolog.logic.quests.QuestLogic
import com.iurispraecepta.herolog.logic.quests.RolloverLogic
import com.iurispraecepta.herolog.logic.quests.TodoLogic
import com.iurispraecepta.herolog.logic.focus.BreakTimerState
import com.iurispraecepta.herolog.logic.focus.FocusApplyLogic
import com.iurispraecepta.herolog.logic.focus.FocusRewardsLogic
import com.iurispraecepta.herolog.logic.focus.FocusSessionConfig
import com.iurispraecepta.herolog.logic.focus.FocusSessionState
import com.iurispraecepta.herolog.logic.focus.PersistedFocusSession
import com.iurispraecepta.herolog.logic.focus.WILDERNESS_GRACE_PERIOD_SECONDS
import com.iurispraecepta.herolog.logic.focus.WildernessInfractionOutcome
import com.iurispraecepta.herolog.logic.focus.resolveWildernessInfraction
import com.iurispraecepta.herolog.logic.focus.resolveCognitiveDeath
import com.iurispraecepta.herolog.logic.focus.resolveRespawn
import com.iurispraecepta.herolog.model.CharClass
import com.iurispraecepta.herolog.model.CharacterState
import com.iurispraecepta.herolog.model.InventoryItem
import com.iurispraecepta.herolog.model.LogEntry
import com.iurispraecepta.herolog.logic.quests.getDifficultyRewards
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
import kotlin.math.roundToInt

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
    private val clock: () -> Long = { System.currentTimeMillis() }
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

    /** Porte de addSystemLog (App.tsx ~1223). Cap 51 (mais recente primeiro). */
    fun addSystemLog(text: String, highlighted: Boolean = false) {
        val timeStr = SimpleDateFormat("HH:mm:ss", Locale.forLanguageTag("pt-BR"))
            .format(Date(clock()))
        _systemLogs.value = (listOf(LogEntry(UUID.randomUUID().toString(), timeStr, text, highlighted)) + _systemLogs.value)
            .take(51)
    }

    private var focusTickJob: Job? = null
    private var focusEndTimeMillis: Long = 0L
    private var graceTickJob: Job? = null
    private var graceEndTimeMillis: Long = 0L
    private var breakTickJob: Job? = null
    private var breakEndTimeMillis: Long = 0L

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

            if (!finalState.hasClaimedLogin) {
                val loginGold = if (finalState.charClass == com.iurispraecepta.herolog.model.CharClass.Warrior) 120 else 100
                finalState = finalState.copy(
                    gold = finalState.gold + loginGold,
                    totalGoldEarned = finalState.totalGoldEarned + loginGold,
                    hasClaimedLogin = true
                )
                addSystemLog("💎 Proclamação Diária: Recebeste +${loginGold} GP por adentrar hoje ao Santuário Sagrado!", true)
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
                }
            }

            recoverFocusSession()
        }
    }

    private suspend fun recoverFocusSession() {
        val persisted = focusSessionRepository.getSession() ?: return

        if (persisted.pendingCalculation != null) {
            // Estado 3: já calculated, só recarrega, NUNCA recalcula.
            _focusSessionState.value = FocusSessionState(
                isRunning = false,
                isPaused = false,
                isFocusCompleted = true,
                timeLeft = 0,
                totalSeconds = persisted.durationMinutes * 60,
                config = persisted.config,
                durationMinutes = persisted.durationMinutes,
                pendingRewardsCalculation = persisted.pendingCalculation
            )
            return
        }

        val remaining = max(0, ((persisted.endTimeMillis - clock()) / 1000.0).roundToInt())

        if (remaining <= 0) {
            // Estado 2: expirou enquanto o app estava fechado. Calcula UMA VEZ agora.
            focusEndTimeMillis = persisted.endTimeMillis
            _focusSessionState.value = FocusSessionState(
                isRunning = false,
                isPaused = false,
                isFocusCompleted = false,
                timeLeft = 0,
                totalSeconds = persisted.durationMinutes * 60,
                config = persisted.config,
                durationMinutes = persisted.durationMinutes
            )
            onFocusSessionCompleted() // já persiste o resultado calculado (Bloco 31)
        } else {
            // Estado 1: ainda em andamento. Retoma o timer normalmente.
            focusEndTimeMillis = persisted.endTimeMillis
            _focusSessionState.value = FocusSessionState(
                isRunning = true,
                isPaused = false,
                isFocusCompleted = false,
                timeLeft = remaining,
                totalSeconds = persisted.durationMinutes * 60,
                pauseCount = 0,
                config = persisted.config,
                durationMinutes = persisted.durationMinutes
            )
            startFocusTickJob()
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
    }

    // Bug corrigido em relacao ao scaffolding anterior do MainActivity: o gold precisa ser
    // incrementado com o sellPrice (fonte real confirmada: hook useInventory.ts, funcao
    // sellItem, gold: prev.gold + sellingPrice). A versao anterior so logava o preco sem
    // aplicar ao estado.
    fun sellItem(item: InventoryItem) {
        val current = _characterState.value ?: return
        val (updatedInventory, sellPrice) = InventoryLogic.sellItem(current.inventory, item)
        saveCharacterState(current.copy(inventory = updatedInventory, gold = current.gold + sellPrice))
    }

    fun buyShopItem(catalogEntry: com.iurispraecepta.herolog.logic.kingdom.ShopCatalogEntry) {
        val current = _characterState.value ?: return
        val result = com.iurispraecepta.herolog.logic.kingdom.ShopLogic.buyItem(
            gold = current.gold,
            inventory = current.inventory,
            catalogEntry = catalogEntry
        ) ?: return
        saveCharacterState(current.copy(gold = result.gold, inventory = result.inventory))
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
        saveCharacterState(result.updatedState.copy(habits = updatedHabits))

        val rewards = getDifficultyRewards(habit.difficulty)
        if (isUp) {
            val xp = if (current.charClass == CharClass.Mage) floor(rewards.xp * 1.2).toInt() else rewards.xp
            val gold = if (current.charClass == CharClass.Warrior) floor(rewards.gold * 1.2).toInt() else rewards.gold
            addSystemLog("✨ Prática Virtuosa: Completou o hábito positivo \"${habit.title}\"! Ganhou +${gold} GP e +${xp} XP.", true)
        } else {
            val damage = if (current.charClass == CharClass.Ranger) max(1, floor(rewards.damage * 0.7).toInt()) else rewards.damage
            addSystemLog("⚠️ Desvio Espiritual: Sofreu dano pelo hábito negativo \"${habit.title}\"! Perdeu -${damage} HP de sua integridade.", false)
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

    fun guildQuestsProcessed(state: CharacterState, referenceDate: Date = Date()): List<ProcessedQuest> {
        return QuestCatalog.GUILD_QUESTS.map { quest ->
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
    }

    fun equipTitle(titleId: String?) {
        val current = _characterState.value ?: return
        when (val result = TitleLogic.equipTitle(current.ownedTitles, titleId)) {
            is EquipTitleResult.Success -> saveCharacterState(current.copy(equippedTitle = result.equippedTitle))
            EquipTitleResult.NotOwned -> { /* no-op: mesma regra da fonte, titulo nao possuido nao equipa */ }
        }
    }

    fun buyTitle(titleId: String, price: Int) {
        val current = _characterState.value ?: return
        when (val result = TitleLogic.buyTitle(current.gold, current.ownedTitles, titleId, price)) {
            is TitlePurchaseResult.Success -> saveCharacterState(current.copy(gold = result.newGold, ownedTitles = result.newOwnedTitles))
            TitlePurchaseResult.InsufficientGold -> { /* no-op: mesma regra da fonte */ }
            TitlePurchaseResult.AlreadyOwned -> { /* no-op: bug já corrigido no Bloco 7 */ }
        }
    }

    fun claimAchievementTitle(titleId: String) {
        val current = _characterState.value ?: return
        saveCharacterState(current.copy(ownedTitles = TitleLogic.claimAchievementTitle(current.ownedTitles, titleId)))
    }

    fun importSaveFromPastedText(rawJson: String): SaveImportOutcome {
        return when (val result = SaveMigrationLogic.normalizeGameState(rawJson)) {
            is SaveImportResult.Success -> {
                saveCharacterState(result.characterState, suppressLevelUpDetection = true)
                SaveImportOutcome.Restored(result.characterState.charName)
            }
            is SaveImportResult.InvalidJson -> SaveImportOutcome.Failed
        }
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
        }
    }

    fun startSession(config: FocusSessionConfig, durationMinutes: Int) {
        if (_focusSessionState.value.isRunning) return
        focusTickJob?.cancel()
        graceTickJob?.cancel()
        breakTickJob?.cancel()
        _breakTimerState.value = BreakTimerState()

        val totalSeconds = durationMinutes * 60
        focusEndTimeMillis = clock() + totalSeconds * 1000L

        _focusSessionState.value = FocusSessionState(
            isRunning = true,
            isPaused = false,
            isFocusCompleted = false,
            timeLeft = totalSeconds,
            totalSeconds = totalSeconds,
            pauseCount = 0,
            config = config,
            durationMinutes = durationMinutes,
            pendingRewardsCalculation = null
        )

        viewModelScope.launch {
            focusSessionRepository.saveSession(
                PersistedFocusSession(
                    config = config,
                    durationMinutes = durationMinutes,
                    endTimeMillis = focusEndTimeMillis,
                    pendingCalculation = null
                )
            )
        }

        startFocusTickJob()
    }

    fun togglePauseQuest() {
        val current = _focusSessionState.value
        if (!current.isRunning) return

        if (!current.isPaused) {
            // Pausando
            focusTickJob?.cancel()
            _focusSessionState.value = current.copy(
                isPaused = true,
                pauseCount = current.pauseCount + 1
            )
            viewModelScope.launch {
                focusSessionRepository.clearSession()
            }
        } else {
            // Retomando
            focusEndTimeMillis = clock() + current.timeLeft * 1000L
            _focusSessionState.value = current.copy(isPaused = false)
            val config = current.config
            val durationMinutes = current.durationMinutes
            if (config != null) {
                viewModelScope.launch {
                    focusSessionRepository.saveSession(
                        PersistedFocusSession(
                            config = config,
                            durationMinutes = durationMinutes,
                            endTimeMillis = focusEndTimeMillis,
                            pendingCalculation = null
                        )
                    )
                }
            }
            startFocusTickJob()
        }
    }

    fun cancelAllTimers() {
        focusTickJob?.cancel()
        graceTickJob?.cancel()
        breakTickJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        cancelAllTimers()
    }

    fun cancelSession() {
        cancelAllTimers()
        _focusSessionState.value = FocusSessionState()
        viewModelScope.launch {
            focusSessionRepository.clearSession()
        }
    }

    fun abandonSession() {
        val wasDungeonMode = _focusSessionState.value.config?.isDungeonMode == true
        cancelSession()
        if (wasDungeonMode) {
            _dungeonSessionsProgress.value = 0
            addSystemLog(
                "💀 FRACASSO NA MASMORRA: Ao abandonar, sua expedição na Masmorra colapsou " +
                    "tragicamente e todo o progresso heróico de focos seguidos foi perdido nas cinzas.",
                highlighted = true
            )
        }
    }

    fun onAppBackgrounded() {
        val current = _focusSessionState.value
        val charState = _characterState.value
        if (!current.isRunning || current.isPaused || current.config?.isWildernessChecked != true ||
            current.isGraceActive || charState?.isPlayerDead == true) return

        val equippedTitleId = charState?.equippedTitle
        when (resolveWildernessInfraction(equippedTitleId)) {
            WildernessInfractionOutcome.CONVERTED_TO_PAUSE -> {
                if (!_focusSessionState.value.isPaused) {
                    togglePauseQuest()
                }
            }
            WildernessInfractionOutcome.GRACE_PERIOD_STARTED -> startGracePeriod()
        }
    }

    private fun startGracePeriod() {
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
                    triggerCognitiveDeath()
                    break
                }
                delay(250)
            }
        }
    }

    private fun triggerCognitiveDeath() {
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
        focusTickJob?.cancel() // cancelar também o job de contagem da sessão em si
        _focusSessionState.value = _focusSessionState.value.copy(
            isGraceActive = false,
            isRunning = false
        )
        viewModelScope.launch { focusSessionRepository.clearSession() }
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

    fun confirmFocusSession(editedNotes: String, selectedTag: String) {
        val current = _focusSessionState.value
        val calc = current.pendingRewardsCalculation ?: return
        val charState = _characterState.value ?: return
        val config = current.config

        val newState = FocusApplyLogic.apply(
            state = charState,
            calc = calc,
            editedNotes = editedNotes,
            selectedTag = selectedTag.ifEmpty { null },
            referenceDate = Date(clock())
        )

        // Skill level up
        val newSkillLevel = newState.skills.getOrNull(calc.skillIdx)?.level
        val oldSkillLevel = charState.skills.getOrNull(calc.skillIdx)?.level
        if (newSkillLevel != null && oldSkillLevel != null && newSkillLevel > oldSkillLevel) {
            addSystemLog("${calc.skillName} alcançou o Nível ${newSkillLevel}.", true)
        }

        // Loot
        calc.lootedItems.forEach {
            addSystemLog("✨ ESPÓLIO ENCONTRADO: Você localizou o item \"${it.emoji} ${it.name}\"! Vá ao Inventário para visualizá-lo ou equipá-lo.", true)
        }

        // Título raro
        calc.droppedTitle?.let {
            addSystemLog("✨ SORTUDO UNMISSABLE: O reino abençoou sua constância e você dropou o TÍTULO RARO [${it.name}]!", true)
        }

        // Equipamento quebrado (charges <= 0 após decremento)
        calc.usedEquipmentIndicesAndCharges.forEach { (index, charges) ->
            if (charges <= 0) {
                charState.equippedEquipment?.getOrNull(index)?.let { item ->
                    addSystemLog("⚠️ O equipamento \"${item.emoji} ${item.name}\" gastou todas as suas cargas e quebrou!", false)
                }
            }
        }

        // Masmorra
        if (calc.dungeonClearGoldBonus > 0) {
            addSystemLog("🏆 EXPLORAÇÃO MASMORRA SUCESSO: Concluiu as 4 sessões heróicas consecutivas! Um bônus monumental místico de +2.500 GP foi adicionado aos teus espólios!", true)
        } else if (calc.isDungeonMode && config != null) {
            val nextSessions = config.dungeonSessions + 1
            addSystemLog("⚔️ Masmorra Progresso: (${nextSessions}/4) focos consecutivos selados. Só mais ${4 - nextSessions} sessões para a glória eterna!", true)
        }

        // Conquistas novas (comparar antes/depois)
        (newState.achievements.toSet() - charState.achievements.toSet()).forEach { id ->
            if (id == "survive_wilderness") {
                addSystemLog("🏆 CONQUISTA HERÓICA: Desbloqueaste o selo [Sobrevivente da Wilderness]!", true)
            } else {
                addSystemLog("🏆 CONQUISTA HERÓICA: Desbloqueada rúnica especial [${id.uppercase()}]!", true)
            }
        }

        saveCharacterState(newState)

        if (config?.isDungeonMode == true) {
            val nextSessions = config.dungeonSessions + 1
            _dungeonSessionsProgress.value = if (nextSessions >= 4) 0 else nextSessions
        }

        val isDungeon = config?.isDungeonMode == true
        val charForBreak = newState
        if (charForBreak.pomodoroSettings.autoStartBreak) {
            val breakMins = if (isDungeon && (config.dungeonSessions + 1) >= 4) {
                charForBreak.pomodoroSettings.longBreakDuration.takeIf { it > 0 } ?: 15
            } else {
                charForBreak.pomodoroSettings.shortBreakDuration.takeIf { it > 0 } ?: 5
            }
            startBreakTimer(breakMins)
        } else {
            enterBreakPrep(wasDungeonMode = isDungeon)
        }

        _focusSessionState.value = FocusSessionState()
        viewModelScope.launch {
            focusSessionRepository.clearSession()
        }
    }

    fun selectBreakDuration(minutes: Int) {
        _breakTimerState.value = _breakTimerState.value.copy(selectedBreakMins = minutes)
    }

    fun enterBreakPrep(wasDungeonMode: Boolean = false) {
        val defaultBreakMins = _characterState.value?.pomodoroSettings?.shortBreakDuration?.takeIf { it > 0 } ?: 5
        _breakTimerState.value = _breakTimerState.value.copy(
            isBreakPrep = true,
            wasLastSessionDungeonMode = wasDungeonMode,
            selectedBreakMins = defaultBreakMins
        )
    }

    fun startBreakTimer(minutes: Int) {
        cancelSession() // mesma chamada de segurança que o React faz, mesmo já esperando sessão zerada
        val totalSeconds = minutes * 60
        breakEndTimeMillis = clock() + totalSeconds * 1000L
        _breakTimerState.value = _breakTimerState.value.copy(
            isBreakPrep = false,
            isBreakActive = true,
            selectedBreakMins = minutes,
            secondsLeft = totalSeconds,
            totalSeconds = totalSeconds
        )
        breakTickJob?.cancel()
        breakTickJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val remaining = max(0, ((breakEndTimeMillis - clock()) / 1000.0).roundToInt())
                _breakTimerState.value = _breakTimerState.value.copy(secondsLeft = remaining)
                if (remaining <= 0) {
                    onBreakTimerCompleted()
                    return@launch
                }
            }
        }
    }

    private fun onBreakTimerCompleted() {
        breakTickJob?.cancel()
        _breakTimerState.value = _breakTimerState.value.copy(isBreakActive = false)
        // SFX (sound.playLevelUp()) fora de escopo deste bloco — ponto de extensão futuro
    }

    fun skipBreak() {
        breakTickJob?.cancel()
        _breakTimerState.value = BreakTimerState()
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

    private fun startFocusTickJob() {
        focusTickJob?.cancel()
        focusTickJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val remaining = max(0, ((focusEndTimeMillis - clock()) / 1000.0).roundToInt())
                _focusSessionState.value = _focusSessionState.value.copy(timeLeft = remaining)
                if (remaining <= 0) {
                    onFocusSessionCompleted()
                    return@launch
                }
            }
        }
    }

    private fun onFocusSessionCompleted() {
        val current = _focusSessionState.value
        val config = current.config ?: return
        val durationMins = current.durationMinutes
        val charState = _characterState.value ?: return

        val calc = FocusRewardsLogic.calculate(
            state = charState,
            config = config,
            studiedMinutes = durationMins
        )

        _focusSessionState.value = current.copy(
            isRunning = false,
            isPaused = false,
            isFocusCompleted = true,
            timeLeft = 0,
            pendingRewardsCalculation = calc
        )

        viewModelScope.launch {
            focusSessionRepository.saveSession(
                PersistedFocusSession(
                    config = config,
                    durationMinutes = durationMins,
                    endTimeMillis = focusEndTimeMillis,
                    pendingCalculation = calc
                )
            )
        }
    }
}
