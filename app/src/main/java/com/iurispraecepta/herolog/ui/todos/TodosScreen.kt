package com.iurispraecepta.herolog.ui.todos

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.R as LucideR
import com.iurispraecepta.herolog.model.Difficulty
import com.iurispraecepta.herolog.model.Todo
import com.iurispraecepta.herolog.ui.components.HeroLogModal
import com.iurispraecepta.herolog.ui.components.ModalVariant
import com.iurispraecepta.herolog.ui.components.difficultyColor
import com.iurispraecepta.herolog.ui.components.difficultyLabel
import com.iurispraecepta.herolog.ui.components.RitualEmptyState
import com.iurispraecepta.herolog.ui.components.RitualTabHeader
import com.iurispraecepta.herolog.ui.components.ritualBottomBorder
import com.iurispraecepta.herolog.ui.theme.Amber100
import com.iurispraecepta.herolog.ui.theme.Amber400
import com.iurispraecepta.herolog.ui.theme.Amber500
import com.iurispraecepta.herolog.ui.theme.Inter
import com.iurispraecepta.herolog.ui.theme.JetBrainsMono
import com.iurispraecepta.herolog.ui.theme.RitualBorder
import com.iurispraecepta.herolog.ui.theme.RitualGold
import com.iurispraecepta.herolog.ui.theme.RitualGreen
import com.iurispraecepta.herolog.ui.theme.RitualSurfaceElevated
import com.iurispraecepta.herolog.ui.theme.RitualTextPrimary
import com.iurispraecepta.herolog.ui.theme.RitualTextSecondary
import com.iurispraecepta.herolog.ui.theme.JetBrainsMono
import com.iurispraecepta.herolog.ui.theme.Stone400
import com.iurispraecepta.herolog.ui.theme.Stone800
import com.iurispraecepta.herolog.ui.theme.Stone900
import com.iurispraecepta.herolog.ui.theme.Zinc300
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.derivedStateOf
import com.iurispraecepta.herolog.ui.form.DeleteConfirmState
import com.iurispraecepta.herolog.ui.form.TodoDraft
import com.iurispraecepta.herolog.ui.form.QuestFormShell
import com.iurispraecepta.herolog.ui.form.isEqualTo
import com.iurispraecepta.herolog.ui.form.mergePendingChecklistInput
import com.iurispraecepta.herolog.ui.form.parseTags

private val Stone500 = Color(0xFF78716C)
private val Amber300 = Color(0xFFFCD34D)

enum class TodoFilter {
    Pending,
    Completed,
    All
}

/**
 * Filtros de Tarefas (spec Bloco F, `TodosTab.tsx:144-187`): linha mono 12sp,
 * gap 8, `px 16 / pb 8`, borda inferior; chip `px 12 / py 4`, raio 8dp, borda 1dp.
 * Ativo: fundo/borda/texto por filtro (ouro/verde/elevado), SemiBold. Inativo:
 * borda transparente, texto `#928C9A`. Lógica `isDisabled` mantida como estava
 * (spec §6 — só o visual muda; o `disabled` do React durante edição é §2).
 */
@Composable
private fun RitualTodoFilterRow(
    filter: TodoFilter,
    isDisabled: Boolean,
    onSelect: (TodoFilter) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .ritualBottomBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RitualFilterChip(TodoFilter.Pending, "Pendentes", filter, isDisabled, onSelect,
                RitualGold)
            RitualFilterChip(TodoFilter.Completed, "Concluídos", filter, isDisabled, onSelect,
                RitualGreen)
            RitualFilterChip(TodoFilter.All, "Todos", filter, isDisabled, onSelect, null)
        }
    }
}

@Composable
private fun RitualFilterChip(
    value: TodoFilter,
    label: String,
    current: TodoFilter,
    isDisabled: Boolean,
    onSelect: (TodoFilter) -> Unit,
    accent: Color?
) {
    val isSelected = current == value
    val bg = when {
        isDisabled || !isSelected -> Color.Transparent
        accent != null -> accent.copy(alpha = 0.15f)
        else -> RitualSurfaceElevated
    }
    val border = when {
        isDisabled || !isSelected -> Color.Transparent
        accent != null -> accent
        else -> RitualBorder
    }
    val textColor = when {
        isDisabled -> Color(0x8052525B)
        isSelected && accent != null -> accent
        isSelected -> RitualTextPrimary
        else -> RitualTextSecondary
    }
    Box(
        modifier = Modifier
            .alpha(if (isDisabled) 0.5f else 1.0f)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(8.dp))
            .clickable(enabled = !isDisabled) { onSelect(value) }
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = JetBrainsMono,
            fontSize = 12.sp,
            lineHeight = 12.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = textColor
        )
    }
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
    initialFilter: TodoFilter = TodoFilter.Pending,
    initialIsCreating: Boolean = false,
    initialEditingTodo: Todo? = null,
    initialExpandedTodoId: String? = null,
    initialChecklistItems: List<String> = emptyList(),
    initialDeleteConfirmState: DeleteConfirmState = DeleteConfirmState.None,
    initialShowDiscard: Boolean = false,
    initialSnapshot: TodoDraft? = null
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

    var deleteConfirmState by remember { mutableStateOf(initialDeleteConfirmState) }
    var showDiscard by remember { mutableStateOf(initialShowDiscard) }

    // FR-005: Snapshot imutavel capturado ao abrir o formulario
    var initialDraft by remember { mutableStateOf<TodoDraft?>(initialSnapshot) }

    // FR-005: Comparacao estrutural do rascunho atual com o snapshot
    val isDirty by remember {
        derivedStateOf {
            val draft = initialDraft ?: return@derivedStateOf false
            val current = TodoDraft(
                title = formTitle,
                notes = formNotes,
                difficulty = formDifficulty,
                tags = formTagInput,
                checklistItems = checklistItems,
                checklistInput = checklistInput
            )
            !draft.isEqualTo(current)
        }
    }

    val isModalOpen = isCreating || editingTodo != null

    fun resetForm() {
        formTitle = ""
        formNotes = ""
        formDifficulty = Difficulty.Easy
        formTagInput = ""
        checklistInput = ""
        checklistItems = emptyList()
        deleteConfirmState = DeleteConfirmState.None
        initialDraft = null
        showDiscard = false
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
        deleteConfirmState = DeleteConfirmState.None
        editingTodo = null
        isCreating = true
        showDiscard = false
        // FR-005: Captura snapshot do estado vazio
        initialDraft = TodoDraft(
            title = "",
            notes = "",
            difficulty = Difficulty.Easy,
            tags = "",
            checklistItems = emptyList(),
            checklistInput = ""
        )
    }

    fun openEditModal(todo: Todo) {
        formTitle = todo.title
        formNotes = todo.notes
        formDifficulty = todo.difficulty
        formTagInput = todo.tags.joinToString(", ")
        checklistInput = ""
        checklistItems = emptyList()
        deleteConfirmState = DeleteConfirmState.None
        isCreating = false
        editingTodo = todo
        showDiscard = false
        // FR-005: Captura snapshot do estado atual do todo
        initialDraft = TodoDraft(
            title = todo.title,
            notes = todo.notes,
            difficulty = todo.difficulty,
            tags = todo.tags.joinToString(", "),
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

    val filteredTodos = when (filter) {
        TodoFilter.Pending -> todos.filter { !it.completed }
        TodoFilter.Completed -> todos.filter { it.completed }
        TodoFilter.All -> todos
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 12.dp)
    ) {
        RitualTabHeader(
            iconRes = LucideR.drawable.lucide_ic_clipboard_list,
            title = "Missões Avulsas",
            counterText = if (filteredTodos.size == 1) "1 missão" else "${filteredTodos.size} missões",
            showNewButton = !isCreating && editingTodo == null,
            newContentDescription = "Novo afazer",
            onNewClick = { openCreateModal() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Filtro de 3 vias (Pendentes / Concluídos / Todos)
        RitualTodoFilterRow(
            filter = filter,
            isDisabled = isModalOpen,
            onSelect = { filter = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Todos List or Empty State
        if (filteredTodos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                RitualEmptyState(
                    message = "Nenhum afazer místico sob este filtro. Adicione novas aventuras e complete-as!"
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
                items(filteredTodos, key = { it.id }) { todo ->
                    RitualTodoCard(
                        todo = todo,
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
            title = if (editingTodo != null) "Editar Tarefa" else "Nova Tarefa",
            variant = ModalVariant.Amber,
            allowBackdropClose = true,
            contentPadding = PaddingValues(0.dp)
        ) {
            QuestFormShell(
                isDirty = isDirty,
                showDiscard = showDiscard,
                isEditing = editingTodo != null,
                onDiscard = { resetForm() },
                onContinueEditing = { showDiscard = false },
                onRequestClose = requestClose,
                onConfirmDelete = {
                    val idToDelete = editingTodo?.id
                    if (idToDelete != null) {
                        onDeleteTodo(idToDelete)
                    }
                    resetForm()
                },
                onSubmit = {
                    val parsedTags = parseTags(formTagInput)

                    // FR-008: Incluir texto pendente do checklist antes de submeter
                    val allChecklistItems = mergePendingChecklistInput(checklistItems, checklistInput)

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
                            allChecklistItems
                        )
                    }
                    resetForm()
                },
                deleteConfirmState = deleteConfirmState,
                onDeleteConfirmChange = { deleteConfirmState = it },
                submitEnabled = formTitle.isNotBlank(),
                submitLabel = if (editingTodo != null) "Salvar" else "Criar"
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
                                    text = "Ex: Resolver 3 problemas de Algoritmo, Escrever redação...",
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
                                    text = "Ex: Utilizar abordagem de árvore binária de busca, revisar testes unitários...",
                                    color = Stone500,
                                    fontSize = 13.sp
                                )
                            }
                            innerTextField()
                        }
                    }
                )

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

                    // Categorias / Tags
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FormFieldLabel("Categorias (Tags, separadas por vírgula)")
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
                                fontSize = 12.sp,
                                fontFamily = Inter
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

                // Checklist — SOMENTE MODO CRIAÇÃO (editingTodo == null)
                if (editingTodo == null) {
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
                                                text = "Ex: Resolver estrutura básica, Fazer deploy...",
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
