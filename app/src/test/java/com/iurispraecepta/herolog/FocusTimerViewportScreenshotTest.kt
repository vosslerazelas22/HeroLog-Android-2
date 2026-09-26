package com.iurispraecepta.herolog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.iurispraecepta.herolog.ui.focus.FocusTimerViewport
import com.iurispraecepta.herolog.ui.theme.HeroLogTheme
import com.iurispraecepta.herolog.ui.theme.QuestPanel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w375dp-h667dp", sdk = [36])
class FocusTimerViewportSmallScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        composeTestRule.mainClock.autoAdvance = false
    }

    @Test
    fun focusTimerViewport_idle_375x667() {
        composeTestRule.setContent {
            HeroLogTheme(darkTheme = true) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(QuestPanel)
                ) {
                FocusTimerViewport(
                    availableHeight = 667.dp - 8.dp,
                    modifier = Modifier.fillMaxWidth(),
                    top = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .background(Color(0x33E5C158)),
                            contentAlignment = Alignment.Center
                        ) { Text("carousel", color = Color.White) }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(22.dp)
                        )
                    },
                    timer = {
                        Box(
                            modifier = Modifier
                                .height(256.dp)
                                .padding(horizontal = 16.dp)
                                .background(Color(0x66A855F7)),
                            contentAlignment = Alignment.Center
                        ) { Text("FocusOrb", color = Color.White) }
                    },
                    bottom = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .background(Color(0x33E5C158)),
                            contentAlignment = Alignment.Center
                        ) { Text("CTA", color = Color.White) }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .background(Color(0x22FFFFFF))
                        )
                    }
                )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/focus_timer_viewport_375x667.png")
    }
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w390dp-h844dp", sdk = [36])
class FocusTimerViewportMediumScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        composeTestRule.mainClock.autoAdvance = false
    }

    @Test
    fun focusTimerViewport_idle_390x844() {
        composeTestRule.setContent {
            HeroLogTheme(darkTheme = true) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(QuestPanel)
                ) {
                FocusTimerViewport(
                    availableHeight = 844.dp - 8.dp,
                    modifier = Modifier.fillMaxWidth(),
                    top = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .background(Color(0x33E5C158)),
                            contentAlignment = Alignment.Center
                        ) { Text("carousel", color = Color.White) }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(22.dp)
                        )
                    },
                    timer = {
                        Box(
                            modifier = Modifier
                                .height(256.dp)
                                .padding(horizontal = 16.dp)
                                .background(Color(0x66A855F7)),
                            contentAlignment = Alignment.Center
                        ) { Text("FocusOrb", color = Color.White) }
                    },
                    bottom = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .background(Color(0x33E5C158)),
                            contentAlignment = Alignment.Center
                        ) { Text("CTA", color = Color.White) }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .background(Color(0x22FFFFFF))
                        )
                    }
                )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/focus_timer_viewport_390x844.png")
    }
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w412dp-h915dp", sdk = [36])
class FocusTimerViewportTallScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        composeTestRule.mainClock.autoAdvance = false
    }

    @Test
    fun focusTimerViewport_idle_412x915() {
        composeTestRule.setContent {
            HeroLogTheme(darkTheme = true) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(QuestPanel)
                ) {
                FocusTimerViewport(
                    availableHeight = 915.dp - 8.dp,
                    modifier = Modifier.fillMaxWidth(),
                    top = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .background(Color(0x33E5C158)),
                            contentAlignment = Alignment.Center
                        ) { Text("carousel", color = Color.White) }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(22.dp)
                        )
                    },
                    timer = {
                        Box(
                            modifier = Modifier
                                .height(256.dp)
                                .padding(horizontal = 16.dp)
                                .background(Color(0x66A855F7)),
                            contentAlignment = Alignment.Center
                        ) { Text("FocusOrb", color = Color.White) }
                    },
                    bottom = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .background(Color(0x33E5C158)),
                            contentAlignment = Alignment.Center
                        ) { Text("CTA", color = Color.White) }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .background(Color(0x22FFFFFF))
                        )
                    }
                )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/focus_timer_viewport_412x915.png")
    }
}
