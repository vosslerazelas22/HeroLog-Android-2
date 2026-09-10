// app/src/main/java/com/iurispraecepta/herolog/ui/kingdom/ShopScreen.kt
package com.iurispraecepta.herolog.ui.kingdom

import com.iurispraecepta.herolog.ui.navigation.LocalBottomBarInset
import androidx.compose.foundation.layout.PaddingValues

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.iurispraecepta.herolog.logic.kingdom.ShopCatalog
import com.iurispraecepta.herolog.logic.kingdom.ShopCatalogEntry
import com.iurispraecepta.herolog.model.BuffType
import com.iurispraecepta.herolog.model.CharacterState
import com.iurispraecepta.herolog.model.InventoryItem
import com.iurispraecepta.herolog.ui.theme.Amber500
import com.iurispraecepta.herolog.ui.theme.Cinzel
import com.iurispraecepta.herolog.ui.theme.JetBrainsMono
import com.iurispraecepta.herolog.ui.theme.Stone400
import com.iurispraecepta.herolog.ui.theme.Stone800
import com.iurispraecepta.herolog.ui.theme.Stone950

// ─── Cores sem equivalente nomeado no tema global (mesmo padrão de GuideScreen.kt/HistoryScreen.kt) ───
private val Champagne300 = Color(0xFFF5DFA0)
private val Champagne400 = Color(0xFFE5C158)
private val Champagne500 = Color(0xFFD4AF37)
private val Amber200 = Color(0xFFFDE68A)
private val Amber100Half = Color(0xFFFEF3C7) // amber-100, usado com alpha p/ desc
private val Stone500 = Color(0xFF78716C) // stone-500 exato (não-confundir com Stone400)

/**
 * Tela do Bazar de Mystara — porte do bloco `activeTab === 'shop'` de `App.tsx` (Bloco E).
 *
 * Estrutura fiel à fonte: cabeçalho com título + sub-toggle interno entre "Consumíveis &
 * Equipamentos" (`ShopTab.tsx`, este arquivo) e "Selos & Títulos Reais" (`TitleShop.tsx`).
 * `shopSubTab` é estado local efêmero (`remember`), default "items" — mesmo tratamento já
 * aprovado pro `questsSubTab` de Missões (Bloco I): sobrevive à navegação dentro da sessão,
 * reseta ao trocar de módulo/reabrir o app, igual ao `useState<'items'|'titles'>('items')`
 * real em `App.tsx`.
 *
 * **Pendência conhecida, não um bug**: a sub-aba "Selos & Títulos Reais" ainda não tem
 * `TitleShopScreen.kt` (Bloco C do módulo Reino, ainda não feito) — mostra um placeholder
 * temporário até esse Bloco fechar. Trocar por `TitleShopScreen(...)` real quando existir.
 *
 * Segue a mesma simplificação já estabelecida em `GuideScreen.kt`: sem o painel externo
 * bordado com sombra (`bg-quest-panel`/shadow) da fonte — só o cabeçalho + conteúdo, já que o
 * `Scaffold`/sheet de navegação do Android já fornece o contorno visual equivalente.
 */
@Composable
fun ShopScreen(
    gold: Int,
    inventory: List<InventoryItem>,
    state: CharacterState,
    onBuyItem: (ShopCatalogEntry) -> Unit,
    onBuyTitle: (String, Int) -> Unit,
    onClaimAchievementTitle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var shopSubTab by remember { mutableStateOf("items") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // ═══ CABEÇALHO (vem do wrapper em App.tsx) ═══
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.MonetizationOn,
                contentDescription = null,
                tint = Champagne500,
                modifier = Modifier.height(16.dp).width(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "BAZAR DE MYSTARA",
                fontFamily = Cinzel,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                letterSpacing = 0.05.em,
                color = Champagne400
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ═══ SUB-TOGGLE: Consumíveis & Equipamentos / Selos & Títulos Reais ═══
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Stone950.copy(alpha = 0.40f), RoundedCornerShape(6.dp))
                .border(1.dp, Amber500.copy(alpha = 0.10f), RoundedCornerShape(6.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ShopSubTabButton(
                label = "Consumíveis & Equipamentos",
                selected = shopSubTab == "items",
                onClick = { shopSubTab = "items" },
                modifier = Modifier.weight(1f)
            )
            ShopSubTabButton(
                label = "Selos & Títulos Reais",
                selected = shopSubTab == "titles",
                onClick = { shopSubTab = "titles" },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (shopSubTab == "items") {
            ShopItemList(gold = gold, inventory = inventory, onBuyItem = onBuyItem)
        } else {
            TitleShopScreen(
                state = state,
                onBuyTitle = onBuyTitle,
                onClaimAchievementTitle = onClaimAchievementTitle
            )
        }
    }
}

@Composable
private fun ShopSubTabButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(
                if (selected) Champagne500.copy(alpha = 0.10f) else Color.Transparent
            )
            .then(
                if (selected) {
                    Modifier.border(1.dp, Champagne500.copy(alpha = 0.20f), RoundedCornerShape(4.dp))
                } else Modifier
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label.uppercase(),
            fontFamily = Cinzel,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            letterSpacing = 0.06.em,
            color = if (selected) Champagne300 else Stone400,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Porte de `ShopTab.tsx`. Recebe o catálogo diretamente de `ShopCatalog.ITEMS` (Bloco A) — não
 * há necessidade de passar o catálogo como parâmetro, é dado estático.
 */
@Composable
private fun ShopItemList(
    gold: Int,
    inventory: List<InventoryItem>,
    onBuyItem: (ShopCatalogEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val catalog = remember { ShopCatalog.ITEMS }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = LocalBottomBarInset.current)
    ) {
        items(catalog) { entry ->
            val canAfford = gold >= entry.price
            val isHoldingShield = entry.buff == BuffType.StreakShield &&
                inventory.any { it.buff == BuffType.StreakShield }

            ShopItemCard(
                entry = entry,
                canAfford = canAfford,
                isHoldingShield = isHoldingShield,
                onBuy = { onBuyItem(entry) }
            )
        }
    }
}

@Composable
private fun ShopItemCard(
    entry: ShopCatalogEntry,
    canAfford: Boolean,
    isHoldingShield: Boolean,
    onBuy: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Stone950.copy(alpha = 0.20f))
            .border(
                width = 1.dp,
                color = if (canAfford) Amber500.copy(alpha = 0.10f) else Stone800,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f, fill = false),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Amber500.copy(alpha = 0.05f))
                    .border(1.dp, Amber500.copy(alpha = 0.10f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = entry.emoji, fontSize = 22.sp)
            }

            Column {
                Text(
                    text = entry.name,
                    fontFamily = Cinzel,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Amber200
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = entry.desc,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = Amber100Half.copy(alpha = 0.50f)
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (isHoldingShield) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Amber500.copy(alpha = 0.05f))
                    .border(1.dp, Amber500.copy(alpha = 0.20f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Já ativo",
                    fontFamily = Cinzel,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp,
                    color = Amber500
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (canAfford) Champagne500.copy(alpha = 0.10f) else Stone800.copy(alpha = 0.10f))
                    .border(
                        width = 1.dp,
                        color = if (canAfford) Champagne500.copy(alpha = 0.40f) else Stone800,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .then(
                        if (canAfford) Modifier.clickable(onClick = onBuy) else Modifier
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${entry.price} GP",
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (canAfford) Champagne300 else Stone500
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = if (canAfford) Champagne300 else Stone500,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}