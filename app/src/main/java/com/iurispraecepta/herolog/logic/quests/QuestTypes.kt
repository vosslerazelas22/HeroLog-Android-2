package com.iurispraecepta.herolog.logic.quests

enum class Difficulty {
    TRIVIAL,
    EASY,
    MEDIUM,
    HARD
}

data class Habit(
    val id: String,
    val title: String,
    val notes: String,
    // up/down controlam só quais botões a UI desenha — ViewModel não valida (fiel à fonte, ver PARIDADE.md). Endurecer no ViewModel é pendência registrada, só após paridade total.
    val up: Boolean,
    val down: Boolean,
    val difficulty: Difficulty,
    val upCount: Int = 0,
    val downCount: Int = 0,
    val streak: Int = 0,
    val tags: List<String> = emptyList(),
    val lastTriggeredDate: String? = null
)

data class Daily(
    val id: String,
    val title: String,
    val notes: String,
    val difficulty: Difficulty,
    val completed: Boolean = false,
    val streak: Int = 0,
    val repeats: RepeatFrequency,
    val every: Int,
    val tags: List<String> = emptyList(),
    val checklist: List<ChecklistItem> = emptyList(),
    val value: Int? = null,
    val createdAt: String? = null
)

enum class RepeatFrequency {
    DAILY,
    WEEKLY,
    MONTHLY
}

data class Todo(
    val id: String,
    val title: String,
    val notes: String,
    val difficulty: Difficulty,
    val completed: Boolean = false,
    val tags: List<String> = emptyList(),
    val checklist: List<ChecklistItem> = emptyList(),
    val createdAt: String? = null,
    val completedAt: String? = null
)

data class ChecklistItem(
    val id: String,
    val text: String,
    val completed: Boolean = false
)

data class DifficultyRewards(
    val xp: Int,
    val gold: Int,
    val damage: Int
)

fun getDifficultyRewards(difficulty: Difficulty): DifficultyRewards = when (difficulty) {
    Difficulty.TRIVIAL -> DifficultyRewards(xp = 4, gold = 2, damage = 1)
    Difficulty.EASY -> DifficultyRewards(xp = 12, gold = 6, damage = 3)
    Difficulty.MEDIUM -> DifficultyRewards(xp = 28, gold = 14, damage = 7)
    Difficulty.HARD -> DifficultyRewards(xp = 60, gold = 25, damage = 15)
}

fun getDifficultyRewards(difficulty: com.iurispraecepta.herolog.model.Difficulty): DifficultyRewards = when (difficulty) {
    com.iurispraecepta.herolog.model.Difficulty.Trivial -> DifficultyRewards(xp = 4, gold = 2, damage = 1)
    com.iurispraecepta.herolog.model.Difficulty.Easy -> DifficultyRewards(xp = 12, gold = 6, damage = 3)
    com.iurispraecepta.herolog.model.Difficulty.Medium -> DifficultyRewards(xp = 28, gold = 14, damage = 7)
    com.iurispraecepta.herolog.model.Difficulty.Hard -> DifficultyRewards(xp = 60, gold = 25, damage = 15)
}
