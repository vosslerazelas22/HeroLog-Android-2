package com.iurispraecepta.herolog.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.iurispraecepta.herolog.ui.theme.Cinzel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect
import kotlinx.coroutines.delay

enum class ModalVariant { Amber, Purple, Red }

/**
 * Fonte do blur de backdrop (spec-002, Opção A adotada em device 09/2026).
 * Fornecido na raiz (`MainActivity`), consumido aqui e lido pelos dialogs —
 * evita propagar `HazeState` por ~15 call sites. Nulo em previews/testes, onde o
 * backdrop cai para o fundo chapado (mesmo visual de antes do Haze).
 */
val LocalHazeState = compositionLocalOf<HazeState?> { null }

/** Tokens do chrome do modal (spec-002 §4) — sem literais mágicos inline. */
private object ModalTokens {
    val PanelMaxWidth = 448.dp // max-w-md
    val OuterMargin = 16.dp // p-4
    const val ContentMaxHeightFraction = 0.8f // max-h-[80vh]: limite DO CONTEÚDO
    val BackdropBlurRadius = 0.25.dp // calibrado em device vs React blur(2px), 09/2026
    const val BackdropNoiseFactor = 0f // HazeStyle usa 0.15 default (grain); CSS é limpo
    const val ExitAnimDurationMs = 220L // tween 220ms da animação de saída
    // Passo 3 (§4): header mobile + título mobile. Breakpoint sm (p-5, text-lg, p-6)
    // deliberadamente ausente — só entra se confirmado necessário em device.
    val HeaderHorizontalPadding = 16.dp // p-4 mobile
    val TitleFontSize = 16.sp // text-base mobile
    val PanelElevation = 20.dp // shadow-xl, sem halo (escuro sobre escuro)
    val ContentDefaultColor = Color(0xFFE7E5E4) // text-stone-200 herdado pelo slot
}

// Hex tokens
private val Stone950 = Color(0xFF0C0A09)
private val Stone900 = Color(0xFF1C1917)
private val Stone800 = Color(0xFF292524)
private val Stone400 = Color(0xFFA8A29E)

// Champagne palette (React fonte de verdade: index.css lines 3-19)
private val Champagne600 = Color(0xFFB48C26)
private val Champagne500 = Color(0xFFD4AF37)
private val Champagne400 = Color(0xFFE5C158)

private val Purple600 = Color(0xFF9333EA)
private val Purple500 = Color(0xFFA855F7)
private val Purple400 = Color(0xFFC084FC)

private val Red600 = Color(0xFFDC2626)
private val Red500 = Color(0xFFEF4444)
private val Red400 = Color(0xFFF87171)

@Composable
fun HeroLogModal(
    isOpen: Boolean,
    onClose: () -> Unit,
    title: String,
    variant: ModalVariant = ModalVariant.Amber,
    hideHeader: Boolean = false,
    allowBackdropClose: Boolean = true,
    disableEscClose: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    var shouldRenderDialog by remember { mutableStateOf(isOpen) }
    var animatedVisible by remember { mutableStateOf(isOpen) }

    DisposableEffect(isOpen) {
        if (isOpen) {
            ModalCountRegistry.increment()
        }
        onDispose {
            if (isOpen) {
                ModalCountRegistry.decrement()
            }
        }
    }

    LaunchedEffect(isOpen) {
        if (isOpen) {
            shouldRenderDialog = true
            animatedVisible = true
        } else {
            animatedVisible = false
            delay(ModalTokens.ExitAnimDurationMs) // Duração da animação de saída (tween 220ms)
            shouldRenderDialog = false
        }
    }

    if (!shouldRenderDialog) return

    // NOTA spec-002: sem BackHandler aqui fora — o dispatcher da janela focada é o do
    // Dialog (ver dentro do Dialog abaixo). Bug confirmado em device 09/2026: Voltar
    // nunca fechava nenhum modal do app.

    val (borderColor, topGradient, glowColor, titleColor) = when (variant) {
        ModalVariant.Amber -> Quadruple(
            Color(0x4DD4AF37), // champagne-500/30
            listOf(Champagne600, Champagne400, Champagne600),
            Color(0x03D4AF37), // champagne-500/[0.01]
            Champagne400
        )
        ModalVariant.Purple -> Quadruple(
            Color(0x4DA855F7), // purple-500/30
            listOf(Purple600, Purple400, Purple600),
            Color(0x03A855F7), // purple-500/[0.01]
            Purple400
        )
        ModalVariant.Red -> Quadruple(
            Color(0x4DEF4444), // red-500/30
            listOf(Red600, Red400, Red600),
            Color(0x03EF4444), // red-500/[0.01]
            Red400
        )
    }

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
        // spec-002: BackHandler DENTRO do Dialog — com o modal aberto, a janela focada
        // é a do dialog e o back é despachado no dispatcher dela, não no da activity.
        // Fora daqui o Voltar nunca chegava (bug confirmado em device 09/2026).
        // Guard isOpen: durante os 220ms de saída o handler desarma, zerando até o
        // disparo benigno nos 3 form modals (Habits/Dailies/Todos, onClose em 2
        // estágios). BH-1 resolvido por construção.
        BackHandler(enabled = isOpen && !disableEscClose) {
            onClose()
        }
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Backdrop (spec-002 FR-003): hazeEffect amostra a cena real (blur de
            // verdade, não da própria camada). Sem HazeState (previews/testes), cai
            // para o fundo chapado. Raio calibrado em device vs React blur(2px).
            val hazeState = LocalHazeState.current
            var backdropModifier = Modifier.fillMaxSize()
            if (hazeState != null) {
                backdropModifier = backdropModifier.hazeEffect(
                    state = hazeState,
                    style = HazeStyle(
                        backgroundColor = Stone950.copy(alpha = 0.8f),
                        tint = null,
                        blurRadius = ModalTokens.BackdropBlurRadius,
                        noiseFactor = ModalTokens.BackdropNoiseFactor
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

            // Backdrop com fade próprio, separado do painel (spec-002 §4).
            AnimatedVisibility(
                visible = animatedVisible,
                enter = fadeIn(tween(220, easing = EaseOut)),
                exit = fadeOut(tween(220, easing = EaseOut))
            ) {
                Box(modifier = backdropModifier)
            }

            // Animated Modal Container (spec-002 §4: easeOut 220ms; y=15 do React
            // em dp→px — 15px CSS ≈ 15dp, não 15px físicos).
            // Deslocamento pré-calculado (o lambda do slide é Density-receiver).
            val slideOffsetPx = with(LocalDensity.current) { 15.dp.toPx().toInt() }
            AnimatedVisibility(
                visible = animatedVisible,
                enter = fadeIn(tween(220, easing = EaseOut)) +
                        scaleIn(initialScale = 0.95f, animationSpec = tween(220, easing = EaseOut)) +
                        slideInVertically(initialOffsetY = { slideOffsetPx }, animationSpec = tween(220, easing = EaseOut)),
                exit = fadeOut(tween(220, easing = EaseOut)) +
                        scaleOut(targetScale = 0.95f, animationSpec = tween(220, easing = EaseOut)) +
                        slideOutVertically(targetOffsetY = { slideOffsetPx }, animationSpec = tween(220, easing = EaseOut))
            ) {
                val screenHeight = LocalConfiguration.current.screenHeightDp.dp
                Box(
                    // spec-002 FR-001: painel dimensionado PELO CONTEÚDO (wrap content).
                    // O teto 0.8f vive no slot de conteúdo abaixo (FR-002), como o
                    // max-h-[80vh] do React — nunca mais no painel externo.
                    modifier = Modifier
                        .padding(ModalTokens.OuterMargin)
                        .widthIn(max = ModalTokens.PanelMaxWidth)
                        .fillMaxWidth()
                        .shadow(ModalTokens.PanelElevation, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(Stone900)
                        .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                ) {
                    // Ambient glow (spec-002: matchParentSize, não fillMaxSize — um Box
                    // vazio com fillMaxSize mede no máximo das constraints e esticava
                    // o painel wrap-content até a altura cheia; o teto 0.8f antigo
                    // mascarava isso. matchParentSize só acompanha o pai medido.)
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Brush.verticalGradient(listOf(glowColor, Color.Transparent)))
                    )

                    // Top Accent Border Line (4dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(Brush.horizontalGradient(topGradient))
                            .align(Alignment.TopCenter)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        if (!hideHeader) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .drawBehind {
                                        drawLine(
                                            color = Stone800,
                                            start = Offset(0f, size.height),
                                            end = Offset(size.width, size.height),
                                            strokeWidth = 1.dp.toPx()
                                        )
                                    }
                                    .padding(horizontal = ModalTokens.HeaderHorizontalPadding, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = title.uppercase(),
                                    modifier = Modifier.weight(1f),
                                    style = androidx.compose.ui.text.TextStyle(
                                        fontFamily = Cinzel,
                                        fontWeight = FontWeight.Black,
                                        fontSize = ModalTokens.TitleFontSize,
                                        color = titleColor,
                                        letterSpacing = 0.8.sp,
                                        // drop-shadow-[0_1px_4px_rgba(variante,0.2)] do React
                                        shadow = Shadow(
                                            color = titleColor.copy(alpha = 0.2f),
                                            offset = Offset(0f, 1f),
                                            blurRadius = 4f
                                        )
                                    )
                                )

                                // Fechar (spec-002 §4): visual 20px preservado; pressionado
                                // replica o hover React (bg stone-800 + tint da variante).
                                // Toque mantido em 32dp (acessibilidade).
                                val closeInteraction = remember { MutableInteractionSource() }
                                val closePressed by closeInteraction.collectIsPressedAsState()
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (closePressed) Stone800 else Color.Transparent)
                                        .clickable(
                                            interactionSource = closeInteraction,
                                            indication = null,
                                            role = Role.Button,
                                            onClick = onClose
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Fechar",
                                        tint = if (closePressed) titleColor else Stone400,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        // Content (spec-002 FR-002): o limite vertical mora AQUI, como o
                        // max-h-[80vh] + overflow-y-auto do React. Conteúdo curto →
                        // painel compacto; longo → rola com header/borda estáveis.
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = screenHeight * ModalTokens.ContentMaxHeightFraction)
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 20.dp, vertical = 20.dp)
                        ) {
                            // text-stone-200 do slot React: herdado pelos filhos sem cor própria.
                            CompositionLocalProvider(
                                LocalContentColor provides ModalTokens.ContentDefaultColor
                            ) {
                                content()
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
