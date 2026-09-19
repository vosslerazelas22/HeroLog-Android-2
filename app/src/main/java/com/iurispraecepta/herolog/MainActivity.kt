package com.iurispraecepta.herolog

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
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
import com.iurispraecepta.herolog.ui.daily.DailyReportModal
import com.iurispraecepta.herolog.ui.components.HeroLogModal
import com.iurispraecepta.herolog.ui.components.LocalHazeState
import com.iurispraecepta.herolog.ui.components.ModalVariant
import com.iurispraecepta.herolog.ui.focus.AMBIENT_SOUNDS
import com.iurispraecepta.herolog.ui.focus.AmbientSoundModal
import com.iurispraecepta.herolog.ui.sfx.SfxManager
import com.iurispraecepta.herolog.ui.sfx.LocalSfxManager
import com.iurispraecepta.herolog.ui.sfx.rememberSfxManager
import com.iurispraecepta.herolog.ui.focus.BreakEndButton
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
import com.iurispraecepta.herolog.ui.navigation.LocalBottomBarInset
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
import com.iurispraecepta.herolog.ui.components.AchievementAnnouncementOverlay
import com.iurispraecepta.herolog.logic.SaveImportOutcome
import com.iurispraecepta.herolog.data.JsonConfig
import com.iurispraecepta.herolog.data.export.GameStateExporter
import com.iurispraecepta.herolog.data.export.GameStateImporter
import com.iurispraecepta.herolog.data.export.SaveClipboard
import com.iurispraecepta.herolog.ui.HeroLogViewModel
import com.iurispraecepta.herolog.ui.ProcessedQuest
import com.iurispraecepta.herolog.ui.theme.Amber400
import com.iurispraecepta.herolog.ui.theme.Amber500
import com.iurispraecepta.herolog.ui.theme.Champagne400
import com.iurispraecepta.herolog.ui.theme.Champagne500
import com.iurispraecepta.herolog.ui.theme.Cinzel
import com.iurispraecepta.herolog.ui.theme.Inter
import com.iurispraecepta.herolog.ui.theme.HeroLogTheme
import com.iurispraecepta.herolog.ui.theme.QuestPanel
import com.iurispraecepta.herolog.ui.theme.JetBrainsMono
import com.iurispraecepta.herolog.ui.theme.Stone900
import com.iurispraecepta.herolog.ui.theme.Stone950
import com.iurispraecepta.herolog.model.OrbConcept
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.iurispraecepta.herolog.logic.toSummary
import com.iurispraecepta.herolog.ui.HeroLogViewModelFactory
import com.iurispraecepta.herolog.ui.focus.FocusCompletionFlow
import com.iurispraecepta.herolog.logic.quests.QuestLogic
import com.iurispraecepta.herolog.logic.focus.FocusSessionConfig
import com.iurispraecepta.herolog.model.CharacterState
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.wrapContentSize
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        setContent {
            HeroLogTheme {
                // Estado de navegação em string, fiel ao `activeTab` da fonte React (`useState<string>('focus')`).
                var activeTab by remember { mutableStateOf("focus") }
                var isFocusMode by remember { mutableStateOf(false) }
                var isCreateModalOpen by remember { mutableStateOf(false) }
                // Prestige info popup da aba Skills (fonte: App.tsx isPrestigeInfoOpen).
                // Hoisted no escopo raiz como no React (persiste ao trocar de aba).
                var isPrestigeInfoOpen by remember { mutableStateOf(false) }
                var isRestoreSaveOpen by remember { mutableStateOf(false) }
                var isGeneralSettingsOpen by remember { mutableStateOf(false) }
                var saveImportOutcome by remember { mutableStateOf<SaveImportOutcome?>(null) }

                val application = LocalContext.current.applicationContext as HeroLogApplication
                val context = LocalContext.current

                // SFX de curta duração (level-up, coins, death, etc.)
                val sfxManager = rememberSfxManager()

                val heroLogViewModel: HeroLogViewModel = viewModel(factory = HeroLogViewModelFactory(application, sfxManager))
                val characterState by heroLogViewModel.characterState.collectAsState()
                var inspectingItem by remember { mutableStateOf<InventoryItem?>(null) }
                var isSfxMuted by remember { mutableStateOf(false) }
                val coroutineScope = rememberCoroutineScope()

                // File picker para import por arquivo .json
                val importFileLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri ->
                    if (uri != null) {
                        val rawJson = GameStateImporter.readFromUri(context, uri)
                        if (rawJson.isNotBlank()) {
                            saveImportOutcome = heroLogViewModel.importSaveFromJson(rawJson)
                        }
                    }
                }

                // HazeState para backdrop-blur no BottomNav (estilo React backdrop-blur-md)
                val hazeState = remember { HazeState() }

                // Controller de som ambiente no escopo raiz do HeroLogTheme (acima do Scaffold)
                // para sobreviver a trocas de aba (recomposição do content do Scaffold).
                // O DisposableEffect(Unit) em rememberAmbientSoundController só libera ao
                // desmontar o composable raiz, não ao trocar de aba.
                val ambientController = rememberAmbientSoundController()

                LaunchedEffect(isSfxMuted) {
                    sfxManager.isMuted = isSfxMuted
                }

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

                // spec-002: Full-screen focus replaces the entire app layout,
                // mirroring React (App.tsx:2055) — when isFocusMode, return
                // <FocusModeScreen> at the top level, no header, no bottom nav.
                val csForFullscreen = characterState
                if (isFocusMode && csForFullscreen != null) {
                    val focusState = heroLogViewModel.focusSessionState.value
                    val breakTimerState = heroLogViewModel.breakTimerState.value
                    val selectedSkillIdx = focusState.config?.selectedSkillIdx ?: 0
                    val validSkillIdx = selectedSkillIdx.coerceIn(0, (csForFullscreen.skills.size - 1).coerceAtLeast(0))
                    val selectedSkillForSession = csForFullscreen.skills.getOrNull(validSkillIdx)
                    val skillName = selectedSkillForSession?.name ?: "Habilidade"
                    val skillEmoji = selectedSkillForSession?.emoji ?: "💻"

                    if (breakTimerState.isBreakActive) {
                        FocusModeScreen(
                            skillName = skillName,
                            skillEmoji = skillEmoji,
                            isDungeonMode = breakTimerState.wasLastSessionDungeonMode,
                            dungeonSessions = breakTimerState.lastSessionDungeonSessions,
                            isWildernessChecked = breakTimerState.wasLastSessionWildernessMode,
                            timeLeft = breakTimerState.secondsLeft,
                            totalSeconds = breakTimerState.selectedBreakMins * 60,
                            isRunning = false,
                            isPaused = false,
                            onTogglePause = {},
                            onExit = { isFocusMode = false },
                            isGraceActive = false,
                            graceSecondsLeft = 3,
                            isPlayerDead = false,
                            onReturnToFocusCap = {},
                            onRespawn = {},
                            orbConcept = csForFullscreen.orbConcept ?: OrbConcept.D,
                            isBreakActive = true,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        val config = focusState.config
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
                            onTogglePause = { heroLogViewModel.togglePauseQuest() },
                            onExit = { isFocusMode = false },
                            isGraceActive = focusState.isGraceActive,
                            graceSecondsLeft = focusState.graceSecondsLeft,
                            isPlayerDead = csForFullscreen.isPlayerDead,
                            onReturnToFocusCap = { heroLogViewModel.returnToFocusFromGrace() },
                            onRespawn = { heroLogViewModel.respawnHero() },
                            orbConcept = csForFullscreen.orbConcept ?: OrbConcept.D,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {

                // spec-002: hazeSource no Scaffold inteiro para que o
                // AppHeader (topBar) também faça parte da fonte do blur.
                // Padrão canônico do Haze; nós de hazeEffect se auto-excluem.
                Scaffold(
                    modifier = Modifier.fillMaxSize().hazeSource(hazeState),
                    containerColor = QuestPanel,
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
                ) { innerPadding ->
                    CompositionLocalProvider(
                        LocalBottomBarInset provides innerPadding.calculateBottomPadding(),
                        LocalSfxManager provides sfxManager,
                        // spec-002: HazeState da raiz alimenta o blur de backdrop dos
                        // dialogs (HeroLogModal) e o BottomNav.
                        LocalHazeState provides hazeState
                    ) {
                    Box(modifier = Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding()).consumeWindowInsets(innerPadding)) {
                        // Background celestial particles reflection effect
                        // Equivalente ao radial-gradient purple-950/20 do React (App.tsx:2075)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0x332E1065), // purple-950 #2E1065 a 20% opacity
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        when (activeTab) {
                            "skills" -> {
                                val state = characterState
                                if (state == null) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("Carregando habilidades...", color = Amber400)
                                    }
                                } else {
                                    // Header "HABILIDADES" + tooltip Prestígio + botão "NOVA".
                                    // Fonte: React App.tsx:3260-3322 (container + header row vivem
                                    // no App, não dentro do SkillsScreen).
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(vertical = 20.dp),
                                        verticalArrangement = Arrangement.spacedBy(24.dp)
                                    ) {
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 16.dp)
                                                    .padding(bottom = 10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        painter = painterResource(R.drawable.lucide_ic_book_open),
                                                        contentDescription = null,
                                                        tint = Champagne400,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Text(
                                                        text = "HABILIDADES",
                                                        fontFamily = Cinzel,
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 14.sp,
                                                        color = Champagne400,
                                                        letterSpacing = 1.sp
                                                    )
                                                    // Botão "?" — mesmo padrão do tooltip do
                                                    // FocusHeaderBanner (Popup overlay, sem afetar layout).
                                                    Box(
                                                        modifier = Modifier
                                                            .size(18.dp)
                                                            .clip(CircleShape)
                                                            .border(1.dp, Champagne500.copy(alpha = 0.3f), CircleShape)
                                                            .clickable { isPrestigeInfoOpen = !isPrestigeInfoOpen },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = "?",
                                                            fontSize = 10.sp,
                                                            lineHeight = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Champagne400.copy(alpha = 0.8f)
                                                        )
                                                    }
                                                }

                                                // Botão "NOVA" — fonte usa PlusCircle + texto champagne.
                                                Row(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(Champagne500.copy(alpha = 0.05f))
                                                        .border(1.dp, Champagne500.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                                        .clickable { isCreateModalOpen = true }
                                                        .padding(horizontal = 12.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Icon(
                                                        painter = painterResource(R.drawable.lucide_ic_circle_plus),
                                                        contentDescription = "Nova Habilidade",
                                                        tint = Champagne400,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Text(
                                                        text = "NOVA",
                                                        fontFamily = Inter,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp,
                                                        color = Champagne400
                                                    )
                                                }
                                            }
                                            // border-b border-amber-500/10 da fonte.
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(1.dp)
                                                    .background(Amber500.copy(alpha = 0.1f))
                                            )
                                        }

                                        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
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

                                    // Popup "MECÂNICA DE PRESTÍGIO" — copia literal da fonte
                                    // (App.tsx:3287-3307, div absolute right-4 top-12).
                                    if (isPrestigeInfoOpen) {
                                        Popup(
                                            alignment = Alignment.TopEnd,
                                            properties = PopupProperties(focusable = false),
                                            onDismissRequest = { isPrestigeInfoOpen = false }
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .padding(top = 48.dp, end = 16.dp)
                                                    .widthIn(max = 280.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xF20C0A09))
                                                    .border(1.dp, Champagne500.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                                    .padding(14.dp)
                                            ) {
                                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = "\uD83D\uDC51 MECÂNICA DE PRESTÍGIO",
                                                            fontFamily = Cinzel,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 11.sp,
                                                            color = Champagne400,
                                                            letterSpacing = 1.sp
                                                        )
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(4.dp))
                                                                .clickable { isPrestigeInfoOpen = false }
                                                                .padding(2.dp)
                                                        ) {
                                                            Text(
                                                                text = "×",
                                                                fontSize = 12.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color(0x66A8A29E)
                                                            )
                                                        }
                                                    }
                                                    Text(
                                                        text = "Habilidades evoluem à medida que você ganha XP. Cada foco concluído com sucesso alimenta a habilidade selecionada no cronômetro.",
                                                        fontSize = 11.sp,
                                                        color = Color(0xCCD6D3D1),
                                                        lineHeight = 16.sp
                                                    )
                                                    Text(
                                                        text = "Ao alcançar o Nível 99, você poderá ativar o Prestígio. Isso reiniciará o progresso de nível dessa habilidade de volta para 1, mas em troca você ganhará um multiplicador permanente e heróico de +25% de XP extra permanente acumulável para acelerar toda a sua evolução futura nessa habilidade!",
                                                        fontSize = 11.sp,
                                                        color = Color(0xCCD6D3D1),
                                                        lineHeight = 16.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            "character" -> {
                                val state = characterState
                                if (state == null) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("Carregando personagem...", color = Amber400)
                                    }
                                } else {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color(0xFF1C1917).copy(alpha = 0.5f))
                                            .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(8.dp))
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        // Header "FICHA DO HERÓI"
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 10.dp)
                                                .border(1.dp, Color(0x1AFFFFFF)),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.lucide_ic_shield),
                                                contentDescription = null,
                                                tint = Champagne500,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "FICHA DO HERÓI",
                                                fontFamily = Cinzel,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 12.sp,
                                                color = Champagne400,
                                                letterSpacing = 1.sp,
                                                modifier = Modifier.padding(start = 6.dp)
                                            )
                                        }

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
                            }
                            "inventory" -> {
                                val state = characterState
                                if (state == null) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("Carregando inventario...", color = Amber400)
                                    }
                                } else {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color(0xFF1C1917).copy(alpha = 0.5f))
                                            .border(1.dp, Color(0x1AF59E0B).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        // Header "EQUIPAMENTOS"
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 10.dp)
                                                .border(1.dp, Color(0x1AF59E0B).copy(alpha = 0.1f)),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.lucide_ic_coins),
                                                contentDescription = null,
                                                tint = Champagne500,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "EQUIPAMENTOS",
                                                fontFamily = Cinzel,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 12.sp,
                                                color = Champagne400,
                                                letterSpacing = 1.sp,
                                                modifier = Modifier.padding(start = 6.dp)
                                            )
                                        }

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
                                     ambientController = ambientController,
                                     onNavigateToTab = { tab -> activeTab = tab },
                                     onEnterFullscreen = { isFocusMode = true }
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
                        coroutineScope = coroutineScope,
                        onExportToFile = {
                            coroutineScope.launch {
                                val payload = heroLogViewModel.buildExportPayload() ?: return@launch
                                val uri = GameStateExporter.writeToCache(context, payload, JsonConfig.pretty)
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/json"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Compartilhar save"))
                            }
                        },
                        onCopyToClipboard = {
                            coroutineScope.launch {
                                val jsonText = heroLogViewModel.exportSaveAsJsonString() ?: return@launch
                                SaveClipboard.copyToClipboard(context, jsonText)
                                Toast.makeText(context, "Código rúnico copiado para a área de transferência", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onImportFromFile = {
                            isGeneralSettingsOpen = false
                            importFileLauncher.launch("application/json")
                        },
                        onOpenImportDialog = {
                            isGeneralSettingsOpen = false
                            isRestoreSaveOpen = true
                        }
                    )

                    // Porte de `activeLevelUp` (useLevelUp.ts) -- popup global, visivel por cima de qualquer
                    // aba, igual a fonte (renderizado no nivel do App.tsx, nao preso a nenhuma tela especifica).
                    val levelUpQueue by heroLogViewModel.levelUpQueue.collectAsState()
                    LevelUpOverlay(
                        event = levelUpQueue.firstOrNull(),
                        onDismiss = { heroLogViewModel.dismissCurrentLevelUp() }
                    )

                    // Relatório Diário: modal fullscreen ao detectar novo dia.
                    // Fonte: React App.tsx:4319-4460 (dailyReport popup).
                    val dailyReport by heroLogViewModel.dailyReport.collectAsState()
                    dailyReport?.let { report ->
                        DailyReportModal(
                            data = report,
                            onDismiss = { heroLogViewModel.dismissDailyReport() }
                        )
                    }
                    } // CompositionLocalProvider
                } // Scaffold
                } // else (not focusMode)
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
    onNavigateToTab: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    onEnterFullscreen: () -> Unit = {}
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
    var isAmbientModalOpen by remember { mutableStateOf(false) }
    var showFocusTooltip by remember { mutableStateOf(false) }
    var isQuestFabOpen by remember { mutableStateOf(false) }

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
                val newAchievements by viewModel.pendingFocusAchievements.collectAsState()

                FocusCompletionFlow(
                    rewardsCalculation = rewards,
                    pauseCount = focusState.pauseCount,
                    streak = streak,
                    shouldShowStreakCelebration = shouldShowStreakCelebration,
                    skillTags = skillTags,
                    newAchievements = newAchievements,
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
        } else if (breakTimerState.isBreakPrep) {
            // Break prep inline — React (App.tsx:2413-2475) mantém banner + carousel +
            // quick actions visíveis; só o viewport do timer vira o card de escolha de pausa.
            Column(modifier = Modifier.fillMaxSize()) {
                FocusHeaderBanner(
                    showTooltip = showFocusTooltip,
                    onToggleTooltip = { showFocusTooltip = !showFocusTooltip },
                    onCloseTooltip = { showFocusTooltip = false },
                    onOpenQuestFab = { isQuestFabOpen = true }
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SkillInlineCarousel(
                        skills = characterState.skills,
                        selectedIndex = validSkillIdx,
                        onSelectIndex = { selectedSkillIdx = it },
                        disabled = false,
                        onOpenSkillsManager = { isSkillSelectorOpen = true }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    BreakPrepScreen(
                        shortBreakMinutes = characterState.pomodoroSettings.shortBreakDuration,
                        longBreakMinutes = characterState.pomodoroSettings.longBreakDuration,
                        selectedBreakMinutes = breakTimerState.selectedBreakMins,
                        isDungeonMode = breakTimerState.wasLastSessionDungeonMode,
                        onSelectDuration = { viewModel.selectBreakDuration(it) },
                        onStartBreak = {
                            viewModel.startBreakTimer(
                                minutes = breakTimerState.selectedBreakMins,
                                wasDungeonMode = breakTimerState.wasLastSessionDungeonMode,
                                wasWildernessMode = breakTimerState.wasLastSessionWildernessMode,
                                lastDungeonSessions = breakTimerState.lastSessionDungeonSessions
                            )
                        },
                        onSkipBreak = { viewModel.skipBreak() }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    QuickActionsBar(
                        isDungeonMode = breakTimerState.wasLastSessionDungeonMode,
                        isWildernessMode = breakTimerState.wasLastSessionWildernessMode,
                        isRunning = false,
                        onOpenModeModal = { isIncursionModalOpen = true },
                        activeAmbientIcon = AMBIENT_SOUNDS.find { it.id == ambientController.selectedTrack }?.icone,
                        onOpenAmbientModal = { isAmbientModalOpen = true },
                        isSettingsEnabled = true,
                        onOpenSettingsModal = { isTimerSettingsOpen = true },
                        onEnterFullscreen = { }
                    )
                }
            }
        } else if (breakTimerState.isBreakActive) {
            // Rest running inline — banner + carousel + orb esmeralda + "Encerrar Pausa" +
            // status bar + quick actions permanecem visíveis (paridade com App.tsx:2476-2494,
            // e TRANSIT CONTROL em App.tsx:2751-2757).
            Column(modifier = Modifier.fillMaxSize()) {
                FocusHeaderBanner(
                    showTooltip = showFocusTooltip,
                    onToggleTooltip = { showFocusTooltip = !showFocusTooltip },
                    onCloseTooltip = { showFocusTooltip = false },
                    onOpenQuestFab = { isQuestFabOpen = true }
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SkillInlineCarousel(
                        skills = characterState.skills,
                        selectedIndex = validSkillIdx,
                        onSelectIndex = { selectedSkillIdx = it },
                        disabled = false,
                        onOpenSkillsManager = { isSkillSelectorOpen = true }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    FocusOrb(
                        timeLeft = breakTimerState.secondsLeft,
                        totalSeconds = breakTimerState.selectedBreakMins * 60,
                        isRunning = false,
                        isPaused = false,
                        isBreakActive = true,
                        isDungeonMode = breakTimerState.wasLastSessionDungeonMode,
                        isWildernessMode = breakTimerState.wasLastSessionWildernessMode,
                        orbConcept = orbConcept,
                        size = FocusOrbSize.STANDARD
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    BreakEndButton(onClick = { viewModel.skipBreak() })
                    Spacer(modifier = Modifier.height(12.dp))
                    RaidModeInfoBox(
                        mode = raidModeFrom(breakTimerState.wasLastSessionDungeonMode, breakTimerState.wasLastSessionWildernessMode),
                        dungeonSessions = breakTimerState.lastSessionDungeonSessions,
                        dungeonOnCooldown = false,
                        lootChancePercent = lootChancePercentFrom(
                            studiedMinutes = focusDuration,
                            isDungeon = breakTimerState.wasLastSessionDungeonMode,
                            equippedTitleId = characterState.equippedTitle
                        ),
                        onShowDungeonHelp = { activeHelpMode = RaidMode.MASMORRA },
                        onShowWildernessHelp = { activeHelpMode = RaidMode.SELVAGEM },
                        onShowStandardHelp = { activeHelpMode = RaidMode.PADRAO }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    QuickActionsBar(
                        isDungeonMode = breakTimerState.wasLastSessionDungeonMode,
                        isWildernessMode = breakTimerState.wasLastSessionWildernessMode,
                        isRunning = false,
                        onOpenModeModal = { isIncursionModalOpen = true },
                        activeAmbientIcon = AMBIENT_SOUNDS.find { it.id == ambientController.selectedTrack }?.icone,
                        onOpenAmbientModal = { isAmbientModalOpen = true },
                        isSettingsEnabled = false,
                        onOpenSettingsModal = {},
                        onEnterFullscreen = { onEnterFullscreen() }
                    )
                }
            }
        } else if (focusState.isRunning) {
            // Sessão ativa, mas NÃO em tela cheia — orb inline + Pausar/Abandonar, igual a
            // fonte real (App.tsx renderiza o orb direto na aba Foco enquanto roda; "Tela Cheia"
            // é ação separada, não automática).
            val config = focusState.config
            val currentRaidModeRunning = raidModeFrom(config?.isDungeonMode ?: false, config?.isWildernessChecked ?: false)

            Column(modifier = Modifier.fillMaxSize()) {
                FocusHeaderBanner(
                    showTooltip = showFocusTooltip,
                    onToggleTooltip = { showFocusTooltip = !showFocusTooltip },
                    onCloseTooltip = { showFocusTooltip = false },
                    onOpenQuestFab = { isQuestFabOpen = true }
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
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
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
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
                            if (focusState.isPaused) "Retomar Missão" else "Pausar Missão",
                            fontFamily = Cinzel,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp,
                            fontSize = 14.sp
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
                        ),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.lucide_ic_x),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isConfirmingAbandon) "Confirmar?" else "Abandonar", fontWeight = if (isConfirmingAbandon) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp)
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
                    onEnterFullscreen = { onEnterFullscreen() }
                )

                Spacer(modifier = Modifier.height(LocalBottomBarInset.current))
                }
            }
        } else {
            val currentRaidMode = raidModeFrom(isDungeonModePreview, isWildernessPreview)

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                FocusHeaderBanner(
                    showTooltip = showFocusTooltip,
                    onToggleTooltip = { showFocusTooltip = !showFocusTooltip },
                    onCloseTooltip = { showFocusTooltip = false },
                    onOpenQuestFab = { isQuestFabOpen = true }
                )

                // Content (existing focus tab UI)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SkillInlineCarousel(
                        skills = characterState.skills,
                        selectedIndex = validSkillIdx,
                        onSelectIndex = { selectedSkillIdx = it },
                        disabled = focusState.isRunning,
                        onOpenSkillsManager = { isSkillSelectorOpen = true }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

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
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFB48C26), Color(0xFFE5C158), Color(0xFFF5DFA0))
                                )
                            )
                            .border(1.dp, Color(0xFFE5C158), RoundedCornerShape(4.dp))
                            .clickable {
                                val config = FocusSessionConfig(
                                    selectedSkillIdx = validSkillIdx,
                                    isWildernessChecked = isWildernessPreview,
                                    isDungeonMode = isDungeonModePreview,
                                    dungeonSessions = dungeonSessionsProgress
                                )
                                viewModel.startSession(config, durationMinutes = focusDuration)
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "▶ INICIAR MISSÃO DE FOCO",
                            fontFamily = Cinzel,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
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
                        onEnterFullscreen = { onEnterFullscreen() }
                    )

                    Spacer(modifier = Modifier.height(LocalBottomBarInset.current))
                }
            }

            // QuestFab Modal — contratos ativos (port de QuestFab.tsx)
            if (characterState != null) {
                val dailyQuests: List<ProcessedQuest> = viewModel.dailyQuests(characterState!!)

                HeroLogModal(
                    isOpen = isQuestFabOpen,
                    onClose = { isQuestFabOpen = false },
                    title = "📜 Contratos Ativos",
                    variant = ModalVariant.Amber
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Contratos Diários
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "🎯 Contratos Diários",
                                fontFamily = Cinzel,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Champagne400,
                                letterSpacing = 0.5.sp
                            )
                            dailyQuests.forEachIndexed { idx, q ->
                                Column(
                                    modifier = Modifier
                                        .then(
                                            if (idx < dailyQuests.lastIndex) Modifier.border(
                                                1.dp, Color(0x0DFFFFFF)
                                            ) else Modifier
                                        )
                                        .padding(vertical = 6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (q.isClaimed) Color(0xFF6B7280)
                                                        else if (q.isCompleted) Champagne400
                                                        else Color(0x4DF59E0B)
                                                    )
                                            )
                                            Text(
                                                text = q.name,
                                                fontSize = 12.sp,
                                                fontFamily = Cinzel,
                                                color = if (q.isClaimed) Color(0x40E5C158)
                                                else if (q.isCompleted) Champagne400
                                                else Color(0xCCD6D3D1),
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                        Text(
                                            text = "${q.progress}/${q.target}",
                                            fontSize = 11.sp,
                                            fontFamily = JetBrainsMono,
                                            color = if (q.isCompleted) Champagne400 else Color(0x99A8A29E),
                                            fontWeight = if (q.isCompleted) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                    Text(
                                        text = q.desc,
                                        fontSize = 9.sp,
                                        color = Color(0x66D6D3D1),
                                        fontFamily = Cinzel,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }

                        // Navigation button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, Champagne500.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .background(Color(0x1A1C1917))
                                .clickable {
                                    isQuestFabOpen = false
                                    onNavigateToTab("quests")
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Ir para Painel de Contratos →",
                                fontFamily = Cinzel,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Champagne400,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
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

            // ── Overlay de anúncio de conquistas fora do foco (FR-009/US-04) ──
            val achievementQueue by viewModel.achievementAnnouncementQueue.collectAsState()
            AchievementAnnouncementOverlay(
                queue = achievementQueue,
                onDismiss = { viewModel.dismissNextAchievementAnnouncement() }
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

// Banner "CÂMARA DE FOCO" + tooltip flutuante — extraído para reaproveitamento entre os
// estados da aba Foco (idle, sessão rodando inline, break prep, break active).
// Fonte: App.tsx:2338-2381 (banner estrutural, sempre visível quando activeTab === 'focus').
@Composable
private fun FocusHeaderBanner(
    showTooltip: Boolean,
    onToggleTooltip: () -> Unit,
    onCloseTooltip: () -> Unit,
    onOpenQuestFab: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        // Banner (without tooltip — tooltip floats above via sibling Box)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0x0DF59E0B), Color(0x0DA855F7))
                    )
                )
                .border(1.dp, Color(0x1AF59E0B))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            // Left: QuestFab button (absolute)
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(20.dp)
                    .clip(CircleShape)
                    .border(1.dp, Champagne500.copy(alpha = 0.3f), CircleShape)
                    .clickable { onOpenQuestFab() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.lucide_ic_scroll),
                    contentDescription = "Ver Contratos Ativos",
                    tint = Champagne400,
                    modifier = Modifier.size(11.dp)
                )
            }

            // Center: Timer icon + title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.lucide_ic_timer),
                    contentDescription = null,
                    tint = Champagne500,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "CÂMARA DE FOCO",
                    fontFamily = Cinzel,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    color = Champagne400,
                    letterSpacing = 1.sp
                )
            }

            // Right: Tooltip button (absolute)
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(18.dp)
                    .clip(CircleShape)
                    .border(1.dp, Champagne500.copy(alpha = 0.3f), CircleShape)
                    .clickable { onToggleTooltip() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "?",
                    fontSize = 10.sp,
                    lineHeight = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Champagne400.copy(alpha = 0.8f)
                )
            }
        }
    }

    // Tooltip popup — floats above banner via Popup overlay, does not affect layout
    if (showTooltip) {
        Popup(
            alignment = Alignment.TopEnd,
            properties = PopupProperties(focusable = false),
            onDismissRequest = { onCloseTooltip() }
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 48.dp, end = 16.dp)
                    .widthIn(max = 280.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xF20C0A09))
                    .border(1.dp, Champagne500.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Câmara de Foco (POMODORO)",
                            fontFamily = Cinzel,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Champagne400,
                            letterSpacing = 0.5.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onCloseTooltip() }
                                .padding(2.dp)
                        ) {
                            Text(
                                text = "×",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0x66A8A29E)
                            )
                        }
                    }
                    Text(
                        text = "O painel principal de controle. Escolha o tipo de missão, defina uma duração e clique em \"Iniciar Missão de Foco\". Você ganha XP a cada minuto que estuda.",
                        fontSize = 11.sp,
                        color = Color(0xCCD6D3D1),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}