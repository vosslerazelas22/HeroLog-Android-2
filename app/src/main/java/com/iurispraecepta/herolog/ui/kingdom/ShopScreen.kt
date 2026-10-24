// app/src/main/java/com/iurispraecepta/herolog/ui/kingdom/ShopScreen.kt
package com.iurispraecepta.herolog.ui.kingdom

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.iurispraecepta.herolog.data.createInitialCharacterState
import com.iurispraecepta.herolog.logic.kingdom.ShopCatalog
import com.iurispraecepta.herolog.logic.kingdom.ShopCatalogEntry
import com.iurispraecepta.herolog.model.BuffType
import com.iurispraecepta.herolog.model.CharacterState
import com.iurispraecepta.herolog.model.InventoryItem
import com.iurispraecepta.herolog.ui.navigation.LocalBottomBarInset
import com.iurispraecepta.herolog.ui.theme.Amber100
import com.iurispraecepta.herolog.ui.theme.Amber200
import com.iurispraecepta.herolog.ui.theme.Amber500
import com.iurispraecepta.herolog.ui.theme.Champagne300
import com.iurispraecepta.herolog.ui.theme.Champagne400
import com.iurispraecepta.herolog.ui.theme.Champagne500
import com.iurispraecepta.herolog.ui.theme.Cinzel
import com.iurispraecepta.herolog.ui.theme.HeroLogTheme
import com.iurispraecepta.herolog.ui.theme.JetBrainsMono
import com.iurispraecepta.herolog.ui.theme.QuestPanel
import com.iurispraecepta.herolog.ui.theme.Stone500
import com.iurispraecepta.herolog.ui.theme.Stone800
import com.iurispraecepta.herolog.ui.theme.Stone950

/**
 * Tela do Bazar de Mystara — porte do bloco `activeTab === 'shop'` de `App.tsx` (Bloco E),
 * com layout redesenhado na exploração visual da issue #50 (opção V2 escolhida por Bruno).
 *
 * Estrutura fiel à fonte no comportamento: cabeçalho com título + sub-toggle interno entre
 * "Consumíveis & Equipamentos" (`ShopTab.tsx`, este arquivo) e "Títulos" (rótulo abreviado
 * por decisão de produto na issue #50; na fonte "Selos & Títulos Reais" — delegado ao
 * [TitleShopScreen] inalterado). `shopSubTab` é estado local
 * efêmero (`remember`), default "items" — mesmo tratamento do `useState<'items'|'titles'>`
 * real em `App.tsx`.
 *
 * Condições de comportamento idênticas à versão anterior (Bloco E): `canAfford = gold >= price`
 * controla cor do preço/botão e o `clickable` da compra; `isHoldingShield` (StreakShield já no
 * inventário) troca o botão pelo badge "Já ativo" — gate visual só na UI, como na fonte.
 *
 * **Divergências visuais conscientes (issue #50)**:
 * - Cabeçalho centralizado com divisor, na estrutura do `HistoryHeader` das Crônicas Diárias
 *   (antes: alinhado à esquerda, sem divisor);
 * - Sub-abas no estilo `HistoryViewModeToggle` (container boxeado + `animateColorAsState`
 *   em bg/borda/texto, Cinzel Bold 10sp) — antes: botões 9sp sem animação;
 * - Lista em linhas full-width com divisores finos (sem cards), emoji circular 40dp,
 *   descrição sempre expandida sem ellipsis;
 * - Alvo de toque do botão de compra em `heightIn(min = 48.dp)`.
 *
 * Segue a mesma simplificação já estabelecida em `GuideScreen.kt`: sem o painel externo
 * bordado com sombra da fonte — só o cabeçalho + conteúdo, já que o `Scaffold`/sheet de
 * navegação do Android já fornece o contorno visual equivalente.
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

        // ═══ CABEÇALHO centralizado + divisor (mesma estrutura do HistoryHeader
        //     da aba Crônicas Diárias) ═══
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 34.dp)
                    .padding(bottom = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.MonetizationOn,
                        contentDescription = null,
                        tint = Champagne500,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "BAZAR DE MYSTARA",
                        fontFamily = Cinzel,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        letterSpacing = 0.05.em,
                        color = Champagne400
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Amber500.copy(alpha = 0.10f))
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Sub-abas no estilo do HistoryViewModeToggle (Crônicas Diárias):
        // container boxeado + botões com transição de cor animada.
        // heightIn(min = 40.dp) nos botões (mesmo padrão do RaidModeButton):
        // iguala a altura dos dois pills — "Consumíveis & Equipamentos" quebra
        // em 2 linhas (38dp) e "Títulos" em 1 (23dp); sem o mínimo, o pill de
        // Títulos ficava menor e alinhado ao topo enquanto o outro definia a
        // altura da célula. CenterVertically cobre qualquer residual.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(Stone950.copy(alpha = 0.4f))
                .border(1.dp, Amber500.copy(alpha = 0.10f), RoundedCornerShape(4.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShopSubTabButton(
                label = "Consumíveis & Equipamentos",
                selected = shopSubTab == "items",
                onClick = { shopSubTab = "items" },
                modifier = Modifier.weight(1f).testTag("shop_subtab_items")
            )
            ShopSubTabButton(
                label = "Títulos",
                selected = shopSubTab == "titles",
                onClick = { shopSubTab = "titles" },
                modifier = Modifier.weight(1f).testTag("shop_subtab_titles")
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (shopSubTab == "items") {
            ShopItemList(
                gold = gold,
                inventory = inventory,
                onBuyItem = onBuyItem
            )
        } else {
            TitleShopScreen(
                state = state,
                onBuyTitle = onBuyTitle,
                onClaimAchievementTitle = onClaimAchievementTitle
            )
        }
    }
}

/**
 * Botão de sub-aba idêntico ao [com.iurispraecepta.herolog.ui.history.HistoryViewModeButton]
 * (Crônicas Diárias): caixa 4dp, transição de cor animada em bg/borda/texto,
 * Cinzel Bold 10sp caixa alta centralizada.
 */
@Composable
private fun ShopSubTabButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) Champagne500.copy(alpha = 0.1f) else Color.Transparent,
        label = "shop_sub_tab_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) Champagne500.copy(alpha = 0.2f) else Color.Transparent,
        label = "shop_sub_tab_border"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) Champagne300 else Stone500,
        label = "shop_sub_tab_text"
    )

    Box(
        modifier = modifier
            .heightIn(min = 40.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label.uppercase(),
            fontFamily = Cinzel,
            fontSize = 10.sp,
            lineHeight = 15.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.05.em,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Lista do catálogo de itens. Recebe o catálogo diretamente de `ShopCatalog.ITEMS` (Bloco A) —
 * dado estático, não há estado vazio. Linhas full-width com divisores; descrição sempre
 * exibida por completa (expande a linha).
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
        contentPadding = PaddingValues(bottom = LocalBottomBarInset.current)
    ) {
        items(catalog) { entry ->
            val canAfford = gold >= entry.price
            val isHoldingShield = entry.buff == BuffType.StreakShield &&
                inventory.any { it.buff == BuffType.StreakShield }

            ShopItemRow(
                entry = entry,
                canAfford = canAfford,
                isHoldingShield = isHoldingShield,
                onBuy = { onBuyItem(entry) }
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Stone800.copy(alpha = 0.60f))
            )
        }
    }
}

@Composable
private fun ShopItemRow(
    entry: ShopCatalogEntry,
    canAfford: Boolean,
    isHoldingShield: Boolean,
    onBuy: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Amber500.copy(alpha = 0.05f))
                .border(1.dp, Amber500.copy(alpha = 0.10f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = entry.emoji, fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.name,
                fontFamily = Cinzel,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Amber200,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = entry.desc,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = Amber100.copy(alpha = 0.50f)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (isHoldingShield) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Amber500.copy(alpha = 0.05f))
                    .border(1.dp, Amber500.copy(alpha = 0.20f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 10.dp, vertical = 12.dp)
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
                    .background(
                        if (canAfford) Champagne500.copy(alpha = 0.10f)
                        else Stone800.copy(alpha = 0.10f)
                    )
                    .border(
                        width = 1.dp,
                        color = if (canAfford) Champagne500.copy(alpha = 0.40f) else Stone800,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .then(
                        if (canAfford) Modifier.clickable(onClick = onBuy) else Modifier
                    )
                    .heightIn(min = 48.dp)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
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

// ─── Previews (tema escuro + fundo QuestPanel — AGENTS.md §6.10) ───

@Preview(name = "Bazar — com dados", showBackground = true, backgroundColor = 0xFF0B0915, heightDp = 800)
@Composable
private fun ShopScreenPreviewDados() {
    HeroLogTheme(darkTheme = true) {
        Box(modifier = Modifier.background(QuestPanel)) {
            ShopScreen(
                gold = 250,
                inventory = emptyList(),
                state = createInitialCharacterState(),
                onBuyItem = {},
                onBuyTitle = { _, _ -> },
                onClaimAchievementTitle = {}
            )
        }
    }
}

@Preview(name = "Bazar — ouro zero", showBackground = true, backgroundColor = 0xFF0B0915, heightDp = 800)
@Composable
private fun ShopScreenPreviewOuroZero() {
    HeroLogTheme(darkTheme = true) {
        Box(modifier = Modifier.background(QuestPanel)) {
            ShopScreen(
                gold = 0,
                inventory = emptyList(),
                state = createInitialCharacterState(),
                onBuyItem = {},
                onBuyTitle = { _, _ -> },
                onClaimAchievementTitle = {}
            )
        }
    }
}

@Preview(name = "Bazar — Já ativo (shield)", showBackground = true, backgroundColor = 0xFF0B0915, heightDp = 800)
@Composable
private fun ShopScreenPreviewShield() {
    HeroLogTheme(darkTheme = true) {
        Box(modifier = Modifier.background(QuestPanel)) {
            ShopScreen(
                gold = 1000,
                inventory = listOf(
                    InventoryItem(
                        id = "StreakShield_preview",
                        name = "Escudo do Santuário",
                        emoji = "🛡️",
                        buff = BuffType.StreakShield,
                        price = 500,
                        desc = "Protege e congela automaticamente sua série de dias se você esquecer de focar um dia."
                    )
                ),
                state = createInitialCharacterState(),
                onBuyItem = {},
                onBuyTitle = { _, _ -> },
                onClaimAchievementTitle = {}
            )
        }
    }
}
