package com.iurispraecepta.herolog.ui.character

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.iurispraecepta.herolog.logic.character.LevelUpEvent
import com.iurispraecepta.herolog.ui.sfx.LocalSfxManager
import com.iurispraecepta.herolog.ui.theme.Cinzel
import kotlin.random.Random

private val Stone950 = Color(0xFF0C0A09)
private val Stone800 = Color(0xFF292524)

private val Amber300 = Color(0xFFFCD34D)
private val Amber400 = Color(0xFFFBBF24)
private val Amber500 = Color(0xFFF59E0B)
private val Amber600 = Color(0xFFD97706)
private val GoldAccent = Color(0xFFE2B054) // hex custom da fonte, sem token tailwind
private val AmberGlow = Color(0x1AE2B054)
private val AmberText200 = Color(0xFFFDE68A)

private val Emerald400 = Color(0xFF34D399)
private val Emerald500 = Color(0xFF10B981)
private val Emerald600 = Color(0xFF059669)
private val EmeraldGlow = Color(0x1A10B981)

/**
 * Porte da seção JSX "EPIC LEVEL UP & SKILL EVOLUTION POPUP SYSTEM" (`App.tsx`, ao redor da
 * renderização de `activeLevelUp`). Dois visuais: dourado/âmbar pra `LevelUpEvent.Combat`,
 * esmeralda pra `LevelUpEvent.Skill` -- mesma paleta e textos da fonte.
 *
 * **Fidelidade de conteúdo**: textos ("Você evoluiu!", "LEVEL UP!", "NÍVEL {n}", "MAESTRIA
 * APRIMORADA", "Continuar") transcritos literalmente.
 *
 * **Divergência deliberada de interação**: a fonte não trata teclado/ESC pra este popup em
 * nenhum lugar do `App.tsx` -- só o clique no botão despacha `dismissCurrentLevelUp`, sem
 * fechamento por clique fora. Aqui o botão físico de voltar do Android também descarta o evento
 * atual (em vez de deixar o usuário sair do app com o popup visível); clique fora continua sem
 * efeito, igual à fonte. **Sinalizado pra confirmação do Bruno.**
 *
 * **Partículas**: a fonte gera 16 partículas com posição/rotação/duração/delay aleatórios a cada
 * render (`Math.random()` direto no JSX). Aqui a lista é sorteada uma vez por evento ativo
 * (`remember(event)`), não a cada recomposição -- resultado visual equivalente (aleatório,
 * subindo, sumindo), sem custo de reamostrar em todo frame. Rotação da fonte (`--rotate`) não foi
 * portada porque o glifo "✦" usado é visualmente simétrico -- rotacioná-lo não muda a percepção.
 *
 * `hasPendingLevelUps` (retorno de `useLevelUp` na fonte) nunca é lido em lugar nenhum do
 * `App.tsx` -- por isso não tem parâmetro correspondente aqui; se algum dia for necessário, dá
 * pra derivar de `HeroLogViewModel.levelUpQueue.value.size > 1`.
 */
@Composable
fun LevelUpOverlay(
    event: LevelUpEvent?,
    onDismiss: () -> Unit
) {
    if (event == null) return

    val sfxManager = LocalSfxManager.current
    LaunchedEffect(event) {
        sfxManager?.playLevelUp()
    }

    BackHandler(onBack = onDismiss)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false, // BackHandler manual acima é a única fonte de verdade
            dismissOnClickOutside = false // fonte não fecha este popup ao clicar fora
        )
    ) {
        var animatedVisible by remember(event) { mutableStateOf(false) }
        LaunchedEffect(event) { animatedVisible = true }

        // Dx-6: fonte usa bg-black/90 (não stone-950) no backdrop.
        // Dx-7: gradiente sutil no backdrop (bg-gradient-to-t from-amber/emerald-500/5).
        val backdropTint = if (event is LevelUpEvent.Combat) Amber400.copy(alpha = 0.05f)
        else Emerald400.copy(alpha = 0.05f)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* backdrop nao fecha, igual a fonte */ },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, backdropTint)))
            )
            // Dx-13/Dx-14/Dx-15: fonte usa spring(damping:20, stiffness:120) ≈
            // dampingRatio 0.9, stiffness 120 — não tween linear.
            AnimatedVisibility(
                visible = animatedVisible,
                enter = fadeIn(animationSpec = spring(dampingRatio = 0.9f, stiffness = 120f)) +
                        scaleIn(initialScale = 0.85f, animationSpec = spring(dampingRatio = 0.9f, stiffness = 120f)) +
                        slideInVertically(initialOffsetY = { 30 }, animationSpec = spring(dampingRatio = 0.9f, stiffness = 120f)),
                exit = fadeOut(animationSpec = spring(dampingRatio = 0.9f, stiffness = 120f)) +
                        scaleOut(targetScale = 0.85f, animationSpec = spring(dampingRatio = 0.9f, stiffness = 120f)) +
                        slideOutVertically(targetOffsetY = { -30 }, animationSpec = spring(dampingRatio = 0.9f, stiffness = 120f))
            ) {
                when (event) {
                    is LevelUpEvent.Combat -> CombatLevelUpCard(event, onDismiss)
                    is LevelUpEvent.Skill -> SkillLevelUpCard(event, onDismiss)
                }
            }
        }
    }
}

@Composable
private fun CombatLevelUpCard(event: LevelUpEvent.Combat, onDismiss: () -> Unit) {
    LevelUpCardShell(borderColor = Amber500, glowColor = AmberGlow, particleColor = Amber400, auraColor = Amber500) {
        PulsingIconBadge(borderColor = Amber500, ringColor = Amber500) {
            // Dx-10: text-4xl = 36px (App.tsx:4512).
            Text(text = "⚔️", fontSize = 36.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            // Dx-1: fonte aplica `uppercase` via CSS sobre "Você evoluiu!" (App.tsx:4518).
            // Dx-3: font-serif = Cinzel (antes FontFamily.Serif do sistema).
            text = "VOCÊ EVOLUIU!",
            style = TextStyle(
                fontFamily = Cinzel,
                fontWeight = FontWeight.Black,
                fontSize = 10.sp,
                color = GoldAccent,
                letterSpacing = 1.5.sp
            )
        )
        Text(
            // D2 (auditoria 20/09): text-2xl = 24px no mobile (App.tsx:4521).
            text = "LEVEL UP!",
            style = TextStyle(
                fontFamily = Cinzel,
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                color = Amber300,
                letterSpacing = 1.sp
            )
        )
        Text(
            // Dx-2/Dx-4 (parte 1): no mobile o span do nível é `block` abaixo (App.tsx:4525) —
            // primeira linha sem o nível, text-sm = 14sp.
            text = "${event.charName.uppercase()} ALCANÇOU O",
            style = TextStyle(
                fontFamily = Cinzel,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Color(0xFFFEF3C7).copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.widthIn(max = 260.dp)
        )
        Text(
            // Dx-2/Dx-4 (parte 2): span text-lg (18px) font-bold text-[#E2B054] (App.tsx:4525).
            text = "NÍVEL ${event.newLevel}",
            style = TextStyle(
                fontFamily = Cinzel,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = GoldAccent,
                textAlign = TextAlign.Center
            )
        )
        Divider(color = Amber500)
        ContinueButton(gradient = listOf(Amber600, Amber500, Amber400), onClick = onDismiss)
    }
}

@Composable
private fun SkillLevelUpCard(event: LevelUpEvent.Skill, onDismiss: () -> Unit) {
    LevelUpCardShell(borderColor = Emerald500, glowColor = EmeraldGlow, particleColor = Emerald400, auraColor = Emerald500) {
        PulsingIconBadge(borderColor = Emerald500, ringColor = Emerald500) {
            Text(text = event.emoji, fontSize = 36.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "MAESTRIA APRIMORADA",
            style = TextStyle(
                fontFamily = Cinzel,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                color = Emerald400,
                letterSpacing = 0.5.sp
            )
        )
        Text(
            text = event.skillName,
            style = TextStyle(
                fontFamily = Cinzel,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                color = Emerald400
            )
        )
        Text(
            // D3 (auditoria 20/09, fecha residual D162): no mobile o span do nível é
            // `block` abaixo (App.tsx:4600) — primeira linha text-sm = 14sp.
            text = "alcançou o",
            style = TextStyle(
                fontFamily = Cinzel,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = AmberText200,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.widthIn(max = 260.dp)
        )
        Text(
            // D3 (parte 2): span text-lg (18px) font-black text-emerald-400 (App.tsx:4600).
            text = "Nível ${event.newLevel}",
            style = TextStyle(
                fontFamily = Cinzel,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = Emerald400,
                textAlign = TextAlign.Center
            )
        )
        Divider(color = Emerald500)
        ContinueButton(gradient = listOf(Emerald600, Emerald500, Emerald400), onClick = onDismiss)
    }
}

private data class LevelUpParticle(
    val xFraction: Float,   // posição horizontal inicial, fração da largura do card (0..1)
    val shiftXDp: Int,      // deslocamento horizontal total ao longo da subida
    val durationMs: Int,
    val delayMs: Int,
    val fontSizeSp: Int
)

/**
 * Casca compartilhada dos dois cartões: fundo, borda, glow e partículas subindo por trás do
 * conteúdo (igual à fonte, que desenha as partículas num painel absoluto atrás do texto).
 */
@Composable
private fun LevelUpCardShell(
    borderColor: Color,
    glowColor: Color,
    particleColor: Color,
    auraColor: Color,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    val particles = remember {
        List(16) {
            LevelUpParticle(
                xFraction = 0.15f + Random.nextFloat() * 0.7f,
                shiftXDp = Random.nextInt(-120, 121),
                durationMs = 1500 + Random.nextInt(0, 2000),
                delayMs = Random.nextInt(0, 1500),
                fontSizeSp = 6 + Random.nextInt(0, 8)
            )
        }
    }

    // Dx-16: goldenRadiance/emeraldRadiance — 2.5s ease-in-out infinito, borda 0.3↔0.95
    // (index.css:64-84). O glow de fundo pulsa junto.
    val glowTransition = rememberInfiniteTransition(label = "cardGlow")
    val glowPulse by glowTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )
    // Dx-9: pulsingAura — 3s ease-in-out infinito, scale 1↔1.12, opacity 0.08↔0.25
    // (App.tsx:4481 + index.css:86-95).
    val auraTransition = rememberInfiniteTransition(label = "aura")
    val auraP by auraTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "auraP"
    )

    BoxWithConstraints(
        modifier = Modifier
            .widthIn(max = 380.dp)
            .fillMaxWidth()
            // Dx-5: glow omnidirecional (shadow 0 0 50px da fonte) + gradiente de topo.
            .shadow(
                elevation = 24.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = borderColor.copy(alpha = 0.3f + 0.35f * glowPulse),
                ambientColor = borderColor.copy(alpha = 0.3f + 0.35f * glowPulse)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(Stone950)
            .background(
                Brush.verticalGradient(
                    listOf(glowColor.copy(alpha = glowColor.alpha * (0.5f + glowPulse)), Color.Transparent)
                )
            )
            .border(2.dp, borderColor.copy(alpha = 0.3f + 0.65f * glowPulse), RoundedCornerShape(16.dp))
    ) {
        val cardWidth = maxWidth
        val cardHeight = maxHeight

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val s = 1f + 0.12f * auraP
                    scaleX = s
                    scaleY = s
                    alpha = 0.08f + 0.17f * auraP
                }
                .background(Brush.radialGradient(listOf(auraColor.copy(alpha = 0.1f), Color.Transparent)))
        )

        particles.forEach { particle ->
            RisingSpark(
                particle = particle,
                color = particleColor,
                cardWidth = cardWidth,
                cardHeight = cardHeight
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}

/** Porte de `.rising-spark` -- um "✦" subindo, se deslocando lateralmente e sumindo (fade). */
@Composable
private fun RisingSpark(
    particle: LevelUpParticle,
    color: Color,
    cardWidth: androidx.compose.ui.unit.Dp,
    cardHeight: androidx.compose.ui.unit.Dp
) {
    val transition = rememberInfiniteTransition(label = "spark")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(particle.durationMs, delayMillis = particle.delayMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sparkProgress"
    )

    val alpha = when {
        progress < 0.15f -> progress / 0.15f
        progress > 0.7f -> ((1f - progress) / 0.3f).coerceIn(0f, 1f)
        else -> 1f
    }

    val startX = cardWidth * particle.xFraction
    val riseY = cardHeight * 0.85f

    Text(
        text = "✦",
        fontSize = particle.fontSizeSp.sp,
        color = color,
        modifier = Modifier
            // Dx-17: riseFast também anima scale 0.3→1.1 (index.css:49,59).
            .graphicsLayer {
                val s = 0.3f + 0.8f * progress
                scaleX = s
                scaleY = s
            }
            .alpha(alpha)
            .offset(
                x = startX + (particle.shiftXDp.dp * progress),
                y = cardHeight - (riseY * progress) - 20.dp
            )
    )
}

@Composable
private fun PulsingIconBadge(
    borderColor: Color,
    ringColor: Color,
    icon: @Composable () -> Unit
) {
    // Dx-8/Dx-18: tailwind animate-ping — scale 1→2, 1s, cubic-bezier(0,0,0.2,1),
    // sobre base bg-amber/emerald-500/10 (App.tsx:4513).
    val pingEasing = CubicBezierEasing(0f, 0f, 0.2f, 1f)
    val transition = rememberInfiniteTransition(label = "ping")
    val pingScale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = pingEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pingScale"
    )
    val pingAlpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = pingEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pingAlpha"
    )
    // Dx-19: tailwind animate-bounce no ícone — translateY -25%↔0, 1s infinito
    // (App.tsx:4512). -8f.dp ≈ -25% do glifo de 36sp. (animation-core 1.7 não tem
    // InfiniteTransition.animateDp — por isso animateFloat + conversão pra Dp.)
    val bounceTransition = rememberInfiniteTransition(label = "bounce")
    val bounceY by bounceTransition.animateFloat(
        initialValue = -8f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1000
                -8f at 0 with CubicBezierEasing(0.8f, 0f, 1f, 1f)
                0f at 500 with CubicBezierEasing(0f, 0f, 0.2f, 1f)
                -8f at 1000
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "bounceY"
    )

    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(80.dp * pingScale)
                .clip(CircleShape)
                .background(ringColor.copy(alpha = 0.1f * pingAlpha))
        )
        Box(
            modifier = Modifier
                .size(80.dp)
                // Dx-11: shadow-lg do badge (App.tsx:4511).
                .shadow(10.dp, CircleShape)
                .clip(CircleShape)
                .background(Brush.verticalGradient(listOf(Stone800, Stone950)))
                .border(2.dp, borderColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(modifier = Modifier.offset(y = bounceY.dp)) {
                icon()
            }
        }
    }
}

@Composable
private fun Divider(color: Color) {
    Box(
        modifier = Modifier
            .padding(vertical = 12.dp)
            // D10 (auditoria 20/09): fonte usa w-1/3 do conteúdo (App.tsx:4527),
            // não largura fixa — fração mantém a proporção em qualquer viewport.
            // Centralizado via horizontalAlignment = CenterHorizontally da Column.
            .fillMaxWidth(1f / 3f)
            .height(2.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(Color.Transparent, color.copy(alpha = 0.5f), Color.Transparent)
                )
            )
    )
}

@Composable
private fun ContinueButton(gradient: List<Color>, onClick: () -> Unit) {
    // Dx-12: active:scale-[0.97] da fonte (App.tsx:4533) — hover não existe no mobile,
    // pressionar escala o botão. Sem ripple (fonte não tem).
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = tween(150),
        label = "pressScale"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.horizontalGradient(gradient))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick
            )
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            // D4 (auditoria 20/09): text-[10px] no mobile (App.tsx:4533).
            text = "CONTINUAR",
            style = TextStyle(
                fontFamily = Cinzel,
                fontWeight = FontWeight.Black,
                fontSize = 10.sp,
                color = Stone950,
                letterSpacing = 1.5.sp
            )
        )
    }
}
