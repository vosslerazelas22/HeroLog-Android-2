package com.iurispraecepta.herolog.ui.dailies

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iurispraecepta.herolog.model.Daily
import com.iurispraecepta.herolog.model.Difficulty
import com.iurispraecepta.herolog.model.RepeatInterval
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

private val Stone700 = Color(0xFF44403C)
private val Stone600 = Color(0xFF57534E)
private val Stone500 = Color(0xFF78716C)
private val Amber300 = Color(0xFFFCD34D)
private val Amber200 = Color(0xFFFDE68A)
private val Purple400 = Color(0xFFC084FC)
private val Purple500 = Color(0xFFA855F7)
private val Sky400 = Color(0xFF38BDF8)

fun repeatIntervalLabel(interval: RepeatInterval): String = when (interval) {
    RepeatInterval.Daily -> "Diário"
    RepeatInterval.Weekly -> "Semanal"
    RepeatInterval.Monthly -> "Mensal"
}

fun getFrequencyText(repeats: RepeatInterval, every: Int): String {
    val base = when (repeats) {
        RepeatInterval.Daily -> "diariamente"
        RepeatInterval.Weekly -> "semanalmente"
        RepeatInterval.Monthly -> "mensalmente"
    }
    val noun = repeatIntervalLabel(repeats)
    return if (every == 1) "Repete $base" else "Repete: $noun (a cada ${every}x)"
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
    initialConfirmDelete: Boolean = false,
    initialConfirmCancel: Boolean = false
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

    var isConfirmingDelete by remember { mutableStateOf(initialConfirmDelete) }
    var isConfirmingCancel by remember { mutableStateOf(initialConfirmCancel) }

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
        isConfirmingDelete = false
        isConfirmingCancel = false
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
        isConfirmingDelete = false
        isConfirmingCancel = false
        editingDaily = null
        isCreating = true
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
        isConfirmingDelete = false
        isConfirmingCancel = false
        isCreating = false
        editingDaily = daily
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
                text = "📅 TAREFAS DIÁRIAS",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Amber400,
                letterSpacing = 1.sp
            )

            if (!isCreating && editingDaily == null) {
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
                        contentDescription = "Novo Diário",
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

        // Dailies List or Empty State
        if (dailies.isEmpty()) {
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
                    text = "Nenhum voto de prática diária estabelecido neste plano. Adicione tarefas para forjar hábitos duradouros todos os dias!",
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
                items(dailies, key = { it.id }) { d ->
                    DailyCardItem(
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
            title = if (editingDaily != null) "Editar Tarefa" else "Nova Tarefa",
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
                            .border(
                                1.dp,
                                if (formTitle.isBlank()) Color(0x33F59E0B) else Amber500,
                                RoundedCornerShape(6.dp)
                            )
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
                                        text = "Ex: Treino matinal, Estudar 30m...",
                                        color = Stone500,
                                        fontSize = 14.sp
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }

                // Notas (h-14 ~ 56dp)
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
                            .height(56.dp)
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

                    // Regularidade (Dropdown tipo + input Every)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "REGULARIDADE",
                            fontFamily = FontFamily.Serif,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xB3FEF3C7),
                            letterSpacing = 0.5.sp
                        )

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
                                    fontFamily = FontFamily.Monospace
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

                // Checklist Criador — SOMENTE MODO CRIAÇÃO (editingDaily == null)
                if (editingDaily == null) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "CHECKLIST DE CRITÉRIOS (OPCIONAL)",
                            fontFamily = FontFamily.Serif,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xB3FEF3C7),
                            letterSpacing = 0.5.sp
                        )

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
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.SansSerif
                                ),
                                singleLine = true,
                                cursorBrush = SolidColor(Amber400),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { addChecklistItem() }),
                                decorationBox = { innerTextField ->
                                    Box {
                                        if (checklistInput.isEmpty()) {
                                            Text(
                                                text = "Novo sub-critério...",
                                                color = Stone500,
                                                fontSize = 13.sp
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
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

                // Linha dupla: Série Inicial (Streak) + Tags (em ambos modos criar e editar)
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
                        Text(
                            text = "SÉRIE INICIAL (STREAK)",
                            fontFamily = FontFamily.Serif,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xB3FEF3C7),
                            letterSpacing = 0.5.sp
                        )
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
                                fontFamily = FontFamily.Monospace
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
                        Text(
                            text = "TAGS (VÍRGULAS)",
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
                                        Text(text = "ex: foco, diario", color = Stone500, fontSize = 13.sp)
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Rodapé (Actions)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left side: Delete button if editing
                    if (editingDaily != null) {
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
                                            val idToDelete = editingDaily?.id
                                            if (idToDelete != null) {
                                                onDeleteDaily(idToDelete)
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

                                    val parsedEvery = (formEvery.toIntOrNull() ?: 1).coerceIn(1, 99)
                                    val parsedStreak = (formStreak.toIntOrNull() ?: 0).coerceAtLeast(0)

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
                                            checklistItems
                                        )
                                    }
                                    resetForm()
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = if (editingDaily != null) "Salvar" else "Criar",
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
private fun DailyCardItem(
    daily: Daily,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onToggleDaily: () -> Unit,
    onToggleChecklistItem: (String) -> Unit,
    onOpenEdit: () -> Unit
) {
    val score = daily.value ?: 0
    val tokens = ScoreColor.forScore(score)
    val hasChecklist = daily.checklist.isNotEmpty()
    val showChecklist = hasChecklist && (isExpanded || daily.completed)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Brush.horizontalGradient(listOf(tokens.start, tokens.end)))
            .border(1.dp, tokens.border, RoundedCornerShape(8.dp))
            .alpha(if (daily.completed) 0.7f else 1.0f)
    ) {
        // Linha Primária
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Banda Esquerda (Toggle Daily)
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .fillMaxHeight()
                    .clip(
                        RoundedCornerShape(
                            topStart = 8.dp,
                            bottomStart = if (showChecklist) 0.dp else 8.dp
                        )
                    )
                    .background(if (daily.completed) Color(0x330284C7) else Stone900)
                    .clickable { onToggleDaily() },
                contentAlignment = Alignment.Center
            ) {
                if (daily.completed) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Desmarcar Diário",
                        tint = Sky400,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0x33F59E0B))
                            .border(1.dp, Amber500, RoundedCornerShape(2.dp))
                    )
                }
            }

            // Corpo Central (Clicável para abrir edição)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onOpenEdit() }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Título + Dificuldade
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = daily.title,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (daily.completed) Color(0xFF7DD3FC) else Amber100,
                        textDecoration = if (daily.completed) TextDecoration.LineThrough else TextDecoration.None,
                        modifier = Modifier.weight(1f, fill = false),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    DifficultyBadge(difficulty = daily.difficulty)
                }

                // Notas se houver
                if (daily.notes.isNotBlank()) {
                    Text(
                        text = daily.notes,
                        color = Stone400,
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Linha de Status (Streak + Frequência + Checklist se houver)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "🏅 ${daily.streak}",
                        color = Amber300,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "•",
                        color = Stone600,
                        fontSize = 11.sp
                    )

                    Text(
                        text = getFrequencyText(daily.repeats, daily.every),
                        color = Stone400,
                        fontSize = 11.sp
                    )

                    if (hasChecklist) {
                        val completedCount = daily.checklist.count { it.completed }
                        Text(
                            text = "•",
                            color = Stone600,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "📋 $completedCount de ${daily.checklist.size} critérios",
                            color = Purple400,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Banda Direita (Chevron expandir/colapsar só se tiver checklist)
            if (hasChecklist) {
                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .fillMaxHeight()
                        .drawBehind {
                            drawLine(
                                color = Color(0x1AF59E0B),
                                start = Offset(0f, 0f),
                                end = Offset(0f, size.height),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                        .clickable { onToggleExpand() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Colapsar critérios" else "Expandir critérios",
                        tint = Amber300,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Painel de Checklist Expandido
        if (showChecklist) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x801C1917))
                    .border(width = 1.dp, color = Color(0x1AF59E0B))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                daily.checklist.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Checkbox quadrado roxo
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (item.completed) Color(0x33A855F7) else Stone950)
                                .border(
                                    1.dp,
                                    if (item.completed) Purple400 else Stone700,
                                    RoundedCornerShape(4.dp)
                                )
                                .clickable { onToggleChecklistItem(item.id) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (item.completed) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(RoundedCornerShape(1.dp))
                                        .background(Purple400)
                                )
                            }
                        }

                        // Texto do item
                        Text(
                            text = item.text,
                            color = if (item.completed) Stone500 else Amber100.copy(alpha = 0.7f),
                            textDecoration = if (item.completed) TextDecoration.LineThrough else TextDecoration.None,
                            fontSize = 13.sp,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onToggleChecklistItem(item.id) }
                        )
                    }
                }
            }
        }
    }
}
