package com.iurispraecepta.herolog.ui.splash

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.iurispraecepta.herolog.ui.theme.HeroLogTheme
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class BrandSplashScreenshotTest {
    @get:Rule val composeTestRule = createComposeRule()
    private var testElapsed by mutableStateOf(0L)
    private var testReadyAt by mutableStateOf<Long?>(null)
    private var testReady by mutableStateOf(false)

    @Test fun capturesEmblemWordmarkHoldAndExitPhases() {
        composeTestRule.setContent {
            HeroLogTheme(darkTheme = true) {
                BrandSplash(
                    characterReady = testReady,
                    onFinished = {},
                    elapsedOverride = testElapsed,
                    readyAtOverride = testReadyAt,
                )
            }
        }
        capture("brand_splash_emblem.png", elapsed = 0, readyAt = null, ready = false)
        capture("brand_splash_wordmark.png", elapsed = 750, readyAt = 0, ready = true)
        capture("brand_splash_hold.png", elapsed = 1_500, readyAt = 2_000, ready = false)
        capture("brand_splash_exit.png", elapsed = 1_350, readyAt = 0, ready = true)
    }

    private fun capture(fileName: String, elapsed: Long, readyAt: Long?, ready: Boolean) {
        composeTestRule.runOnIdle {
            testElapsed = elapsed
            testReadyAt = readyAt
            testReady = ready
        }
        composeTestRule.waitForIdle()
        val path = "src/test/screenshots/$fileName"
        composeTestRule.onRoot().captureRoboImage(filePath = path)
        assertTrue("Roborazzi did not record $path", File(path).exists())
    }
}
