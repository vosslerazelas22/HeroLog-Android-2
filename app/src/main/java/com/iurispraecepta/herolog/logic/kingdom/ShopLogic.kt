// app/src/main/java/com/iurispraecepta/herolog/logic/kingdom/ShopLogic.kt
package com.iurispraecepta.herolog.logic.kingdom

import com.iurispraecepta.herolog.logic.InventoryLogic
import com.iurispraecepta.herolog.logic.focus.LootTable
import com.iurispraecepta.herolog.model.BuffType
import com.iurispraecepta.herolog.model.InventoryItem

/**
 * Um item do catálogo do Bazar, antes de virar um [InventoryItem] real (sem `id` ainda —
 * o `id` só é atribuído no momento da compra). Espelha o tipo `Omit<InventoryItem, 'id'>`
 * usado por `SHOP_CATALOG` em `ShopTab.tsx`.
 */
data class ShopCatalogEntry(
    val name: String,
    val emoji: String,
    val desc: String,
    val price: Int,
    val buff: BuffType,
    val isEquipment: Boolean? = null,
    val charges: Int? = null,
    val maxCharges: Int? = null
)

/**
 * Catálogo do Bazar — porte literal de `SHOP_CATALOG` (`src/modules/kingdom/ShopTab.tsx`).
 * Dado literal escrito diretamente por Claude a partir da fonte real (não delegado ao AI
 * Studio), seguindo a mesma metodologia adotada para `TitleCatalog.kt`/`LootTable.kt` após o
 * incidente de fabricação de conteúdo (ver `PARIDADE.md`, nota de risco sobre catálogos
 * extensos). Auditado contra o commit `52822c2` (main, 24/08/2026) — 9 itens, ordem preservada.
 */
object ShopCatalog {
    val ITEMS: List<ShopCatalogEntry> = listOf(
        ShopCatalogEntry(
            name = "Pergaminho de Espólios",
            emoji = "📜",
            desc = "Dobra o ganho de Ouro (GP) base na próxima Missão de Foco.",
            price = 200,
            buff = BuffType.DoubleLoot
        ),
        ShopCatalogEntry(
            name = "Elixir do Foco Extremo",
            emoji = "⚗️",
            desc = "Aumenta em +20% a quantidade de Experiência (XP) ganha na próxima sessão.",
            price = 150,
            buff = BuffType.FocusElixir
        ),
        ShopCatalogEntry(
            name = "Cristal da Clareza Rúnica",
            emoji = "💎",
            desc = "Concede +100% de bônus base de XP nas próximas 2 sessões concluídas.",
            price = 400,
            buff = BuffType.CrystalClarity
        ),
        ShopCatalogEntry(
            name = "Runa das Riquezas",
            emoji = "🔮",
            desc = "O herói sintoniza a sorte rúnica para dobrar as recompensas de Ouro.",
            price = 300,
            buff = BuffType.RuneFortune
        ),
        ShopCatalogEntry(
            name = "Escudo do Santuário",
            emoji = "🛡️",
            desc = "Protege e congela automaticamente sua série de dias se você esquecer de focar um dia.",
            price = 500,
            buff = BuffType.StreakShield
        ),
        ShopCatalogEntry(
            name = "Coruja Pixelada",
            emoji = "🦉",
            desc = "Equipável: +5% de XP em todas as sessões. (8 Cargas)",
            price = 250,
            buff = BuffType.PixelOwl,
            isEquipment = true,
            charges = 8,
            maxCharges = 8
        ),
        ShopCatalogEntry(
            name = "Pena de Dragão",
            emoji = "🪶",
            desc = "Equipável: +8% de XP em sessões de 45 min+. (8 Cargas)",
            price = 300,
            buff = BuffType.DragonQuill,
            isEquipment = true,
            charges = 8,
            maxCharges = 8
        ),
        ShopCatalogEntry(
            name = "Bola de Cristal",
            emoji = "🔮",
            desc = "Equipável: +10% de XP em todas as sessões. (10 Cargas)",
            price = 400,
            buff = BuffType.CrystalBall,
            isEquipment = true,
            charges = 10,
            maxCharges = 10
        ),
        ShopCatalogEntry(
            name = "Tomo Antigo",
            emoji = "📖",
            desc = "Equipável: +15% de XP em sessões de 60 min+. (8 Cargas)",
            price = 500,
            buff = BuffType.AncientTome,
            isEquipment = true,
            charges = 8,
            maxCharges = 8
        )
    )
}

/**
 * Resultado de uma compra bem-sucedida no Bazar: novo saldo de ouro + inventário atualizado.
 */
data class ShopPurchaseResult(
    val gold: Int,
    val inventory: List<InventoryItem>
)

/**
 * Porte literal de `buyGoblinShopItem` (`useShop.ts`) + `handlePurchase` (`ShopTab.tsx`).
 *
 * Fiel à fonte real (commit `52822c2`, 24/08/2026):
 * - Ouro insuficiente → no-op (`null`), replicando o guard duplicado da fonte (early-return em
 *   `useShop.ts` E em `ShopTab.tsx`, ambos checando a mesma condição antes de agir).
 * - `id` do item comprado segue o padrão textual `"${buff}_${timestamp}"`, igual à fonte
 *   (`` `${item.buff}_${Date.now()}` ``) — não é um UUID.
 * - `rarity` é derivada por lookup em `LootTable.LOOT_TABLE` pelo campo `buff`, no momento da
 *   compra. Só os 4 itens equipáveis (PixelOwl/DragonQuill/CrystalBall/AncientTome) têm entrada
 *   correspondente na Loot Table — os 4 consumíveis (DoubleLoot/FocusElixir/CrystalClarity/
 *   RuneFortune) e o StreakShield NÃO têm entrada lá, então `rarity` fica `null` para esses 5
 *   itens, fielmente (a fonte só popula `rarity` com spread condicional quando há match).
 * - Reusa `InventoryLogic.addItemToInventory` (já portado no Bloco 7 de Inventário) para anexar
 *   o item ao inventário, igual à fonte (`addItemToInventory` de `inventoryUtils.ts`).
 *
 * **Decisão de escopo consciente, replicando a fonte fielmente**: esta função NÃO verifica se o
 * jogador já possui um `StreakShield` no inventário antes de permitir a compra de outro. Na
 * fonte React, esse bloqueio (`isHoldingShield`) existe SÓ na UI (`ShopTab.tsx` desabilita o
 * botão e troca o texto para "Já ativo"), não em `useShop.ts`. Chamar esta função diretamente
 * permite comprar um segundo StreakShield — a tela (`ShopScreen.kt`, Bloco E) é responsável por
 * replicar o gate visual da fonte, exatamente como no React. Ver `MOC_reino_pre_port.md` seção 2
 * e `PARIDADE.md` para o registro completo desta decisão.
 *
 * `sound.playCoins()` da fonte deliberadamente não portado (SFX fora de escopo, convenção já
 * estabelecida no projeto).
 *
 * @param now timestamp injetável (default `System.currentTimeMillis()`) para permitir testes
 *   determinísticos do formato do `id` gerado.
 */
object ShopLogic {
    fun buyItem(
        gold: Int,
        inventory: List<InventoryItem>,
        catalogEntry: ShopCatalogEntry,
        now: Long = System.currentTimeMillis()
    ): ShopPurchaseResult? {
        if (gold < catalogEntry.price) return null

        val matchingLootItem = LootTable.LOOT_TABLE.find { it.buff == catalogEntry.buff }

        val newItem = InventoryItem(
            id = "${catalogEntry.buff.name}_$now",
            name = catalogEntry.name,
            emoji = catalogEntry.emoji,
            buff = catalogEntry.buff,
            price = catalogEntry.price,
            desc = catalogEntry.desc,
            isEquipment = catalogEntry.isEquipment,
            charges = catalogEntry.charges,
            maxCharges = catalogEntry.maxCharges,
            rarity = matchingLootItem?.rarity
        )

        return ShopPurchaseResult(
            gold = gold - catalogEntry.price,
            inventory = InventoryLogic.addItemToInventory(inventory, newItem)
        )
    }
}