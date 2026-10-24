package com.iurispraecepta.herolog.data

import androidx.compose.ui.graphics.Color
import com.iurispraecepta.herolog.model.CharacterState

enum class TitleCategory { Common, Rare, Epic, Legendary, Achievement, Drop }

data class TitleVisualStyle(
    val borderColor: Color,
    val backgroundColor: Color,
    val textColor: Color,
    val glowTextColor: Color
)

data class TitleItem(
    val id: String,
    val name: String,
    val emoji: String,
    val category: TitleCategory,
    val price: Int? = null,
    val perks: List<String>? = null,
    val requirementText: String? = null,
    val dropChanceText: String? = null,
    val visualStyle: TitleVisualStyle,
    val checkUnlocked: ((CharacterState) -> Boolean)? = null
)

// --- Estilos reutilizáveis por categoria ---

private val CommonStyle = TitleVisualStyle(
    borderColor = Color(0xFF44403C).copy(alpha = 0.5f),   // stone-700/50
    backgroundColor = Color(0xFF1C1917).copy(alpha = 0.4f), // stone-900/40
    textColor = Color(0xFFD6D3D1),                          // stone-300
    glowTextColor = Color(0xFFFDE68A).copy(alpha = 0.8f)    // amber-200/80
)

private val RareStyle = TitleVisualStyle(
    borderColor = Color(0xFFD97706).copy(alpha = 0.3f),     // amber-600/30
    backgroundColor = Color(0xFF451A03).copy(alpha = 0.1f), // amber-950/10
    textColor = Color(0xFFFEF3C7),                           // amber-100
    glowTextColor = Color(0xFFFCD34D)                        // amber-300
)

private val EpicStyle = TitleVisualStyle(
    borderColor = Color(0xFFA855F7).copy(alpha = 0.2f),     // purple-500/20
    backgroundColor = Color(0xFF3B0764).copy(alpha = 0.1f), // purple-950/10
    textColor = Color(0xFFF3E8FF),                           // purple-100
    glowTextColor = Color(0xFFD8B4FE)                         // purple-300
)

private val LegendaryWatcherStyle = TitleVisualStyle(
    borderColor = Color(0xFFF97316).copy(alpha = 0.3f),     // orange-500/30
    backgroundColor = Color(0xFF431407).copy(alpha = 0.1f), // orange-950/10
    textColor = Color(0xFFFFEDD5),                            // orange-100
    glowTextColor = Color(0xFFFDBA74)                         // orange-300
)

private val LegendaryEternalScholarStyle = TitleVisualStyle(
    borderColor = Color(0xFFCA8A04).copy(alpha = 0.3f),     // yellow-600/30
    backgroundColor = Color(0xFF422006).copy(alpha = 0.1f), // yellow-950/10
    textColor = Color(0xFFFEF9C3),                            // yellow-100 (corrigindo o typo yellow-105 do React)
    glowTextColor = Color(0xFFFACC15)                         // yellow-400
)

private val AchievementStyle = TitleVisualStyle(
    borderColor = Color(0xFF4E3C28).copy(alpha = 0.7f),
    backgroundColor = Color(0xFF251E16),
    textColor = Color(0xFFCCBDA8),
    glowTextColor = Color(0xFFFEF3C7).copy(alpha = 0.9f) // fallback padrão (amber-100/90)
)

val TITLE_CATALOG: List<TitleItem> = listOf(

    // ============ COMMON (150-500 GP) — 8 títulos ============
    TitleItem(
        id = "APPRENTICE", name = "Aprendiz", emoji = "⭐",
        category = TitleCategory.Common, price = 150, visualStyle = CommonStyle
    ),
    TitleItem(
        id = "SCHOLAR", name = "Erudito", emoji = "📖",
        category = TitleCategory.Common, price = 200, visualStyle = CommonStyle
    ),
    TitleItem(
        id = "GRINDER", name = "Incansável", emoji = "⚔️",
        category = TitleCategory.Common, price = 200, visualStyle = CommonStyle
    ),
    TitleItem(
        id = "SCRIBE", name = "Escriba", emoji = "📝",
        category = TitleCategory.Common, price = 250, visualStyle = CommonStyle
    ),
    TitleItem(
        id = "ARCANE", name = "Arcanista", emoji = "🔮",
        category = TitleCategory.Common, price = 300, visualStyle = CommonStyle
    ),
    TitleItem(
        id = "WANDERER", name = "Andarilho", emoji = "🌿",
        category = TitleCategory.Common, price = 300, visualStyle = CommonStyle
    ),
    TitleItem(
        id = "SEEKER", name = "Buscador", emoji = "🔍",
        category = TitleCategory.Common, price = 400, visualStyle = CommonStyle
    ),
    TitleItem(
        id = "NIGHT_OWL", name = "Coruja Noturna", emoji = "🦉",
        category = TitleCategory.Common, price = 500, visualStyle = CommonStyle
    ),

    // ============ RARE (1.500-6.000 GP) — 6 títulos ============
    TitleItem(
        id = "CHAMPION", name = "Campeão", emoji = "👑",
        category = TitleCategory.Rare, price = 1500, visualStyle = RareStyle
    ),
    TitleItem(
        id = "DUELIST", name = "Duelista", emoji = "🗡️",
        category = TitleCategory.Rare, price = 2000, visualStyle = RareStyle
    ),
    TitleItem(
        id = "STORMBORN", name = "Nascido da Tempestade", emoji = "⛈️",
        category = TitleCategory.Rare, price = 2500, visualStyle = RareStyle
    ),
    TitleItem(
        id = "ALCHEMIST", name = "Alquimista", emoji = "🧪",
        category = TitleCategory.Rare, price = 3000, visualStyle = RareStyle
    ),
    TitleItem(
        id = "ILLUMINATED", name = "Iluminado", emoji = "💡",
        category = TitleCategory.Rare, price = 4000, visualStyle = RareStyle
    ),
    TitleItem(
        id = "WARLORD", name = "Senhor da Guerra", emoji = "🔱",
        category = TitleCategory.Rare, price = 6000, visualStyle = RareStyle
    ),

    // ============ EPIC (10.000-50.000 GP) — 5 títulos ============
    TitleItem(
        id = "LEGEND_GP", name = "Lenda", emoji = "💀",
        category = TitleCategory.Epic, price = 10000,
        perks = listOf("+5% de XP em todas as sessões"),
        visualStyle = EpicStyle
    ),
    TitleItem(
        id = "INFERNO", name = "Inferno", emoji = "🔥",
        category = TitleCategory.Epic, price = 15000,
        perks = listOf("+8% de XP · Eventos multiplicadores duram 15% mais"),
        visualStyle = EpicStyle
    ),
    TitleItem(
        id = "STARBOUND", name = "Nascido das Estrelas", emoji = "☀️",
        category = TitleCategory.Epic, price = 22000,
        perks = listOf("+10% de XP · +8% de ouro de todas as fontes"),
        visualStyle = EpicStyle
    ),
    TitleItem(
        id = "DRAGONBORN", name = "Dragonborn", emoji = "🐉",
        category = TitleCategory.Epic, price = 35000,
        perks = listOf("+15% de XP · Bônus de XP de Prestígio 25% mais fortes"),
        visualStyle = EpicStyle
    ),
    TitleItem(
        id = "VOIDWALKER", name = "Caminhante do Vazio", emoji = "🌌",
        category = TitleCategory.Epic, price = 50000,
        perks = listOf("+20% de XP · +50% de chance de obter saques e títulos"),
        visualStyle = EpicStyle
    ),

    // ============ LEGENDARY (100.000-500.000 GP) — 3 títulos ============
    TitleItem(
        id = "THE_WATCHER", name = "O Vigilante", emoji = "👁️",
        category = TitleCategory.Legendary, price = 100000,
        perks = listOf("+25% de XP · +15% de ouro de todas as fontes"),
        visualStyle = LegendaryWatcherStyle
    ),
    TitleItem(
        id = "TRANSCENDENT", name = "Transcendente", emoji = "♾️",
        category = TitleCategory.Legendary, price = 250000,
        perks = listOf("+30% de XP · +20% de ouro · Chance de obtenção de saques em dobro"),
        visualStyle = LegendaryWatcherStyle
    ),
    TitleItem(
        id = "THE_ETERNAL_SCHOLAR", name = "O Erudito Eterno", emoji = "🌠",
        category = TitleCategory.Legendary, price = 500000,
        perks = listOf("+50% de XP · +30% de ouro · Bônus de Prestígio 50% mais fortes"),
        visualStyle = LegendaryEternalScholarStyle
    ),

    // ============ ACHIEVEMENT (marcos de conquista) — 18 títulos ============
    TitleItem(
        id = "IRON_WILL", name = "Vontade de Ferro", emoji = "🔥",
        category = TitleCategory.Achievement,
        perks = listOf("+5% de XP em todas as sessões"),
        requirementText = "30-Day streak",
        checkUnlocked = { state -> state.bestStreak >= 30 },
        visualStyle = AchievementStyle
    ),
    TitleItem(
        id = "DIAMOND_MIND", name = "Mente de Diamante", emoji = "💎",
        category = TitleCategory.Achievement,
        perks = listOf("+8% de XP · +5% de ouro obtido"),
        requirementText = "60-Day streak",
        checkUnlocked = { state -> state.bestStreak >= 60 },
        visualStyle = AchievementStyle
    ),
    TitleItem(
        id = "THE_CENTURY", name = "O Centenário", emoji = "💯",
        category = TitleCategory.Achievement,
        perks = listOf("+12% de XP · +8% de ouro obtido"),
        requirementText = "100-Day streak",
        checkUnlocked = { state -> state.bestStreak >= 100 },
        visualStyle = AchievementStyle
    ),
    TitleItem(
        id = "A_FULL_YEAR", name = "Um Ano Inteiro", emoji = "☀️",
        category = TitleCategory.Achievement,
        perks = listOf("+25% de XP · +15% de ouro obtido"),
        requirementText = "365-Day streak",
        checkUnlocked = { state -> state.bestStreak >= 365 },
        visualStyle = AchievementStyle
    ),
    TitleItem(
        id = "CENTURION", name = "Centurião", emoji = "⚔️",
        category = TitleCategory.Achievement,
        perks = listOf("+5% de XP em todas as sessões"),
        requirementText = "100 sessions",
        checkUnlocked = { state -> state.totalSessions >= 100 },
        visualStyle = AchievementStyle
    ),
    TitleItem(
        id = "THE_OBSESSED", name = "O Obcecado", emoji = "💫",
        category = TitleCategory.Achievement,
        perks = listOf("+15% de XP · +10% de ouro obtido"),
        requirementText = "1,000 sessions",
        checkUnlocked = { state -> state.totalSessions >= 1000 },
        visualStyle = AchievementStyle
    ),
    TitleItem(
        id = "IMMORTAL_SCHOLAR", name = "Erudito Imortal", emoji = "🏛️",
        category = TitleCategory.Achievement,
        perks = listOf("+25% de XP · +20% de ouro · +50% de chance de obtenção de saques"),
        requirementText = "2,500 sessions",
        checkUnlocked = { state -> state.totalSessions >= 2500 },
        visualStyle = AchievementStyle
    ),
    TitleItem(
        id = "DEATH_PROOF", name = "Imune à Morte", emoji = "💀",
        category = TitleCategory.Achievement,
        perks = listOf("+25% de XP na Terra Selvagem · Imunidade à morte na Terra Selvagem"),
        requirementText = "Survive 50 Wilderness",
        checkUnlocked = { state -> state.wildernessWins >= 50 },
        visualStyle = AchievementStyle
    ),
    TitleItem(
        id = "DUNGEON_LORD", name = "Senhor das Masmorras", emoji = "🏰",
        category = TitleCategory.Achievement,
        perks = listOf("+15% de XP nas masmorras"),
        requirementText = "Clear 10 dungeons",
        checkUnlocked = { state -> state.dungeonProgress >= 10 },
        visualStyle = AchievementStyle
    ),
    TitleItem(
        id = "RAID_VETERAN", name = "Veterano das Incursões", emoji = "🔱",
        category = TitleCategory.Achievement,
        perks = listOf("+25% de XP nas masmorras · +20% de ouro nas recompensas de masmorra"),
        requirementText = "Clear 25 dungeons",
        checkUnlocked = { state -> state.dungeonProgress >= 25 },
        visualStyle = AchievementStyle
    ),
    TitleItem(
        id = "LEGEND_ACH", name = "Lenda", emoji = "👑",
        category = TitleCategory.Achievement,
        perks = listOf("+10% de XP em todas as sessões"),
        requirementText = "Level 99 in any skill",
        checkUnlocked = { state -> state.skills.any { it.level >= 99 } },
        visualStyle = AchievementStyle
    ),
    TitleItem(
        id = "PANTHEON", name = "Panteão", emoji = "🌠",
        category = TitleCategory.Achievement,
        perks = listOf("+20% de XP · Bônus de XP de Prestígio 30% mais fortes"),
        requirementText = "Level 99 in 5 skills",
        checkUnlocked = { state -> state.skills.count { it.level >= 99 } >= 5 },
        visualStyle = AchievementStyle
    ),
    TitleItem(
        id = "MANIAC", name = "Maníaco", emoji = "⚡",
        category = TitleCategory.Achievement,
        perks = listOf("+10% de XP · Eventos multiplicadores duram 30% mais"),
        requirementText = "50-Session combo",
        checkUnlocked = { state -> state.combo >= 50 },
        visualStyle = AchievementStyle
    ),
    TitleItem(
        id = "IN_THE_ZONE", name = "No Ritmo", emoji = "🌀",
        category = TitleCategory.Achievement,
        perks = listOf("+5% de XP · Eventos multiplicadores duram 20% mais"),
        requirementText = "30-Session combo",
        checkUnlocked = { state -> state.combo >= 30 },
        visualStyle = AchievementStyle
    ),
    TitleItem(
        id = "MARATHONER", name = "Maratonista", emoji = "🏃",
        category = TitleCategory.Achievement,
        perks = listOf("+15% de XP em sessões com mais de 60 minutos"),
        requirementText = "10 sessions of 90+ min",
        checkUnlocked = { state -> state.history.count { it.duration >= 90 } >= 10 },
        visualStyle = AchievementStyle
    ),
    TitleItem(
        id = "XP_GOD", name = "Deus do XP", emoji = "💥",
        category = TitleCategory.Achievement,
        perks = listOf("+20% de XP · +10% de ouro obtido"),
        requirementText = "5,000,000 total XP",
        checkUnlocked = { state -> state.totalXP >= 5000000 },
        visualStyle = AchievementStyle
    ),
    TitleItem(
        id = "NOCTURNAL", name = "Noturno", emoji = "🌙",
        category = TitleCategory.Achievement,
        perks = listOf("+15% de XP · +30% de chance de obter saques e títulos"),
        requirementText = "500 hours studied",
        checkUnlocked = { state -> state.totalMinutes >= 30000 }, // 500 horas * 60 min
        visualStyle = AchievementStyle
    ),
    TitleItem(
        id = "ASCENDED", name = "Ascendido", emoji = "♾️",
        category = TitleCategory.Achievement,
        perks = listOf("+10% de XP · Bônus de XP de Prestígio 50% mais fortes"),
        requirementText = "Prestige 10 times",
        checkUnlocked = { state -> state.skills.sumOf { it.prestige ?: 0 } >= 10 },
        visualStyle = AchievementStyle
    ),

    // ============ DROP (títulos raros de loot) — 7 títulos ============
    // Nota: dropChanceText permanece em inglês (sem tradução definida).
    TitleItem(
        id = "BLESSED", name = "Abençoado", emoji = "🌸",
        category = TitleCategory.Drop,
        perks = listOf("+8% de XP · +10% de ouro de todas as fontes"),
        dropChanceText = "ULTRA-RARE SESSION DROP",
        visualStyle = TitleVisualStyle(
            borderColor = Color(0xFF92400E).copy(alpha = 0.35f),
            backgroundColor = Color(0xFF451A03).copy(alpha = 0.30f),
            textColor = Color(0xFFFEF3C7),
            glowTextColor = Color(0xFFFEF3C7).copy(alpha = 0.90f)
        )
    ),
    TitleItem(
        id = "SHADOW", name = "Sombra", emoji = "🌑",
        category = TitleCategory.Drop,
        perks = listOf("+10% de XP · +75% de chance de obter saques e títulos"),
        dropChanceText = "ULTRA-RARE SESSION DROP",
        visualStyle = TitleVisualStyle(
            borderColor = Color(0xFF1E293B).copy(alpha = 0.70f),
            backgroundColor = Color(0xFF0F172A).copy(alpha = 0.40f),
            textColor = Color(0xFFF1F5F9),
            glowTextColor = Color(0xFFFEF3C7).copy(alpha = 0.90f)
        )
    ),
    TitleItem(
        id = "THE_FORSAKEN", name = "O Abandonado", emoji = "🔮",
        category = TitleCategory.Drop,
        perks = listOf("+15% de XP na Terra Selvagem · Imunidade à morte na Terra Selvagem"),
        dropChanceText = "EXTREMELY RARE SESSION DROP",
        visualStyle = TitleVisualStyle(
            borderColor = Color(0xFF581C87).copy(alpha = 0.50f),
            backgroundColor = Color(0xFF3B0764).copy(alpha = 0.30f),
            textColor = Color(0xFFF3E8FF),
            glowTextColor = Color(0xFFFEF3C7).copy(alpha = 0.90f)
        )
    ),
    TitleItem(
        id = "CELESTIAL", name = "Celestial", emoji = "✨",
        category = TitleCategory.Drop,
        perks = listOf("+20% de XP · +15% de ouro · Chance de obtenção de saques em dobro"),
        dropChanceText = "RAREST OF ALL DROP TITLES",
        visualStyle = TitleVisualStyle(
            borderColor = Color(0xFFEAB308).copy(alpha = 0.40f),
            backgroundColor = Color(0xFF422006).copy(alpha = 0.20f),
            textColor = Color(0xFFFEF3C7),
            glowTextColor = Color(0xFFFEF3C7).copy(alpha = 0.90f)
        )
    ),
    TitleItem(
        id = "THUNDERSTRUCK", name = "Atingido pelo Trovão", emoji = "⚡",
        category = TitleCategory.Drop,
        perks = listOf("+25% de XP na Terra Selvagem · Imunidade à morte · +10% de ouro"),
        dropChanceText = "RARE WILDERNESS-ONLY DROP",
        visualStyle = TitleVisualStyle(
            borderColor = Color(0xFFF59E0B).copy(alpha = 0.30f),
            backgroundColor = Color(0xFF1E293B).copy(alpha = 0.40f),
            textColor = Color(0xFFFEF3C7),
            glowTextColor = Color(0xFFFEF3C7).copy(alpha = 0.90f)
        )
    ),
    TitleItem(
        id = "HAUNTED", name = "Assombrado", emoji = "👻",
        category = TitleCategory.Drop,
        perks = listOf("+10% de XP · +20% de ouro de todas as fontes"),
        dropChanceText = "RARE WILDERNESS-ONLY DROP",
        visualStyle = TitleVisualStyle(
            borderColor = Color(0xFFA855F7).copy(alpha = 0.30f),
            backgroundColor = Color(0xFF0F172A).copy(alpha = 0.30f),
            textColor = Color(0xFFF3E8FF),
            glowTextColor = Color(0xFFFEF3C7).copy(alpha = 0.90f)
        )
    ),
    TitleItem(
        id = "BLOOD_FORGED", name = "Forjado em Sangue", emoji = "🩸",
        category = TitleCategory.Drop,
        perks = listOf("+20% de XP nas masmorras · +20% de ouro nas recompensas de masmorra"),
        dropChanceText = "DUNGEON LOOT ONLY",
        visualStyle = TitleVisualStyle(
            borderColor = Color(0xFF4C0519), // sólido, sem alpha — igual ao React
            backgroundColor = Color(0xFF450A0A).copy(alpha = 0.30f),
            textColor = Color(0xFFFFE4E6),
            glowTextColor = Color(0xFFFEF3C7).copy(alpha = 0.90f)
        )
    )
)