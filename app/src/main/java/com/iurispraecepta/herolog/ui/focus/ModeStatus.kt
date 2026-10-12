package com.iurispraecepta.herolog.ui.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.R
import com.iurispraecepta.herolog.ui.theme.Cinzel

/**
 * Ponto de controle único do experimento da caixa do mockup (FR-106, P6).
 * `true` = renderiza borda/fundo do mockup; decisão final após device.
 */
const val MODE_STATUS_BOXED = true

private const val MODE_STATUS_BOX_BORDER_ALPHA = 0.30f
private const val MODE_STATUS_BOX_BG_ALPHA = 0.08f

/**
 * Estado de cada pip do progresso da Masmorra (FR-107/FR-108).
 */
enum class PipState {
    DONE,
    CURRENT,
    PENDING,
}

/**
 * Função pura testável (FR-108 — modelo único, P9):
 * - `showCurrent = true` (sessão rodando/pausada): `completed` DONE, 1 CURRENT,
 *   resto PENDING; `completed >= 4` → 4 DONE; `completed` com `coerceIn(0, 3)`.
 * - `showCurrent = false` (ocioso, descanso, completion flow): `completed`
 *   `coerceIn(0, 4)` DONE, resto PENDING; nunca CURRENT.
 * `showCurrent` sem valor padrão: cada call site decide explicitamente (FR-108).
 */
fun dungeonPipStates(completed: Int, showCurrent: Boolean): List<PipState> {
    if (!showCurrent) {
        val safe = completed.coerceIn(0, 4)
        return List(4) { idx -> if (idx < safe) PipState.DONE else PipState.PENDING }
    }
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
 * Origem de `completed` por situação (FR-010 da spec original — só apresentação):
 * - idle, Masmorra selecionada → `dungeonSessionsProgress`
 * - sessão rodando/pausada → `config.dungeonSessions`
 * - descanso (prep/ativo) → `lastSessionDungeonSessions + 1` (o campo guarda
 *   a contagem *antes* da sessão concluída — FocusSessionService.kt:476).
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
 * Pips do progresso da Masmorra, estilo Pomocat (D6 — visual inalterado):
 * - concluída = círculo cheio `accent`
 * - em andamento = anel + ponto (◎) `accent`
 * - pendente = contorno `accent` a 35% de alpha
 *
 * Acessibilidade (FR-110): `showCurrent = true` descreve a sessão em andamento;
 * `showCurrent = false` descreve as concluídas.
 */
@Composable
fun DungeonPips(
    completed: Int,
    accent: Color,
    showCurrent: Boolean,
    modifier: Modifier = Modifier,
) {
    val states = dungeonPipStates(completed, showCurrent)
    val description = if (showCurrent) {
        val sessionNumber = (completed + 1).coerceAtMost(4)
        "Sessão $sessionNumber de 4 em andamento"
    } else {
        "${completed.coerceIn(0, 4)} de 4 concluídas"
    }
    Row(
        modifier = modifier.semantics {
            contentDescription = description
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
 * Status do modo da Câmara de Foco (FR-101–FR-106, P1/P3–P6).
 *
 * Layout do mockup "Câmara de Foco — Terra Selvagem": linha (ícone Lucide 18dp +
 * coluna de texto), bloco centralizado na tela. Título em Cinzel Bold 12sp com
 * `letterSpacing` 1sp; subtítulo 11sp `#A1A1AA`. Sem `.uppercase()`: Cinzel é
 * unicase e já renderiza em caixa-alta.
 *
 * @param inProgress `true` somente com sessão rodando/pausada (◎ visível);
 * descanso e ocioso passam `false` (FR-109). Sem valor padrão.
 * @param boxed experimento da caixa do mockup (FR-106); default [MODE_STATUS_BOXED].
 */
@Composable
fun ModeStatus(
    mode: RaidMode,
    completed: Int,
    dungeonOnCooldown: Boolean,
    cooldownText: String,
    lootChancePercent: Int,
    theme: ModeTheme,
    inProgress: Boolean,
    boxed: Boolean = MODE_STATUS_BOXED,
    modifier: Modifier = Modifier,
) {
    val iconRes = when (mode) {
        RaidMode.PADRAO -> R.drawable.lucide_ic_target
        RaidMode.MASMORRA -> R.drawable.lucide_ic_swords
        RaidMode.SELVAGEM -> R.drawable.lucide_ic_skull
    }
    val title = when (mode) {
        RaidMode.PADRAO -> "Modo Padrão"
        RaidMode.MASMORRA -> "Explorando Masmorra"
        RaidMode.SELVAGEM -> "Terra Selvagem Ativa"
    }
    val subtitle = when (mode) {
        RaidMode.PADRAO -> "• Chance de Saque: $lootChancePercent%"
        RaidMode.MASMORRA -> "Bônus +2.500 GP & Quad Loot"
        RaidMode.SELVAGEM -> "Bônus +25% XP & GP"
    }
    val showCooldown = mode == RaidMode.MASMORRA && completed >= 4 && dungeonOnCooldown

    var container = modifier.fillMaxWidth()
    if (boxed) {
        container = container
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(theme.accentDim.copy(alpha = MODE_STATUS_BOX_BG_ALPHA))
            .border(
                1.dp,
                theme.accent.copy(alpha = MODE_STATUS_BOX_BORDER_ALPHA),
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .heightIn(min = 50.dp)
    }
    Row(
        modifier = container,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(
                    if (showCooldown) R.drawable.lucide_ic_hourglass else iconRes
                ),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (showCooldown) theme.accent else theme.statusMain
            )
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (showCooldown) {
                    // FR-011/FR-102: descanso pós-4ª com cooldown — timer no lugar dos pips.
                    Text(
                        text = cooldownText,
                        fontFamily = Cinzel,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp,
                        color = theme.accent,
                        modifier = Modifier.testTag("modeStatusMain")
                    )
                } else {
                    Text(
                        text = title,
                        fontFamily = Cinzel,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp,
                        color = theme.statusMain,
                        modifier = Modifier.testTag("modeStatusMain")
                    )
                }
                if (mode == RaidMode.MASMORRA && !showCooldown) {
                    DungeonPips(
                        completed = completed,
                        accent = theme.accent,
                        showCurrent = inProgress
                    )
                }
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color(0xFFA1A1AA)
                )
            }
        }
    }
}
