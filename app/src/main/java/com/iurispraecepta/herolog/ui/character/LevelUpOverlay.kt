package com.iurispraecepta.herolog.ui.character

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.iurispraecepta.herolog.logic.character.LevelUpEvent
import com.iurispraecepta.herolog.ui.sfx.LocalSfxManager
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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Stone950.copy(alpha = 0.9f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* backdrop nao fecha, igual a fonte */ },
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = animatedVisible,
                enter = fadeIn(tween(260)) +
                        scaleIn(initialScale = 0.85f, animationSpec = tween(260)) +
                        slideInVertically(initialOffsetY = { 30 }, animationSpec = tween(260)),
                exit = fadeOut(tween(180)) +
                        scaleOut(targetScale = 0.85f, animationSpec = tween(180)) +
                        slideOutVertically(targetOffsetY = { -30 }, animationSpec = tween(180))
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
    LevelUpCardShell(borderColor = Amber500, glowColor = AmberGlow, particleColor = Amber400) {
        PulsingIconBadge(borderColor = Amber500, ringColor = Amber500) {
            Text(text = "⚔️", fontSize = 32.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Você evoluiu!",
            style = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                color = GoldAccent,
                letterSpacing = 1.5.sp
            )
        )
        Text(
            text = "LEVEL UP!",
            style = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Black,
                fontSize = 26.sp,
                color = Amber300,
                letterSpacing = 1.sp
            )
        )
        Text(
            text = "${event.charName.uppercase()} ALCANÇOU O NÍVEL ${event.newLevel}",
            style = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = Color(0xFFFEF3C7).copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.widthIn(max = 260.dp)
        )
        Divider(color = Amber500)
        ContinueButton(gradient = listOf(Amber600, Amber500, Amber400), onClick = onDismiss)
    }
}

@Composable
private fun SkillLevelUpCard(event: LevelUpEvent.Skill, onDismiss: () -> Unit) {
    LevelUpCardShell(borderColor = Emerald500, glowColor = EmeraldGlow, particleColor = Emerald400) {
        PulsingIconBadge(borderColor = Emerald500, ringColor = Emerald500) {
            Text(text = event.emoji, fontSize = 32.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "MAESTRIA APRIMORADA",
            style = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = Emerald400,
                letterSpacing = 0.5.sp
            )
        )
        Text(
            text = event.skillName,
            style = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                color = Emerald400
            )
        )
        Text(
            text = "alcançou o Nível ${event.newLevel}",
            style = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = AmberText200
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

    BoxWithConstraints(
        modifier = Modifier
            .widthIn(max = 380.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Stone950)
            .background(Brush.verticalGradient(listOf(glowColor, Color.Transparent)))
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
    ) {
        val cardWidth = maxWidth
        val cardHeight = maxHeight

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
    val transition = rememberInfiniteTransition(label = "ping")
    val pingScale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = EaseInOutSine),
            repeatMode = RepeatMode.Restart
        ),
        label = "pingScale"
    )
    val pingAlpha by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = EaseInOutSine),
            repeatMode = RepeatMode.Restart
        ),
        label = "pingAlpha"
    )

    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(80.dp * pingScale)
                .clip(CircleShape)
                .background(ringColor.copy(alpha = pingAlpha))
        )
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Brush.verticalGradient(listOf(Stone800, Stone950)))
                .border(2.dp, borderColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
    }
}

@Composable
private fun Divider(color: Color) {
    Box(
        modifier = Modifier
            .padding(vertical = 12.dp)
            .width(90.dp)
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.horizontalGradient(gradient))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onClick
            )
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "CONTINUAR",
            style = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                color = Stone950,
                letterSpacing = 1.5.sp
            )
        )
    }
}
