package com.iurispraecepta.herolog.ui.focus

import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowUpward
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iurispraecepta.herolog.data.JsonConfig
import com.iurispraecepta.herolog.logic.focus.DroppedTitle
import com.iurispraecepta.herolog.logic.focus.LootItem
import com.iurispraecepta.herolog.data.entity.PendingRewardCelebrationEntity
import com.iurispraecepta.herolog.model.Rarity
import com.iurispraecepta.herolog.logic.achievements.AchievementCatalog
import com.iurispraecepta.herolog.ui.theme.Amber500
import com.iurispraecepta.herolog.ui.theme.Champagne400
import com.iurispraecepta.herolog.ui.theme.Cinzel
import com.iurispraecepta.herolog.ui.theme.JetBrainsMono
import com.iurispraecepta.herolog.ui.theme.Stone950
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.draw.shadow

/**
 * Item de breakdown de uma sessão individual no modal agregado (spec-008, T11).
 *
 * Exibe: skill + duração, XP/Gold, loot (emoji+nome + badge especial),
 * título droppado, level-up badge. Expandível (AnimatedVisibility).
 */
@Composable
fun SessionBreakdownItem(
    entity: PendingRewardCelebrationEntity,
    onToggle: () -> Unit,
    expanded: Boolean,
    // BUG: faltava emoji ao lado do nome da skill no header de cada sessão. A entity só
    // guarda `skillName` (String) -- sem o emoji persistido -- então resolvemos por nome
    // contra as skills atuais do personagem; se a skill foi apagada, cai sem emoji (nulo),
    // igual ao comportamento anterior.
    skills: List<com.iurispraecepta.herolog.model.Skill> = emptyList()
) {
    val skillEmoji = skills.firstOrNull { it.name == entity.skillName }?.emoji
    val lootItems = runCatching { JsonConfig.default.decodeFromString<List<LootItem>>(entity.lootedItems) }
        .getOrDefault(emptyList())

    val droppedTitle = entity.droppedTitle?.let {
        runCatching { JsonConfig.default.decodeFromString<DroppedTitle>(it) }.getOrNull()
    }

    val achievements = runCatching { JsonConfig.default.decodeFromString<List<String>>(entity.achievementsUnlocked) }
        .getOrDefault(emptyList())

    val hasLoot = lootItems.isNotEmpty()
    val hasDroppedTitle = droppedTitle != null
    val hasLevelUp = entity.leveledUp
    val hasAchievements = achievements.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1C1917), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF292524), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        // Header: skill + duração + toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!skillEmoji.isNullOrBlank()) {
                        Text(
                            text = skillEmoji,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                    }
                    Text(
                        text = entity.skillName,
                        color = Color(0xFFE7E5E4),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Cinzel,
                        letterSpacing = 0.5.sp
                    )
                }
                Text(
                    text = "${entity.durationMinutes} min",
                    color = Color(0xFFA8A29E),
                    fontSize = 12.sp,
                    fontFamily = JetBrainsMono
                )
            }

            // XP/Gold
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "+${entity.xpGained} XP",
                    color = Color(0xFF34D399),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = JetBrainsMono,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color(0xFF34D399).copy(alpha = 0.25f),
                            offset = androidx.compose.ui.geometry.Offset(0f, 1f),
                            blurRadius = 4f
                        )
                    )
                )
                Text(
                    text = "+${entity.goldGained} GP",
                    color = Champagne400,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = JetBrainsMono,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color(0xFFE2B054).copy(alpha = 0.25f),
                            offset = androidx.compose.ui.geometry.Offset(0f, 1f),
                            blurRadius = 4f
                        )
                    )
                )
            }

            // Toggle expand
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clickable(onClick = onToggle)
                    .background(Color(0xFF292524), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFF44403C), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icons.Default.ArrowDropDown.let { icon ->
                    Icon(
                        imageVector = icon,
                        contentDescription = if (expanded) "Ocultar detalhes" else "Ver detalhes",
                        tint = Color(0xFFA8A29E),
                        modifier = Modifier
                            .graphicsLayer { rotationZ = if (expanded) 180f else 0f }
                            .size(24.dp)
                    )
                }
            }
        }

        // Expandable content
        androidx.compose.animation.AnimatedVisibility(
            visible = expanded,
            enter = androidx.compose.animation.expandHorizontally(
                animationSpec = androidx.compose.animation.core.tween(200, easing = androidx.compose.animation.core.EaseOut)
            ) +
            androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(150)),
            exit = androidx.compose.animation.shrinkHorizontally(
                animationSpec = androidx.compose.animation.core.tween(150)
            ) +
            androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(100))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFF292524))
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Loot
                if (entity.lootedItems != "[]") {
                    LootBreakdownSection(
                        lootItems = runCatching { JsonConfig.default.decodeFromString<List<LootItem>>(entity.lootedItems) }
                            .getOrDefault(emptyList())
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Dropped Title
                if (entity.droppedTitle != null) {
                    DroppedTitleBreakdown(
                        droppedTitle = runCatching { JsonConfig.default.decodeFromString<DroppedTitle>(entity.droppedTitle) }
                            .getOrNull()!!
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Level Up
                if (entity.leveledUp) {
                    LevelUpBreakdown(
                        previousLevel = entity.previousLevel!!,
                        newLevel = entity.newLevel!!
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Achievements
                if (entity.achievementsUnlocked != "[]") {
                    AchievementsBreakdownSection(
                        achievementIds = runCatching { JsonConfig.default.decodeFromString<List<String>>(entity.achievementsUnlocked) }
                            .getOrDefault(emptyList())
                    )
                }
            }
        }
    }
}

@Composable
private fun LootBreakdownSection(lootItems: List<LootItem>) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "ESPÓLIO",
            color = Color(0xFFA855F7),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = Cinzel,
            letterSpacing = 1.sp
        )

        androidx.compose.foundation.layout.FlowRow(
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            lootItems.forEach { item ->
                val isEspecial = item.rarity == Rarity.Especial
                val borderColor = if (isEspecial) Color(0xFFA855F7).copy(alpha = 0.3f) else Color(0xFF44403C)
                val badgeColor = if (isEspecial) Color(0xFFC084FC) else Color(0xFFA8A29E)
                val badgeText = if (isEspecial) "★ ESPECIAL ★" else "COMUM"
                val bgBrush = if (isEspecial) {
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF2E1065).copy(alpha = 0.3f),
                            Color(0xFF0C0A09),
                            Color(0xFF0C0A09)
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF292524).copy(alpha = 0.4f),
                            Color(0xFF0C0A09),
                            Color(0xFF0C0A09)
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .width(140.dp)
                        .height(100.dp)
                        .background(bgBrush, RoundedCornerShape(12.dp))
                        .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // BUG: `.fillMaxSize()` aqui fazia o emoji reivindicar toda a altura
                        // da caixa de 100dp dentro da Column, empurrando nome e badge pra fora
                        // da área visível -- por isso a caixa de espólio só mostrava o emoji,
                        // "sem dizer o nome do item". Sem o fillMaxSize o emoji só ocupa o
                        // espaço do próprio texto, igual ao padrão usado em AchievementsBreakdownSection.
                        Text(
                            text = item.emoji,
                            fontSize = 28.sp
                        )

                        Text(
                            text = item.name,
                            color = if (isEspecial) Color(0xFFC4B5FD) else Color(0xFFD6D3D1),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = Cinzel,
                            letterSpacing = 0.5.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = if (isEspecial) "★ ESPECIAL ★" else "COMUM",
                            color = if (isEspecial) Color(0xFFC084FC) else Color(0xFFA8A29E),
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = JetBrainsMono,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DroppedTitleBreakdown(droppedTitle: DroppedTitle) {
    val titleCornerColor = Color(0xFFE5C158).copy(alpha = 0.4f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF854D0E).copy(alpha = 0.4f),
                        Color(0xFF0C0A09),
                        Color(0xFF0C0A09)
                    )
                ),
                RoundedCornerShape(12.dp)
            )
            .border(1.dp, Color(0xFFE5C158).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        // Corner accents
        Box(modifier = Modifier.align(Alignment.TopStart).size(6.dp).border(1.dp, Color(0xFFE5C158).copy(alpha = 0.4f), RoundedCornerShape(topStart = 2.dp)))
        Box(modifier = Modifier.align(Alignment.TopEnd).size(6.dp).border(1.dp, Color(0xFFE5C158).copy(alpha = 0.4f), RoundedCornerShape(topEnd = 2.dp)))
        Box(modifier = Modifier.align(Alignment.BottomStart).size(6.dp).border(1.dp, Color(0xFFE5C158).copy(alpha = 0.4f), RoundedCornerShape(bottomStart = 2.dp)))
        Box(modifier = Modifier.align(Alignment.BottomEnd).size(6.dp).border(1.dp, Color(0xFFE5C158).copy(alpha = 0.4f), RoundedCornerShape(bottomEnd = 2.dp)))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = droppedTitle.emoji,
                fontSize = 28.sp,
                modifier = Modifier.shadow(10.dp, androidx.compose.foundation.shape.CircleShape, ambientColor = Color(0xFFE5C158).copy(alpha = 0.4f))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = droppedTitle.name,
                        color = Color(0xFFFCD34D),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Cinzel,
                        letterSpacing = 0.5.sp
                    )
                    Text("★ TÍTULO RARO ★", color = Color(0xFFE5C158), fontSize = 7.sp, fontWeight = FontWeight.Bold, fontFamily = JetBrainsMono, letterSpacing = 1.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Pode ser equipado na tela de Títulos.",
                    color = Color(0xFFA8A29E).copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    fontFamily = Cinzel
                )
            }
        }
    }
}

@Composable
private fun LevelUpBreakdown(previousLevel: Int, newLevel: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF2E1065).copy(alpha = 0.4f),
                        Color(0xFF0C0A09),
                        Color(0xFF0C0A09)
                    )
                ),
                RoundedCornerShape(12.dp)
            )
            .border(1.dp, Color(0xFFA855F7).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🎉 Level Up! Nv. $previousLevel → $newLevel",
                color = Color(0xFFC084FC),
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = Cinzel,
                letterSpacing = 0.5.sp,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color(0xFFA855F7).copy(alpha = 0.3f),
                        offset = androidx.compose.ui.geometry.Offset(0f, 1f),
                        blurRadius = 6f
                    )
                )
            )
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(Color(0xFFA855F7), androidx.compose.foundation.shape.CircleShape)
                    .shadow(8.dp, androidx.compose.foundation.shape.CircleShape, ambientColor = Color(0xFFA855F7).copy(alpha = 0.3f))
            ) {
                Icons.Default.ArrowUpward.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = Color(0xFF0C0A09),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AchievementsBreakdownSection(achievementIds: List<String>) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "CONQUISTAS",
            color = Color(0xFFE5C158),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = Cinzel,
            letterSpacing = 1.sp
        )

        // Simple horizontal list of achievements
        androidx.compose.foundation.layout.FlowRow(
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            achievementIds.forEach { id ->
                val achievement = AchievementCatalog.ACHIEVEMENTS_LIST
                    .firstOrNull { it.id == id }
                if (achievement != null) {
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(100.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFF2E1065).copy(alpha = 0.3f),
                                        Color(0xFF0C0A09),
                                        Color(0xFF0C0A09)
                                    )
                                ),
                                RoundedCornerShape(12.dp)
                            )
                            .border(1.dp, Color(0xFFA855F7).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = achievement.icon,
                                fontSize = 28.sp,
                                modifier = Modifier.shadow(10.dp, androidx.compose.foundation.shape.CircleShape, ambientColor = Color(0xFFA855F7).copy(alpha = 0.4f))
                            )
                            Text(
                                text = achievement.name,
                                color = Color(0xFFC4B5FD),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = Cinzel,
                                letterSpacing = 0.5.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}