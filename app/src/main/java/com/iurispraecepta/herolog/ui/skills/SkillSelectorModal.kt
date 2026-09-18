package com.iurispraecepta.herolog.ui.skills

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import com.iurispraecepta.herolog.ui.components.FlowRowStable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iurispraecepta.herolog.logic.SkillLogic
import com.iurispraecepta.herolog.model.Skill
import com.iurispraecepta.herolog.ui.components.HeroLogModal
import com.iurispraecepta.herolog.ui.components.ModalVariant
import com.iurispraecepta.herolog.ui.theme.Amber100
import com.iurispraecepta.herolog.ui.theme.Champagne300
import com.iurispraecepta.herolog.ui.theme.Champagne400
import com.iurispraecepta.herolog.ui.theme.Champagne500
import com.iurispraecepta.herolog.ui.theme.Stone400
import com.iurispraecepta.herolog.ui.theme.Stone800
import com.iurispraecepta.herolog.ui.theme.Stone950
import com.iurispraecepta.herolog.ui.theme.Zinc300
import kotlin.math.roundToInt

private val Stone900 = Color(0xFF1C1917)
private val Stone850 = Color(0xFF211E1C)
private val Yellow400 = Color(0xFFFACC15)
private val Yellow300 = Color(0xFFFDE047)

@Composable
fun SkillSelectorModal(
    isOpen: Boolean,
    onClose: () -> Unit,
    skills: List<Skill>,
    selectedSkillIdx: Int,
    onSelectSkill: (Int) -> Unit
) {
    HeroLogModal(
        isOpen = isOpen,
        onClose = onClose,
        title = "Selecionar Habilidade Ativa",
        variant = ModalVariant.Amber
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "ESCOLHA QUAL HABILIDADE RECEBERÁ O BÔNUS DE XP OBTIDO DURANTE ESTA SESSÃO DE FOCO:",
                style = androidx.compose.ui.text.TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Italic,
                    color = Amber100.copy(alpha = 0.6f),
                    lineHeight = 16.sp
                )
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                skills.forEachIndexed { idx, sk ->
                    val reqXP = SkillLogic.requiredXpForLevel(sk.level)
                    val progressPercent = (sk.xp.toFloat() / reqXP.toFloat() * 100f).coerceIn(0f, 100f)
                    val isActive = idx == selectedSkillIdx

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (isActive) {
                                    Modifier.shadow(
                                        elevation = 8.dp,
                                        shape = RoundedCornerShape(12.dp),
                                        ambientColor = Champagne500.copy(alpha = 0.15f),
                                        spotColor = Champagne500.copy(alpha = 0.15f)
                                    )
                                } else Modifier
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isActive) Color(0x0AD4AF37) else Color(0x990C0A09))
                            .border(
                                width = 1.dp,
                                color = if (isActive) Champagne400 else Stone800,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                onSelectSkill(idx)
                                onClose()
                            }
                            .padding(14.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Top row with emoji, title/info and optional active badge
                            Box(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(end = if (isActive) 52.dp else 0.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    // Emoji Container
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isActive) Color(0x1AD4AF37) else Stone900)
                                            .border(
                                                width = 1.dp,
                                                color = if (isActive) Champagne400 else Stone800,
                                                shape = RoundedCornerShape(8.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = sk.emoji ?: "🎯",
                                            fontSize = 24.sp
                                        )
                                    }

                                    // Info Column
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(2.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = sk.name.uppercase(),
                                                fontFamily = FontFamily.Serif,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = if (isActive) Champagne300 else Zinc300.copy(alpha = 0.9f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            if (sk.prestige != null && sk.prestige > 0) {
                                                Text(
                                                    text = "👑" + "★".repeat(sk.prestige),
                                                    color = Yellow400,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "Nv. ${sk.level}",
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = Champagne400
                                            )

                                            if (sk.prestige != null && sk.prestige > 0) {
                                                Text(
                                                    text = "+${sk.prestige * 25}% XP",
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 10.sp,
                                                    color = Color(0xB3EAB308) // yellow-500/70
                                                )
                                            }
                                        }
                                    }
                                }

                                if (isActive) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .clip(CircleShape)
                                            .background(Champagne400)
                                            .border(1.dp, Champagne500.copy(alpha = 0.2f), CircleShape)
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "ATIVA",
                                            color = Stone950,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Serif,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 0.8.sp
                                        )
                                    }
                                }
                            }

                            // Progress Bar Section
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(CircleShape)
                                        .background(Stone950)
                                        .border(1.dp, Stone900, CircleShape)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(progressPercent / 100f)
                                            .fillMaxHeight()
                                            .clip(CircleShape)
                                            .background(
                                                if (isActive) {
                                                    Brush.horizontalGradient(listOf(Champagne500, Champagne300))
                                                } else {
                                                    SolidColor(Color(0xB3D4AF37))
                                                }
                                            )
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Progresso: ${sk.xp} / $reqXP XP",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp,
                                        color = Champagne400.copy(alpha = 0.4f)
                                    )
                                    Text(
                                        text = "${progressPercent.roundToInt()}%",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp,
                                        color = Champagne400.copy(alpha = 0.4f)
                                    )
                                }
                            }

                            // Tags Section
                            if (!sk.tags.isNullOrEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .drawBehind {
                                            drawLine(
                                                color = Stone850,
                                                start = Offset(0f, 0f),
                                                end = Offset(size.width, 0f),
                                                strokeWidth = 1.dp.toPx()
                                            )
                                        }
                                        .padding(top = 6.dp)
                                ) {
                                    FlowRowStable(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        sk.tags.forEach { tag ->
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(0xCC1C1917))
                                                    .border(1.dp, Stone800, RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Sell,
                                                    contentDescription = null,
                                                    tint = Champagne400.copy(alpha = 0.5f),
                                                    modifier = Modifier.size(8.dp)
                                                )
                                                Text(
                                                    text = tag,
                                                    fontSize = 9.sp,
                                                    color = Zinc300.copy(alpha = 0.5f),
                                                    fontFamily = FontFamily.SansSerif
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
