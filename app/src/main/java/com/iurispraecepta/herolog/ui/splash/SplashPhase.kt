package com.iurispraecepta.herolog.ui.splash

enum class SplashPhase { EMBLEM, WORDMARK, HOLD, EXIT, DONE }

fun phaseAt(elapsedMs: Long, readyAtMs: Long?, reducedMotion: Boolean): SplashPhase {
    val elapsed = elapsedMs.coerceAtLeast(0L)
    if (reducedMotion) {
        return if (elapsed >= SplashTimings.CAP || (readyAtMs != null && elapsed >= readyAtMs)) {
            SplashPhase.DONE
        } else SplashPhase.EMBLEM
    }
    val exitStart = maxOf(SplashTimings.MIN_TOTAL, readyAtMs ?: SplashTimings.CAP)
        .coerceAtMost(SplashTimings.CAP)
    return when {
        elapsed < SplashTimings.EMBLEM_HOLD -> SplashPhase.EMBLEM
        elapsed < SplashTimings.EMBLEM_HOLD + SplashTimings.CROSSFADE -> SplashPhase.WORDMARK
        elapsed < exitStart -> SplashPhase.HOLD
        elapsed < exitStart + SplashTimings.EXIT_FADE -> SplashPhase.EXIT
        else -> SplashPhase.DONE
    }
}
