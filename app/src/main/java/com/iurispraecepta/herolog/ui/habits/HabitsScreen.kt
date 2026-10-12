package com.iurispraecepta.herolog.ui.habits

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.R as LucideR
import com.iurispraecepta.herolog.model.Difficulty
import com.iurispraecepta.herolog.model.Habit
import com.iurispraecepta.herolog.ui.components.HeroLogModal
import com.iurispraecepta.herolog.ui.components.ModalVariant
import com.iurispraecepta.herolog.ui.components.difficultyColor
import com.iurispraecepta.herolog.ui.components.difficultyLabel
import com.iurispraecepta.herolog.ui.components.RitualEmptyState
import com.iurispraecepta.herolog.ui.components.RitualTabHeader
import com.iurispraecepta.herolog.ui.components.rememberTodayDateString
import com.iurispraecepta.herolog.ui.form.DeleteConfirmState
import com.iurispraecepta.herolog.ui.form.HabitDraft
import com.iurispraecepta.herolog.ui.form.QuestFormShell
import com.iurispraecepta.herolog.ui.form.isEqualTo
import com.iurispraecepta.herolog.ui.form.parseTags
import com.iurispraecepta.herolog.ui.theme.Amber100
import com.iurispraecepta.herolog.ui.theme.Amber400
import com.iurispraecepta.herolog.ui.theme.Amber500
import com.iurispraecepta.herolog.ui.theme.Inter
import com.iurispraecepta.herolog.ui.theme.JetBrainsMono
import com.iurispraecepta.herolog.ui.theme.Stone400
import com.iurispraecepta.herolog.ui.theme.Stone800
import com.iurispraecepta.herolog.ui.theme.Stone900

import com.iurispraecepta.herolog.ui.theme.Zinc300

private val Stone500 = Color(0xFF78716C)
private val Emerald500 = Color(0xFF10B981)
private val Emerald400 = Color(0xFF34D399)
private val Rose500 = Color(0xFFF43F5E)
private val Rose400 = Color(0xFFFB7185)

@Composable
fun HabitsScreen(
    habits: List<Habit>,
    onTriggerHabit: (String, Boolean) -> Unit,
    onAddHabit: (title: String, notes: String, up: Boolean, down: Boolean, difficulty: Difficulty, tags: List<String>) -> Unit,
    onEditHabit: (Habit) -> Unit,
    onDeleteHabit: (String) -> Unit,
    modifier: Modifier = Modifier,
    initialIsCreating: Boolean = false,
    initialEditingHabit: Habit? = null,
    initialDeleteConfirmState: DeleteConfirmState = DeleteConfirmState.None,
    initialShowDiscard: Boolean = false,
    initialSnapshot: HabitDraft? = null
) {
    var isCreating by remember { mutableStateOf(initialIsCreating) }
    var editingHabit by remember { mutableStateOf(initialEditingHabit) }

    var formTitle by remember { mutableStateOf(initialEditingHabit?.title ?: "") }
    var formNotes by remember { mutableStateOf(initialEditingHabit?.notes ?: "") }
    var formUp by remember { mutableStateOf(initialEditingHabit?.up ?: true) }
    var formDown by remember { mutableStateOf(initialEditingHabit?.down ?: false) }
    var formDifficulty by remember { mutableStateOf(initialEditingHabit?.difficulty ?: Difficulty.Easy) }
    var formTagInput by remember { mutableStateOf(initialEditingHabit?.tags?.joinToString(", ") ?: "") }

    var deleteConfirmState by remember { mutableStateOf(initialDeleteConfirmState) }
    var showDiscard by remember { mutableStateOf(initialShowDiscard) }

    // FR-005: Snapshot imutavel capturado ao abrir o formulario
    var initialDraft by remember { mutableStateOf<HabitDraft?>(initialSnapshot) }

    // FR-005: Comparacao estrutural do rascunho atual com o snapshot
    val isDirty by remember {
        derivedStateOf {
            val draft = initialDraft ?: return@derivedStateOf false
            val current = HabitDraft(
                title = formTitle,
                notes = formNotes,
                up = formUp,
                down = formDown,
                difficulty = formDifficulty,
                tags = formTagInput
            )
            !draft.isEqualTo(current)
        }
    }

    fun resetForm() {
        formTitle = ""
        formNotes = ""
        formUp = true
        formDown = false
        formDifficulty = Difficulty.Easy
        formTagInput = ""
        deleteConfirmState = DeleteConfirmState.None
        initialDraft = null
        showDiscard = false
        isCreating = false
        editingHabit = null
    }

    fun openCreateModal() {
        formTitle = ""
        formNotes = ""
        formUp = true
        formDown = false
        formDifficulty = Difficulty.Easy
        formTagInput = ""
        deleteConfirmState = DeleteConfirmState.None
        editingHabit = null
        isCreating = true
        showDiscard = false
        // FR-005: Captura snapshot do estado vazio
        initialDraft = HabitDraft(
            title = "",
            notes = "",
            up = true,
            down = false,
            difficulty = Difficulty.Easy,
            tags = ""
        )
    }

    fun openEditModal(habit: Habit) {
        formTitle = habit.title
        formNotes = habit.notes
        formUp = habit.up
        formDown = habit.down
        formDifficulty = habit.difficulty
        formTagInput = habit.tags.joinToString(", ")
        deleteConfirmState = DeleteConfirmState.None
        isCreating = false
        editingHabit = habit
        // FR-005: Captura snapshot do estado atual do habito
        initialDraft = HabitDraft(
            title = habit.title,
            notes = habit.notes,
            up = habit.up,
            down = habit.down,
            difficulty = habit.difficulty,
            tags = habit.tags.joinToString(", ")
        )
    }

    val isModalOpen = isCreating || editingHabit != null
    val todayDateStr = rememberTodayDateString(habits)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 12.dp)
    ) {
        RitualTabHeader(
            iconRes = LucideR.drawable.lucide_ic_zap,
            title = "Capela de Hábitos",
            counterText = if (habits.size == 1) "1 hábito" else "${habits.size} hábitos",
            showNewButton = !isCreating && editingHabit == null,
            newContentDescription = "Novo hábito",
            onNewClick = { openCreateModal() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Habit List or Empty State
        if (habits.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                RitualEmptyState(
                    message = "A heráldica de seus hábitos está em branco. Comece definindo comportamentos diários positivos ou rituais de quebra de vícios!"
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
                items(habits, key = { it.id }) { h ->
                    RitualHabitCard(
                        habit = h,
                        todayDateStr = todayDateStr,
                        onTriggerHabit = onTriggerHabit,
                        onOpenEdit = { openEditModal(h) }
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
            title = if (editingHabit != null) "Editar Tarefa" else "Nova Tarefa",
            variant = ModalVariant.Amber,
            allowBackdropClose = true,
            contentPadding = PaddingValues(0.dp)
        ) {
            QuestFormShell(
                isDirty = isDirty,
                showDiscard = showDiscard,
                isEditing = editingHabit != null,
                onDiscard = { resetForm() },
                onContinueEditing = { showDiscard = false },
                onRequestClose = requestClose,
                onConfirmDelete = {
                    val idToDelete = editingHabit?.id
                    if (idToDelete != null) {
                        onDeleteHabit(idToDelete)
                    }
                    resetForm()
                },
                onSubmit = {
                    val parsedTags = parseTags(formTagInput)

                    val currentEditing = editingHabit
                    if (currentEditing != null) {
                        onEditHabit(
                            currentEditing.copy(
                                title = formTitle.trim(),
                                notes = formNotes.trim(),
                                up = formUp,
                                down = formDown,
                                difficulty = formDifficulty,
                                tags = parsedTags
                            )
                        )
                    } else {
                        onAddHabit(
                            formTitle.trim(),
                            formNotes.trim(),
                            formUp,
                            formDown,
                            formDifficulty,
                            parsedTags
                        )
                    }
                    resetForm()
                },
                deleteConfirmState = deleteConfirmState,
                onDeleteConfirmChange = { deleteConfirmState = it },
                submitEnabled = formTitle.isNotBlank(),
                submitLabel = if (editingHabit != null) "Salvar" else "Criar"
            ) {
                // Título
                FormFieldLabel("Título")
                BasicTextField(
                    value = formTitle,
                    onValueChange = { formTitle = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Stone900, RoundedCornerShape(6.dp))
                        .border(1.dp, if (formTitle.isBlank()) Color(0x33F59E0B) else Amber500, RoundedCornerShape(6.dp))
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
                                    text = "Ex: Beber água purificada, Estudar grimório, Procrastinar...",
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
                                    text = "Ex: Cada gole limpa a mente, estudar por 20 minutos consecutivamente...",
                                    color = Stone500,
                                    fontSize = 12.sp
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                // Caminhos Permitidos em linha própria + Dificuldade em linha nova
                // (paridade React mobile: grid grid-cols-1; sm:grid-cols-2 só em viewport larga)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Caminhos Permitidos
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FormFieldLabel("Caminhos Permitidos")

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Toggle Positivo (+)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (formUp) Color(0x3310B981) else Color(0x4D1C1917))
                                    .border(
                                        width = 1.dp,
                                        color = if (formUp) Emerald500 else Color(0x3344403C),
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable { formUp = !formUp },
                                contentAlignment = Alignment.Center
                            ) {
                                // Melhoria consciente vs React "+ POSITIVO (+)": sem icone redundante
                                Text(
                                    text = "Positivo (+)".uppercase(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = Inter,
                                    color = if (formUp) Emerald400 else Stone500
                                )
                            }

                            // Toggle Negativo (-)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (formDown) Color(0x33F43F5E) else Color(0x4D1C1917))
                                    .border(
                                        width = 1.dp,
                                        color = if (formDown) Rose500 else Color(0x3344403C),
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable { formDown = !formDown },
                                contentAlignment = Alignment.Center
                            ) {
                                // Melhoria consciente vs React "− NEGATIVO (-)": sem icone redundante
                                Text(
                                    text = "Negativo (-)".uppercase(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = Inter,
                                    color = if (formDown) Rose400 else Stone500
                                )
                            }
                        }
                    }

                    // Dificuldade Dropdown — linha nova (paridade React mobile)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
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
                }

                // Tags / Categorias
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
                                    fontSize = 12.sp
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
