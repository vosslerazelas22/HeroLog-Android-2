package com.iurispraecepta.herolog.ui.splash

import org.junit.Assert.assertEquals
import org.junit.Test

class SplashPhaseTest {
    @Test fun emblemPhase_before400() { assertEquals(SplashPhase.EMBLEM, phaseAt(399, 0, false)) }
    @Test fun wordmarkPhase_400to750() { assertEquals(SplashPhase.WORDMARK, phaseAt(400, 0, false)) }
    @Test fun holdsUntilReady_beyondMin() { assertEquals(SplashPhase.HOLD, phaseAt(1_500, 2_000, false)) }
    @Test fun exitsAtMin_whenReadyEarly() { assertEquals(SplashPhase.EXIT, phaseAt(1_200, 100, false)) }
    @Test fun exitsAtCap_whenNeverReady() { assertEquals(SplashPhase.EXIT, phaseAt(4_000, null, false)) }
    @Test fun reducedMotion_skipsToExit() { assertEquals(SplashPhase.DONE, phaseAt(100, 50, true)) }
    @Test fun doneAfterExit() { assertEquals(SplashPhase.DONE, phaseAt(1_500, 0, false)) }
    @Test fun negativeElapsed_isTreatedAsZero() { assertEquals(SplashPhase.EMBLEM, phaseAt(-1, null, false)) }
}
