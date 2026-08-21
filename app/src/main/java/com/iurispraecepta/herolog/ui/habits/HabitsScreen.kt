package com.iurispraecepta.herolog.ui.habits

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
import androidx.compose.ui.text.font.FontFamily
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
import com.iurispraecepta.herolog.ui.theme.Amber100
import com.iurispraecepta.herolog.ui.theme.Amber400
import com.iurispraecepta.herolog.ui.theme.Amber500
import com.iurispraecepta.herolog.ui.theme.ScoreColor
import com.iurispraecepta.herolog.ui.theme.Stone400
import com.iurispraecepta.herolog.ui.theme.Stone800
import com.iurispraecepta.herolog.ui.theme.Stone900
import com.iurispraecepta.herolog.ui.theme.Stone950
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    initialConfirmDelete: Boolean = false,
    initialConfirmCancel: Boolean = false
) {
    var isCreating by remember { mutableStateOf(initialIsCreating) }
    var editingHabit by remember { mutableStateOf(initialEditingHabit) }

    var formTitle by remember { mutableStateOf(initialEditingHabit?.title ?: "") }
    var formNotes by remember { mutableStateOf(initialEditingHabit?.notes ?: "") }
    var formUp by remember { mutableStateOf(initialEditingHabit?.up ?: true) }
    var formDown by remember { mutableStateOf(initialEditingHabit?.down ?: false) }
    var formDifficulty by remember { mutableStateOf(initialEditingHabit?.difficulty ?: Difficulty.Easy) }
    var formTagInput by remember { mutableStateOf(initialEditingHabit?.tags?.joinToString(", ") ?: "") }

    var isConfirmingDelete by remember { mutableStateOf(initialConfirmDelete) }
    var isConfirmingCancel by remember { mutableStateOf(initialConfirmCancel) }

    fun resetForm() {
        formTitle = ""
        formNotes = ""
        formUp = true
        formDown = false
        formDifficulty = Difficulty.Easy
        formTagInput = ""
        isConfirmingDelete = false
        isConfirmingCancel = false
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
        isConfirmingDelete = false
        isConfirmingCancel = false
        editingHabit = null
        isCreating = true
    }

    fun openEditModal(habit: Habit) {
        formTitle = habit.title
        formNotes = habit.notes
        formUp = habit.up
        formDown = habit.down
        formDifficulty = habit.difficulty
        formTagInput = habit.tags.joinToString(", ")
        isConfirmingDelete = false
        isConfirmingCancel = false
        isCreating = false
        editingHabit = habit
    }

    val isModalOpen = isCreating || editingHabit != null
    val todayDateStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Stone950)
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
                text = "⚡ CAPELA DE HÁBITOS",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Amber400,
                letterSpacing = 1.sp
            )

            if (!isCreating && editingHabit == null) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x26F59E0B))
                        .border(1.dp, Amber400, RoundedCornerShape(6.dp))
                        .clickable { openCreateModal() }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "Novo Hábito",
                        tint = Amber300,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Novo",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Amber200
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
                            color = Color(0x33FBBF24),
                            style = Stroke(
                                width = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                            ),
                            cornerRadius = CornerRadius(8.dp.toPx())
                        )
                    }
                    .background(Color(0x1A1C1917), RoundedCornerShape(8.dp))
                    .padding(horizontal = 20.dp, vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "A heráldica de seus hábitos está em branco. Comece definindo comportamentos diários positivos ou rituais de quebra de vícios!",
                    style = TextStyle(
                        fontStyle = FontStyle.Italic,
                        fontFamily = FontFamily.Serif,
                        color = Stone400,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
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

    // Modal (HeroLogModal)
    if (isModalOpen) {
        HeroLogModal(
            isOpen = isModalOpen,
            onClose = {
                if (isConfirmingCancel) {
                    resetForm()
                } else {
                    isConfirmingCancel = true
                }
            },
            title = if (editingHabit != null) "Editar Tarefa" else "Nova Tarefa",
            variant = ModalVariant.Amber,
            allowBackdropClose = true
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Título
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "TÍTULO *",
                        fontFamily = FontFamily.Serif,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Amber300,
                        letterSpacing = 0.5.sp
                    )
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
                            fontSize = 14.sp,
                            fontFamily = FontFamily.SansSerif
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(Amber400),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        decorationBox = { innerTextField ->
                            Box {
                                if (formTitle.isEmpty()) {
                                    Text(
                                        text = "Ex: Beber 2L de água, Ler 10 páginas...",
                                        color = Stone500,
                                        fontSize = 14.sp
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }

                // Notas
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "NOTAS",
                        fontFamily = FontFamily.Serif,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xB3FEF3C7),
                        letterSpacing = 0.5.sp
                    )
                    BasicTextField(
                        value = formNotes,
                        onValueChange = { formNotes = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .background(Stone900, RoundedCornerShape(6.dp))
                            .border(1.dp, Color(0x33F59E0B), RoundedCornerShape(6.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        textStyle = TextStyle(
                            color = Amber100,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.SansSerif
                        ),
                        cursorBrush = SolidColor(Amber400),
                        decorationBox = { innerTextField ->
                            Box {
                                if (formNotes.isEmpty()) {
                                    Text(
                                        text = "Detalhes adicionais, contexto ou regras...",
                                        color = Stone500,
                                        fontSize = 13.sp
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }

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
                        Text(
                            text = "CAMINHOS PERMITIDOS",
                            fontFamily = FontFamily.Serif,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xB3FEF3C7),
                            letterSpacing = 0.5.sp
                        )

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
                                        text = "Positivo",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
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
                                        text = "Negativo",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
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
                        Text(
                            text = "DIFICULDADE",
                            fontFamily = FontFamily.Serif,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xB3FEF3C7),
                            letterSpacing = 0.5.sp
                        )

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

                // Tags
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "TAGS (SEPARADAS POR VÍRGULA)",
                        fontFamily = FontFamily.Serif,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xB3FEF3C7),
                        letterSpacing = 0.5.sp
                    )
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
                            fontSize = 13.sp,
                            fontFamily = FontFamily.SansSerif
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(Amber400),
                        decorationBox = { innerTextField ->
                            Box {
                                if (formTagInput.isEmpty()) {
                                    Text(
                                        text = "ex: saude, rotina, foco",
                                        color = Stone500,
                                        fontSize = 13.sp
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Rodapé (Actions)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left side: Delete button if editing
                    if (editingHabit != null) {
                        if (!isConfirmingDelete) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0x1ADC2626))
                                    .border(1.dp, Color(0x4DDC2626), RoundedCornerShape(6.dp))
                                    .clickable { isConfirmingDelete = true }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "Excluir",
                                    color = Color(0xFFF87171),
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Excluir?",
                                    color = Color(0xFFF87171),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFDC2626))
                                        .clickable {
                                            val idToDelete = editingHabit?.id
                                            if (idToDelete != null) {
                                                onDeleteHabit(idToDelete)
                                            }
                                            resetForm()
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Sim",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Stone800)
                                        .clickable { isConfirmingDelete = false }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Não",
                                        color = Stone400,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    // Right side: Cancel + Submit buttons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Cancel button flow
                        if (!isConfirmingCancel) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0x331C1917))
                                    .border(1.dp, Color(0x3344403C), RoundedCornerShape(6.dp))
                                    .clickable { isConfirmingCancel = true }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "Cancelar",
                                    color = Stone400,
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Descartar?",
                                    color = Amber300,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0x33F59E0B))
                                        .border(1.dp, Amber400, RoundedCornerShape(4.dp))
                                        .clickable { resetForm() }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Sim",
                                        color = Amber200,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Stone800)
                                        .clickable { isConfirmingCancel = false }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Não",
                                        color = Stone400,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Submit (Criar / Salvar)
                        val isSubmitEnabled = formTitle.isNotBlank()
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSubmitEnabled) Color(0xFFF59E0B) else Color(0x40F59E0B))
                                .clickable(enabled = isSubmitEnabled) {
                                    val parsedTags = formTagInput
                                        .split(",")
                                        .map { it.trim().lowercase() }
                                        .filter { it.isNotEmpty() }

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
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = if (editingHabit != null) "Salvar" else "Criar",
                                color = if (isSubmitEnabled) Stone950 else Color(0x800C0A09),
                                fontFamily = FontFamily.Serif,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }
    }
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
                    fontFamily = FontFamily.Serif,
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
                    color = Stone400,
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
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Text(
                        text = "○ Pendente hoje",
                        color = Stone500,
                        fontSize = 11.sp
                    )
                }

                Text(
                    text = "•",
                    color = Stone600,
                    fontSize = 11.sp
                )

                Text(
                    text = "(+${habit.upCount} | -${habit.downCount})",
                    color = Stone400,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
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
