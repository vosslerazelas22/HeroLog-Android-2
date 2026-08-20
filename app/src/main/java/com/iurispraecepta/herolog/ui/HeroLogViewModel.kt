package com.iurispraecepta.herolog.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iurispraecepta.herolog.data.createInitialCharacterState
import com.iurispraecepta.herolog.data.repository.CharacterRepository
import com.iurispraecepta.herolog.data.repository.FocusSessionRepository
import com.iurispraecepta.herolog.logic.EquipTitleResult
import com.iurispraecepta.herolog.logic.InventoryLogic
import com.iurispraecepta.herolog.logic.TitleLogic
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
import com.iurispraecepta.herolog.model.CharacterState
import com.iurispraecepta.herolog.model.InventoryItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

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
            if (!finalState.hasClaimedLogin) {
                val loginGold = if (finalState.charClass == com.iurispraecepta.herolog.model.CharClass.Warrior) 120 else 100
                finalState = finalState.copy(
                    gold = finalState.gold + loginGold,
                    totalGoldEarned = finalState.totalGoldEarned + loginGold,
                    hasClaimedLogin = true
                )
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

    fun saveCharacterState(state: CharacterState) {
        viewModelScope.launch {
            repository.saveCharacterState(state)
            _characterState.value = state
        }
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

    fun discardItem(item: InventoryItem) {
        val current = _characterState.value ?: return
        val updatedInventory = InventoryLogic.discardItem(current.inventory, item)
        saveCharacterState(current.copy(inventory = updatedInventory))
    }

    fun triggerHabit(habitId: String, isUp: Boolean) {
        val current = _characterState.value ?: return
        val habit = current.habits.find { it.id == habitId } ?: return
        val result = HabitLogic.trigger(habit, current, isUp)
        val updatedHabits = current.habits.map { if (it.id == habitId) result.updatedHabit else it }
        saveCharacterState(result.updatedState.copy(habits = updatedHabits))
    }

    fun toggleDaily(dailyId: String) {
        val current = _characterState.value ?: return
        val daily = current.dailies.find { it.id == dailyId } ?: return
        val result = DailyLogic.toggle(daily, current)
        val updatedDailies = current.dailies.map { if (it.id == dailyId) result.updatedDaily else it }
        saveCharacterState(result.updatedState.copy(dailies = updatedDailies))
    }

    fun toggleTodo(todoId: String) {
        val current = _characterState.value ?: return
        val todo = current.todos.find { it.id == todoId } ?: return
        val result = TodoLogic.toggle(todo, current)
        val updatedTodos = current.todos.map { if (it.id == todoId) result.updatedTodo else it }
        saveCharacterState(result.updatedState.copy(todos = updatedTodos))
    }

    fun addHabit(title: String, notes: String, up: Boolean, down: Boolean, difficulty: Difficulty, tags: List<String>) {
        val current = _characterState.value ?: return
        val newHabit = Habit(
            id = java.util.UUID.randomUUID().toString(),
            title = title, notes = notes, up = up, down = down, difficulty = difficulty,
            upCount = 0, downCount = 0, streak = 0, tags = tags
        )
        saveCharacterState(current.copy(habits = current.habits + newHabit))
    }

    fun editHabit(edited: Habit) {
        val current = _characterState.value ?: return
        val updated = current.habits.map { if (it.id == edited.id) edited else it }
        saveCharacterState(current.copy(habits = updated))
    }

    fun deleteHabit(habitId: String) {
        val current = _characterState.value ?: return
        saveCharacterState(current.copy(habits = current.habits.filter { it.id != habitId }))
    }

    fun addDaily(title: String, notes: String, difficulty: Difficulty, streak: Int, repeats: RepeatInterval, every: Int, tags: List<String>, checklistTexts: List<String>) {
        val current = _characterState.value ?: return
        val checklist = checklistTexts.map { ChecklistItem(id = java.util.UUID.randomUUID().toString(), text = it, completed = false) }
        val newDaily = Daily(
            id = java.util.UUID.randomUUID().toString(),
            title = title, notes = notes, difficulty = difficulty, completed = false,
            streak = streak, repeats = repeats, every = every, tags = tags, checklist = checklist,
            value = 0, createdAt = java.time.Instant.now().toString()
        )
        saveCharacterState(current.copy(dailies = current.dailies + newDaily))
    }

    fun editDaily(edited: Daily) {
        val current = _characterState.value ?: return
        val updated = current.dailies.map { if (it.id == edited.id) edited else it }
        saveCharacterState(current.copy(dailies = updated))
    }

    fun deleteDaily(dailyId: String) {
        val current = _characterState.value ?: return
        saveCharacterState(current.copy(dailies = current.dailies.filter { it.id != dailyId }))
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
        val checklist = checklistTexts.map { ChecklistItem(id = java.util.UUID.randomUUID().toString(), text = it, completed = false) }
        val newTodo = Todo(
            id = java.util.UUID.randomUUID().toString(),
            title = title, notes = notes, difficulty = difficulty, completed = false,
            tags = tags, checklist = checklist, createdAt = java.time.Instant.now().toString()
        )
        saveCharacterState(current.copy(todos = current.todos + newTodo))
    }

    fun editTodo(edited: Todo) {
        val current = _characterState.value ?: return
        val updated = current.todos.map { if (it.id == edited.id) edited else it }
        saveCharacterState(current.copy(todos = updated))
    }

    fun deleteTodo(todoId: String) {
        val current = _characterState.value ?: return
        saveCharacterState(current.copy(todos = current.todos.filter { it.id != todoId }))
    }

    fun toggleTodoChecklistItem(todoId: String, itemId: String) {
        val current = _characterState.value ?: return
        val updated = current.todos.map { t ->
            if (t.id == todoId) t.copy(checklist = t.checklist.map { if (it.id == itemId) it.copy(completed = !it.completed) else it })
            else t
        }
        saveCharacterState(current.copy(todos = updated))
    }

    fun equipTitle(titleId: String?) {
        val current = _characterState.value ?: return
        when (val result = TitleLogic.equipTitle(current.ownedTitles, titleId)) {
            is EquipTitleResult.Success -> saveCharacterState(current.copy(equippedTitle = result.equippedTitle))
            EquipTitleResult.NotOwned -> { /* no-op: mesma regra da fonte, titulo nao possuido nao equipa */ }
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
