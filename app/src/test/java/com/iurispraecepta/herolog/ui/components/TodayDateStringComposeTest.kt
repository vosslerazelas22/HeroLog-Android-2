package com.iurispraecepta.herolog.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.iurispraecepta.herolog.ui.theme.HeroLogTheme
import com.iurispraecepta.herolog.ui.theme.QuestPanel
import org.junit.After
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.TimeZone

/**
 * SPEC-Rebranding item 4 — `rememberTodayDateString(refreshKey)`.
 *
 * A string "hoje" só é recalculada quando [refreshKey] muda (ou em `ON_RESUME`, ou na
 * meia-noite do `LaunchedEffect`). Aqui se prova a parte do `refreshKey`: trocar o fuso
 * muda o dia calculado, mas **não** reaparece sozinho — só quando a chave muda.
 *
 * Fuso A = UTC-12, fuso B = UTC+14: as datas são sempre diferentes (diferença de 26h),
 * então o teste não depende do horário em que roda.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TodayDateStringComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var originalZone: TimeZone

    @Before
    fun setUp() {
        originalZone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("Etc/GMT+12"))
    }

    @After
    fun tearDown() {
        TimeZone.setDefault(originalZone)
    }

    @Test
    fun refreshKeyChange_recomputesToday_withoutKeyNothingChanges() {
        var refreshKey by mutableStateOf(0)
        composeTestRule.setContent {
            HeroLogTheme(darkTheme = true) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(QuestPanel)
                ) {
                    Text(text = rememberTodayDateString(refreshKey))
                }
            }
        }

        val inZoneA = formatTodayDate()
        composeTestRule.onNodeWithText(inZoneA).assertIsDisplayed()

        // Muda o fuso para UTC+14: o dia renderizado NÃO muda sozinho (sem key)
        TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Kiritimati"))
        val inZoneB = formatTodayDate()
        assertNotEquals(
            "Os dois fusos precisam gerar datas diferentes (26h de diferença)",
            inZoneA,
            inZoneB
        )
        composeTestRule.onNodeWithText(inZoneA).assertIsDisplayed()

        // Trocando o refreshKey (ex.: `rolloverTick` do VM), "hoje" é recalculado
        refreshKey = 1
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(inZoneB).assertIsDisplayed()
        composeTestRule.onNodeWithText(inZoneA).assertDoesNotExist()
    }
}
