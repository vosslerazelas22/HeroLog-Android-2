// app/src/test/java/com/iurispraecepta/herolog/ShopLogicTest.kt
package com.iurispraecepta.herolog

import com.iurispraecepta.herolog.logic.focus.LootTable
import com.iurispraecepta.herolog.logic.kingdom.ShopCatalog
import com.iurispraecepta.herolog.logic.kingdom.ShopLogic
import com.iurispraecepta.herolog.model.BuffType
import com.iurispraecepta.herolog.model.InventoryItem
import com.iurispraecepta.herolog.model.Rarity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ShopLogicTest {

    // ---------------------------------------------------------------------
    // Integridade do catálogo
    // ---------------------------------------------------------------------

    @Test
    fun shopCatalog_hasExactlyNineItemsInSourceOrder() {
        val expectedBuffsInOrder = listOf(
            BuffType.DoubleLoot,
            BuffType.FocusElixir,
            BuffType.CrystalClarity,
            BuffType.RuneFortune,
            BuffType.StreakShield,
            BuffType.PixelOwl,
            BuffType.DragonQuill,
            BuffType.CrystalBall,
            BuffType.AncientTome
        )

        assertEquals(9, ShopCatalog.ITEMS.size)
        assertEquals(expectedBuffsInOrder, ShopCatalog.ITEMS.map { it.buff })
    }

    @Test
    fun shopCatalog_pricesMatchSource() {
        val pricesByBuff = ShopCatalog.ITEMS.associate { it.buff to it.price }

        assertEquals(200, pricesByBuff[BuffType.DoubleLoot])
        assertEquals(150, pricesByBuff[BuffType.FocusElixir])
        assertEquals(400, pricesByBuff[BuffType.CrystalClarity])
        assertEquals(300, pricesByBuff[BuffType.RuneFortune])
        assertEquals(500, pricesByBuff[BuffType.StreakShield])
        assertEquals(250, pricesByBuff[BuffType.PixelOwl])
        assertEquals(300, pricesByBuff[BuffType.DragonQuill])
        assertEquals(400, pricesByBuff[BuffType.CrystalBall])
        assertEquals(500, pricesByBuff[BuffType.AncientTome])
    }

    @Test
    fun shopCatalog_onlyTheFourEquipablesCarryChargeFields() {
        val equipables = ShopCatalog.ITEMS.filter { it.isEquipment == true }
        val nonEquipables = ShopCatalog.ITEMS.filter { it.isEquipment != true }

        assertEquals(4, equipables.size)
        assertEquals(5, nonEquipables.size)
        assertTrue(equipables.all { it.charges != null && it.maxCharges != null })
        assertTrue(nonEquipables.all { it.charges == null && it.maxCharges == null })
    }

    // ---------------------------------------------------------------------
    // buyItem — guard de ouro
    // ---------------------------------------------------------------------

    @Test
    fun buyItem_insufficientGold_returnsNull() {
        val entry = ShopCatalog.ITEMS.first { it.buff == BuffType.DoubleLoot } // price 200

        val result = ShopLogic.buyItem(gold = 199, inventory = emptyList(), catalogEntry = entry)

        assertNull(result)
    }

    @Test
    fun buyItem_exactGold_succeeds() {
        val entry = ShopCatalog.ITEMS.first { it.buff == BuffType.DoubleLoot } // price 200

        val result = ShopLogic.buyItem(gold = 200, inventory = emptyList(), catalogEntry = entry)

        assertEquals(0, result?.gold)
        assertEquals(1, result?.inventory?.size)
    }

    @Test
    fun buyItem_sufficientGold_deductsPriceAndAppendsToInventory() {
        val entry = ShopCatalog.ITEMS.first { it.buff == BuffType.FocusElixir } // price 150
        val existingItem = InventoryItem(
            id = "existing_1",
            name = "Item Existente",
            emoji = "🗡️",
            buff = BuffType.UnwaveringSword,
            price = 110,
            desc = "desc"
        )

        val result = ShopLogic.buyItem(
            gold = 500,
            inventory = listOf(existingItem),
            catalogEntry = entry
        )!!

        assertEquals(350, result.gold)
        assertEquals(2, result.inventory.size)
        assertEquals(existingItem, result.inventory[0])
        assertEquals("Elixir do Foco Extremo", result.inventory[1].name)
        assertEquals(150, result.inventory[1].price)
    }

    // ---------------------------------------------------------------------
    // buyItem — formato do id
    // ---------------------------------------------------------------------

    @Test
    fun buyItem_generatesIdAsBuffUnderscoreTimestamp_matchingSourcePattern() {
        val entry = ShopCatalog.ITEMS.first { it.buff == BuffType.RuneFortune }

        val result = ShopLogic.buyItem(
            gold = 1000,
            inventory = emptyList(),
            catalogEntry = entry,
            now = 1_724_500_000_000L
        )!!

        assertEquals("RuneFortune_1724500000000", result.inventory[0].id)
    }

    // ---------------------------------------------------------------------
    // buyItem — rarity via lookup na LootTable
    // ---------------------------------------------------------------------

    @Test
    fun buyItem_equipableItem_setsRarityFromLootTableMatch() {
        val entry = ShopCatalog.ITEMS.first { it.buff == BuffType.PixelOwl }
        // confirma pré-condição: a LootTable real tem mesmo uma entrada Especial pra PixelOwl
        val expectedRarity = LootTable.LOOT_TABLE.first { it.buff == BuffType.PixelOwl }.rarity
        assertEquals(Rarity.Especial, expectedRarity)

        val result = ShopLogic.buyItem(gold = 1000, inventory = emptyList(), catalogEntry = entry)!!

        assertEquals(Rarity.Especial, result.inventory[0].rarity)
    }

    @Test
    fun buyItem_consumableItemsWithNoLootTableEntry_leaveRarityNull() {
        // DoubleLoot/FocusElixir/CrystalClarity/RuneFortune/StreakShield não têm entrada na
        // LootTable real — fielmente, rarity deve ficar null pros 5, não só "não especial".
        val consumableBuffs = listOf(
            BuffType.DoubleLoot,
            BuffType.FocusElixir,
            BuffType.CrystalClarity,
            BuffType.RuneFortune,
            BuffType.StreakShield
        )

        consumableBuffs.forEach { buff ->
            val entry = ShopCatalog.ITEMS.first { it.buff == buff }
            val result = ShopLogic.buyItem(gold = 1000, inventory = emptyList(), catalogEntry = entry)!!
            assertNull("rarity deveria ser null para $buff", result.inventory[0].rarity)
        }
    }

    // ---------------------------------------------------------------------
    // buyItem — StreakShield duplicado (decisão de fidelidade consciente)
    // ---------------------------------------------------------------------

    @Test
    fun buyItem_allowsBuyingSecondStreakShield_sinceGateIsUiOnlyInSource() {
        val entry = ShopCatalog.ITEMS.first { it.buff == BuffType.StreakShield }
        val existingShield = InventoryItem(
            id = "StreakShield_1",
            name = "Escudo do Santuário",
            emoji = "🛡️",
            buff = BuffType.StreakShield,
            price = 500,
            desc = "desc"
        )

        val result = ShopLogic.buyItem(
            gold = 1000,
            inventory = listOf(existingShield),
            catalogEntry = entry
        )

        // Fiel à fonte: useShop.ts não bloqueia isso, só a UI de ShopTab.tsx desabilita o botão.
        assertEquals(2, result?.inventory?.count { it.buff == BuffType.StreakShield })
    }
}