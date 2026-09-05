package com.iurispraecepta.herolog

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.iurispraecepta.herolog.model.CharClass
import com.iurispraecepta.herolog.model.OrbConcept
import com.iurispraecepta.herolog.ui.components.GeneralSettingsModal
import com.iurispraecepta.herolog.ui.theme.HeroLogTheme
import kotlinx.coroutines.test.TestScope
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GeneralSettingsModalScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testScope = TestScope()

    @Test
    fun generalSettings_mage_classA_selected() {
        composeTestRule.setContent {
            HeroLogTheme {
                GeneralSettingsModal(
                    isOpen = true,
                    onDismiss = {},
                    characterName = "Aethel",
                    charClass = CharClass.Mage,
                    orbConcept = OrbConcept.A,
                    onNameChange = {},
                    onClassChange = {},
                    onOrbConceptChange = {},
                    coroutineScope = testScope
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/general_settings_mage_classA.png")
    }

    @Test
    fun generalSettings_warrior_classB_selected() {
        composeTestRule.setContent {
            HeroLogTheme {
                GeneralSettingsModal(
                    isOpen = true,
                    onDismiss = {},
                    characterName = "Thorin",
                    charClass = CharClass.Warrior,
                    orbConcept = OrbConcept.B,
                    onNameChange = {},
                    onClassChange = {},
                    onOrbConceptChange = {},
                    coroutineScope = testScope
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/general_settings_warrior_classB.png")
    }

    @Test
    fun generalSettings_ranger_classC_selected() {
        composeTestRule.setContent {
            HeroLogTheme {
                GeneralSettingsModal(
                    isOpen = true,
                    onDismiss = {},
                    characterName = "Legolas",
                    charClass = CharClass.Ranger,
                    orbConcept = OrbConcept.C,
                    onNameChange = {},
                    onClassChange = {},
                    onOrbConceptChange = {},
                    coroutineScope = testScope
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/general_settings_ranger_classC.png")
    }

    @Test
    fun generalSettings_mage_classD_selected() {
        composeTestRule.setContent {
            HeroLogTheme {
                GeneralSettingsModal(
                    isOpen = true,
                    onDismiss = {},
                    characterName = "Gandalf",
                    charClass = CharClass.Mage,
                    orbConcept = OrbConcept.D,
                    onNameChange = {},
                    onClassChange = {},
                    onOrbConceptChange = {},
                    coroutineScope = testScope
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/general_settings_mage_classD.png")
    }

    @Test
    fun generalSettings_closed() {
        composeTestRule.setContent {
            HeroLogTheme {
                GeneralSettingsModal(
                    isOpen = false,
                    onDismiss = {},
                    characterName = "",
                    charClass = CharClass.Mage,
                    orbConcept = OrbConcept.D,
                    onNameChange = {},
                    onClassChange = {},
                    onOrbConceptChange = {},
                    coroutineScope = testScope
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/general_settings_closed.png")
    }
}
