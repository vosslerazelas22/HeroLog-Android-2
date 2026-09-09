package com.iurispraecepta.herolog.ui.todos

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iurispraecepta.herolog.logic.quests.TodoDecayLogic
import com.iurispraecepta.herolog.model.Difficulty
import com.iurispraecepta.herolog.model.Todo
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
import java.util.Date

private val Champagne400 = Color(0xFFE5C158)
private val Champagne300 = Color(0xFFF5DFA0)
private val Stone700 = Color(0xFF44403C)
private val Stone600 = Color(0xFF57534E)
private val Stone500 = Color(0xFF78716C)
private val Amber300 = Color(0xFFFCD34D)
private val Amber200 = Color(0xFFFDE68A)
private val Purple400 = Color(0xFFC084FC)
private val Emerald400 = Color(0xFF34D399)

enum class TodoFilter {
    Pending,
    Completed,
    All
}

@Composable
fun TodosScreen(
    todos: List<Todo>,
    onToggleTodo: (String) -> Unit,
    onToggleChecklistItem: (String, String) -> Unit,
    onAddTodo: (
        title: String,
        notes: String,
        difficulty: Difficulty,
        tags: List<String>,
        checklistTexts: List<String>
    ) -> Unit,
    onEditTodo: (Todo) -> Unit,
    onDeleteTodo: (String) -> Unit,
    modifier: Modifier = Modifier,
    currentDate: Date = Date(),
    initialFilter: TodoFilter = TodoFilter.Pending,
    initialIsCreating: Boolean = false,
    initialEditingTodo: Todo? = null,
    initialExpandedTodoId: String? = null,
    initialChecklistItems: List<String> = emptyList(),
    initialConfirmDelete: Boolean = false,
    initialConfirmCancel: Boolean = false
) {
    var filter by remember { mutableStateOf(initialFilter) }
    var isCreating by remember { mutableStateOf(initialIsCreating) }
    var editingTodo by remember { mutableStateOf(initialEditingTodo) }
    var expandedTodoId by remember { mutableStateOf<String?>(initialExpandedTodoId) }

    var formTitle by remember { mutableStateOf(initialEditingTodo?.title ?: "") }
    var formNotes by remember { mutableStateOf(initialEditingTodo?.notes ?: "") }
    var formDifficulty by remember { mutableStateOf(initialEditingTodo?.difficulty ?: Difficulty.Easy) }
    var formTagInput by remember { mutableStateOf(initialEditingTodo?.tags?.joinToString(", ") ?: "") }
    var checklistInput by remember { mutableStateOf("") }
    var checklistItems by remember { mutableStateOf(initialChecklistItems) }

    var isConfirmingDelete by remember { mutableStateOf(initialConfirmDelete) }
    var isConfirmingCancel by remember { mutableStateOf(initialConfirmCancel) }

    val isModalOpen = isCreating || editingTodo != null

    fun resetForm() {
        formTitle = ""
        formNotes = ""
        formDifficulty = Difficulty.Easy
        formTagInput = ""
        checklistInput = ""
        checklistItems = emptyList()
        isConfirmingDelete = false
        isConfirmingCancel = false
        isCreating = false
        editingTodo = null
    }

    fun openCreateModal() {
        formTitle = ""
        formNotes = ""
        formDifficulty = Difficulty.Easy
        formTagInput = ""
        checklistInput = ""
        checklistItems = emptyList()
        isConfirmingDelete = false
        isConfirmingCancel = false
        editingTodo = null
        isCreating = true
    }

    fun openEditModal(todo: Todo) {
        formTitle = todo.title
        formNotes = todo.notes
        formDifficulty = todo.difficulty
        formTagInput = todo.tags.joinToString(", ")
        checklistInput = ""
        checklistItems = emptyList()
        isConfirmingDelete = false
        isConfirmingCancel = false
        isCreating = false
        editingTodo = todo
    }

    fun addChecklistItem() {
        val trimmed = checklistInput.trim()
        if (trimmed.isNotEmpty()) {
            checklistItems = checklistItems + trimmed
            checklistInput = ""
        }
    }

    val filteredTodos = when (filter) {
        TodoFilter.Pending -> todos.filter { !it.completed }
        TodoFilter.Completed -> todos.filter { it.completed }
        TodoFilter.All -> todos
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Stone950)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Header: "🗒️ Missões Avulsas" + Botão Novo
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🗒️ Missões Avulsas",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Champagne400,
                letterSpacing = 1.sp
            )

            if (!isCreating && editingTodo == null) {
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
                        contentDescription = "Novo Afazer",
                        tint = Champagne400,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Novo",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Champagne400
                    )
                }
            }
        }

        // Filtro de 3 vias (Pendentes / Concluídos / Todos)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val filters = listOf(
                TodoFilter.Pending to "Pendentes",
                TodoFilter.Completed to "Concluídos",
                TodoFilter.All to "Todos"
            )

            filters.forEach { (f, label) ->
                val isSelected = filter == f
                val isDisabled = isModalOpen

                Box(
                    modifier = Modifier
                        .alpha(if (isDisabled) 0.5f else 1.0f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when {
                                isSelected -> Color(0x26D4AF37)
                                else -> Color.Transparent
                            }
                        )
                        .border(
                            1.dp,
                            when {
                                isSelected -> Champagne400
                                else -> Color.Transparent
                            },
                            RoundedCornerShape(6.dp)
                        )
                        .clickable(enabled = !isDisabled) {
                            filter = f
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = label,
                        fontFamily = FontFamily.Serif,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = when {
                            isDisabled -> Color(0x8052525B)
                            isSelected -> Champagne300
                            else -> Color(0xFF71717A)
                        }
                    )
                }
            }
        }

        // Todos List or Empty State
        if (filteredTodos.isEmpty()) {
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
                    text = "Nenhum afazer místico sob este filtro. Adicione novas aventuras e complete-as!",
                    style = TextStyle(
                        fontStyle = FontStyle.Italic,
                        fontFamily = FontFamily.Serif,
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
                items(filteredTodos, key = { it.id }) { todo ->
                    TodoCardItem(
                        todo = todo,
                        currentDate = currentDate,
                        isExpanded = expandedTodoId == todo.id,
                        onToggleExpand = {
                            expandedTodoId = if (expandedTodoId == todo.id) null else todo.id
                        },
                        onToggleTodo = { onToggleTodo(todo.id) },
                        onToggleChecklistItem = { itemId -> onToggleChecklistItem(todo.id, itemId) },
                        onOpenEdit = { openEditModal(todo) }
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
            title = if (editingTodo != null) "Editar Tarefa" else "Nova Tarefa",
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
                                        text = "Ex: Resolver 3 problemas de Algoritmo, Escrever redação...",
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
                                        text = "Ex: Utilizar abordagem de árvore binária de busca, revisar testes unitários...",
                                        color = Stone500,
                                        fontSize = 13.sp
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }

                // Linha dupla: Dificuldade + Categorias (Tags)
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

                    // Categorias / Tags
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "CATEGORIAS (TAGS)",
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
                                .height(40.dp)
                                .background(Stone900, RoundedCornerShape(6.dp))
                                .border(1.dp, Color(0x33F59E0B), RoundedCornerShape(6.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
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
                                        Text(text = "study, workout, project...", color = Stone500, fontSize = 13.sp)
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }
                }

                // Checklist Criador — SOMENTE MODO CRIAÇÃO (editingTodo == null)
                if (editingTodo == null) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "CHECKLIST (OPCIONAL)",
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
                                                text = "Ex: Resolver estrutura básica, Fazer deploy...",
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

                Spacer(modifier = Modifier.height(6.dp))

                // Rodapé (Actions)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left side: Delete button if editing
                    if (editingTodo != null) {
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
                                            val idToDelete = editingTodo?.id
                                            if (idToDelete != null) {
                                                onDeleteTodo(idToDelete)
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

                                    val currentEditing = editingTodo
                                    if (currentEditing != null) {
                                        onEditTodo(
                                            currentEditing.copy(
                                                title = formTitle.trim(),
                                                notes = formNotes.trim(),
                                                difficulty = formDifficulty,
                                                tags = parsedTags
                                            )
                                        )
                                    } else {
                                        onAddTodo(
                                            formTitle.trim(),
                                            formNotes.trim(),
                                            formDifficulty,
                                            parsedTags,
                                            checklistItems
                                        )
                                    }
                                    resetForm()
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = if (editingTodo != null) "Salvar" else "Criar",
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
private fun TodoCardItem(
    todo: Todo,
    currentDate: Date,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onToggleTodo: () -> Unit,
    onToggleChecklistItem: (String) -> Unit,
    onOpenEdit: () -> Unit
) {
    val score = TodoDecayLogic.getTodoDecayValue(todo, currentDate)
    val tokens = ScoreColor.forScore(score)
    val hasChecklist = todo.checklist.isNotEmpty()
    val showChecklist = hasChecklist && isExpanded

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .then(
                if (todo.completed) {
                    Modifier
                        .background(Color(0x08022C22)) // emerald-950/[0.03], fiel à fonte React
                        .border(1.dp, Color(0x4D10B981), RoundedCornerShape(8.dp))
                } else {
                    Modifier
                        .background(Brush.horizontalGradient(listOf(tokens.start, tokens.end)))
                        .border(1.dp, tokens.border, RoundedCornerShape(8.dp))
                }
            )
    ) {
        // Linha Primária
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Banda Esquerda (Toggle Completo)
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
                    .background(if (todo.completed) Color(0x3310B981) else Stone900)
                    .clickable { onToggleTodo() },
                contentAlignment = Alignment.Center
            ) {
                if (todo.completed) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Desmarcar Tarefa",
                        tint = Emerald400,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color(0x73F59E0B), CircleShape)
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
                        text = todo.title,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (todo.completed) Stone500 else Amber100,
                        textDecoration = if (todo.completed) TextDecoration.LineThrough else TextDecoration.None,
                        modifier = Modifier.weight(1f, fill = false),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    DifficultyBadge(difficulty = todo.difficulty)
                }

                // Notas se houver
                if (todo.notes.isNotBlank()) {
                    Text(
                        text = todo.notes,
                        color = Amber100.copy(alpha = 0.65f),
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Linha de Meta: "🛡️ Tarefa Única" + Checklist se houver
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "🛡️ Tarefa Única",
                        color = Amber100.copy(alpha = 0.4f),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    if (hasChecklist) {
                        val completedCount = todo.checklist.count { it.completed }
                        Text(
                            text = "•",
                            color = Color(0x33F59E0B),
                            fontSize = 11.sp
                        )
                        Text(
                            text = "📋 $completedCount de ${todo.checklist.size} critérios",
                            color = Purple400,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
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

        // Painel de Checklist Expandido (apenas se showChecklist = hasChecklist && isExpanded)
        if (showChecklist) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x661C1917))
                    .drawBehind {
                        drawLine(
                            color = Color(0x0DF59E0B),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                todo.checklist.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Checkbox quadrado roxo corrigido (14dp)
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
                            fontWeight = if (item.completed) FontWeight.Bold else FontWeight.Normal,
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