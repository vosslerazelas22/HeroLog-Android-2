package com.iurispraecepta.herolog.ui.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Estado de cada pip do progresso da Masmorra (FR-009).
 */
enum class PipState {
    DONE,
    CURRENT,
    PENDING,
}

/**
 * Função pura testável (FR-009): `completed` 0..3 → `completed` DONE,
 * 1 CURRENT, resto PENDING. `completed = 4` → 4 DONE.
 * Semântica de *sessão atual* (D6): o CURRENT é a sessão em curso.
 */
fun dungeonPipStates(completed: Int): List<PipState> {
    if (completed >= 4) return List(4) { PipState.DONE }
    val safe = completed.coerceIn(0, 3)
    return List(4) { idx ->
        when {
            idx < safe -> PipState.DONE
            idx == safe -> PipState.CURRENT
            else -> PipState.PENDING
        }
    }
}

/**
 * Origem de `completed` por situação (FR-010 — só apresentação, o dado
 * `dungeonSessionsProgress` e o log "(n/4) de concluídas" não mudam):
 * - idle, Masmorra selecionada → `dungeonSessionsProgress`
 * - sessão rodando/pausada → `config.dungeonSessions`
 * - descanso (prep/ativo) → `lastSessionDungeonSessions + 1` (o campo guarda
 *   a contagem *antes* da sessão concluída — FocusSessionService.kt:476 —
 *   então soma 1 para corrigir a defasagem do status de descanso).
 */
fun dungeonStatusCompleted(
    isRunning: Boolean,
    isBreak: Boolean,
    dungeonSessionsProgress: Int,
    configDungeonSessions: Int,
    lastSessionDungeonSessions: Int,
): Int = when {
    isRunning -> configDungeonSessions
    isBreak -> (lastSessionDungeonSessions + 1).coerceAtMost(4)
    else -> dungeonSessionsProgress
}

/**
 * Pips do progresso da Masmorra, estilo Pomocat (D6): 4 posições.
 * - concluída = círculo cheio `accent`
 * - atual = anel + ponto (◎) `accent`
 * - pendente = contorno `accent` a 35% de alpha
 */
@Composable
fun DungeonPips(
    completed: Int,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    val states = dungeonPipStates(completed)
    val sessionNumber = (completed + 1).coerceAtMost(4)
    Row(
        modifier = modifier.semantics {
            contentDescription = "Sessão $sessionNumber de 4"
        },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        states.forEach { state ->
            when (state) {
                PipState.DONE -> Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(accent)
                )
                PipState.CURRENT -> Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, accent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(accent)
                    )
                }
                PipState.PENDING -> Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .border(1.dp, accent.copy(alpha = 0.35f), CircleShape)
                )
            }
        }
    }
}

/**
 * Substituto do `RaidModeInfoBox` (FR-007/FR-008): posicionado entre o timer
 * e o CTA/botões de sessão. Sem o botão "?" (a ajuda foi para o acordeão
 * do modal — D8) e, por decisão de produto (OQ-3), sem caixa de fundo.
 * Textos literais atuais mantidos.
 */
@Composable
fun ModeStatus(
    mode: RaidMode,
    completed: Int,
    dungeonOnCooldown: Boolean,
    cooldownText: String,
    lootChancePercent: Int,
    theme: ModeTheme,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        when (mode) {
            RaidMode.PADRAO -> {
                Text(
                    text = "🎯 Modo Padrão",
                    fontSize = 10.sp,
                    color = theme.statusMain,
                    modifier = Modifier.testTag("modeStatusMain")
                )
                Text(
                    text = "• Chance de Saque: $lootChancePercent%",
                    fontSize = 9.sp,
                    color = Color(0xFFA1A1AA)
                )
            }
            RaidMode.MASMORRA -> {
                Text(
                    text = "⚔️ Explorando Masmorra",
                    fontSize = 10.sp,
                    color = theme.statusMain,
                    modifier = Modifier.testTag("modeStatusMain")
                )
                if (completed >= 4 && dungeonOnCooldown) {
                    // FR-011: descanso pós-4ª com cooldown — ⏳ no lugar dos pips.
                    Text(
                        text = "⏳ $cooldownText",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = theme.accent
                    )
                } else {
                    DungeonPips(completed = completed, accent = theme.accent)
                }
                Text(
                    text = "Bônus +2.500 GP & Quad Loot",
                    fontSize = 9.sp,
                    color = Color(0xFFA1A1AA)
                )
            }
            RaidMode.SELVAGEM -> {
                Text(
                    text = "💀 Terra Selvagem Ativa",
                    fontSize = 10.sp,
                    color = theme.statusMain,
                    modifier = Modifier.testTag("modeStatusMain")
                )
                Text(
                    text = "Bônus +25% XP & GP",
                    fontSize = 9.sp,
                    color = Color(0xFFA1A1AA)
                )
            }
        }
    }
}
