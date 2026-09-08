package com.iurispraecepta.herolog.ui.daily

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iurispraecepta.herolog.model.DailyReportData
import com.iurispraecepta.herolog.ui.theme.Amber100
import com.iurispraecepta.herolog.ui.theme.Amber400
import com.iurispraecepta.herolog.ui.theme.Amber400
import com.iurispraecepta.herolog.ui.theme.Amber500
import com.iurispraecepta.herolog.ui.theme.Cinzel
import com.iurispraecepta.herolog.ui.theme.Inter
import com.iurispraecepta.herolog.ui.theme.JetBrainsMono
import com.iurispraecepta.herolog.ui.theme.Stone400
import com.iurispraecepta.herolog.ui.theme.Stone800
import com.iurispraecepta.herolog.ui.theme.Stone900
import com.iurispraecepta.herolog.ui.theme.Stone950
import kotlin.math.roundToInt

@Composable
fun DailyReportModal(
    data: DailyReportData,
    onDismiss: () -> Unit
) {
    var visible by remember { mutableStateOf(true) }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(600)) + slideInVertically(
            initialOffsetY = { it / 4 },
            animationSpec = tween(600, easing = FastOutSlowInEasing)
        ),
        exit = fadeOut(tween(400)) + slideOutVertically(
            targetOffsetY = { -it / 4 },
            animationSpec = tween(400, easing = FastOutSlowInEasing)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f)),
            contentAlignment = Alignment.Center
        ) {
            // Backdrop blur
            var backdropModifier = Modifier.fillMaxSize()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                backdropModifier = backdropModifier.blur(12.dp)
            }
            Box(modifier = backdropModifier)

            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Amber500.copy(alpha = 0.05f),
                                Color.Transparent,
                                Color.Transparent
                            )
                        )
                    )
            )

            // Sparkle particles
            SparkleParticles()

            // Main card
            CardContainer(data = data) {
                visible = false
                onDismiss()
            }
        }
    }
}

@Composable
private fun SparkleParticles() {
    val infiniteTransition = rememberInfiniteTransition(label = "sparkles")

    val seeds = remember {
        listOf(
            ParticleSeed(leftDp = 30, sizeSp = 8, delayMs = 0, durationMs = 1800),
            ParticleSeed(leftDp = 55, sizeSp = 6, delayMs = 200, durationMs = 2100),
            ParticleSeed(leftDp = 80, sizeSp = 10, delayMs = 400, durationMs = 1600),
            ParticleSeed(leftDp = 110, sizeSp = 7, delayMs = 100, durationMs = 2000),
            ParticleSeed(leftDp = 140, sizeSp = 9, delayMs = 300, durationMs = 1900),
            ParticleSeed(leftDp = 170, sizeSp = 6, delayMs = 500, durationMs = 1700),
            ParticleSeed(leftDp = 200, sizeSp = 8, delayMs = 150, durationMs = 2200),
            ParticleSeed(leftDp = 230, sizeSp = 7, delayMs = 350, durationMs = 1800),
            ParticleSeed(leftDp = 60, sizeSp = 9, delayMs = 250, durationMs = 2000),
            ParticleSeed(leftDp = 120, sizeSp = 6, delayMs = 450, durationMs = 1600),
            ParticleSeed(leftDp = 180, sizeSp = 10, delayMs = 50, durationMs = 2100),
            ParticleSeed(leftDp = 250, sizeSp = 8, delayMs = 380, durationMs = 1900),
            ParticleSeed(leftDp = 45, sizeSp = 7, delayMs = 180, durationMs = 1700),
            ParticleSeed(leftDp = 155, sizeSp = 9, delayMs = 320, durationMs = 2000),
            ParticleSeed(leftDp = 210, sizeSp = 6, delayMs = 80, durationMs = 1800),
            ParticleSeed(leftDp = 95, sizeSp = 8, delayMs = 280, durationMs = 2100)
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        seeds.forEach { seed ->
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(seed.durationMs, delayMillis = seed.delayMs),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "alpha_${seed.leftDp}"
            )
            val offsetY by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = -100f,
                animationSpec = infiniteRepeatable(
                    animation = tween(seed.durationMs, delayMillis = seed.delayMs),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "offsetY_${seed.leftDp}"
            )

            Text(
                text = "\u2726",
                color = Amber400.copy(alpha = alpha),
                fontSize = seed.sizeSp.sp,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset {
                        IntOffset(
                            x = seed.leftDp.dp.roundToPx(),
                            y = offsetY.dp.roundToPx()
                        )
                    }
            )
        }
    }
}

private data class ParticleSeed(
    val leftDp: Int,
    val sizeSp: Int,
    val delayMs: Int,
    val durationMs: Int
)

@Composable
private fun CardContainer(
    data: DailyReportData,
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "icon_bounce")
    val iconScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconScale"
    )

    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
            .shadow(
                elevation = 50.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color(0xFFE2B054).copy(alpha = 0.35f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(Stone950)
            .border(2.dp, Amber500, RoundedCornerShape(16.dp))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Sun icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .border(2.dp, Amber500, CircleShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Stone800, Stone950)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "\u2600\uFE0F",
                fontSize = 36.sp,
                modifier = Modifier.size((36 * iconScale).dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Header
        Text(
            text = "Um novo dia come\u00e7a.",
            fontFamily = Cinzel,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 11.sp,
            color = Color(0xFFE2B054),
            letterSpacing = 0.15.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "RELAT\u00d3RIO DI\u00c1RIO",
            fontFamily = Cinzel,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp,
            color = Amber400,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Gold reward card
        GoldRewardCard(data.rewardAmount)

        Spacer(modifier = Modifier.height(24.dp))

        // Streak section
        StreakSection(data)

        Spacer(modifier = Modifier.height(16.dp))

        // Tasks section
        TasksSection(data)

        Spacer(modifier = Modifier.height(24.dp))

        // Continue button
        Button(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Stone950
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Amber500, Amber400)
                        ),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Continuar",
                    fontFamily = Cinzel,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}

@Composable
private fun GoldRewardCard(amount: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Stone900.copy(alpha = 0.6f))
            .border(1.dp, Amber500.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "TESOURO RECEBIDO",
            fontFamily = JetBrainsMono,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = Amber400,
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "\uD83D\uDC8E +$amount GP",
            fontFamily = Cinzel,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 30.sp,
            color = Amber400,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Recompensa Di\u00e1ria",
            fontFamily = Cinzel,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            color = Amber100.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun StreakSection(data: DailyReportData) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "SEQU\u00caNCIA DE DIAS (STREAK)",
            fontFamily = JetBrainsMono,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = Amber400.copy(alpha = 0.5f),
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        when {
            data.streakLost -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF450a0a).copy(alpha = 0.2f))
                        .border(1.dp, Color(0xFF7F1D1D).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Sua chama se apagou por um dia. Hoje \u00e9 uma nova oportunidade para reacend\u00ea-la.",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = Color(0xFFFCA5A5),
                        lineHeight = 18.sp
                    )
                }
            }
            data.streakProtected -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFFF3C6).copy(alpha = 0.05f))
                        .border(1.dp, Color(0xFFFFF3C6).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "\uD83D\uDEE1\uFE0F", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "O Escudo do Santu\u00e1rio protegeu sua sequ\u00eancia.",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = Color(0xFFFFF3C6),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Amber500.copy(alpha = 0.02f))
                        .border(1.dp, Amber500.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "\uD83D\uDD25", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Sua chama permanece acesa h\u00e1 ${data.currentStreak} ${
                                if (data.currentStreak == 1) "dia" else "dias"
                            }.",
                            fontFamily = JetBrainsMono,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Amber400
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TasksSection(data: DailyReportData) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "TAREFAS DI\u00c1RIAS DE ONTEM",
            fontFamily = JetBrainsMono,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = Amber400.copy(alpha = 0.5f),
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        when {
            data.allDailiesCompleted -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFFF3C6).copy(alpha = 0.05f))
                        .border(1.dp, Color(0xFFFFF3C6).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Incr\u00edvel! Todas as suas tarefas de ontem foram conclu\u00eddas.",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = Color(0xFFFFF3C6),
                        lineHeight = 18.sp
                    )
                }
            }
            data.missedDailiesCount > 0 -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Stone900.copy(alpha = 0.4f))
                        .border(1.dp, Stone800, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Dailies Incompletas:",
                            fontFamily = JetBrainsMono,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFFE2B054)
                        )
                        Text(
                            text = "${data.missedDailiesCount}",
                            fontFamily = JetBrainsMono,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFFF87171)
                        )
                    }
                    Text(
                        text = "Algumas miss\u00f5es ficaram pelo caminho. Voc\u00ea sofreu -${data.damageTaken} HP.",
                        fontFamily = Inter,
                        fontSize = 11.sp,
                        color = Color(0xFFFCA5A5),
                        lineHeight = 16.sp
                    )
                }
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Stone900.copy(alpha = 0.3f))
                        .border(1.dp, Stone800.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhuma tarefa di\u00e1ria pendente de ontem.",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        fontStyle = FontStyle.Italic,
                        color = Stone400.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}
