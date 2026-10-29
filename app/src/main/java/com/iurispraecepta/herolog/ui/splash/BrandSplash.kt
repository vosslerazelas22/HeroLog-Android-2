package com.iurispraecepta.herolog.ui.splash

import android.content.Context
import android.provider.Settings
import android.os.SystemClock
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iurispraecepta.herolog.R
import com.iurispraecepta.herolog.ui.theme.Cinzel
import com.iurispraecepta.herolog.ui.theme.QuestDeep
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun BrandSplash(
    characterReady: Boolean,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    elapsedOverride: Long? = null,
    readyAtOverride: Long? = null,
) {
    val context = LocalContext.current
    val startTime = remember { SystemClock.elapsedRealtime() }
    var elapsed by remember { mutableLongStateOf(0L) }
    var readyAt by remember { mutableStateOf<Long?>(null) }
    val reducedMotion = remember { animatorDurationScale(context) == 0f }
    LaunchedEffect(characterReady) {
        if (characterReady && readyAt == null) readyAt = SystemClock.elapsedRealtime() - startTime
    }
    LaunchedEffect(elapsedOverride) {
        if (elapsedOverride == null) {
            while (true) {
                elapsed = SystemClock.elapsedRealtime() - startTime
                delay(16)
            }
        }
    }
    val now = elapsedOverride ?: elapsed
    val ready = readyAtOverride ?: readyAt
    val phase = phaseAt(now, ready, reducedMotion)
    LaunchedEffect(phase) { if (phase == SplashPhase.DONE) onFinished() }
    val emblemAlpha by animateFloatAsState(
        targetValue = if (phase == SplashPhase.EMBLEM) 1f else 0f,
        animationSpec = tween(if (phase == SplashPhase.WORDMARK) SplashTimings.CROSSFADE.toInt() else 0),
        label = "splash-emblem-alpha",
    )
    val wordAlpha by animateFloatAsState(
        targetValue = when (phase) {
            SplashPhase.WORDMARK, SplashPhase.HOLD -> 1f
            SplashPhase.EXIT -> exitAlpha(now, ready, reducedMotion)
            else -> 0f
        },
        animationSpec = tween(if (phase == SplashPhase.WORDMARK) SplashTimings.CROSSFADE.toInt() else 0),
        label = "splash-wordmark-alpha",
    )
    val wordScale by animateFloatAsState(
        targetValue = if (phase == SplashPhase.WORDMARK || phase == SplashPhase.HOLD) 1f else 0.9f,
        animationSpec = tween(SplashTimings.CROSSFADE.toInt(), easing = FastOutSlowInEasing),
        label = "splash-wordmark-scale",
    )
    val pulse = if (phase == SplashPhase.HOLD && !characterReady && !reducedMotion) {
        0.97f + (sin((now % 1_600L).toDouble() / 1_600.0 * 2.0 * PI) * 0.03).toFloat()
    } else 1f
    Box(
        modifier = modifier.fillMaxSize()
            .background(QuestDeep)
            .clearAndSetSemantics { contentDescription = "HeroLog" }
            .pointerInput(Unit) { detectTapGestures { } },
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_splash_emblem),
            contentDescription = null,
            modifier = Modifier.size(288.dp).alpha(emblemAlpha),
        )
        Text(
            text = "HeroLog",
            color = Color(0xFFD8BA7A),
            fontFamily = Cinzel,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 38.sp,
            modifier = Modifier.alpha(wordAlpha).graphicsLayer(scaleX = wordScale * pulse, scaleY = wordScale * pulse),
        )
    }
}

private fun exitAlpha(elapsed: Long, readyAt: Long?, reduced: Boolean): Float {
    if (reduced) return 0f
    val start = maxOf(SplashTimings.MIN_TOTAL, readyAt ?: SplashTimings.CAP).coerceAtMost(SplashTimings.CAP)
    return (1f - ((elapsed - start).toFloat() / SplashTimings.EXIT_FADE).coerceIn(0f, 1f))
}

private fun animatorDurationScale(context: Context): Float = try {
    Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
} catch (_: Exception) { 1f }
