package com.iurispraecepta.herolog.ui.habits

import com.iurispraecepta.herolog.ui.navigation.LocalBottomBarInset

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iurispraecepta.herolog.model.Difficulty
import com.iurispraecepta.herolog.model.Habit
import com.iurispraecepta.herolog.ui.components.DifficultyBadge
import com.iurispraecepta.herolog.ui.components.HeroLogModal
import com.iurispraecepta.herolog.ui.components.ModalVariant
import com.iurispraecepta.herolog.ui.components.difficultyColor
import com.iurispraecepta.herolog.ui.components.difficultyLabel
import com.iurispraecepta.herolog.ui.form.DeleteConfirmState
import com.iurispraecepta.herolog.ui.form.HabitDraft
import com.iurispraecepta.herolog.ui.form.QuestFormShell
import com.iurispraecepta.herolog.ui.form.isEqualTo
import com.iurispraecepta.herolog.ui.form.parseTags
import com.iurispraecepta.herolog.ui.theme.Amber100
import com.iurispraecepta.herolog.ui.theme.Amber400
import com.iurispraecepta.herolog.ui.theme.Amber500
import com.iurispraecepta.herolog.ui.theme.Cinzel
import com.iurispraecepta.herolog.ui.theme.Inter
import com.iurispraecepta.herolog.ui.theme.JetBrainsMono
import com.iurispraecepta.herolog.ui.theme.ScoreColor
import com.iurispraecepta.herolog.ui.theme.Stone400
import com.iurispraecepta.herolog.ui.theme.Stone800
import com.iurispraecepta.herolog.ui.theme.Stone900

import com.iurispraecepta.herolog.ui.theme.Zinc300
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Champagne400 = Color(0xFFE5C158)
private val Stone600 = Color(0xFF57534E)
private val Stone500 = Color(0xFF78716C)
private val Amber300 = Color(0xFFFCD34D)
private val Amber200 = Color(0xFFFDE68A)
private val Emerald500 = Color(0xFF10B981)
private val Emerald400 = Color(0xFF34D399)
private val Purple400 = Color(0xFFC084FC)
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
    val todayDateStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚡ Capela de Hábitos",
                fontFamily = Cinzel,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Champagne400,
                letterSpacing = 1.sp
            )

            if (!isCreating && editingHabit == null) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x0DD4AF37))
                        .border(1.dp, Color(0x4DD4AF37), RoundedCornerShape(6.dp))
                        .clickable { openCreateModal() }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "Novo Hábito",
                        tint = Champagne400,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Novo",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Champagne400
                    )
                }
            }
        }

        // Habit List or Empty State
        if (habits.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
                    .drawBehind {
                        drawRoundRect(
                            color = Color(0x1AF59E0B),
                            style = Stroke(
                                width = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                            ),
                            cornerRadius = CornerRadius(8.dp.toPx())
                        )
                    }
                    .background(Color(0x330C0A09), RoundedCornerShape(8.dp))
                    .padding(horizontal = 20.dp, vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "A heráldica de seus hábitos está em branco. Comece definindo comportamentos diários positivos ou rituais de quebra de vícios!",
                    style = TextStyle(
                        fontStyle = FontStyle.Italic,
                        fontFamily = Cinzel,
                        color = Amber100.copy(alpha = 0.4f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = LocalBottomBarInset.current)
            ) {
                items(habits, key = { it.id }) { h ->
                    HabitCardItem(
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

                // Linha dupla: Caminhos Permitidos + Dificuldade
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Caminhos Permitidos
                    Column(
                        modifier = Modifier.weight(1f),
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Positivo",
                                        tint = if (formUp) Emerald400 else Stone500,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Positivo (+)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = Inter,
                                        color = if (formUp) Emerald400 else Stone500
                                    )
                                }
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = "Negativo",
                                        tint = if (formDown) Rose400 else Stone500,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Negativo (-)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = Inter,
                                        color = if (formDown) Rose400 else Stone500
                                    )
                                }
                            }
                        }
                    }

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
        text = text,
        fontFamily = Inter,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = Zinc300,
        letterSpacing = 0.5.sp
    )
}

@Composable
private fun HabitCardItem(
    habit: Habit,
    todayDateStr: String,
    onTriggerHabit: (String, Boolean) -> Unit,
    onOpenEdit: () -> Unit
) {
    val score = habit.upCount - habit.downCount
    val tokens = ScoreColor.forScore(score)
    val isDoneToday = habit.lastTriggeredDate == todayDateStr

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(8.dp))
            .background(Brush.horizontalGradient(listOf(tokens.start, tokens.end)))
            .border(1.dp, tokens.border, RoundedCornerShape(8.dp))
    ) {
        // Zona Esquerda (Up)
        if (habit.up) {
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .fillMaxHeight()
                    .background(Color(0x3310B981))
                    .clickable { onTriggerHabit(habit.id, true) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Trigger Positivo",
                    tint = Emerald400,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .fillMaxHeight()
                    .background(Stone900)
            )
        }

        // Zona Central (Conteúdo / Clicável para editar)
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable { onOpenEdit() }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Linha Título + Badge de Dificuldade
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = habit.title,
                    fontFamily = Cinzel,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Amber100,
                    modifier = Modifier.weight(1f, fill = false),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                DifficultyBadge(difficulty = habit.difficulty)
            }

            // Notas se houver
            if (habit.notes.isNotBlank()) {
                Text(
                    text = habit.notes,
                    fontFamily = Inter,
                    color = Amber100.copy(alpha = 0.65f),
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Linha de Status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (isDoneToday) {
                    Text(
                        text = "✓ Feito hoje",
                        color = Amber400,
                        fontSize = 11.sp,
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Text(
                        text = "○ Pendente hoje",
                        color = Stone500,
                        fontSize = 11.sp,
                        fontFamily = JetBrainsMono
                    )
                }

                Text(
                    text = "•",
                    color = Amber500.copy(alpha = 0.2f),
                    fontSize = 11.sp,
                    fontFamily = JetBrainsMono
                )

                Text(
                    text = "(+${habit.upCount} | -${habit.downCount})",
                    color = Amber100.copy(alpha = 0.4f),
                    fontSize = 11.sp,
                    fontFamily = JetBrainsMono
                )
            }
        }

        // Zona Direita (Down)
        if (habit.down) {
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .fillMaxHeight()
                    .background(Color(0x33F43F5E))
                    .clickable { onTriggerHabit(habit.id, false) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Trigger Negativo",
                    tint = Rose400,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .fillMaxHeight()
                    .background(Stone900)
            )
        }
    }
}
