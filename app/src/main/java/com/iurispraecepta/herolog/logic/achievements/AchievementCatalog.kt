package com.iurispraecepta.herolog.logic.achievements

object AchievementCatalog {

    val ACHIEVEMENTS_LIST: List<Achievement> = listOf(
        // ── Existentes (8) ────────────────────────────────────────────
        Achievement(
            id = "first_quest",
            name = "Primeira Incursão",
            desc = "Concluiu com êxito a primeira Missão de Foco.",
            icon = "⚔️",
            check = { s -> s.totalSessions >= 1 }
        ),
        Achievement(
            id = "streak_3",
            name = "Fagulha de Disciplina",
            desc = "Manteve uma série consecutiva de 3 dias de estudo.",
            icon = "🔥",
            check = { s -> s.bestStreak >= 3 }
        ),
        Achievement(
            id = "streak_7",
            name = "Inabalável",
            desc = "Conquistou a lendária marca de 7 dias focando em sequência.",
            icon = "🏆",
            check = { s -> s.bestStreak >= 7 }
        ),
        Achievement(
            id = "xp_1000",
            name = "Mestre Alfabetizado",
            desc = "Acumulou uma soma de 1000 pontos totais de XP.",
            icon = "📚",
            check = { s -> s.totalXP >= 1000 }
        ),
        Achievement(
            id = "xp_10000",
            name = "Sábio Iluminado",
            desc = "Superou a extraordinária marca de 10.000 pontos totais de XP.",
            icon = "🧙",
            check = { s -> s.totalXP >= 10000 }
        ),
        Achievement(
            id = "gp_1000",
            name = "Rico em Espólios",
            desc = "Armazenou nas arcas um patrimônio eterno de 1000 GP gulosamente.",
            icon = "💰",
            check = { s -> s.totalGoldEarned >= 1000 }
        ),
        Achievement(
            id = "sessions_10",
            name = "Veterano de Guerras",
            desc = "Concluiu um total de 10 sessões na Gilda dos Aventureiros.",
            icon = "🎖️",
            check = { s -> s.totalSessions >= 10 }
        ),
        Achievement(
            id = "survive_wilderness",
            name = "Superação Extrema",
            desc = "Sobreviveu à perigosa incursão sob o efeito de Wilderness.",
            icon = "💀",
            check = { s -> s.wildernessWins >= 1 }
        ),

        // ── Constância (5) ───────────────────────────────────────────
        Achievement(
            id = "streak_14",
            name = "Juramento de Quatorze Sóis",
            desc = "Manteve uma série consecutiva de 14 dias de estudo.",
            icon = "☀️",
            check = { s -> s.bestStreak >= 14 }
        ),
        Achievement(
            id = "streak_30",
            name = "Guardião do Mês",
            desc = "Manteve uma série consecutiva de 30 dias de estudo.",
            icon = "🛡️",
            check = { s -> s.bestStreak >= 30 }
        ),
        Achievement(
            id = "streak_60",
            name = "Sentinela das Sessenta Luas",
            desc = "Manteve uma série consecutiva de 60 dias de estudo.",
            icon = "🌙",
            check = { s -> s.bestStreak >= 60 }
        ),
        Achievement(
            id = "streak_100",
            name = "Centúria da Constância",
            desc = "Manteve uma série consecutiva de 100 dias de estudo.",
            icon = "🏛️",
            check = { s -> s.bestStreak >= 100 }
        ),
        Achievement(
            id = "streak_365",
            name = "Ano Inquebrável",
            desc = "Manteve uma série consecutiva de 365 dias de estudo.",
            icon = "♾️",
            check = { s -> s.bestStreak >= 365 }
        ),

        // ── Foco acumulado (6) ───────────────────────────────────────
        Achievement(
            id = "sessions_25",
            name = "Rota dos 25 Focos",
            desc = "Concluiu um total de 25 sessões de foco.",
            icon = "🧭",
            check = { s -> s.totalSessions >= 25 }
        ),
        Achievement(
            id = "sessions_50",
            name = "Cinquenta Chamados",
            desc = "Concluiu um total de 50 sessões de foco.",
            icon = "🔔",
            check = { s -> s.totalSessions >= 50 }
        ),
        Achievement(
            id = "sessions_100",
            name = "Centurião do Foco",
            desc = "Concluiu um total de 100 sessões de foco.",
            icon = "⚜️",
            check = { s -> s.totalSessions >= 100 }
        ),
        Achievement(
            id = "sessions_250",
            name = "Arquimago da Rotina",
            desc = "Concluiu um total de 250 sessões de foco.",
            icon = "🔮",
            check = { s -> s.totalSessions >= 250 }
        ),
        Achievement(
            id = "sessions_500",
            name = "Legião da Concentração",
            desc = "Concluiu um total de 500 sessões de foco.",
            icon = "⚡",
            check = { s -> s.totalSessions >= 500 }
        ),
        Achievement(
            id = "sessions_1000",
            name = "Mil Incursões",
            desc = "Concluiu um total de 1.000 sessões de foco.",
            icon = "🌟",
            check = { s -> s.totalSessions >= 1000 }
        ),

        // ── Tempo acumulado (3) ──────────────────────────────────────
        Achievement(
            id = "minutes_100",
            name = "Primeira Centena de Minutos Sagrados",
            desc = "Acumulou 100 minutos de foco no total.",
            icon = "⏳",
            check = { s -> s.totalMinutes >= 100 }
        ),
        Achievement(
            id = "minutes_600",
            name = "Dez Horas no Santuário",
            desc = "Acumulou 600 minutos de foco no total.",
            icon = "🕙",
            check = { s -> s.totalMinutes >= 600 }
        ),
        Achievement(
            id = "minutes_6000",
            name = "Cem Horas de Forja",
            desc = "Acumulou 6.000 minutos de foco no total.",
            icon = "🔨",
            check = { s -> s.totalMinutes >= 6000 }
        ),

        // ── XP acumulado (2) ─────────────────────────────────────────
        Achievement(
            id = "xp_50000",
            name = "Arquivo Vivo",
            desc = "Acumulou 50.000 XP no total.",
            icon = "📖",
            check = { s -> s.totalXP >= 50000 }
        ),
        Achievement(
            id = "xp_100000",
            name = "Conhecimento Transcendente",
            desc = "Acumulou 100.000 XP no total.",
            icon = "✨",
            check = { s -> s.totalXP >= 100000 }
        ),

        // ── GP acumulado (2) ─────────────────────────────────────────
        Achievement(
            id = "gp_10000",
            name = "Cofres da Guilda",
            desc = "Acumulou 10.000 GP obtidos no total.",
            icon = "🏦",
            check = { s -> s.totalGoldEarned >= 10000 }
        ),
        Achievement(
            id = "gp_50000",
            name = "Tesouro de Rei",
            desc = "Acumulou 50.000 GP obtidos no total.",
            icon = "👑",
            check = { s -> s.totalGoldEarned >= 50000 }
        ),

        // ── Combate (4) ──────────────────────────────────────────────
        Achievement(
            id = "combat_5",
            name = "Iniciado da Guilda",
            desc = "Alcançou Combat Level 5.",
            icon = "🗡️",
            check = { s -> s.combatLevel >= 5 }
        ),
        Achievement(
            id = "combat_10",
            name = "Cavaleiro da Guilda",
            desc = "Alcançou Combat Level 10.",
            icon = "🛡️",
            check = { s -> s.combatLevel >= 10 }
        ),
        Achievement(
            id = "combat_25",
            name = "Campeão da Guilda",
            desc = "Alcançou Combat Level 25.",
            icon = "🏅",
            check = { s -> s.combatLevel >= 25 }
        ),
        Achievement(
            id = "combat_50",
            name = "Ascensão Marcial",
            desc = "Alcançou Combat Level 50.",
            icon = "🐉",
            check = { s -> s.combatLevel >= 50 }
        ),

        // ── Skills (4) ───────────────────────────────────────────────
        Achievement(
            id = "skill_level_10",
            name = "Primeira Trilha Dominada",
            desc = "Levou qualquer skill ao nível 10.",
            icon = "🎯",
            check = { s -> s.skills.any { it.level >= 10 } }
        ),
        Achievement(
            id = "skill_level_25",
            name = "Mestre de Ofício",
            desc = "Levou qualquer skill ao nível 25.",
            icon = "⚒️",
            check = { s -> s.skills.any { it.level >= 25 } }
        ),
        Achievement(
            id = "skills_3_level_10",
            name = "Conselho das Três Artes",
            desc = "Ter três skills no nível 10 ou superior.",
            icon = "🎓",
            check = { s -> s.skills.count { it.level >= 10 } >= 3 }
        ),
        Achievement(
            id = "prestige_any",
            name = "Primeiro Prestígio",
            desc = "Alcançou ao menos um prestígio somando todas as skills.",
            icon = "💎",
            check = { s -> s.skills.any { (it.prestige ?: 0) >= 1 } }
        ),

        // ── Wilderness (3) ───────────────────────────────────────────
        Achievement(
            id = "wilderness_5",
            name = "Explorador do Ermo",
            desc = "Alcançou 5 vitórias em Wilderness.",
            icon = "🗺️",
            check = { s -> s.wildernessWins >= 5 }
        ),
        Achievement(
            id = "wilderness_25",
            name = "Andarilho Sem Retorno",
            desc = "Alcançou 25 vitórias em Wilderness.",
            icon = "🏕️",
            check = { s -> s.wildernessWins >= 25 }
        ),
        Achievement(
            id = "wilderness_50",
            name = "Lenda das Terras Selvagens",
            desc = "Alcançou 50 vitórias em Wilderness.",
            icon = "🐲",
            check = { s -> s.wildernessWins >= 50 }
        ),

        // ── Masmorras (3) ────────────────────────────────────────────
        Achievement(
            id = "dungeon_1",
            name = "Primeira Masmorra Selada",
            desc = "Progrediu ao menos um nível nas masmorras.",
            icon = "🏚️",
            check = { s -> s.dungeonProgress >= 1 }
        ),
        Achievement(
            id = "dungeon_10",
            name = "Dez Masmorras Seladas",
            desc = "Alcançou progresso 10 nas masmorras.",
            icon = "🏰",
            check = { s -> s.dungeonProgress >= 10 }
        ),
        Achievement(
            id = "dungeon_25",
            name = "Senhor das Profundezas",
            desc = "Alcançou progresso 25 nas masmorras.",
            icon = "🌋",
            check = { s -> s.dungeonProgress >= 25 }
        ),

        // ── Coleção (1) ──────────────────────────────────────────────
        Achievement(
            id = "titles_5",
            name = "Arsenal de Títulos",
            desc = "Possui cinco títulos.",
            icon = "📜",
            check = { s -> (s.ownedTitles?.size ?: 0) >= 5 }
        ),

        // ── Hábitos (1) ──────────────────────────────────────────────
        Achievement(
            id = "habit_streak_7",
            name = "Hábito Lapidado",
            desc = "Alcançou streak 7 em qualquer hábito.",
            icon = "💎",
            check = { s -> s.habits.any { it.streak >= 7 } }
        )
    )
}
