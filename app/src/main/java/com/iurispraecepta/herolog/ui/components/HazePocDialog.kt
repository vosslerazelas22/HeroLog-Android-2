package com.iurispraecepta.herolog.ui.components

import androidx.activity.compose.BackHandler
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect

/**
 * TEMPORÁRIO — PoC do teste decisório de Haze (spec-002 §6, Opção A).
 *
 * Dialog de teste que compartilha o mesmo [HazeState] da tela raiz (via [LocalHazeState])
 * e aplica `hazeEffect` ao backdrop, com tint stone-950 translúcido e blur de 2dp.
 * NÃO alterar modais de produção aqui; este arquivo deve ser DELETADO após a decisão
 * arquitetural (Opção A adotada ou descartada em favor da Opção B).
 *
 * Roteiro de validação (device real, spec-002 §6): abrir sobre conteúdo visual
 * reconhecível (AppHeader, cards coloridos, bottom nav), capturar em 375×667 e
 * 390×844, na API mínima e em API 31+, checar blur suave da cena sem borrar o
 * painel, sem recorte/deslocamento, + observar jank com BottomNav e backdrop
 * consumindo o mesmo HazeState. Repetir com backdrop close e back close
 * desativados (toggles internos).
 */
val LocalHazeState = compositionLocalOf<HazeState?> { null }

private val Stone950 = Color(0xFF0C0A09)
private val Stone900 = Color(0xFF1C1917)
private val Stone200 = Color(0xFFE7E5E4)
private val Stone400 = Color(0xFFA8A29E)
private val Champagne400 = Color(0xFFE5C158)

private const val HAZE_POC_TAG = "HazePoC"

// TEMP spec-002: candidatos de raio para calibração visual contra o React
// (backdrop-blur 2px, degrau mínimo da escala). O vencedor vira constante.
private val POC_RADIUS_CANDIDATES = listOf(0.5f, 1f, 1.5f, 2f)
private const val POC_NOISE_REFERENCE = 0.15f // default do HazeStyle, p/ comparação

@Composable
fun HazePocDialog(
    isOpen: Boolean,
    onClose: () -> Unit,
    allowBackdropClose: Boolean = true,
    disableEscClose: Boolean = false,
    onAllowBackdropCloseChange: (Boolean) -> Unit = {},
    onDisableEscCloseChange: (Boolean) -> Unit = {},
) {
    if (!isOpen) return

    // TEMP spec-002: breadcrumbs p/ diagnosticar o Voltar no device (logcat: HazePoC).
    // Teste-controle: repetir o Voltar no IncursionModeModal de produção na mesma sessão
    // (sistêmico vs. PoC-específico).
    Log.d(HAZE_POC_TAG, "composing isOpen=$isOpen disableEscClose=$disableEscClose allowBackdropClose=$allowBackdropClose")
    BackHandler(enabled = !disableEscClose) {
        Log.d(HAZE_POC_TAG, "BackHandler fired (disableEscClose=$disableEscClose), closing")
        onClose()
    }

    // TEMP spec-002: calibração de intensidade. Baseline noiseFactor=0f (HazeStyle usa
    // 0.15 de grain por default, provado no bytecode do 1.6.10 — mais intenso que o CSS).
    var radiusDp by remember { mutableStateOf(1f) }
    var noise by remember { mutableStateOf(0f) }
    Log.d(HAZE_POC_TAG, "style radius=${radiusDp}dp noise=$noise")

    val hazeState = LocalHazeState.current

    Dialog(
        onDismissRequest = {
            if (allowBackdropClose) onClose()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false, // BackHandler manual é a única fonte de verdade
            dismissOnClickOutside = allowBackdropClose
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Backdrop PoC: hazeEffect sobre a cena + tint stone-950.
            // Se o HazeState não estiver disponível, cai para o fundo chapado
            // (mesmo comportamento do HeroLogModal atual) para o dialog continuar funcional.
            var backdropModifier = Modifier.fillMaxSize()
            if (hazeState != null) {
                backdropModifier = backdropModifier.hazeEffect(
                    state = hazeState,
                    style = HazeStyle(
                        backgroundColor = Stone950.copy(alpha = 0.8f),
                        tint = null,
                        blurRadius = radiusDp.dp,
                        noiseFactor = noise
                    )
                )
            } else {
                backdropModifier = backdropModifier.background(Stone950.copy(alpha = 0.8f))
            }
            if (allowBackdropClose) {
                backdropModifier = backdropModifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    onClose()
                }
            }
            Box(modifier = backdropModifier)

            // Painel de diagnóstico — propositalmente simples, deve permanecer NÍTIDO
            // enquanto a cena atrás desfoca. Toggles alteram as flags do caller ao vivo,
            // permitindo o roteiro do spec-002 §6.5 sem fechar/reabrir pelo código.
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .widthIn(max = 448.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Stone900)
                    .border(1.dp, Champagne400.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Haze PoC — Opção A (temp)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Champagne400
                    )
                    Text(
                        text = if (hazeState != null) {
                            "HazeState compartilhado: ON — a cena atrás (header, cards, bottom nav) deveria aparecer desfocada. Este painel deve ficar nítido."
                        } else {
                            "HazeState compartilhado: OFF (fallback chapado) — verifique o CompositionLocal."
                        },
                        fontSize = 13.sp,
                        color = Stone200
                    )
                    Text(
                        text = "Raio (alvo: quase imperceptível, degrau mínimo):",
                        fontSize = 12.sp,
                        color = Stone400
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        POC_RADIUS_CANDIDATES.forEach { candidate ->
                            val selected = candidate == radiusDp
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (selected) Champagne400.copy(alpha = 0.25f)
                                        else Stone950
                                    )
                                    .border(
                                        1.dp,
                                        if (selected) Champagne400 else Stone400.copy(alpha = 0.4f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { radiusDp = candidate }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$candidate",
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) Champagne400 else Stone200
                                )
                            }
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = noise != 0f,
                            onCheckedChange = { on -> noise = if (on) POC_NOISE_REFERENCE else 0f }
                        )
                        Text(
                            text = "Grain 0.15 (default Haze, p/ referência)",
                            fontSize = 12.sp,
                            color = Stone400
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(checked = allowBackdropClose, onCheckedChange = onAllowBackdropCloseChange)
                        Text(
                            text = "Backdrop fecha",
                            fontSize = 12.sp,
                            color = Stone400
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(checked = disableEscClose, onCheckedChange = onDisableEscCloseChange)
                        Text(
                            text = "Bloquear Voltar",
                            fontSize = 12.sp,
                            color = Stone400
                        )
                    }
                    Text(
                        text = "Feche por backdrop, botão e Voltar; repita com as flags bloqueadas.",
                        fontSize = 11.sp,
                        color = Stone400
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Champagne400.copy(alpha = 0.2f))
                            .border(1.dp, Champagne400.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .clickable(onClick = onClose)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Fechar", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Champagne400)
                    }
                }
            }
        }
    }
}
