package com.iurispraecepta.herolog.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class BuffType {
    @SerialName("DoubleLoot") DoubleLoot,
    @SerialName("FocusElixir") FocusElixir,
    @SerialName("CrystalClarity") CrystalClarity,
    @SerialName("RuneFortune") RuneFortune,
    @SerialName("StreakShield") StreakShield,
    @SerialName("PixelOwl") PixelOwl,
    @SerialName("DragonQuill") DragonQuill,
    @SerialName("CrystalBall") CrystalBall,
    @SerialName("AncientTome") AncientTome,
    @SerialName("SilverGrimoire") SilverGrimoire,
    @SerialName("AncientScroll") AncientScroll,
    @SerialName("CelestinePotion") CelestinePotion,
    @SerialName("StarPowder") StarPowder,
    @SerialName("GoldBrooch") GoldBrooch,
    @SerialName("ChaosGrimoire") ChaosGrimoire,
    @SerialName("UnwaveringSword") UnwaveringSword,
    @SerialName("SacredChalice") SacredChalice,
    @SerialName("ArcaneRelic") ArcaneRelic,
    @SerialName("RunicStone") RunicStone
}

@Serializable
enum class Rarity {
    @SerialName("comum") Comum,
    @SerialName("especial") Especial
}

@Serializable
data class InventoryItem(
    val id: String,
    val name: String,
    val emoji: String,
    val buff: BuffType,
    val price: Int,
    val desc: String,
    val isEquipment: Boolean? = null,
    val charges: Int? = null,
    val maxCharges: Int? = null,
    val rarity: Rarity? = null
)

@Serializable
data class Skill(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val level: Int,
    val xp: Int,
    val emoji: String? = null,
    val prestige: Int? = null,
    val tags: List<String>? = null
)

@Serializable
data class HistoryEntry(
    val id: String,
    val skillName: String,
    val date: String,
    val duration: Int, // in minutes
    val xp: Int,
    val gold: Int,
    val notes: String,
    val wilderness: Boolean,
    val aiChronicle: String? = null,
    val subskillTag: String? = null,
    /** Android-first: marca sessões de Masmorra (sem equivalente no React). Default `false` mantém saves antigos íntegros; `JsonConfig` ignora chaves desconhecidas. */
    val isDungeon: Boolean = false
)

/**
 * Apresentação da entrada no feed da Taverna (Spec F). O mapeamento `source → type` é fixo:
 * `session→focus`, `todo→mission`, `habit→ritual`, `daily→ritual`.
 */
@Serializable
enum class ActivityType {
    @SerialName("focus") Focus,
    @SerialName("mission") Mission,
    @SerialName("ritual") Ritual
}

/** Origem da entrada no feed; é por `source` que [com.iurispraecepta.herolog.logic.ActivityFeedLogic.revoke] casa. */
@Serializable
enum class ActivitySource {
    @SerialName("session") Session,
    @SerialName("todo") Todo,
    @SerialName("habit") Habit,
    @SerialName("daily") Daily
}

/**
 * Entrada do feed persistido `CharacterState.recentActivity` (Spec F) — mais recente primeiro,
 * máx. 20. Forma JSON idêntica à do React (D8): save exportado de um app importa no outro.
 *
 * - [id]: `"${source}_${epochMs}"` (prefixo = valor JSON de [source]).
 * - [at]: instante ISO-8601 em UTC com milissegundos (`2026-09-20T14:23:10.000Z`), mesmo
 *   formato do `completedAt` do React (`new Date().toISOString()`).
 * - [minutes]: só faz sentido para `type=focus`; `0` nos demais tipos.
 */
@Serializable
data class ActivityEntry(
    val id: String,
    val type: ActivityType,
    val source: ActivitySource,
    val refId: String,
    val title: String,
    val minutes: Int = 0,
    val xp: Int = 0,
    val gold: Int = 0,
    val at: String
)

@Serializable
enum class Difficulty {
    @SerialName("Trivial") Trivial,
    @SerialName("Easy") Easy,
    @SerialName("Medium") Medium,
    @SerialName("Hard") Hard
}

/**
 * Hábito da aba Rituais.
 *
 * Datas no formato `yyyy-MM-dd` (o mesmo de `HabitLogic`/`formatTodayDate`), fuso do
 * dispositivo.
 *
 * - [lastTriggeredDate]: **legado de save** (SPEC-Rebranding item 4). Continua no modelo
 *   para não quebrar saves antigos, mas **não é mais lida nem gravada**; em saves antigos
 *   seu valor é copiado para [lastUpDate] pela migração (`SaveMigrationLogic.migrateHabitDates`).
 * - [lastUpDate]: último dia em que o `+` foi registrado.
 * - [lastDownDate]: último dia em que o `−` foi registrado.
 *
 * Os dois são independentes: marcar `−` e depois `+` no mesmo dia deixa os dois
 * "acesos" (os botões da UI leem cada um por separado).
 */
@Serializable
data class Habit(
    val id: String,
    val title: String,
    val notes: String,
    val up: Boolean,
    val down: Boolean,
    val difficulty: Difficulty,
    val upCount: Int,
    val downCount: Int,
    val streak: Int,
    val tags: List<String>,
    val lastTriggeredDate: String? = null,
    val lastUpDate: String? = null,
    val lastDownDate: String? = null
) {
    /**
     * Único ponto de comparação "registrou `+` hoje?" (SPEC-Rebranding item 4).
     * [todayStr] precisa estar em `yyyy-MM-dd` (formato de [lastUpDate]); composable
     * nenhum compara data diretamente.
     */
    fun isUpToday(todayStr: String): Boolean = lastUpDate == todayStr

    /** Único ponto de comparação "registrou `−` hoje?". Mesmo formato de [isUpToday]. */
    fun isDownToday(todayStr: String): Boolean = lastDownDate == todayStr
}

@Serializable
enum class RepeatInterval {
    @SerialName("Daily") Daily,
    @SerialName("Weekly") Weekly,
    @SerialName("Monthly") Monthly
}

@Serializable
data class ChecklistItem(
    val id: String,
    val text: String,
    val completed: Boolean
)

@Serializable
data class Daily(
    val id: String,
    val title: String,
    val notes: String,
    val difficulty: Difficulty,
    val completed: Boolean,
    val streak: Int,
    val repeats: RepeatInterval,
    val every: Int,
    val tags: List<String>,
    val checklist: List<ChecklistItem>,
    val value: Int? = null,
    val createdAt: String? = null
)

@Serializable
data class Todo(
    val id: String,
    val title: String,
    val notes: String,
    val difficulty: Difficulty,
    val completed: Boolean,
    val tags: List<String>,
    val checklist: List<ChecklistItem>,
    val createdAt: String? = null,
    val completedAt: String? = null
)

@Serializable
enum class CharClass {
    @SerialName("Mage") Mage,
    @SerialName("Warrior") Warrior,
    @SerialName("Ranger") Ranger
}

@Serializable
enum class OrbConcept {
    @SerialName("A") A,
    @SerialName("B") B,
    @SerialName("C") C,
    @SerialName("D") D
}

@Serializable
data class PomodoroSettings(
    val focusDuration: Int,
    val shortBreakDuration: Int,
    val longBreakDuration: Int,
    val autoStartBreak: Boolean,
    val autoStartFocus: Boolean
)

@Serializable
data class CharacterState(
    val gold: Int,
    val totalXP: Int,
    val totalGoldEarned: Int,
    val totalSessions: Int,
    val totalMinutes: Int,
    val combatLevel: Int,
    val combatXP: Int,
    val skills: List<Skill>,
    val history: List<HistoryEntry>,
    val inventory: List<InventoryItem>,
    val streak: Int,
    val bestStreak: Int,
    val lastStudyDate: String?,
    val wildernessWins: Int,
    val combo: Int,
    val dungeonProgress: Int, // index of unlocked floor
    val isDungeonMode: Boolean,
    val dungeonSessions: Int,
    val achievements: List<String>, // achievement ids
    val charName: String,
    val charClass: CharClass,
    val todayXP: Int,
    val todayMinutes: Int,
    val todayDate: String,
    val hasClaimedLogin: Boolean,
    val hp: Int,
    val maxHp: Int,
    val habits: List<Habit>,
    val dailies: List<Daily>,
    val todos: List<Todo>,
    val equippedTitle: String? = null,
    val ownedTitles: List<String>? = null,
    val equippedEquipment: List<InventoryItem?>? = null,
    val pomodoroSettings: PomodoroSettings,
    val orbConcept: OrbConcept = OrbConcept.D,
    val lastDungeonClearedTime: Long = 0L,
    val isPlayerDead: Boolean = false,
    /** Feed persistido de atividades recentes (Spec F) — mais recente primeiro, máx. 20. */
    val recentActivity: List<ActivityEntry> = emptyList()
)

@Serializable
data class CharacterSummary(
    val charName: String,
    val charClass: CharClass,
    val equippedTitle: String? = null,
    val streak: Int,
    val bestStreak: Int,
    val totalMinutes: Int,
    val combatLevel: Int,
    val combatXP: Int,
    val hp: Int,
    val maxHp: Int,
    val isPlayerDead: Boolean = false
)

data class DailyReportData(
    val rewardAmount: Int,
    val currentStreak: Int,
    val streakLost: Boolean,
    val streakProtected: Boolean,
    val missedDailiesCount: Int,
    val damageTaken: Int,
    val allDailiesCompleted: Boolean
)

