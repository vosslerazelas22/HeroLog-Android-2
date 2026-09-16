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
 * Normaliza tags para comparacao semantica: separa por virgula, trim, lowercase, descarta vazios.
 * FR-006: "Categorias devem ter comparacao semantica: separar por virgula, trim,
 * descartar vazios e normalizar caixa antes de comparar."
 */
fun normalizeTags(raw: String): List<String> =
    raw.split(",")
        .map { it.trim().lowercase() }
        .filter { it.isNotEmpty() }
        .sorted()

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
