package com.iurispraecepta.herolog

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.iurispraecepta.herolog.model.BuffType
import com.iurispraecepta.herolog.model.CharClass
import com.iurispraecepta.herolog.model.CharacterSummary
import com.iurispraecepta.herolog.model.InventoryItem
import com.iurispraecepta.herolog.model.Rarity
import com.iurispraecepta.herolog.ui.character.CharacterScreen
import com.iurispraecepta.herolog.ui.theme.HeroLogTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class CharacterScreenScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun characterScreen_equipped_title_equipment_buffs_screenshot() {
        val character = CharacterSummary(
            charName = "Aethelgard",
            charClass = CharClass.Warrior,
            equippedTitle = "CHAMPION",
            streak = 12,
            bestStreak = 45,
            totalMinutes = 125,
            combatLevel = 8,
            combatXP = 340,
            hp = 85,
            maxHp = 100
        )

        val equippedEquipment = listOf(
            InventoryItem(
                id = "eq_1",
                name = "Espada Flamejante",
                emoji = "⚔️",
                buff = BuffType.UnwaveringSword,
                price = 500,
                desc = "Espada encantada com chamas purificadoras.",
                isEquipment = true,
                charges = 6,
                maxCharges = 8,
                rarity = Rarity.Especial
            ),
            InventoryItem(
                id = "eq_2",
                name = "Escudo Rúnico",
                emoji = "🛡️",
                buff = BuffType.RunicStone,
                price = 450,
                desc = "Escudo gravado com runas antigas de proteção.",
                isEquipment = true,
                charges = 4,
                maxCharges = 8,
                rarity = Rarity.Comum
            ),
            InventoryItem(
                id = "eq_3",
                name = "Cálice Sagrado",
                emoji = "🏆",
                buff = BuffType.SacredChalice,
                price = 600,
                desc = "Cálice lendário de purificação.",
                isEquipment = true,
                charges = 8,
                maxCharges = 8,
                rarity = Rarity.Especial
            )
        )

        val activeBuffs = listOf(
            InventoryItem(
                id = "buff_1",
                name = "Elixir de Foco",
                emoji = "🧪",
                buff = BuffType.FocusElixir,
                price = 200,
                desc = "Garante clareza mental durante o estudo."
            ),
            InventoryItem(
                id = "buff_2",
                name = "Runa da Fortuna",
                emoji = "✨",
                buff = BuffType.RuneFortune,
                price = 300,
                desc = "Aumenta os ganhos de ouro temporariamente."
            )
        )

        composeTestRule.setContent {
            HeroLogTheme(darkTheme = true) {
                CharacterScreen(
                    character = character,
                    equippedEquipment = equippedEquipment,
                    activeBuffs = activeBuffs,
                    onUnequipItem = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/character_screen_equipped.png")
    }

    @Test
    fun characterScreen_zeroed_state_screenshot() {
        val character = CharacterSummary(
            charName = "Novato",
            charClass = CharClass.Mage,
            equippedTitle = null,
            streak = 0,
            bestStreak = 0,
            totalMinutes = 0,
            combatLevel = 1,
            combatXP = 0,
            hp = 100,
            maxHp = 100
        )

        composeTestRule.setContent {
            HeroLogTheme(darkTheme = true) {
                CharacterScreen(
                    character = character,
                    equippedEquipment = listOf(null, null, null),
                    activeBuffs = emptyList(),
                    onUnequipItem = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/character_screen_zeroed.png")
    }

    @Test
    fun characterScreen_no_title_with_equipment_buffs_screenshot() {
        val character = CharacterSummary(
            charName = "Aventureiro Sem Título",
            charClass = CharClass.Ranger,
            equippedTitle = null,
            streak = 7,
            bestStreak = 15,
            totalMinutes = 480,
            combatLevel = 5,
            combatXP = 250,
            hp = 60,
            maxHp = 100
        )

        val equippedEquipment = listOf(
            InventoryItem(
                id = "eq_1",
                name = "Arco Longo",
                emoji = "🏹",
                buff = BuffType.UnwaveringSword,
                price = 400,
                desc = "Arco de precisão elven.",
                isEquipment = true,
                charges = 3,
                maxCharges = 8,
                rarity = Rarity.Especial
            ),
            null,
            InventoryItem(
                id = "eq_3",
                name = "Poção de Cura",
                emoji = "🧪",
                buff = BuffType.FocusElixir,
                price = 150,
                desc = "Restaura vida instantaneamente.",
                isEquipment = true,
                charges = 2,
                maxCharges = 5,
                rarity = Rarity.Comum
            )
        )

        val activeBuffs = listOf(
            InventoryItem(
                id = "buff_1",
                name = "Bênção da Natureza",
                emoji = "🌿",
                buff = BuffType.RuneFortune,
                price = 350,
                desc = "Aumenta chance de loot raro."
            )
        )

        composeTestRule.setContent {
            HeroLogTheme(darkTheme = true) {
                CharacterScreen(
                    character = character,
                    equippedEquipment = equippedEquipment,
                    activeBuffs = activeBuffs,
                    onUnequipItem = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/character_screen_no_title.png")
    }

    @Test
    fun characterScreen_long_name_screenshot() {
        val character = CharacterSummary(
            charName = "Thaddeus, O Grande Mago das Terras Distantes",
            charClass = CharClass.Mage,
            equippedTitle = "ARCHMAGE",
            streak = 30,
            bestStreak = 30,
            totalMinutes = 1200,
            combatLevel = 15,
            combatXP = 1250,
            hp = 45,
            maxHp = 120
        )

        val equippedEquipment = listOf(
            InventoryItem(
                id = "eq_1",
                name = "Cajado do Arcanjo",
                emoji = "🪄",
                buff = BuffType.UnwaveringSword,
                price = 2000,
                desc = "Cajado lendário de poder arcano.",
                isEquipment = true,
                charges = 8,
                maxCharges = 8,
                rarity = Rarity.Especial
            ),
            InventoryItem(
                id = "eq_2",
                name = "Manto Estelar",
                emoji = "🧥",
                buff = BuffType.RunicStone,
                price = 1800,
                desc = "Manto tecido com luz das estrelas.",
                isEquipment = true,
                charges = 5,
                maxCharges = 8,
                rarity = Rarity.Especial
            ),
            InventoryItem(
                id = "eq_3",
                name = "Orbe da Sabedoria",
                emoji = "🔮",
                buff = BuffType.SacredChalice,
                price = 2500,
                desc = "Concede visão além do tempo.",
                isEquipment = true,
                charges = 8,
                maxCharges = 8,
                rarity = Rarity.Especial
            )
        )

        val activeBuffs = listOf(
            InventoryItem(
                id = "buff_1",
                name = "Elixir de Foco Supremo",
                emoji = "🧪",
                buff = BuffType.FocusElixir,
                price = 500,
                desc = "Foco absoluto por 2 horas."
            ),
            InventoryItem(
                id = "buff_2",
                name = "Aura Protetora",
                emoji = "✨",
                buff = BuffType.RuneFortune,
                price = 600,
                desc = "Imune a dano por 30s."
            ),
            InventoryItem(
                id = "buff_3",
                name = "Bênção Divina",
                emoji = "⛪",
                buff = BuffType.SacredChalice,
                price = 800,
                desc = "XP dobrado nas missões."
            )
        )

        composeTestRule.setContent {
            HeroLogTheme(darkTheme = true) {
                CharacterScreen(
                    character = character,
                    equippedEquipment = equippedEquipment,
                    activeBuffs = activeBuffs,
                    onUnequipItem = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/character_screen_long_name.png")
    }
}