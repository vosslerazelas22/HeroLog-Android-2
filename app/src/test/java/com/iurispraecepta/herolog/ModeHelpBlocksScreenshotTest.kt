package com.iurispraecepta.herolog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.iurispraecepta.herolog.data.TITLE_CATALOG
import com.iurispraecepta.herolog.ui.components.ModalVariant
import com.iurispraecepta.herolog.ui.focus.ModeHelpBlocks
import com.iurispraecepta.herolog.ui.focus.RaidModeHelpContent
import com.iurispraecepta.herolog.ui.focus.TitleDisplay
import com.iurispraecepta.herolog.ui.focus.buildStandardLootHelpBlocks
import com.iurispraecepta.herolog.ui.theme.HeroLogTheme
import com.iurispraecepta.herolog.ui.theme.QuestPanel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Baselines dos blocos de ajuda (FR-017): mesmo renderer usado no acordeão
 * inline do IncursionModeModal. Substitui o ModeDescriptionModalScreenshotTest
 * (modal removido na T-016).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class ModeHelpBlocksScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setBlocks(variant: ModalVariant, blocks: List<com.iurispraecepta.herolog.ui.focus.RaidModeHelpBlock>) {
        composeTestRule.setContent {
            HeroLogTheme(darkTheme = true) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(QuestPanel)
                        .padding(16.dp)
                ) {
                    ModeHelpBlocks(blocks = blocks, variant = variant)
                }
            }
        }
    }

    @Test
    fun modeHelpBlocks_standard_amber_screenshot() {
        val blocks = buildStandardLootHelpBlocks(
            studiedMinutes = 25,
            equippedTitleId = "CHAMPION",
            titleLookup = { id ->
                TITLE_CATALOG.find { it.id == id }?.let {
                    TitleDisplay(it.emoji, it.name)
                }
            }
        )
        setBlocks(ModalVariant.Amber, blocks)

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/mode_help_blocks_standard_amber.png")
    }

    @Test
    fun modeHelpBlocks_dungeon_purple_screenshot() {
        setBlocks(ModalVariant.Purple, RaidModeHelpContent.MASMORRA)

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/mode_help_blocks_dungeon_purple.png")
    }

    @Test
    fun modeHelpBlocks_wilderness_red_screenshot() {
        setBlocks(ModalVariant.Red, RaidModeHelpContent.SELVAGEM)

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/mode_help_blocks_wilderness_red.png")
    }
}
