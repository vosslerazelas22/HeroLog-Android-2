package com.iurispraecepta.herolog

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.iurispraecepta.herolog.model.BuffType
import com.iurispraecepta.herolog.model.InventoryItem
import com.iurispraecepta.herolog.model.Rarity
import com.iurispraecepta.herolog.logic.InventoryLogic
import com.iurispraecepta.herolog.ui.components.ItemInspectAction
import com.iurispraecepta.herolog.ui.components.ItemInspectModal
import com.iurispraecepta.herolog.ui.components.ItemInspectVariant
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
class ItemInspectModalScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val equipmentItem = InventoryItem(
        id = "item_sword",
        name = "Espada Inabalável",
        emoji = "⚔️",
        buff = BuffType.UnwaveringSword,
        price = 500,
        desc = "Uma lâmina lendária forjada para combates intensos e foco inabalável.",
        isEquipment = true,
        charges = 5,
        maxCharges = 8,
        rarity = Rarity.Especial
    )

    private val collectibleItem = InventoryItem(
        id = "item_relic",
        name = "Relíquia Arcana",
        emoji = "🏺",
        buff = BuffType.ArcaneRelic,
        price = 1000,
        desc = "Uma relíquia misteriosa com aura encantada de tempos antigos.",
        isEquipment = false
    )

    private val commonEquipmentItem = InventoryItem(
        id = "item_shield",
        name = "Escudo de Treino",
        emoji = "🛡️",
        buff = BuffType.UnwaveringSword,
        price = 100,
        desc = "Um escudo simples de madeira reforçada.",
        isEquipment = true,
        charges = 8,
        maxCharges = 8,
        rarity = Rarity.Comum
    )

    private val nullRarityEquipmentItem = InventoryItem(
        id = "item_charm",
        name = "Amuleto Misterioso",
        emoji = "📿",
        buff = BuffType.ArcaneRelic,
        price = 250,
        desc = "Um amuleto de origem desconhecida.",
        isEquipment = true,
        charges = null,
        maxCharges = null,
        rarity = null
    )

    // LATENTE (D10): variante Primary não tem caller em produção
    // (CharacterScreen usa Danger; InventoryScreen usa Success+Danger).
    // Mantido como cobertura da variante, não como fluxo real.
    @Test
    fun itemInspectModal_latentPrimaryVariant_screenshot() {
        composeTestRule.setContent {
            HeroLogTheme {
                ItemInspectModal(
                    item = equipmentItem,
                    onClose = {},
                    actions = listOf(
                        ItemInspectAction(
                            label = "Equipar",
                            onClick = {},
                            variant = ItemInspectVariant.Primary
                        )
                    )
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/item_inspect_modal_latent_primary.png")
    }

    // Estrutural: ramo isEquipment=false (sem card de durabilidade).
    // Ação Danger única exercita o mesmo caminho de layout do
    // "Desequipar" de CharacterScreen.kt:85-93 (Danger único, w-full).
    @Test
    fun itemInspectModal_nonEquipment_screenshot() {
        composeTestRule.setContent {
            HeroLogTheme {
                ItemInspectModal(
                    item = collectibleItem,
                    onClose = {},
                    actions = listOf(
                        ItemInspectAction(
                            label = "Vender (50 GP)",
                            onClick = {},
                            variant = ItemInspectVariant.Danger
                        )
                    )
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/item_inspect_modal_non_equipment.png")
    }

    // Layout isolado do slot selector. Produção (InventoryScreen.kt:227-237)
    // sempre combina slots + ações — ver itemInspectModal_inventoryFullFlow.
    @Test
    fun itemInspectModal_slotSelector_screenshot() {
        composeTestRule.setContent {
            HeroLogTheme {
                ItemInspectModal(
                    item = equipmentItem,
                    onClose = {},
                    showSlotSelector = true,
                    onSelectSlot = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/item_inspect_modal_slot_selector.png")
    }

    // Layout de duas ações com stretch (D12). Labels ilustrativos;
    // o par real de produção é coberto em itemInspectModal_inventoryFullFlow.
    @Test
    fun itemInspectModal_asymmetricActions_successAndDanger_screenshot() {
        composeTestRule.setContent {
            HeroLogTheme {
                ItemInspectModal(
                    item = equipmentItem,
                    onClose = {},
                    actions = listOf(
                        ItemInspectAction(
                            label = "Equipar",
                            onClick = {},
                            variant = ItemInspectVariant.Success
                        ),
                        ItemInspectAction(
                            label = "Descartar",
                            onClick = {},
                            variant = ItemInspectVariant.Danger
                        )
                    )
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/item_inspect_modal_asymmetric_actions.png")
    }

    // Produção: inspeção de buff ativo em CharacterScreen.kt:81-98
    // (inspectingItem != null, inspectingSlotIdx == null → actions vazio).
    @Test
    fun itemInspectModal_noActions_screenshot() {
        composeTestRule.setContent {
            HeroLogTheme {
                ItemInspectModal(
                    item = collectibleItem,
                    onClose = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/item_inspect_modal_no_actions.png")
    }

    // LATENTE (D10): variante Stone só existe via default do parâmetro —
    // nenhum caller passa Stone explicitamente. Mantido como cobertura
    // da variante, não como fluxo real.
    @Test
    fun itemInspectModal_latentStoneVariant_screenshot() {
        composeTestRule.setContent {
            HeroLogTheme {
                ItemInspectModal(
                    item = collectibleItem,
                    onClose = {},
                    actions = listOf(
                        ItemInspectAction(
                            label = "Inspecionar",
                            onClick = {},
                            variant = ItemInspectVariant.Stone
                        )
                    )
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/item_inspect_modal_latent_stone.png")
    }

    // Ramo rarity=comum (React ItemInspectModal.tsx L83-84).

    @Test
    fun itemInspectModal_commonRarity_screenshot() {
        composeTestRule.setContent {
            HeroLogTheme {
                ItemInspectModal(
                    item = commonEquipmentItem,
                    onClose = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/item_inspect_modal_common_rarity.png")
    }

    // Ramo rarity=null + charges nulos (D14: Android exibe "0 / 8";
    // React exibe vazio — divergência consciente documentada).
    @Test
    fun itemInspectModal_nullRarity_screenshot() {
        composeTestRule.setContent {
            HeroLogTheme {
                ItemInspectModal(
                    item = nullRarityEquipmentItem,
                    onClose = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/item_inspect_modal_null_rarity.png")
    }

    // FLUXO REAL — InventoryScreen.kt:209-237: equipamento inspecionado na
    // mochila mostra slots 1-2-3 + par Vender/Descartar + Voltar.
    // Label e preço via InventoryLogic.calculateSellPrice, idêntico à produção.
    @Test
    fun itemInspectModal_inventoryFullFlow_screenshot() {
        val sellPrice = InventoryLogic.calculateSellPrice(equipmentItem)
        composeTestRule.setContent {
            HeroLogTheme {
                ItemInspectModal(
                    item = equipmentItem,
                    onClose = {},
                    showSlotSelector = true,
                    onSelectSlot = {},
                    actions = listOf(
                        ItemInspectAction(
                            label = "💰 Vender ($sellPrice GP)",
                            onClick = {},
                            variant = ItemInspectVariant.Success
                        ),
                        ItemInspectAction(
                            label = "Descartar",
                            onClick = {},
                            variant = ItemInspectVariant.Danger
                        )
                    )
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/item_inspect_modal_inventory_full_flow.png")
    }

    // FLUXO REAL — CharacterScreen.kt:81-98: equipamento de slot inspecionado
    // mostra ação única Danger "Desequipar" + Voltar, sem slot selector.
    @Test
    fun itemInspectModal_characterUnequipFlow_screenshot() {
        composeTestRule.setContent {
            HeroLogTheme {
                ItemInspectModal(
                    item = equipmentItem,
                    onClose = {},
                    actions = listOf(
                        ItemInspectAction(
                            label = "Desequipar",
                            onClick = {},
                            variant = ItemInspectVariant.Danger
                        )
                    )
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/item_inspect_modal_character_unequip_flow.png")
    }
}
