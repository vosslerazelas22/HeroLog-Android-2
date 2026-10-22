package com.iurispraecepta.herolog.ui.inventory

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.R as LucideR
import com.iurispraecepta.herolog.logic.InventoryLogic
import com.iurispraecepta.herolog.model.BuffType
import com.iurispraecepta.herolog.model.InventoryItem
import com.iurispraecepta.herolog.model.Rarity
import com.iurispraecepta.herolog.ui.components.ItemInspectAction
import com.iurispraecepta.herolog.ui.components.ItemInspectModal
import com.iurispraecepta.herolog.ui.components.ItemInspectVariant
import com.iurispraecepta.herolog.ui.navigation.LocalBottomBarInset
import com.iurispraecepta.herolog.ui.theme.Amber100
import com.iurispraecepta.herolog.ui.theme.Champagne400
import com.iurispraecepta.herolog.ui.theme.Champagne500
import com.iurispraecepta.herolog.ui.theme.Cinzel
import com.iurispraecepta.herolog.ui.theme.HeroLogTheme
import com.iurispraecepta.herolog.ui.theme.Inter
import com.iurispraecepta.herolog.ui.theme.JetBrainsMono
import com.iurispraecepta.herolog.ui.theme.QuestPanel
import com.iurispraecepta.herolog.ui.theme.RitualBorder
import com.iurispraecepta.herolog.ui.theme.Stone400
import com.iurispraecepta.herolog.ui.theme.Stone700
import com.iurispraecepta.herolog.ui.theme.Stone800
import com.iurispraecepta.herolog.ui.theme.Stone900
import com.iurispraecepta.herolog.ui.theme.Stone950

/**
 * Aba de Equipamentos (mochila do herói).
 *
 * Redesenho (exploração visual, 21/10/2026): **grade de 2 colunas** com tiles
 * (quadro do emoji, nome, descrição, chip de raridade), header "EQUIPAMENTOS"
 * no mesmo desenho do header da "CÂMARA DE FOCO" (`FocusHeaderBanner`) e da
 * "FICHA DO HERÓI" (`CharacterSheetHeader`), e estado vazio no mesmo desenho do
 * card tracejado das abas de Rituais (`RitualEmptyState`).
 *
 * Paleta: as cores de **equipamento** passaram de âmbar (`Amber500`/`Amber300`)
 * para **champagne** (`Champagne500`/`Champagne400`) — decisão do mantenedor.
 * O nome do item segue creme (`Amber100`) e a descrição cinza (`Stone400`).
 *
 * Comportamento preservado da versão anterior (nada de lógica mudou):
 * - buffs virtuais (`DoubleLoot`, `FocusElixir`, `CrystalClarity`, `RuneFortune`,
 *   `StreakShield`) nunca aparecem na lista — vão para "Efeitos Ativos" da ficha;
 * - a lista só existe quando sobram itens físicos; caso contrário, o estado vazio;
 * - o chip de raridade só aparece quando `rarity != null`;
 * - o realce de equipamento segue `isEquipment == true`;
 * - o `ItemInspectModal` recebe as mesmas ações (vender/descartar) e o mesmo
 *   seletor de slot.
 */
@Composable
fun InventoryScreen(
    inventory: List<InventoryItem>,
    inspectingItem: InventoryItem?,
    onInspectItem: (InventoryItem) -> Unit,
    onCloseInspection: () -> Unit,
    onEquipItem: (InventoryItem, Int) -> Unit,
    onSellItem: (InventoryItem) -> Unit,
    onDiscardItem: (InventoryItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val virtualBuffs = remember {
        setOf(
            BuffType.DoubleLoot,
            BuffType.FocusElixir,
            BuffType.CrystalClarity,
            BuffType.RuneFortune,
            BuffType.StreakShield
        )
    }

    val physicalItems = remember(inventory) {
        inventory.filter { it.buff !in virtualBuffs }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        InventoryHeader()

        Spacer(modifier = Modifier.height(12.dp))

        if (physicalItems.isNotEmpty()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = LocalBottomBarInset.current
                ),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(items = physicalItems, key = { it.id }) { item ->
                    InventoryGridTile(
                        item = item,
                        onClick = { onInspectItem(item) }
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                InventoryEmptyState()
            }
        }
    }

    val modalActions = if (inspectingItem != null) {
        val sellPrice = InventoryLogic.calculateSellPrice(inspectingItem)
        listOf(
            ItemInspectAction(
                label = "💰 Vender ($sellPrice GP)",
                onClick = { onSellItem(inspectingItem) },
                variant = ItemInspectVariant.Success
            ),
            ItemInspectAction(
                label = "Descartar",
                onClick = { onDiscardItem(inspectingItem) },
                variant = ItemInspectVariant.Danger
            )
        )
    } else {
        emptyList()
    }

    ItemInspectModal(
        item = inspectingItem,
        onClose = onCloseInspection,
        showSlotSelector = true,
        onSelectSlot = { slotIdx ->
            if (inspectingItem != null) {
                onEquipItem(inspectingItem, slotIdx)
            }
        },
        actions = modalActions
    )
}

/**
 * Cabeçalho "EQUIPAMENTOS" — mesmo desenho do header da "CÂMARA DE FOCO"
 * (`FocusHeaderBanner`, `MainActivity.kt`) e da "FICHA DO HERÓI"
 * (`CharacterSheetHeader`, `ui/character/CharacterScreen.kt`): ícone 16dp +
 * título Cinzel Black 12sp champagne, centralizado, `min-height 35dp` e
 * divisor de 1dp na base.
 */
@Composable
private fun InventoryHeader(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 35.dp)
            .drawBehind {
                drawRect(
                    color = Champagne500.copy(alpha = 0.10f),
                    topLeft = Offset(0f, size.height - 1.dp.toPx()),
                    size = Size(size.width, 1.dp.toPx())
                )
            }
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 11.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(LucideR.drawable.lucide_ic_backpack),
                contentDescription = null,
                tint = Champagne500,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "EQUIPAMENTOS",
                fontFamily = Cinzel,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                color = Champagne400,
                letterSpacing = 0.6.sp // tracking-wider (0.05em) do React
            )
        }
    }
}

/** Tile da grade: quadro do emoji + nome + descrição + chip de raridade. */
@Composable
private fun InventoryGridTile(
    item: InventoryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEquip = item.isEquipment == true
    val cardBg = if (isEquip) Champagne500.copy(alpha = 0.04f) else Stone900.copy(alpha = 0.60f)
    val cardBorder = if (isEquip) Champagne500.copy(alpha = 0.40f) else Champagne500.copy(alpha = 0.10f)
    val emojiBorder = if (isEquip) Champagne500.copy(alpha = 0.30f) else Stone800

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Stone950.copy(alpha = 0.70f))
                .border(1.dp, emojiBorder, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = item.emoji, fontSize = 30.sp)
        }

        Text(
            text = item.name,
            style = TextStyle(
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                lineHeight = 15.sp,
                color = Amber100
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = itemEffectText(item.desc),
            style = TextStyle(
                fontFamily = Cinzel,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                color = Stone400
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (item.rarity != null) {
                InventoryRarityChip(rarity = item.rarity)
            }
            if (item.charges != null) {
                InventoryChargesChip(item = item)
            }
        }
    }
}

/** Chip de raridade — Especial em champagne, Comum em pedra. */
@Composable
private fun InventoryRarityChip(
    rarity: Rarity,
    modifier: Modifier = Modifier
) {
    val isEspecial = rarity == Rarity.Especial
    val badgeBg = if (isEspecial) Champagne500.copy(alpha = 0.20f) else Stone800.copy(alpha = 0.80f)
    val badgeText = if (isEspecial) Champagne400 else Stone400
    val badgeBorder = if (isEspecial) Champagne500.copy(alpha = 0.40f) else Stone700

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(badgeBg)
            .border(1.dp, badgeBorder, RoundedCornerShape(4.dp))
            .padding(horizontal = 5.dp, vertical = 3.dp)
    ) {
        Text(
            text = if (isEspecial) "Especial" else "Comum",
            style = TextStyle(
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
                color = badgeText,
                letterSpacing = 0.05.em
            )
        )
    }
}

/**
 * Efeito do item para o tile.
 *
 * A descrição do catálogo (`LootTable`/`ShopCatalog`) vem no formato
 * `"Equipável: <efeito>. (N Cargas)"`. Na grade de 2 colunas (~142dp de texto
 * a 375dp) esse texto inteiro estourava as 2 linhas e a reticência cortava
 * justamente o número de cargas. A tela então separa os dois trechos:
 * o sufixo de cargas vira chip próprio ([InventoryChargesChip]) e o prefixo
 * "Equipável: " é dispensado (a equipabilidade segue implícita no card de
 * equipamento e no subtítulo do modal).
 *
 * O recorte do sufixo é por padrão de texto (`(N Cargas)`), independente do
 * valor do modelo — assim o número nunca aparece duplicado nem com conflito
 * com `charges`/`maxCharges`.
 */
private fun itemEffectText(desc: String): String =
    ChargesSuffixRegex.replace(desc, "").removePrefix("Equipável: ")

private val ChargesSuffixRegex = Regex("\\s*\\(\\d+\\s*Cargas?\\)\\s*$")

/**
 * Chip de cargas — `🔋 5/8` (restante/total), mesma notação do bloco de
 * durabilidade do `ItemInspectModal` ("🔋 5 / 8 Cargas"). Estilo pedra para
 * o champagne continuar reservado à raridade "Especial" / card de equipamento.
 */
@Composable
private fun InventoryChargesChip(
    item: InventoryItem,
    modifier: Modifier = Modifier
) {
    val charges = item.charges ?: 0
    val maxCharges = item.maxCharges
    val label = if (maxCharges != null) "🔋 $charges/$maxCharges" else "🔋 $charges"

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Stone800.copy(alpha = 0.80f))
            .border(1.dp, Stone700, RoundedCornerShape(4.dp))
            .padding(horizontal = 5.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
                color = Stone400,
                letterSpacing = 0.05.em
            )
        )
    }
}

/**
 * Estado vazio — mesmo desenho do card tracejado das abas de Rituais
 * (`RitualEmptyState`, `ui/components/RitualTabChrome.kt`): borda tracejada
 * 1dp na **mesma cor** do card das Rituais (`RitualBorder`), fundo nas **cores
 * de equipamento** (champagne). Sem o pulso de opacidade da versão anterior.
 */
@Composable
private fun InventoryEmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    color = RitualBorder,
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                    ),
                    cornerRadius = CornerRadius(12.dp.toPx())
                )
            }
            .background(Champagne500.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
            .padding(horizontal = 24.dp, vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Mochila vazia. Drops ocorrem ao concluir focos ou compre no Bazar.",
            style = TextStyle(
                fontFamily = Inter,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 22.sp,
                color = Stone400,
                textAlign = TextAlign.Center
            )
        )
    }
}

// ============================================================================
// PREVIEWS
// ============================================================================

private fun inventorySampleItems(): List<InventoryItem> = listOf(
    InventoryItem(
        id = "sword_1",
        name = "Espada do Foco Inabalável",
        emoji = "🗡️",
        buff = BuffType.UnwaveringSword,
        price = 110,
        desc = "Equipável: +2% de Ouro em todas as sessões. (5 Cargas)",
        isEquipment = true,
        charges = 5,
        maxCharges = 5,
        rarity = Rarity.Comum
    ),
    InventoryItem(
        id = "owl_1",
        name = "Coruja Pixelada",
        emoji = "🦉",
        buff = BuffType.PixelOwl,
        price = 250,
        desc = "Equipável: +5% de XP em todas as sessões. (8 Cargas)",
        isEquipment = true,
        charges = 8,
        maxCharges = 8,
        rarity = Rarity.Especial
    ),
    InventoryItem(
        id = "chalice_1",
        name = "Cálice Sagrado da Sabedoria",
        emoji = "🏆",
        buff = BuffType.SacredChalice,
        price = 100,
        desc = "Equipável: +1% de XP e +1% de Ouro. (5 Cargas)",
        isEquipment = true,
        charges = 5,
        maxCharges = 5,
        rarity = Rarity.Comum
    ),
    InventoryItem(
        id = "relic_1",
        name = "Relíquia Secreta Arcana",
        emoji = "🔱",
        buff = BuffType.ArcaneRelic,
        price = 130,
        desc = "Equipável: +3% de XP em sessões de 45 min+. (4 Cargas)",
        isEquipment = true,
        charges = 4,
        maxCharges = 4,
        rarity = Rarity.Comum
    ),
    // Item virtual (buff ativo) — some da grade (mesma regra de sempre).
    InventoryItem(
        id = "shield_1",
        name = "Escudo do Santuário",
        emoji = "🛡️",
        buff = BuffType.StreakShield,
        price = 500,
        desc = "Protege e congela automaticamente sua série de dias se você esquecer de focar um dia."
    )
)

@Preview(name = "Equipamentos — com dados", showBackground = true, backgroundColor = 0xFF0B0915, heightDp = 760)
@Composable
private fun InventoryPreviewComDados() {
    HeroLogTheme(darkTheme = true) {
        Box(modifier = Modifier.background(QuestPanel)) {
            InventoryScreen(
                inventory = inventorySampleItems(),
                inspectingItem = null,
                onInspectItem = {},
                onCloseInspection = {},
                onEquipItem = { _, _ -> },
                onSellItem = {},
                onDiscardItem = {}
            )
        }
    }
}

@Preview(name = "Equipamentos — vazio", showBackground = true, backgroundColor = 0xFF0B0915, heightDp = 500)
@Composable
private fun InventoryPreviewVazio() {
    HeroLogTheme(darkTheme = true) {
        Box(modifier = Modifier.background(QuestPanel)) {
            InventoryScreen(
                inventory = emptyList(),
                inspectingItem = null,
                onInspectItem = {},
                onCloseInspection = {},
                onEquipItem = { _, _ -> },
                onSellItem = {},
                onDiscardItem = {}
            )
        }
    }
}

/** Só itens virtuais (buffs ativos) → a lista é filtrada e sobra o estado vazio. */
@Preview(name = "Equipamentos — só buffs virtuais (vazio)", showBackground = true, backgroundColor = 0xFF0B0915, heightDp = 500)
@Composable
private fun InventoryPreviewSoVirtuais() {
    HeroLogTheme(darkTheme = true) {
        Box(modifier = Modifier.background(QuestPanel)) {
            InventoryScreen(
                inventory = listOf(inventorySampleItems().last()),
                inspectingItem = null,
                onInspectItem = {},
                onCloseInspection = {},
                onEquipItem = { _, _ -> },
                onSellItem = {},
                onDiscardItem = {}
            )
        }
    }
}

/** Item sem `rarity` → sem chip (mesma condição `rarity != null`). */
@Preview(name = "Equipamentos — sem raridade (sem chip)", showBackground = true, backgroundColor = 0xFF0B0915, heightDp = 500)
@Composable
private fun InventoryPreviewSemRaridade() {
    HeroLogTheme(darkTheme = true) {
        Box(modifier = Modifier.background(QuestPanel)) {
            InventoryScreen(
                inventory = listOf(
                    inventorySampleItems().first().copy(
                        id = "sem_raridade",
                        name = "Relíquia Arcana",
                        rarity = null,
                        isEquipment = false
                    )
                ),
                inspectingItem = null,
                onInspectItem = {},
                onCloseInspection = {},
                onEquipItem = { _, _ -> },
                onSellItem = {},
                onDiscardItem = {}
            )
        }
    }
}

/** Item já usado: o chip de cargas mostra o restante (`🔋 3/8`). */
@Preview(name = "Equipamentos — item usado (3/8 cargas)", showBackground = true, backgroundColor = 0xFF0B0915, heightDp = 500)
@Composable
private fun InventoryPreviewCargaParcial() {
    HeroLogTheme(darkTheme = true) {
        Box(modifier = Modifier.background(QuestPanel)) {
            InventoryScreen(
                inventory = listOf(
                    inventorySampleItems()[1].copy(
                        id = "carga_parcial",
                        charges = 3,
                        maxCharges = 8
                    )
                ),
                inspectingItem = null,
                onInspectItem = {},
                onCloseInspection = {},
                onEquipItem = { _, _ -> },
                onSellItem = {},
                onDiscardItem = {}
            )
        }
    }
}

/** Nome longo (fixture de borda — não é conteúdo do jogo). */
@Preview(name = "Equipamentos — nome longo", showBackground = true, backgroundColor = 0xFF0B0915, heightDp = 500)
@Composable
private fun InventoryPreviewNomeLongo() {
    HeroLogTheme(darkTheme = true) {
        Box(modifier = Modifier.background(QuestPanel)) {
            InventoryScreen(
                inventory = listOf(
                    inventorySampleItems().first().copy(
                        id = "nome_longo",
                        name = "Espada do Foco Inabalável do Herói Ancestral",
                        rarity = Rarity.Especial
                    )
                ),
                inspectingItem = null,
                onInspectItem = {},
                onCloseInspection = {},
                onEquipItem = { _, _ -> },
                onSellItem = {},
                onDiscardItem = {}
            )
        }
    }
}
