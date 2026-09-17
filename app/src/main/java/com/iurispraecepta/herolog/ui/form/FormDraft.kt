package com.iurispraecepta.herolog.ui.form

import com.iurispraecepta.herolog.model.Difficulty

/**
 * Snapshot imutável capturado ao abrir o formulário (criar ou editar).
 * Usado para comparacao estrutural e determinar se o rascunho esta dirty.
 * FR-005, FR-006 da Spec 003.
 */
data class HabitDraft(
    val title: String,
    val notes: String,
    val up: Boolean,
    val down: Boolean,
    val difficulty: Difficulty,
    val tags: String
)

data class DailyDraft(
    val title: String,
    val notes: String,
    val difficulty: Difficulty,
    val repeats: String,
    val every: String,
    val streak: String,
    val tags: String,
    val checklistItems: List<String>,
    val checklistInput: String
)

data class TodoDraft(
    val title: String,
    val notes: String,
    val difficulty: Difficulty,
    val tags: String,
    val checklistItems: List<String>,
    val checklistInput: String
)

/**
 * Parse de tags preservando a ordem de entrada: separa por virgula, trim,
 * lowercase, descarta vazios. Usado no submit (paridade com o React).
 */
fun parseTags(raw: String): List<String> =
    raw.split(",")
        .map { it.trim().lowercase() }
        .filter { it.isNotEmpty() }

/**
 * Normaliza tags para comparacao semantica: parse + ordenacao.
 * FR-006: "Categorias devem ter comparacao semantica: separar por virgula, trim,
 * descartar vazios e normalizar caixa antes de comparar."
 */
fun normalizeTags(raw: String): List<String> =
    parseTags(raw).sorted()

/**
 * FR-008 (divergencia consciente da spec vs React): incluir o texto pendente
 * do input do checklist na lista ao submeter. O React usa apenas
 * `checklistItems` no submit; a spec manda incorporar `checklistInput`
 * quando nao-vazio (trim).
 */
fun mergePendingChecklistInput(items: List<String>, input: String): List<String> =
    if (input.isNotBlank()) items + input.trim() else items

fun HabitDraft.isEqualTo(other: HabitDraft): Boolean =
    title == other.title &&
        notes == other.notes &&
        up == other.up &&
        down == other.down &&
        difficulty == other.difficulty &&
        normalizeTags(tags) == normalizeTags(other.tags)

fun DailyDraft.isEqualTo(other: DailyDraft): Boolean =
    title == other.title &&
        notes == other.notes &&
        difficulty == other.difficulty &&
        repeats == other.repeats &&
        every == other.every &&
        streak == other.streak &&
        normalizeTags(tags) == normalizeTags(other.tags) &&
        checklistItems == other.checklistItems &&
        checklistInput == other.checklistInput

fun TodoDraft.isEqualTo(other: TodoDraft): Boolean =
    title == other.title &&
        notes == other.notes &&
        difficulty == other.difficulty &&
        normalizeTags(tags) == normalizeTags(other.tags) &&
        checklistItems == other.checklistItems &&
        checklistInput == other.checklistInput
