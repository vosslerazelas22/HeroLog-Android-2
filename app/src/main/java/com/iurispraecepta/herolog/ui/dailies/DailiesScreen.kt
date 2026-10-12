package com.iurispraecepta.herolog.ui.dailies

import com.iurispraecepta.herolog.ui.navigation.LocalBottomBarInset

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iurispraecepta.herolog.model.Daily
import com.composables.icons.lucide.R as LucideR
import com.iurispraecepta.herolog.model.Difficulty
import com.iurispraecepta.herolog.model.RepeatInterval
import com.iurispraecepta.herolog.ui.components.HeroLogModal
import com.iurispraecepta.herolog.ui.components.ModalVariant
import com.iurispraecepta.herolog.ui.components.difficultyColor
import com.iurispraecepta.herolog.ui.components.difficultyLabel
import com.iurispraecepta.herolog.ui.components.RitualEmptyState
import com.iurispraecepta.herolog.ui.components.RitualTabHeader
import com.iurispraecepta.herolog.ui.theme.Amber100
import com.iurispraecepta.herolog.ui.theme.Amber400
import com.iurispraecepta.herolog.ui.theme.Amber500
import com.iurispraecepta.herolog.ui.theme.Inter
import com.iurispraecepta.herolog.ui.theme.JetBrainsMono
import com.iurispraecepta.herolog.ui.theme.Stone400
import com.iurispraecepta.herolog.ui.theme.Stone800
import com.iurispraecepta.herolog.ui.theme.Stone900
import com.iurispraecepta.herolog.ui.theme.Zinc300
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.derivedStateOf
import com.iurispraecepta.herolog.ui.form.DeleteConfirmState
import com.iurispraecepta.herolog.ui.form.DailyDraft
import com.iurispraecepta.herolog.ui.form.QuestFormShell
import com.iurispraecepta.herolog.ui.form.isEqualTo
import com.iurispraecepta.herolog.ui.form.mergePendingChecklistInput
import com.iurispraecepta.herolog.ui.form.parseTags

private val Stone500 = Color(0xFF78716C)
private val Amber300 = Color(0xFFFCD34D)

fun repeatIntervalLabel(interval: RepeatInterval): String = when (interval) {
    RepeatInterval.Daily -> "Diário"
    RepeatInterval.Weekly -> "Semanal"
    RepeatInterval.Monthly -> "Mensal"
}

@Composable
fun DailiesScreen(
    dailies: List<Daily>,
    onToggleDaily: (String) -> Unit,
    onToggleChecklistItem: (String, String) -> Unit,
    onAddDaily: (
        title: String,
        notes: String,
        difficulty: Difficulty,
        streak: Int,
        repeats: RepeatInterval,
        every: Int,
        tags: List<String>,
        checklistTexts: List<String>
    ) -> Unit,
    onEditDaily: (Daily) -> Unit,
    onDeleteDaily: (String) -> Unit,
    modifier: Modifier = Modifier,
    initialIsCreating: Boolean = false,
    initialEditingDaily: Daily? = null,
    initialExpandedDailyId: String? = null,
    initialChecklistItems: List<String> = emptyList(),
    initialDeleteConfirmState: DeleteConfirmState = DeleteConfirmState.None,
    initialShowDiscard: Boolean = false,
    initialSnapshot: DailyDraft? = null
) {
    var isCreating by remember { mutableStateOf(initialIsCreating) }
    var editingDaily by remember { mutableStateOf(initialEditingDaily) }
    var expandedDailyId by remember { mutableStateOf<String?>(initialExpandedDailyId) }

    var formTitle by remember { mutableStateOf(initialEditingDaily?.title ?: "") }
    var formNotes by remember { mutableStateOf(initialEditingDaily?.notes ?: "") }
    var formDifficulty by remember { mutableStateOf(initialEditingDaily?.difficulty ?: Difficulty.Easy) }
    var formRepeats by remember { mutableStateOf(initialEditingDaily?.repeats ?: RepeatInterval.Daily) }
    var formEvery by remember { mutableStateOf(initialEditingDaily?.every?.toString() ?: "1") }
    var formStreak by remember { mutableStateOf(initialEditingDaily?.streak?.toString() ?: "0") }
    var formTagInput by remember { mutableStateOf(initialEditingDaily?.tags?.joinToString(", ") ?: "") }
    var checklistInput by remember { mutableStateOf("") }
    var checklistItems by remember { mutableStateOf(initialChecklistItems) }

    var deleteConfirmState by remember { mutableStateOf(initialDeleteConfirmState) }
    var showDiscard by remember { mutableStateOf(initialShowDiscard) }

    // FR-005: Snapshot imutavel capturado ao abrir o formulario
    var initialDraft by remember { mutableStateOf<DailyDraft?>(initialSnapshot) }

    // FR-005: Comparacao estrutural do rascunho atual com o snapshot
    val isDirty by remember {
        derivedStateOf {
            val draft = initialDraft ?: return@derivedStateOf false
            val current = DailyDraft(
                title = formTitle,
                notes = formNotes,
                difficulty = formDifficulty,
                repeats = formRepeats.name,
                every = formEvery,
                streak = formStreak,
                tags = formTagInput,
                checklistItems = checklistItems,
                checklistInput = checklistInput
            )
            !draft.isEqualTo(current)
        }
    }

    fun resetForm() {
        formTitle = ""
        formNotes = ""
        formDifficulty = Difficulty.Easy
        formRepeats = RepeatInterval.Daily
        formEvery = "1"
        formStreak = "0"
        formTagInput = ""
        checklistInput = ""
        checklistItems = emptyList()
        deleteConfirmState = DeleteConfirmState.None
        initialDraft = null
        showDiscard = false
        isCreating = false
        editingDaily = null
    }

    fun openCreateModal() {
        formTitle = ""
        formNotes = ""
        formDifficulty = Difficulty.Easy
        formRepeats = RepeatInterval.Daily
        formEvery = "1"
        formStreak = "0"
        formTagInput = ""
        checklistInput = ""
        checklistItems = emptyList()
        deleteConfirmState = DeleteConfirmState.None
        editingDaily = null
        isCreating = true
        showDiscard = false
        // FR-005: Captura snapshot do estado vazio
        initialDraft = DailyDraft(
            title = "",
            notes = "",
            difficulty = Difficulty.Easy,
            repeats = RepeatInterval.Daily.name,
            every = "1",
            streak = "0",
            tags = "",
            checklistItems = emptyList(),
            checklistInput = ""
        )
    }

    fun openEditModal(daily: Daily) {
        formTitle = daily.title
        formNotes = daily.notes
        formDifficulty = daily.difficulty
        formRepeats = daily.repeats
        formEvery = daily.every.toString()
        formStreak = daily.streak.toString()
        formTagInput = daily.tags.joinToString(", ")
        checklistInput = ""
        checklistItems = emptyList()
        deleteConfirmState = DeleteConfirmState.None
        isCreating = false
        editingDaily = daily
        showDiscard = false
        // FR-005: Captura snapshot do estado atual do daily
        initialDraft = DailyDraft(
            title = daily.title,
            notes = daily.notes,
            difficulty = daily.difficulty,
            repeats = daily.repeats.name,
            every = daily.every.toString(),
            streak = daily.streak.toString(),
            tags = daily.tags.joinToString(", "),
            checklistItems = emptyList(),
            checklistInput = ""
        )
    }

    fun addChecklistItem() {
        val trimmed = checklistInput.trim()
        if (trimmed.isNotEmpty()) {
            checklistItems = checklistItems + trimmed
            checklistInput = ""
        }
    }

    val isModalOpen = isCreating || editingDaily != null

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 12.dp)
    ) {
        RitualTabHeader(
            iconRes = LucideR.drawable.lucide_ic_calendar_days,
            title = "Tarefas Diárias",
            counterText = if (dailies.size == 1) "1 tarefa" else "${dailies.size} tarefas",
            showNewButton = !isCreating && editingDaily == null,
            newContentDescription = "Nova diária",
            onNewClick = { openCreateModal() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Dailies List or Empty State
        if (dailies.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                RitualEmptyState(
                    message = "Nenhum voto de prática diária estabelecido neste plano. Adicione tarefas para forjar hábitos duradouros todos os dias!"
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 0.dp,
                    bottom = LocalBottomBarInset.current
                )
            ) {
                items(dailies, key = { it.id }) { d ->
                    RitualDailyCard(
                        daily = d,
                        isExpanded = expandedDailyId == d.id,
                        onToggleExpand = {
                            expandedDailyId = if (expandedDailyId == d.id) null else d.id
                        },
                        onToggleDaily = { onToggleDaily(d.id) },
                        onToggleChecklistItem = { itemId -> onToggleChecklistItem(d.id, itemId) },
                        onOpenEdit = { openEditModal(d) }
                    )
                }
            }
        }
    }

    // Modal (HeroLogModal) — FR-001/FR-005/FR-007: shell compartilhado com dirty state
    if (isModalOpen) {
        // FR-007: Unica rota de solicitacao de fechamento
        val requestClose: () -> Unit = {
            if (isDirty) {
                showDiscard = true
            } else {
                resetForm()
            }
        }

        HeroLogModal(
            isOpen = isModalOpen,
            onClose = requestClose,
            title = if (editingDaily != null) "Editar Tarefa" else "Nova Tarefa",
            variant = ModalVariant.Amber,
            allowBackdropClose = true,
            contentPadding = PaddingValues(0.dp)
        ) {
            QuestFormShell(
                isDirty = isDirty,
                showDiscard = showDiscard,
                isEditing = editingDaily != null,
                onDiscard = { resetForm() },
                onContinueEditing = { showDiscard = false },
                onRequestClose = requestClose,
                onConfirmDelete = {
                    val idToDelete = editingDaily?.id
                    if (idToDelete != null) {
                        onDeleteDaily(idToDelete)
                    }
                    resetForm()
                },
                onSubmit = {
                    val parsedTags = parseTags(formTagInput)

                    val parsedEvery = (formEvery.toIntOrNull() ?: 1).coerceIn(1, 99)
                    val parsedStreak = (formStreak.toIntOrNull() ?: 0).coerceAtLeast(0)

                    // FR-008: Incluir texto pendente do checklist antes de submeter
                    val allChecklistItems = mergePendingChecklistInput(checklistItems, checklistInput)

                    val currentEditing = editingDaily
                    if (currentEditing != null) {
                        onEditDaily(
                            currentEditing.copy(
                                title = formTitle.trim(),
                                notes = formNotes.trim(),
                                difficulty = formDifficulty,
                                repeats = formRepeats,
                                every = parsedEvery,
                                streak = parsedStreak,
                                tags = parsedTags
                            )
                        )
                    } else {
                        onAddDaily(
                            formTitle.trim(),
                            formNotes.trim(),
                            formDifficulty,
                            parsedStreak,
                            formRepeats,
                            parsedEvery,
                            parsedTags,
                            allChecklistItems
                        )
                    }
                    resetForm()
                },
                deleteConfirmState = deleteConfirmState,
                onDeleteConfirmChange = { deleteConfirmState = it },
                submitEnabled = formTitle.isNotBlank(),
                submitLabel = if (editingDaily != null) "Salvar" else "Criar"
            ) {
                // Título
                FormFieldLabel("Título")
                BasicTextField(
                    value = formTitle,
                    onValueChange = { formTitle = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Stone900, RoundedCornerShape(6.dp))
                        .border(
                            1.dp,
                            if (formTitle.isBlank()) Color(0x33F59E0B) else Amber500,
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 12.sp,
                        fontFamily = Inter
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(Amber400),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    decorationBox = { innerTextField ->
                        Box {
                            if (formTitle.isEmpty()) {
                                Text(
                                    text = "Ex: Beber medicação, Fazer Duolingo",
                                    color = Stone500,
                                    fontSize = 12.sp
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                // Notas — FR-002: multiline, altura minima 96dp
                FormFieldLabel("Notas")
                BasicTextField(
                    value = formNotes,
                    onValueChange = { formNotes = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 96.dp)
                        .background(Stone900, RoundedCornerShape(6.dp))
                        .border(1.dp, Color(0x33F59E0B), RoundedCornerShape(6.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    textStyle = TextStyle(
                        color = Amber100,
                        fontSize = 12.sp,
                        fontFamily = Inter
                    ),
                    cursorBrush = SolidColor(Amber400),
                    decorationBox = { innerTextField ->
                        Box {
                            if (formNotes.isEmpty()) {
                                Text(
                                    text = "Ex: Ao acordar em jejum, abrir lição no celular...",
                                    color = Stone500,
                                    fontSize = 12.sp
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                // Linha dupla: Dificuldade + Regularidade
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Dificuldade Dropdown
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FormFieldLabel("Dificuldade")

                        var difficultyMenuExpanded by remember { mutableStateOf(false) }

                        Box(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Stone900)
                                    .border(1.dp, Color(0x33F59E0B), RoundedCornerShape(6.dp))
                                    .clickable { difficultyMenuExpanded = true }
                                    .padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = difficultyLabel(formDifficulty),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = difficultyColor(formDifficulty)
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Selecionar Dificuldade",
                                    tint = Amber400
                                )
                            }

                            DropdownMenu(
                                expanded = difficultyMenuExpanded,
                                onDismissRequest = { difficultyMenuExpanded = false },
                                modifier = Modifier.background(Stone900)
                            ) {
                                listOf(
                                    Difficulty.Trivial,
                                    Difficulty.Easy,
                                    Difficulty.Medium,
                                    Difficulty.Hard
                                ).forEach { diff ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = difficultyLabel(diff),
                                                color = difficultyColor(diff),
                                                fontWeight = if (formDifficulty == diff) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        onClick = {
                                            formDifficulty = diff
                                            difficultyMenuExpanded = false
                                        },
                                        colors = MenuDefaults.itemColors()
                                    )
                                }
                            }
                        }
                    }

                    // Regularidade (Dropdown tipo + input Every)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FormFieldLabel("Regularidade")

                        var repeatMenuExpanded by remember { mutableStateOf(false) }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Dropdown Diário/Semanal/Mensal
                            Box(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Stone900)
                                        .border(1.dp, Color(0x33F59E0B), RoundedCornerShape(6.dp))
                                        .clickable { repeatMenuExpanded = true }
                                        .padding(horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = repeatIntervalLabel(formRepeats),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Amber100,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Selecionar Intervalo",
                                        tint = Amber400
                                    )
                                }

                                DropdownMenu(
                                    expanded = repeatMenuExpanded,
                                    onDismissRequest = { repeatMenuExpanded = false },
                                    modifier = Modifier.background(Stone900)
                                ) {
                                    listOf(
                                        RepeatInterval.Daily,
                                        RepeatInterval.Weekly,
                                        RepeatInterval.Monthly
                                    ).forEach { interval ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = repeatIntervalLabel(interval),
                                                    color = Amber100,
                                                    fontWeight = if (formRepeats == interval) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            onClick = {
                                                formRepeats = interval
                                                repeatMenuExpanded = false
                                            },
                                            colors = MenuDefaults.itemColors()
                                        )
                                    }
                                }
                            }

                            // Input every
                            BasicTextField(
                                value = formEvery,
                                onValueChange = { input ->
                                    val filtered = input.filter { it.isDigit() }
                                    if (filtered.isEmpty()) {
                                        formEvery = ""
                                    } else {
                                        val num = filtered.toIntOrNull() ?: 1
                                        formEvery = num.coerceIn(1, 99).toString()
                                    }
                                },
                                modifier = Modifier
                                    .width(44.dp)
                                    .height(40.dp)
                                    .background(Stone900, RoundedCornerShape(6.dp))
                                    .border(1.dp, Color(0x33F59E0B), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 10.dp),
                                textStyle = TextStyle(
                                    color = Amber100,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    fontFamily = JetBrainsMono
                                ),
                                singleLine = true,
                                cursorBrush = SolidColor(Amber400),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                )
                            )
                        }
                    }
                }

                // Checklist — SOMENTE MODO CRIAÇÃO (editingDaily == null)
                // FR-003: fidelidade React (DailiesTab) — Checklist antes de Streak/Categorias
                if (editingDaily == null) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        FormFieldLabel("Checklist")

                        // Input + Botão Add
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BasicTextField(
                                value = checklistInput,
                                onValueChange = { checklistInput = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Stone900, RoundedCornerShape(6.dp))
                                    .border(1.dp, Color(0x33F59E0B), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                textStyle = TextStyle(
                                    color = Amber100,
                                    fontSize = 12.sp,
                                    fontFamily = Inter
                                ),
                                singleLine = true,
                                cursorBrush = SolidColor(Amber400),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { addChecklistItem() }),
                                decorationBox = { innerTextField ->
                                    Box {
                                        if (checklistInput.isEmpty()) {
                                            Text(
                                                text = "Ex: Lição completa, Revisar caderno...",
                                                color = Stone500,
                                                fontSize = 12.sp
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )

                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0x33F59E0B))
                                    .border(1.dp, Amber400, RoundedCornerShape(6.dp))
                                    .clickable { addChecklistItem() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Adicionar critério",
                                    tint = Amber300,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Lista de itens do checklist criados localmente
                        if (checklistItems.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                checklistItems.forEachIndexed { index, itemText ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0x331C1917))
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "• $itemText",
                                            color = Amber100,
                                            fontSize = 12.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clickable {
                                                    checklistItems = checklistItems.filterIndexed { i, _ -> i != index }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remover critério",
                                                tint = Color(0xFFF87171),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Linha dupla: Série Inicial (Streak) + Categorias
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Streak inicial
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FormFieldLabel("Série Inicial (Streak)")
                        BasicTextField(
                            value = formStreak,
                            onValueChange = { input ->
                                val filtered = input.filter { it.isDigit() }
                                formStreak = filtered
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Stone900, RoundedCornerShape(6.dp))
                                .border(1.dp, Color(0x33F59E0B), RoundedCornerShape(6.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            textStyle = TextStyle(
                                color = Amber100,
                                fontSize = 13.sp,
                                fontFamily = JetBrainsMono
                            ),
                            singleLine = true,
                            cursorBrush = SolidColor(Amber400),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (formStreak.isEmpty()) {
                                        Text(text = "0", color = Stone500, fontSize = 13.sp)
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }

                    // Tags
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FormFieldLabel("Categorias (Tags, separadas por vírgula)")
                        BasicTextField(
                            value = formTagInput,
                            onValueChange = { formTagInput = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Stone900, RoundedCornerShape(6.dp))
                                .border(1.dp, Color(0x33F59E0B), RoundedCornerShape(6.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            textStyle = TextStyle(
                                color = Amber100,
                                fontSize = 12.sp,
                                fontFamily = Inter
                            ),
                            singleLine = true,
                            cursorBrush = SolidColor(Amber400),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (formTagInput.isEmpty()) {
                                        Text(
                                            text = "study, workout, health...",
                                            color = Stone500,
                                            fontSize = 13.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FormFieldLabel(text: String) {
    Text(
        // Paridade React: labels usam classe CSS `uppercase`
        text = text.uppercase(),
        fontFamily = Inter,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = Zinc300,
        letterSpacing = 0.5.sp
    )
}
