package com.iurispraecepta.herolog

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.iurispraecepta.herolog.logic.DeleteSkillEligibility
import com.iurispraecepta.herolog.logic.InventoryLogic
import com.iurispraecepta.herolog.logic.SkillLogic
import com.iurispraecepta.herolog.logic.SkillOperationResult
import com.iurispraecepta.herolog.model.BuffType
import com.iurispraecepta.herolog.model.CharClass
import com.iurispraecepta.herolog.model.CharacterSummary
import com.iurispraecepta.herolog.model.InventoryItem
import com.iurispraecepta.herolog.model.Rarity
import com.iurispraecepta.herolog.model.Skill
import com.iurispraecepta.herolog.ui.character.CharacterScreen
import com.iurispraecepta.herolog.ui.character.LevelUpOverlay
import com.iurispraecepta.herolog.ui.components.ModalVariant
import com.iurispraecepta.herolog.ui.focus.AMBIENT_SOUNDS
import com.iurispraecepta.herolog.ui.focus.AmbientSoundModal
import com.iurispraecepta.herolog.ui.focus.BreakPrepScreen
import com.iurispraecepta.herolog.ui.focus.FocusModeScreen
import com.iurispraecepta.herolog.ui.focus.FocusOrb
import com.iurispraecepta.herolog.ui.focus.FocusOrbSize
import com.iurispraecepta.herolog.ui.focus.IncursionModeModal
import com.iurispraecepta.herolog.ui.focus.ModeDescriptionModal
import com.iurispraecepta.herolog.ui.focus.QuickActionsBar
import com.iurispraecepta.herolog.ui.focus.TimerSettingsModal
import com.iurispraecepta.herolog.ui.focus.AmbientSoundController
import com.iurispraecepta.herolog.ui.focus.rememberAmbientSoundController
import com.iurispraecepta.herolog.ui.focus.RaidMode
import com.iurispraecepta.herolog.ui.focus.RaidModeHelpContent
import com.iurispraecepta.herolog.ui.focus.RaidModeInfoBox
import com.iurispraecepta.herolog.ui.focus.RaidModeSegmentedControl
import com.iurispraecepta.herolog.ui.focus.SkillInlineCarousel
import com.iurispraecepta.herolog.ui.focus.TitleDisplay
import com.iurispraecepta.herolog.ui.focus.buildStandardLootHelpBlocks
import com.iurispraecepta.herolog.ui.focus.lootChancePercentFrom
import com.iurispraecepta.herolog.ui.focus.raidModeFrom
import com.iurispraecepta.herolog.ui.focus.toLegacyFlags
import com.iurispraecepta.herolog.ui.inventory.InventoryScreen
import com.iurispraecepta.herolog.ui.skills.SkillSelectorModal
import com.iurispraecepta.herolog.ui.skills.SkillsScreen
import com.iurispraecepta.herolog.ui.habits.HabitsScreen
import com.iurispraecepta.herolog.ui.dailies.DailiesScreen
import com.iurispraecepta.herolog.ui.todos.TodosScreen
import com.iurispraecepta.herolog.ui.quests.QuestsScreen
import com.iurispraecepta.herolog.ui.history.HistoryScreen
import com.iurispraecepta.herolog.ui.navigation.HeroLogBottomNav
import com.iurispraecepta.herolog.ui.navigation.MODULE_TITLES
import com.iurispraecepta.herolog.ui.navigation.getActiveModule
import com.iurispraecepta.herolog.ui.components.PlaceholderScreen
import com.iurispraecepta.herolog.ui.kingdom.GuideScreen
import com.iurispraecepta.herolog.ui.kingdom.HeatmapScreen
import com.iurispraecepta.herolog.ui.kingdom.LogsScreen
import com.iurispraecepta.herolog.ui.kingdom.ShopScreen
import com.iurispraecepta.herolog.ui.kingdom.StatsScreen
import com.iurispraecepta.herolog.ui.kingdom.AchievementsScreen
import com.iurispraecepta.herolog.ui.kingdom.TitleSelectorScreen
import com.iurispraecepta.herolog.ui.components.AppHeader
import com.iurispraecepta.herolog.ui.components.GeneralSettingsModal
import com.iurispraecepta.herolog.ui.components.RestoreSaveDialog
import com.iurispraecepta.herolog.ui.components.SaveImportResultDialog
import com.iurispraecepta.herolog.logic.SaveImportOutcome
import com.iurispraecepta.herolog.ui.theme.Amber400
import com.iurispraecepta.herolog.ui.theme.HeroLogTheme
import com.iurispraecepta.herolog.ui.theme.Stone900
import com.iurispraecepta.herolog.ui.theme.Stone950
import com.iurispraecepta.herolog.model.OrbConcept
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.iurispraecepta.herolog.logic.toSummary
import com.iurispraecepta.herolog.ui.HeroLogViewModel
import com.iurispraecepta.herolog.ui.HeroLogViewModelFactory
import com.iurispraecepta.herolog.ui.focus.FocusCompletionFlow
import com.iurispraecepta.herolog.logic.quests.QuestLogic
import com.iurispraecepta.herolog.logic.focus.FocusSessionConfig
import com.iurispraecepta.herolog.model.CharacterState
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            HeroLogTheme {
                // Estado de navegação em string, fiel ao `activeTab` da fonte React (`useState<string>('focus')`).
                var activeTab by remember { mutableStateOf("focus") }
                var questsSubTab by remember { mutableStateOf("daily") }
                var isCreateModalOpen by remember { mutableStateOf(false) }
                var isRestoreSaveOpen by remember { mutableStateOf(false) }
                var isGeneralSettingsOpen by remember { mutableStateOf(false) }
                var saveImportOutcome by remember { mutableStateOf<SaveImportOutcome?>(null) }

                val application = LocalContext.current.applicationContext as HeroLogApplication
                val heroLogViewModel: HeroLogViewModel = viewModel(factory = HeroLogViewModelFactory(application))
                val characterState by heroLogViewModel.characterState.collectAsState()
                var inspectingItem by remember { mutableStateOf<InventoryItem?>(null) }
                var isSfxMuted by remember { mutableStateOf(false) }
                val coroutineScope = rememberCoroutineScope()

                // HazeState para backdrop-blur no BottomNav (estilo React backdrop-blur-md)
                val hazeState = remember { HazeState() }

                // Controller de som ambiente no escopo raiz do HeroLogTheme (acima do Scaffold)
                // para sobreviver a trocas de aba (recomposição do content do Scaffold).
                // O DisposableEffect(Unit) em rememberAmbientSoundController só libera ao
                // desmontar o composable raiz, não ao trocar de aba.
                val ambientController = rememberAmbientSoundController()

                val lifecycleOwner = LocalLifecycleOwner.current
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_STOP -> heroLogViewModel.onAppBackgrounded()
                            Lifecycle.Event.ON_START -> heroLogViewModel.onAppForegrounded()
                            else -> {}
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        AppHeader(
                            streak = characterState?.streak ?: 0,
                            gold = characterState?.gold ?: 0,
                            isSfxMuted = isSfxMuted,
                            onToggleSfx = { isSfxMuted = !isSfxMuted },
                            onOpenSettings = {
                                isGeneralSettingsOpen = true
                            }
                        )
                    },
                    bottomBar = {
                        HeroLogBottomNav(
                            activeTab = activeTab,
                            onChangeTab = { activeTab = it },
                            hazeState = hazeState
                        )
                    },
                    floatingActionButton = {
                        if (activeTab == "skills") {
                            FloatingActionButton(
                                onClick = { isCreateModalOpen = true },
                                containerColor = Amber400,
                                contentColor = Stone950
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Adicionar Habilidade"
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding).hazeSource(hazeState)) {
                        when (activeTab) {
                            "skills" -> {
                                val state = characterState
                                if (state == null) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("Carregando habilidades...", color = Amber400)
                                    }
                                } else {
                                    SkillsScreen(
                                        skills = state.skills,
                                        onAddTagToSkill = { skillIdx, newTag ->
                                            heroLogViewModel.addTagToSkill(skillIdx, newTag)
                                        },
                                        onRemoveTagFromSkill = { skillIdx, tagIdx ->
                                            heroLogViewModel.removeTagFromSkill(skillIdx, tagIdx)
                                        },
                                        onAddCustomSkill = { name, emoji ->
                                            when (val result = heroLogViewModel.addCustomSkill(name, emoji)) {
                                                is SkillOperationResult.Success -> {
                                                    isCreateModalOpen = false
                                                }
                                                is SkillOperationResult.Error -> {
                                                    Log.d("HeroLog", "Falha ao adicionar skill: ${result.reason}")
                                                }
                                            }
                                        },
                                        onDeleteSkill = { idx ->
                                            val eligibility = heroLogViewModel.deleteSkill(idx)
                                            if (eligibility != DeleteSkillEligibility.Eligible) {
                                                Log.d("HeroLog", "Falha ao deletar skill: $eligibility")
                                            }
                                        },
                                        onPrestigeSkill = { idx ->
                                            heroLogViewModel.prestigeSkill(idx)
                                        },
                                        onRenameSkill = { idx, newName ->
                                            when (val result = heroLogViewModel.renameSkill(idx, newName)) {
                                                is SkillOperationResult.Success -> {
                                                    // Success state automatically flow via characterState
                                                }
                                                is SkillOperationResult.Error -> {
                                                    Log.d("HeroLog", "Falha ao renomear skill: ${result.reason}")
                                                }
                                            }
                                        },
                                        isCreateModalOpen = isCreateModalOpen,
                                        onCreateModalOpenChange = { isCreateModalOpen = it }
                                    )
                                }
                            }
                            "character" -> {
                                val state = characterState
                                if (state == null) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("Carregando personagem...", color = Amber400)
                                    }
                                } else {
                                    CharacterScreen(
                                        character = state.toSummary(),
                                        equippedEquipment = state.equippedEquipment ?: listOf(null, null, null),
                                        activeBuffs = InventoryLogic.activeBuffs(state.inventory),
                                        onUnequipItem = { slotIdx -> heroLogViewModel.unequipItem(slotIdx) },
                                        ownedTitles = state.ownedTitles ?: emptyList(),
                                        onEquipTitle = { titleId -> heroLogViewModel.equipTitle(titleId) }
                                    )
                                }
                            }
                            "inventory" -> {
                                val state = characterState
                                if (state == null) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("Carregando inventario...", color = Amber400)
                                    }
                                } else {
                                    InventoryScreen(
                                        inventory = state.inventory,
                                        inspectingItem = inspectingItem,
                                        onInspectItem = { item -> inspectingItem = item },
                                        onCloseInspection = { inspectingItem = null },
                                        onEquipItem = { item, slotIdx ->
                                            heroLogViewModel.equipItem(item, slotIdx)
                                            inspectingItem = null
                                        },
                                        onSellItem = { item ->
                                            heroLogViewModel.sellItem(item)
                                            inspectingItem = null
                                        },
                                        onDiscardItem = { item ->
                                            heroLogViewModel.discardItem(item)
                                            inspectingItem = null
                                        }
                                    )
                                }
                            }
                            "habits" -> {
                                val state = characterState
                                if (state == null) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("Carregando hábitos...", color = Amber400)
                                    }
                                } else {
                                    HabitsScreen(
                                        habits = state.habits,
                                        onTriggerHabit = { habitId, isUp -> heroLogViewModel.triggerHabit(habitId, isUp) },
                                        onAddHabit = { title, notes, up, down, difficulty, tags ->
                                            heroLogViewModel.addHabit(title, notes, up, down, difficulty, tags)
                                        },
                                        onEditHabit = { habit -> heroLogViewModel.editHabit(habit) },
                                        onDeleteHabit = { habitId -> heroLogViewModel.deleteHabit(habitId) }
                                    )
                                }
                            }
                            "dailies" -> {
                                val state = characterState
                                if (state == null) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("Carregando tarefas diárias...", color = Amber400)
                                    }
                                } else {
                                    DailiesScreen(
                                        dailies = state.dailies,
                                        onToggleDaily = { dailyId -> heroLogViewModel.toggleDaily(dailyId) },
                                        onToggleChecklistItem = { dailyId, itemId ->
                                            heroLogViewModel.toggleDailyChecklistItem(dailyId, itemId)
                                        },
                                        onAddDaily = { title, notes, difficulty, streak, repeats, every, tags, checklistTexts ->
                                            heroLogViewModel.addDaily(title, notes, difficulty, streak, repeats, every, tags, checklistTexts)
                                        },
                                        onEditDaily = { daily -> heroLogViewModel.editDaily(daily) },
                                        onDeleteDaily = { dailyId -> heroLogViewModel.deleteDaily(dailyId) }
                                    )
                                }
                            }
                            "todos" -> {
                                val state = characterState
                                if (state == null) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("Carregando missões avulsas...", color = Amber400)
                                    }
                                } else {
                                    TodosScreen(
                                        todos = state.todos,
                                        onToggleTodo = { todoId -> heroLogViewModel.toggleTodo(todoId) },
                                        onToggleChecklistItem = { todoId, itemId ->
                                            heroLogViewModel.toggleTodoChecklistItem(todoId, itemId)
                                        },
                                        onAddTodo = { title, notes, difficulty, tags, checklistTexts ->
                                            heroLogViewModel.addTodo(title, notes, difficulty, tags, checklistTexts)
                                        },
                                        onEditTodo = { todo -> heroLogViewModel.editTodo(todo) },
                                        onDeleteTodo = { todoId -> heroLogViewModel.deleteTodo(todoId) }
                                    )
                                }
                            }
                            "focus" -> {
                                FocusOrbPreviewScreen(
                                    viewModel = heroLogViewModel,
                                    characterState = characterState,
                                    orbConcept = characterState?.orbConcept ?: OrbConcept.D,
                                    ambientController = ambientController
                                )
                            }
                            // Contratos e Crônicas Diárias (Missões) e todo o módulo Reino
                            // (Bazar/Títulos/Heatmap/Estatísticas/Conquistas/Registros/Tutorial)
                            // ainda não portados — placeholder até virarem blocos reais.
                            "quests" -> {
                                val state = characterState
                                if (state != null) {
                                    QuestsScreen(
                                        dailyQuests = heroLogViewModel.dailyQuests(state),
                                        guildQuests = heroLogViewModel.guildQuestsProcessed(state),
                                        activeSubTab = questsSubTab,
                                        onSubTabChange = { questsSubTab = it },
                                        onClaimQuestReward = heroLogViewModel::claimQuestReward
                                    )
                                }
                            }
                            "history" -> {
                                val state = characterState
                                if (state != null) {
                                    HistoryScreen(history = state.history)
                                }
                            }
                            "shop" -> {
                                val state = characterState
                                if (state == null) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("Carregando bazar...", color = Amber400)
                                    }
                                } else {
                                    ShopScreen(
                                        gold = state.gold,
                                        inventory = state.inventory,
                                        state = state,
                                        onBuyItem = { entry -> heroLogViewModel.buyShopItem(entry) },
                                        onBuyTitle = { id, price -> heroLogViewModel.buyTitle(id, price) },
                                        onClaimAchievementTitle = { id -> heroLogViewModel.claimAchievementTitle(id) }
                                    )
                                }
                            }
                            "titles" -> {
                                val state = characterState
                                if (state == null) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("Carregando títulos...", color = Amber400)
                                    }
                                } else {
                                    TitleSelectorScreen(
                                        ownedTitles = state.ownedTitles ?: emptyList(),
                                        equippedTitle = state.equippedTitle,
                                        onEquipTitle = { titleId -> heroLogViewModel.equipTitle(titleId) }
                                    )
                                }
                            }
                            "heatmap" -> {
                                val state = characterState
                                if (state == null) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("Carregando heatmap...", color = Amber400)
                                    }
                                } else {
                                    HeatmapScreen(history = state.history, streak = state.streak)
                                }
                            }
                            "stats" -> {
                                val state = characterState
                                if (state == null) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("Carregando estatísticas...", color = Amber400)
                                    }
                                } else {
                                    StatsScreen(state = state)
                                }
                            }
                            "achievements" -> {
                                val state = characterState
                                if (state == null) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("Carregando conquistas...", color = Amber400)
                                    }
                                } else {
                                    AchievementsScreen(state = state)
                                }
                            }
                            "logs" -> LogsScreen(logs = heroLogViewModel.systemLogs.collectAsState().value)
                            "guide" -> GuideScreen()
                            else -> PlaceholderScreen(title = activeTab)
                        }
                    }

                    RestoreSaveDialog(
                        isOpen = isRestoreSaveOpen,
                        onDismiss = { isRestoreSaveOpen = false },
                        onConfirm = { rawJson ->
                            saveImportOutcome = heroLogViewModel.importSaveFromPastedText(rawJson)
                        }
                    )

                    SaveImportResultDialog(
                        result = saveImportOutcome,
                        onDismiss = { saveImportOutcome = null }
                    )

                    GeneralSettingsModal(
                        isOpen = isGeneralSettingsOpen,
                        onDismiss = { isGeneralSettingsOpen = false },
                        characterName = characterState?.charName ?: "",
                        charClass = characterState?.charClass ?: CharClass.Mage,
                        orbConcept = characterState?.orbConcept ?: OrbConcept.D,
                        onNameChange = { name -> heroLogViewModel.updateCharacterProfile(name, characterState?.charClass ?: CharClass.Mage) },
                        onClassChange = { cls -> heroLogViewModel.updateCharacterProfile(characterState?.charName ?: "", cls) },
                        onOrbConceptChange = { concept -> heroLogViewModel.updateOrbConcept(concept) },
                        coroutineScope = coroutineScope
                    )

                    // Porte de `activeLevelUp` (useLevelUp.ts) -- popup global, visivel por cima de qualquer
                    // aba, igual a fonte (renderizado no nivel do App.tsx, nao preso a nenhuma tela especifica).
                    val levelUpQueue by heroLogViewModel.levelUpQueue.collectAsState()
                    LevelUpOverlay(
                        event = levelUpQueue.firstOrNull(),
                        onDismiss = { heroLogViewModel.dismissCurrentLevelUp() }
                    )
                }
            }
        }
    }
}

@Composable
fun FocusOrbPreviewScreen(
    viewModel: HeroLogViewModel,
    characterState: CharacterState?,
    ambientController: AmbientSoundController,
    orbConcept: OrbConcept = OrbConcept.D,
    modifier: Modifier = Modifier
) {
    if (characterState == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Carregando personagem...", color = Amber400)
        }
        return
    }

    val selectedSkill = characterState.skills.firstOrNull()
    if (selectedSkill == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "Por favor, crie uma habilidade primeiro para poder iniciar o Foco!",
                color = Amber400,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(24.dp)
            )
        }
        return
    }

    var isDungeonModePreview by remember { mutableStateOf(false) }
    var isWildernessPreview by remember { mutableStateOf(false) }
    var activeHelpMode by remember { mutableStateOf<RaidMode?>(null) }
    var isIncursionModalOpen by remember { mutableStateOf(false) }
    var isSkillSelectorOpen by remember { mutableStateOf(false) }
    var isTimerSettingsOpen by remember { mutableStateOf(false) }
    var selectedSkillIdx by remember { mutableStateOf(0) }
    var isFocusMode by remember { mutableStateOf(false) }
    var isAmbientModalOpen by remember { mutableStateOf(false) }

    var isConfirmingAbandon by remember { mutableStateOf(false) }
    var confirmAbandonJob by remember { mutableStateOf<Job?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val focusState by viewModel.focusSessionState.collectAsState()
    val dungeonSessionsProgress by viewModel.dungeonSessionsProgress.collectAsState()
    val breakTimerState by viewModel.breakTimerState.collectAsState()

    LaunchedEffect(focusState.isRunning, focusState.isPaused, breakTimerState.isBreakActive) {
        ambientController.sync(
            isWorkSessionActive = focusState.isRunning && !focusState.isPaused && !breakTimerState.isBreakActive
        )
    }

    // Troca de trilha em tempo real: quando selectedTrack muda (via modal), chama sync()
    // com o estado atual da sessão. Paridade com React useAmbientSound.ts:60-96
    // (useEffect com dependência em selectedTrack que faz audio.src = newSrc; audio.load()).
    LaunchedEffect(ambientController.selectedTrack) {
        ambientController.sync(
            isWorkSessionActive = focusState.isRunning && !focusState.isPaused && !breakTimerState.isBreakActive
        )
    }

    LaunchedEffect(dungeonSessionsProgress) {
        if (dungeonSessionsProgress == 0 && isDungeonModePreview) {
            isDungeonModePreview = false
        }
    }

    val validSkillIdx = selectedSkillIdx.coerceIn(0, (characterState.skills.size - 1).coerceAtLeast(0))
    val focusDuration = characterState.pomodoroSettings.focusDuration

    Box(modifier = modifier.fillMaxSize()) {
        if (focusState.isFocusCompleted) {
            val rewards = focusState.pendingRewardsCalculation
            if (rewards != null) {
                val streak = characterState.streak
                val todayString = QuestLogic.toDateStringJs(java.util.Date())
                val shouldShowStreakCelebration = characterState.lastStudyDate != todayString
                val selectedSkillForSession = characterState.skills.getOrNull(rewards.skillIdx)
                val skillTags = selectedSkillForSession?.tags ?: emptyList()

                FocusCompletionFlow(
                    rewardsCalculation = rewards,
                    pauseCount = focusState.pauseCount,
                    streak = streak,
                    shouldShowStreakCelebration = shouldShowStreakCelebration,
                    skillTags = skillTags,
                    onConfirm = { editedNotes, selectedTag ->
                        viewModel.confirmFocusSession(editedNotes, selectedTag)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Erro: Cálculo de recompensa pendente ausente.", color = Amber400)
                }
            }
        } else if ((focusState.isRunning && isFocusMode) || characterState.isPlayerDead) {
            val config = focusState.config
            val selectedSkillForSession = characterState.skills.getOrNull(config?.selectedSkillIdx ?: 0)
            val skillName = selectedSkillForSession?.name ?: "Habilidade"
            val skillEmoji = selectedSkillForSession?.emoji ?: "💻"

            FocusModeScreen(
                skillName = skillName,
                skillEmoji = skillEmoji,
                isDungeonMode = config?.isDungeonMode ?: false,
                dungeonSessions = config?.dungeonSessions ?: 0,
                isWildernessChecked = config?.isWildernessChecked ?: false,
                timeLeft = focusState.timeLeft,
                totalSeconds = focusState.totalSeconds,
                isRunning = focusState.isRunning,
                isPaused = focusState.isPaused,
                onTogglePause = { viewModel.togglePauseQuest() },
                onExit = {
                    isFocusMode = false
                },
                isGraceActive = focusState.isGraceActive,
                graceSecondsLeft = focusState.graceSecondsLeft,
                isPlayerDead = characterState.isPlayerDead,
                onReturnToFocusCap = { viewModel.returnToFocusFromGrace() },
                onRespawn = { viewModel.respawnHero() },
                orbConcept = characterState?.orbConcept ?: OrbConcept.D,
                modifier = Modifier.fillMaxSize()
            )
        } else if (breakTimerState.isBreakPrep) {
            BreakPrepScreen(
                shortBreakMinutes = characterState.pomodoroSettings.shortBreakDuration,
                longBreakMinutes = characterState.pomodoroSettings.longBreakDuration,
                selectedBreakMinutes = breakTimerState.selectedBreakMins,
                isDungeonMode = breakTimerState.wasLastSessionDungeonMode,
                onSelectDuration = { viewModel.selectBreakDuration(it) },
                onStartBreak = { viewModel.startBreakTimer(breakTimerState.selectedBreakMins) },
                onSkipBreak = { viewModel.skipBreak() }
            )
        } else if (breakTimerState.isBreakActive) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                FocusOrb(
                    timeLeft = breakTimerState.secondsLeft,
                    totalSeconds = breakTimerState.selectedBreakMins * 60,
                    isRunning = true,
                    isPaused = false,
                    isBreakActive = true,
                    orbConcept = orbConcept,
                    size = FocusOrbSize.STANDARD
                )
                Spacer(modifier = Modifier.height(20.dp))
                OutlinedButton(
                    onClick = { viewModel.skipBreak() }
                ) {
                    Text("⏩ Pular Descanso")
                }
            }
        } else if (focusState.isRunning) {
            // Sessão ativa, mas NÃO em tela cheia — orb inline + Pausar/Abandonar, igual a
            // fonte real (App.tsx renderiza o orb direto na aba Foco enquanto roda; "Tela Cheia"
            // é ação separada, não automática).
            val config = focusState.config
            val currentRaidModeRunning = raidModeFrom(config?.isDungeonMode ?: false, config?.isWildernessChecked ?: false)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                SkillInlineCarousel(
                    skills = characterState.skills,
                    selectedIndex = config?.selectedSkillIdx ?: 0,
                    onSelectIndex = { selectedSkillIdx = it },
                    disabled = true,
                    onOpenSkillsManager = { isSkillSelectorOpen = true }
                )
                Spacer(modifier = Modifier.height(16.dp))
                FocusOrb(
                    timeLeft = focusState.timeLeft,
                    totalSeconds = focusState.totalSeconds,
                    isRunning = focusState.isRunning,
                    isPaused = focusState.isPaused,
                    isDungeonMode = config?.isDungeonMode ?: false,
                    isWildernessMode = config?.isWildernessChecked ?: false,
                    orbConcept = orbConcept,
                    size = FocusOrbSize.STANDARD
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.togglePauseQuest() },
                        colors = if (focusState.isPaused) {
                            ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0xFF581C87).copy(alpha = 0.1f),
                                contentColor = Color(0xFFD8B4FE)
                            )
                        } else {
                            ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0xFF0C0A09).copy(alpha = 0.4f),
                                contentColor = Color(0xFFF5DFA0)
                            )
                        },
                        border = BorderStroke(
                            1.dp,
                            if (focusState.isPaused) Color(0xFFA855F7) else Color(0xFFE5C158).copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            painter = painterResource(
                                if (focusState.isPaused) R.drawable.lucide_ic_play else R.drawable.lucide_ic_pause
                            ),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            if (focusState.isPaused) "RETOMAR MISSÃO" else "PAUSAR MISSÃO",
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            if (!isConfirmingAbandon) {
                                isConfirmingAbandon = true
                                viewModel.addSystemLog(
                                    "⚠️ Atenção: Clique novamente em \"Abandonar\" para confirmar a desistência da missão."
                                )
                                confirmAbandonJob?.cancel()
                                confirmAbandonJob = coroutineScope.launch {
                                    delay(5000)
                                    isConfirmingAbandon = false
                                }
                            } else {
                                confirmAbandonJob?.cancel()
                                isConfirmingAbandon = false
                                viewModel.abandonSession()
                            }
                        },
                        colors = if (isConfirmingAbandon) {
                            ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0xFFDC2626),
                                contentColor = Color.White
                            )
                        } else {
                            ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0xFF450A0A).copy(alpha = 0.4f),
                                contentColor = Color(0xFFF87171)
                            )
                        },
                        border = BorderStroke(
                            1.dp,
                            if (isConfirmingAbandon) Color(0xFFF87171) else Color(0xFFEF4444).copy(alpha = 0.3f)
                        )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.lucide_ic_x),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isConfirmingAbandon) "CONFIRMAR?" else "ABANDONAR", fontWeight = if (isConfirmingAbandon) FontWeight.Bold else FontWeight.Normal)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                RaidModeInfoBox(
                    mode = currentRaidModeRunning,
                    dungeonSessions = dungeonSessionsProgress,
                    dungeonOnCooldown = false,
                    lootChancePercent = lootChancePercentFrom(
                        studiedMinutes = focusDuration,
                        isDungeon = config?.isDungeonMode ?: false,
                        equippedTitleId = characterState.equippedTitle
                    ),
                    onShowDungeonHelp = { activeHelpMode = RaidMode.MASMORRA },
                    onShowWildernessHelp = { activeHelpMode = RaidMode.SELVAGEM },
                    onShowStandardHelp = { activeHelpMode = RaidMode.PADRAO }
                )

                Spacer(modifier = Modifier.height(8.dp))

                QuickActionsBar(
                    isDungeonMode = config?.isDungeonMode ?: false,
                    isWildernessMode = config?.isWildernessChecked ?: false,
                    isRunning = true,
                    onOpenModeModal = { /* Modo não editável com sessão em andamento, fonte já trata via disabled */ },
                    activeAmbientIcon = AMBIENT_SOUNDS.find { it.id == ambientController.selectedTrack }?.icone,
                    onOpenAmbientModal = { isAmbientModalOpen = true },
                    isSettingsEnabled = false,
                    onOpenSettingsModal = {},
                    onEnterFullscreen = { isFocusMode = true }
                )
            }
        } else {
            val currentRaidMode = raidModeFrom(isDungeonModePreview, isWildernessPreview)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                SkillInlineCarousel(
                    skills = characterState.skills,
                    selectedIndex = validSkillIdx,
                    onSelectIndex = { selectedSkillIdx = it },
                    disabled = focusState.isRunning,
                    onOpenSkillsManager = { isSkillSelectorOpen = true }
                )

                Spacer(modifier = Modifier.height(16.dp))

                FocusOrb(
                    timeLeft = focusDuration * 60,
                    totalSeconds = focusDuration * 60,
                    isRunning = false,
                    isPaused = false,
                    isBreakActive = false,
                    isDungeonMode = isDungeonModePreview,
                    isWildernessMode = isWildernessPreview,
                    orbConcept = orbConcept,
                    size = FocusOrbSize.STANDARD
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFFB48C26), Color(0xFFE5C158), Color(0xFFF5DFA0))
                            )
                        )
                        .border(1.dp, Color(0xFFE5C158), RoundedCornerShape(6.dp))
                        .clickable {
                            val config = FocusSessionConfig(
                                selectedSkillIdx = validSkillIdx,
                                isWildernessChecked = isWildernessPreview,
                                isDungeonMode = isDungeonModePreview,
                                dungeonSessions = dungeonSessionsProgress
                            )
                            viewModel.startSession(config, durationMinutes = focusDuration)
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "▶ INICIAR MISSÃO DE FOCO",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        letterSpacing = 1.2.sp,
                        color = Color(0xFF0C0A09)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                RaidModeInfoBox(
                    mode = currentRaidMode,
                    dungeonSessions = dungeonSessionsProgress,
                    dungeonOnCooldown = false,
                    lootChancePercent = lootChancePercentFrom(
                        studiedMinutes = focusDuration,
                        isDungeon = isDungeonModePreview,
                        equippedTitleId = characterState.equippedTitle
                    ),
                    onShowDungeonHelp = { activeHelpMode = RaidMode.MASMORRA },
                    onShowWildernessHelp = { activeHelpMode = RaidMode.SELVAGEM },
                    onShowStandardHelp = { activeHelpMode = RaidMode.PADRAO }
                )

                Spacer(modifier = Modifier.height(8.dp))

                QuickActionsBar(
                    isDungeonMode = isDungeonModePreview,
                    isWildernessMode = isWildernessPreview,
                    isRunning = false,
                    onOpenModeModal = { isIncursionModalOpen = true },
                    activeAmbientIcon = AMBIENT_SOUNDS.find { it.id == ambientController.selectedTrack }?.icone,
                    onOpenAmbientModal = { isAmbientModalOpen = true },
                    isSettingsEnabled = true,
                    onOpenSettingsModal = { isTimerSettingsOpen = true },
                    onEnterFullscreen = { /* fonte: sem sessão ativa, tela cheia não faz sentido */ }
                )
            }

            IncursionModeModal(
                isOpen = isIncursionModalOpen,
                onClose = { isIncursionModalOpen = false },
                currentMode = currentRaidMode,
                dungeonCooldownRemainingMs = 0L,
                onSelectMode = { newMode ->
                    val (dungeon, wilderness) = newMode.toLegacyFlags()
                    isDungeonModePreview = dungeon
                    isWildernessPreview = wilderness
                }
            )

            SkillSelectorModal(
                isOpen = isSkillSelectorOpen,
                onClose = { isSkillSelectorOpen = false },
                skills = characterState.skills,
                selectedSkillIdx = validSkillIdx,
                onSelectSkill = { selectedSkillIdx = it }
            )

            TimerSettingsModal(
                isOpen = isTimerSettingsOpen,
                onClose = { isTimerSettingsOpen = false },
                pomodoroSettings = characterState.pomodoroSettings,
                isRunning = focusState.isRunning,
                isBreakActive = breakTimerState.isBreakActive,
                onSavePresetDuration = { viewModel.changeFocusDuration(it) },
                onSaveCustomSettings = { f, s, l -> viewModel.saveCustomTimerSettings(f, s, l) },
                onToggleAutoStartBreak = { viewModel.toggleAutoStartBreak() },
                onToggleAutoStartFocus = { viewModel.toggleAutoStartFocus() }
            )
        }

        val helpBlocks = when (activeHelpMode) {
            RaidMode.PADRAO -> buildStandardLootHelpBlocks(
                studiedMinutes = focusDuration,
                equippedTitleId = characterState.equippedTitle,
                titleLookup = { id ->
                    com.iurispraecepta.herolog.data.TITLE_CATALOG.find { it.id == id }?.let {
                        TitleDisplay(it.emoji, it.name)
                    }
                }
            )
            RaidMode.MASMORRA -> RaidModeHelpContent.MASMORRA
            RaidMode.SELVAGEM -> RaidModeHelpContent.SELVAGEM
            null -> emptyList()
        }
        val helpVariant = when (activeHelpMode) {
            RaidMode.PADRAO -> ModalVariant.Amber
            RaidMode.MASMORRA -> ModalVariant.Purple
            RaidMode.SELVAGEM -> ModalVariant.Red
            null -> ModalVariant.Amber
        }
        val helpTitle = when (activeHelpMode) {
            RaidMode.PADRAO -> "Modo Padrão & Saques"
            RaidMode.MASMORRA -> "Incursão por Masmorra"
            RaidMode.SELVAGEM -> "Terra Selvagem"
            null -> ""
        }

        ModeDescriptionModal(
            isOpen = activeHelpMode != null,
            onClose = { activeHelpMode = null },
            title = helpTitle,
            variant = helpVariant,
            blocks = helpBlocks
        )

        AmbientSoundModal(
            isOpen = isAmbientModalOpen,
            onClose = { isAmbientModalOpen = false },
            selectedTrack = ambientController.selectedTrack,
            volume = ambientController.volume,
            onSelectTrack = { ambientController.selectTrack(it) },
            onSetVolume = { ambientController.setVolume(it) },
            tracks = AMBIENT_SOUNDS
        )
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}