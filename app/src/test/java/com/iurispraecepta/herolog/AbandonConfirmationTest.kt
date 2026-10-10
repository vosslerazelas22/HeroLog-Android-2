package com.iurispraecepta.herolog

import com.iurispraecepta.herolog.service.ServiceState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * T201 — Prazo de confirmação de abandono (spec NOTIFICACOES_FOCO, FR-C5, NFR-C3).
 * Puro: a decisão é `now < deadline`, nunca flag.
 */
class AbandonConfirmationTest {

    @Test
    fun isAbandonArmed_nowBeforeDeadline_returnsTrue() {
        val state = ServiceState(abandonConfirmDeadlineMillis = 10_000L)

        assertTrue(state.isAbandonArmed(now = 9_999L))
    }

    @Test
    fun isAbandonArmed_nowEqualDeadline_returnsFalse() {
        val state = ServiceState(abandonConfirmDeadlineMillis = 10_000L)

        assertFalse(state.isAbandonArmed(now = 10_000L))
    }

    @Test
    fun isAbandonArmed_zeroDeadline_returnsFalse() {
        val state = ServiceState(abandonConfirmDeadlineMillis = 0L)

        assertFalse(state.isAbandonArmed(now = 5_000L))
    }

    @Test
    fun isAbandonArmed_expiredDeadline_returnsFalse() {
        val state = ServiceState(abandonConfirmDeadlineMillis = 10_000L)

        assertFalse(state.isAbandonArmed(now = 10_001L))
    }
}
