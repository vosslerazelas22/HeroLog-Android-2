package com.iurispraecepta.herolog.ui.character

import com.iurispraecepta.herolog.logic.CharacterViewLogic
import com.iurispraecepta.herolog.ui.navigation.LocalBottomBarInset

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.R as LucideR
import com.iurispraecepta.herolog.R
import com.iurispraecepta.herolog.data.TITLE_CATALOG
import com.iurispraecepta.herolog.model.CharClass
import com.iurispraecepta.herolog.model.CharacterSummary
import com.iurispraecepta.herolog.model.InventoryItem
import com.iurispraecepta.herolog.ui.components.HeroLogModal
import com.iurispraecepta.herolog.ui.components.ItemInspectAction
import com.iurispraecepta.herolog.ui.components.ItemInspectModal
import com.iurispraecepta.herolog.ui.components.ItemInspectVariant
import com.iurispraecepta.herolog.ui.theme.Amber300
import com.iurispraecepta.herolog.ui.theme.Amber400
import com.iurispraecepta.herolog.ui.theme.Amber500
import com.iurispraecepta.herolog.ui.theme.Champagne300
import com.iurispraecepta.herolog.ui.theme.Champagne400
import com.iurispraecepta.herolog.ui.theme.Champagne500
import com.iurispraecepta.herolog.ui.theme.Cinzel
import com.iurispraecepta.herolog.ui.theme.Emerald300
import com.iurispraecepta.herolog.ui.theme.Emerald400
import com.iurispraecepta.herolog.ui.theme.Emerald500
import com.iurispraecepta.herolog.ui.theme.JetBrainsMono
import com.iurispraecepta.herolog.ui.theme.Purple300
import com.iurispraecepta.herolog.ui.theme.Purple400
import com.iurispraecepta.herolog.ui.theme.Purple500
import com.iurispraecepta.herolog.ui.theme.Purple950
import com.iurispraecepta.herolog.ui.theme.Red500
import com.iurispraecepta.herolog.ui.theme.Red600
import com.iurispraecepta.herolog.ui.theme.Rose400
import com.iurispraecepta.herolog.ui.theme.Stone300
import com.iurispraecepta.herolog.ui.theme.Stone400
import com.iurispraecepta.herolog.ui.theme.Stone500
import com.iurispraecepta.herolog.ui.theme.Stone600
import com.iurispraecepta.herolog.ui.theme.Stone700
import com.iurispraecepta.herolog.ui.theme.Stone800
import com.iurispraecepta.herolog.ui.theme.Stone900
import com.iurispraecepta.herolog.ui.theme.Stone950

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CharacterScreen(
    character: CharacterSummary,
    equippedEquipment: List<InventoryItem?>,
    activeBuffs: List<InventoryItem>,
    onUnequipItem: (Int) -> Unit,
    ownedTitles: List<String> = emptyList(),
    onEquipTitle: (String?) -> Unit = {},
    onNavigateToInventory: () -> Unit = {}
) {
    var isTitleModalOpen by remember { mutableStateOf(false) }
    var inspectingItem by remember { mutableStateOf<InventoryItem?>(null) }
    var inspectingSlotIdx by remember { mutableStateOf<Int?>(null) }

    val modalActions = remember(inspectingItem, inspectingSlotIdx) {
        if (inspectingItem != null && inspectingSlotIdx != null) {
            val slotIdx = inspectingSlotIdx!!
            listOf(
                ItemInspectAction(
                    label = "Desequipar",
                    onClick = {
                        onUnequipItem(slotIdx)
                        inspectingItem = null
                        inspectingSlotIdx = null
                    },
                    variant = ItemInspectVariant.Danger
                )
            )
        } else {
            emptyList()
        }
    }

    val avatarRes = when (character.charClass) {
        CharClass.Mage -> R.drawable.mage_idle
        CharClass.Warrior -> R.drawable.warrior_idle
        CharClass.Ranger -> R.drawable.ranger_idle
    }

    val classLabel = when (character.charClass) {
        CharClass.Mage -> "🧙 Mago d'Arraia"
        CharClass.Warrior -> "🛡️ Guerreiro de Aço"
        CharClass.Ranger -> "🏹 Patrulheiro Silvestre"
    }

    val titleItem = if (character.equippedTitle != null) {
        TITLE_CATALOG.firstOrNull { it.id == character.equippedTitle }
    } else {
        null
    }

    val equippedTitleCategory = titleItem?.category

    val rarityBadge = CharacterViewLogic.rarityBadge(equippedTitleCategory)
    val xpMax = CharacterViewLogic.xpMax(character.combatLevel)
    val xpPercent = CharacterViewLogic.xpPercent(character.combatXP, character.combatLevel)
    val hpProgress = CharacterViewLogic.hpProgress(character.hp, character.maxHp)
    val streakLabel = CharacterViewLogic.streakLabel(character.streak)
    val totalFocusFormatted = CharacterViewLogic.formatHoursMinutes(character.totalMinutes)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = LocalBottomBarInset.current),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        IdentityCard(
            character = character,
            avatarRes = avatarRes,
            classLabel = classLabel,
            titleItem = titleItem,
            rarityBadge = rarityBadge,
            onTitleClick = { isTitleModalOpen = true }
        )

        ProgressionCard(
            combatLevel = character.combatLevel,
            combatXP = character.combatXP,
            xpMax = xpMax,
            xpPercent = xpPercent,
            streak = character.streak,
            bestStreak = character.bestStreak,
            streakLabel = streakLabel,
            totalMinutes = character.totalMinutes,
            totalFocusFormatted = totalFocusFormatted,
            hp = character.hp,
            maxHp = character.maxHp,
            hpProgress = hpProgress
        )

        EquipmentCard(
            equippedEquipment = equippedEquipment,
            onUnequipItem = onUnequipItem,
            onInspectItem = { item, slotIdx ->
                inspectingItem = item
                inspectingSlotIdx = slotIdx
            },
            onNavigateToInventory = onNavigateToInventory
        )

        ActiveEffectsCard(
            activeBuffs = activeBuffs,
            onInspectItem = { item ->
                inspectingItem = item
                inspectingSlotIdx = null
            }
        )
    }

    // ITEM INSPECT MODAL
    ItemInspectModal(
        item = inspectingItem,
        onClose = {
            inspectingItem = null
            inspectingSlotIdx = null
        },
        actions = modalActions
    )

    // TITLE EQUIP MODAL
    TitleEquipModal(
        isOpen = isTitleModalOpen,
        onClose = { isTitleModalOpen = false },
        ownedTitles = ownedTitles,
        equippedTitle = character.equippedTitle,
        onEquipTitle = onEquipTitle
    )
}

@Composable
private fun IdentityCard(
    character: CharacterSummary,
    avatarRes: Int,
    classLabel: String,
    titleItem: com.iurispraecepta.herolog.data.TitleItem?,
    rarityBadge: CharacterViewLogic.RarityBadge,
    onTitleClick: () -> Unit
) {
    // Cantos decorativos (12dp, borda 2dp champagne-400/40)
    @Composable
    fun DecorativeCorner() {
        Box(
            modifier = Modifier
                .size(12.dp)
                .border(2.dp, Champagne400.copy(alpha = 0.4f), RoundedCornerShape(0.dp))
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Stone950.copy(alpha = 0.8f))
            .border(1.dp, Champagne500.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(16.dp)
            .clip(RoundedCornerShape(12.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Decorative corners
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.align(Alignment.TopStart), contentAlignment = Alignment.TopStart) { DecorativeCorner() }
            Box(modifier = Modifier.align(Alignment.TopEnd), contentAlignment = Alignment.TopEnd) { DecorativeCorner() }
            Box(modifier = Modifier.align(Alignment.BottomStart), contentAlignment = Alignment.BottomStart) { DecorativeCorner() }
            Box(modifier = Modifier.align(Alignment.BottomEnd), contentAlignment = Alignment.BottomEnd) { DecorativeCorner() }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar (96dp)
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Stone950)
                    .border(2.dp, Champagne400.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = avatarRes),
                    contentDescription = character.charName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Purple500.copy(alpha = 0.5f), Color.Transparent)
                            )
                        )
                        .clip(RoundedCornerShape(16.dp))
                )
            }

            // Name + Rarity Badge
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = character.charName,
                        style = TextStyle(
                            fontFamily = Cinzel,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = Champagne300,
                            letterSpacing = 0.02.em,
                            textAlign = TextAlign.Center
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                            .background(rarityBadge.backgroundColor)
                            .border(1.dp, rarityBadge.borderColor, RoundedCornerShape(999.dp))
                            .graphicsLayer { shadowElevation = 4.dp.toPx() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = rarityBadge.label,
                            style = TextStyle(
                                fontFamily = JetBrainsMono,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = rarityBadge.textColor,
                                letterSpacing = 0.5.em,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }

                // Class
                Text(
                    text = classLabel,
                    style = TextStyle(
                        fontFamily = Cinzel,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Purple400,
                        letterSpacing = 0.1.em,
                        textAlign = TextAlign.Center
                    )
                )

                // Title button
                if (titleItem != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(Champagne500.copy(alpha = 0.1f))
                            .border(1.dp, Champagne500.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                            .clickable { onTitleClick() }
                            .padding(vertical = 4.dp, horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = titleItem.emoji,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = titleItem.name,
                                style = TextStyle(
                                    fontFamily = Cinzel,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = Champagne300,
                                    letterSpacing = 0.05.em
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "· Alterar",
                                style = TextStyle(
                                    fontFamily = JetBrainsMono,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 9.sp,
                                    color = Stone500,
                                    letterSpacing = 0.05.em
                                )
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(Stone900.copy(alpha = 0.8f))
                            .border(1.dp, Stone800, RoundedCornerShape(4.dp))
                            .clickable { onTitleClick() }
                            .padding(vertical = 4.dp, horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+ Equipar título",
                            style = TextStyle(
                                fontFamily = Cinzel,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = Stone400,
                                letterSpacing = 0.05.em
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressionCard(
    combatLevel: Int,
    combatXP: Int,
    xpMax: Int,
    xpPercent: Int,
    streak: Int,
    bestStreak: Int,
    streakLabel: String,
    totalMinutes: Int,
    totalFocusFormatted: String,
    hp: Int,
    maxHp: Int,
    hpProgress: Float
) {
    // Heart pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "hp_heart_pulse")
    val heartAlpha by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heart_alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Stone950.copy(alpha = 0.6f))
            .border(1.dp, Stone800.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
            .padding(16.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(0.dp)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(LucideR.drawable.lucide_ic_swords),
                    contentDescription = null,
                    tint = Champagne500,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "PROGRESSÃO DO HERÓI",
                    style = TextStyle(
                        fontFamily = Cinzel,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        color = Champagne400,
                        letterSpacing = 0.05.em
                    )
                )
            }
            Box(
                modifier = Modifier
                    .background(Champagne500.copy(alpha = 0.1f))
                    .border(1.dp, Champagne500.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nível $combatLevel",
                    style = TextStyle(
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = Champagne400.copy(alpha = 0.9f)
                    )
                )
            }
        }

        // XP Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Stone900.copy(alpha = 0.7f))
                .border(1.dp, Stone800, RoundedCornerShape(8.dp))
                .padding(12.dp)
                .clip(RoundedCornerShape(8.dp)),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        painter = painterResource(LucideR.drawable.lucide_ic_sparkles),
                        contentDescription = null,
                        tint = Emerald400,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "XP de Combate",
                        style = TextStyle(
                            fontFamily = Cinzel,
                            fontWeight = FontWeight.Normal,
                            fontSize = 10.sp,
                            color = Stone400,
                            letterSpacing = 0.05.em
                        )
                    )
                }
                Text(
                    text = "$combatXP / $xpMax ($xpPercent%)",
                    style = TextStyle(
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = Emerald400
                    )
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .background(Stone950)
                    .border(1.dp, Stone800, RoundedCornerShape(999.dp))
                    .clip(RoundedCornerShape(999.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(xpPercent / 100f)
                        .height(10.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Emerald600, Emerald400)
                            )
                        )
                        .clip(RoundedCornerShape(999.dp))
                )
            }
        }

        // Metrics: Streak + Focus Total
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricTile(
                iconRes = LucideR.drawable.lucide_ic_flame,
                iconBgColor = Color(0xFFED8936).copy(alpha = 0.1f), // orange-500/10
                iconBorderColor = Color(0xFFED8936).copy(alpha = 0.2f), // orange-500/20
                iconColor = Color(0xFFF6AD55), // orange-400
                label = "Sequência Ativa",
                value = streakLabel,
                detail = "Recorde: ${bestStreak}d"
            )
            MetricTile(
                iconRes = LucideR.drawable.lucide_ic_clock,
                iconBgColor = Color(0xFF0EA5E9).copy(alpha = 0.1f), // sky-500/10
                iconBorderColor = Color(0xFF0EA5E9).copy(alpha = 0.2f), // sky-500/20
                iconColor = Color(0xFF38BDF8), // sky-400
                label = "Foco Total",
                value = totalFocusFormatted,
                detail = "$totalMinutes min"
            )
        }

        // HP Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Stone900.copy(alpha = 0.7f))
                .border(1.dp, Stone800, RoundedCornerShape(8.dp))
                .padding(12.dp)
                .clip(RoundedCornerShape(8.dp)),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        painter = painterResource(LucideR.drawable.lucide_ic_heart),
                        contentDescription = "HP",
                        tint = Red500.copy(alpha = heartAlpha),
                        modifier = Modifier
                            .size(14.dp)
                            .alpha(heartAlpha)
                    )
                    Text(
                        text = "HP (Pontos de Vida)",
                        style = TextStyle(
                            fontFamily = Cinzel,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = Rose400,
                            letterSpacing = 0.05.em
                        )
                    )
                }
                Text(
                    text = "$hp / $maxHp",
                    style = TextStyle(
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = Rose400
                    )
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .background(Stone950)
                    .border(1.dp, Stone800, RoundedCornerShape(999.dp))
                    .clip(RoundedCornerShape(999.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(hpProgress)
                        .height(10.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Red600, Rose400)
                            )
                        )
                        .clip(RoundedCornerShape(999.dp))
                )
            }
        }
    }
}

@Composable
private fun MetricTile(
    iconRes: Int,
    iconBgColor: Color,
    iconBorderColor: Color,
    iconColor: Color,
    label: String,
    value: String,
    detail: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Stone900.copy(alpha = 0.5f))
            .border(1.dp, Stone800, RoundedCornerShape(8.dp))
            .padding(12.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(iconBgColor)
                        .border(1.dp, iconBorderColor, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = label,
                        style = TextStyle(
                            fontFamily = Cinzel,
                            fontWeight = FontWeight.Normal,
                            fontSize = 9.5.sp,
                            color = Stone500,
                            letterSpacing = 0.05.em
                        )
                    )
                    Text(
                        text = value,
                        style = TextStyle(
                            fontFamily = JetBrainsMono,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Champagne400
                        )
                    )
                }
            }
            Text(
                text = detail,
                style = TextStyle(
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Normal,
                    fontSize = 10.sp,
                    color = Stone500
                )
            )
        }
    }
}

@Composable
private fun EquipmentCard(
    equippedEquipment: List<InventoryItem?>,
    onUnequipItem: (Int) -> Unit,
    onInspectItem: (InventoryItem, Int) -> Unit,
    onNavigateToInventory: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Stone950.copy(alpha = 0.6f))
            .border(1.dp, Stone800.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
            .padding(16.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(0.dp)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(LucideR.drawable.lucide_ic_shield),
                        contentDescription = null,
                        tint = Champagne500,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "ITENS EQUIPADOS (3 SLOTS)",
                        style = TextStyle(
                            fontFamily = Cinzel,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            color = Champagne400,
                            letterSpacing = 0.05.em
                        )
                    )
                }
                Text(
                    text = "Artefatos que concedem bônus passivos e chances extras durante suas expedições.",
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 11.sp,
                        color = Stone400
                    )
                )
            }
            Box(
                modifier = Modifier
                    .background(Champagne500.copy(alpha = 0.1f))
                    .border(1.dp, Champagne500.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                    .clickable { onNavigateToInventory() }
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        painter = painterResource(LucideR.drawable.lucide_ic_backpack),
                        contentDescription = null,
                        tint = Champagne400,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "Abrir Mochila",
                        style = TextStyle(
                            fontFamily = Cinzel,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = Champagne400,
                            letterSpacing = 0.05.em
                        )
                    )
                }
            }
        }

        // 3 Slots stacked
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            for (slotIdx in 0..2) {
                val item = equippedEquipment.getOrNull(slotIdx)
                if (item != null) {
                    EquipmentSlotFilled(
                        item = item,
                        slotIdx = slotIdx,
                        onClick = { onInspectItem(item, slotIdx) },
                        onUnequipClick = { onUnequipItem(slotIdx) }
                    )
                } else {
                    EquipmentSlotEmpty(
                        slotIdx = slotIdx,
                        onClick = onNavigateToInventory
                    )
                }
            }
        }
    }
}

@Composable
private fun EquipmentSlotFilled(
    item: InventoryItem,
    slotIdx: Int,
    onClick: () -> Unit,
    onUnequipClick: () -> Unit
) {
    val charges = item.charges ?: 0
    val maxCharges = item.maxCharges ?: 8

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp)
            .background(Stone900.copy(alpha = 0.8f))
            .border(1.dp, Champagne500.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(Color(0xFF7F1D1D).copy(alpha = 0.8f)) // red-950/80
                        .border(1.dp, Red500.copy(alpha = 0.4f), RoundedCornerShape(999.dp))
                        .clip(RoundedCornerShape(999.dp))
                        .clickable { onUnequipClick() }
                        .padding(0.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "×",
                        style = TextStyle(
                            fontFamily = Cinzel,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = Color(0xFFFCA5A5) // red-300
                        )
                    )
                }
            }
            Text(
                text = item.emoji,
                fontSize = 30.sp
            )
            Text(
                text = item.name,
                style = TextStyle(
                    fontFamily = Cinzel,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = Champagne300,
                    textAlign = TextAlign.Center
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Text(
                text = "🔋 $charges/$maxCharges cargas",
                style = TextStyle(
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = Emerald400
                )
            )
        }
    }
}

@Composable
private fun EquipmentSlotEmpty(
    slotIdx: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp)
            .background(Stone950.copy(alpha = 0.4f))
            .border(1.dp, Stone800, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "🛡️",
                fontSize = 24.sp
            )
            Text(
                text = "Slot ${slotIdx + 1} Vazio",
                style = TextStyle(
                    fontFamily = Cinzel,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Stone500,
                    letterSpacing = 0.05.em
                )
            )
            Text(
                text = "Clique para equipar",
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 9.5.sp,
                    color = Stone600
                )
            )
        }
    }
}

@Composable
private fun ActiveEffectsCard(
    activeBuffs: List<InventoryItem>,
    onInspectItem: (InventoryItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Stone950.copy(alpha = 0.6f))
            .border(1.dp, Stone800.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
            .padding(16.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(0.dp)),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(LucideR.drawable.lucide_ic_sparkles),
                    contentDescription = null,
                    tint = Purple400,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Efeitos & Bênçãos Ativas",
                    style = TextStyle(
                        fontFamily = Cinzel,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        color = Champagne400,
                        letterSpacing = 0.05.em
                    )
                )
            }
            Text(
                text = "Encantamentos e elixires em vigor concedendo vantagens temporárias.",
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 11.sp,
                    color = Stone400
                )
            )
        }

        // Buffs
        if (activeBuffs.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                activeBuffs.forEach { buff ->
                    Box(
                        modifier = Modifier
                            .background(Purple950.copy(alpha = 0.4f))
                            .border(1.dp, Purple500.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onInspectItem(buff) }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = buff.emoji,
                                fontSize = 14.sp
                            )
                            Text(
                                text = buff.name,
                                style = TextStyle(
                                    fontFamily = Cinzel,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Purple300
                                )
                            )
                        }
                    }
                }
            }
        } else {
            Text(
                text = "Nenhuma bênção ativa. Visite o Bazar de Mystara para adquirir elixires e poções.",
                style = TextStyle(
                    fontFamily = Cinzel,
                    fontStyle = FontStyle.Italic,
                    fontSize = 10.sp,
                    color = Stone500
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }
    }
}

// Private color constants for Tailwind colors not in theme (hex v3 convention)
private val Emerald600 = Color(0xFF059669)
private val Red300 = Color(0xFFFCA5A5)