package com.iurispraecepta.herolog.ui.tavern

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import com.composables.icons.lucide.R as LucideR
import com.iurispraecepta.herolog.R
import com.iurispraecepta.herolog.logic.TavernLogic
import com.iurispraecepta.herolog.model.ActivityEntry
import com.iurispraecepta.herolog.model.ActivityType
import com.iurispraecepta.herolog.model.CharClass
import com.iurispraecepta.herolog.ui.theme.Amber400
import com.iurispraecepta.herolog.ui.theme.Amber500
import com.iurispraecepta.herolog.ui.theme.Cinzel
import com.iurispraecepta.herolog.ui.theme.Emerald400
import com.iurispraecepta.herolog.ui.theme.Inter
import com.iurispraecepta.herolog.ui.theme.JetBrainsMono
import com.iurispraecepta.herolog.ui.theme.Purple300
import com.iurispraecepta.herolog.ui.theme.Purple400
import com.iurispraecepta.herolog.ui.theme.Purple500
import com.iurispraecepta.herolog.ui.theme.Purple950
import com.iurispraecepta.herolog.ui.theme.Stone400
import com.iurispraecepta.herolog.ui.theme.Stone500
import com.iurispraecepta.herolog.ui.theme.Stone800
import com.iurispraecepta.herolog.ui.theme.Stone950
import com.iurispraecepta.herolog.ui.theme.Yellow500

// ============================================================================
// CONSTANTES DE COR — literais do React (TavernFeat c89af8f,
// src/modules/tavern/TavernScreen.tsx). Convênio do projeto: cores "de sistema" em hex
// Tailwind v3. Privadas deste arquivo: a spec manda criar as constantes `Tavern*` aqui,
// não no tema global.
// ============================================================================

private val TavernGold = Color(0xFFE7C873)
private val TavernCardBg = Color(0xFF15121A) // bg-[#15121A]/95 dos cards de atalho
private val TavernCardBgPressed = Color(0xFF1D1627) // hover:bg-[#1D1627]
private val TavernGradientStart = Color(0xFF15121A) // from-[#15121A]
private val TavernGradientMid = Color(0xFF100D14) // via-[#100D14]
private val TavernGradientEnd = Color(0xFF1E1729) // to-[#1E1729]
private val TavernAuraPurple = Color(0xFF241A33) // aura inferior bg-[#241A33]/40
private val TavernTextPrimary = Color(0xFFF2EDF5) // text-[#F2EDF5]
private val TavernTextMuted = Color(0xFFA89DB5) // text-[#A89DB5]
private val TavernTextDim = Color(0xFF8E8499) // text-[#8E8499]
private val TavernWhite10 = Color(0x1AFFFFFF) // border-white/10
private val TavernWhite5 = Color(0x0DFFFFFF) // border-white/5
private val TavernPurple900 = Color(0xFF581C87) // border-purple-900/35
private val TavernEmerald600 = Color(0xFF059669) // from-emerald-600
private val TavernSky400 = Color(0xFF38BDF8) // sky-400
private val TavernSky300 = Color(0xFF7DD3FC) // sky-300
private val TavernEmerald300 = Color(0xFF6EE7B7) // emerald-300
private val TavernOrange500 = Color(0xFFF97316) // orange-500

// tracking do Tailwind: wide = 0.025em, wider = 0.05em, widest = 0.1em
private val trackingWide = 0.025.em
private val trackingWider = 0.05.em
private val trackingWidest = 0.1.em

// leading-tight = 1.25 (títulos 11sp) · leading-snug = 1.375 (descrições 10sp)
private val leadingTight: TextUnit = 14.sp
private val leadingSnug: TextUnit = 14.sp

/**
 * Tela Taverna — hub/home do app (Spec B).
 *
 * Composable **puramente apresentacional**: recebe valores primitivos, o feed da Spec F
 * ([recentActivity]) e [onNavigate]. Formatação e cálculos vivem em [TavernLogic].
 *
 * Fonte: `TavernFeat c89af8f` — `src/modules/tavern/TavernScreen.tsx` (450 linhas). Valores
 * **base (telefone)**: as classes `sm:`/`md:` da fonte são ignoradas.
 *
 * 3 seções: (1) cartão do herói, (2) atalhos 2x2, (3) Recentes. O painel-wrapper fica no
 * `MainActivity` (spec B §2 — "Abordagem escolhida").
 */
@Composable
fun TavernScreen(
    charName: String,
    charClass: CharClass,
    combatLevel: Int,
    combatXP: Int,
    streak: Int,
    bestStreak: Int,
    totalMinutes: Int,
    gold: Int,
    recentActivity: List<ActivityEntry>,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp) // space-y-4 (base)
    ) {
        HeroCard(
            charName = charName,
            charClass = charClass,
            combatLevel = combatLevel,
            combatXP = combatXP,
            streak = streak,
            bestStreak = bestStreak,
            totalMinutes = totalMinutes,
            gold = gold
        )
        ShortcutsSection(onNavigate = onNavigate)
        RecentSection(recentActivity = recentActivity, onNavigate = onNavigate)
    }
}

// ---------------------------------------------------------------------------
// 1. CARTÃO DO HERÓI — identidade + XP de combate + 3 métricas (TAV-1..4)
// ---------------------------------------------------------------------------

@Composable
private fun HeroCard(
    charName: String,
    charClass: CharClass,
    combatLevel: Int,
    combatXP: Int,
    streak: Int,
    bestStreak: Int,
    totalMinutes: Int,
    gold: Int
) {
    val shape = RoundedCornerShape(12.dp)
    val xpNeeded = TavernLogic.xpNeeded(combatLevel)
    val xpPercent = TavernLogic.xpPercent(combatXP, combatLevel)

    val avatarRes = when (charClass) {
        CharClass.Mage -> R.drawable.mage_idle
        CharClass.Warrior -> R.drawable.warrior_idle
        CharClass.Ranger -> R.drawable.ranger_idle
    }

    // React: `charClass === 'Mage' ? "Mago d'Arraia" : ...` (sem emoji na Taverna).
    val classLabel = when (charClass) {
        CharClass.Mage -> "Mago d'Arraia"
        CharClass.Warrior -> "Guerreiro de Aço"
        CharClass.Ranger -> "Patrulheiro Silvestre"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                // rounded-xl bg-gradient-to-br from-[#15121A] via-[#100D14] to-[#1E1729]
                Brush.linearGradient(
                    colors = listOf(TavernGradientStart, TavernGradientMid, TavernGradientEnd)
                )
            )
            .border(1.dp, TavernGold.copy(alpha = 0.2f), shape) // border-[#E7C873]/20
    ) {
        // Auras (React `blur-3xl` → radialGradient — decisão D5)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 48.dp, y = (-48).dp) // -top-12 -right-12 (176dp - 128dp)
                .size(176.dp) // w-44 h-44
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            TavernGold.copy(alpha = 0.10f), // bg-[#E7C873]/10
                            TavernGold.copy(alpha = 0f)
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-48).dp, y = 48.dp) // -bottom-12 -left-12
                .size(176.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            TavernAuraPurple.copy(alpha = 0.40f), // bg-[#241A33]/40
                            TavernAuraPurple.copy(alpha = 0f)
                        )
                    )
                )
        )

        // padding 14dp (p-3.5) do cartão; as auras ficam fora dele (position absolute)
        Column(modifier = Modifier.padding(14.dp)) {
            // ── Barra de atmosfera: Beer + TAVERNA DE MYSTARA + divisória (TAV-1) ──
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp), // pb-3
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(LucideR.drawable.lucide_ic_beer),
                        contentDescription = null,
                        tint = TavernGold,
                        modifier = Modifier.size(16.dp) // w-4 h-4
                    )
                    Spacer(modifier = Modifier.width(8.dp)) // gap-2
                    Text(
                        text = "TAVERNA DE MYSTARA",
                        style = TextStyle(
                            fontFamily = Cinzel,
                            fontWeight = FontWeight.ExtraBold, // font-black (Cinzel topa em 800)
                            fontSize = 12.sp, // text-xs
                            color = TavernGold,
                            letterSpacing = trackingWider
                        )
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(TavernWhite10)
                )
            }

            // ── Identidade: avatar + nome/classe/nível + barra de XP (TAV-2, TAV-3) ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp), // mt-3
                horizontalArrangement = Arrangement.spacedBy(12.dp), // gap-3
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp) // w-14 h-14
                        .clip(RoundedCornerShape(12.dp)) // rounded-xl
                        .background(Stone950) // bg-stone-950
                        .border(2.dp, TavernGold.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(avatarRes),
                        contentDescription = charClass.name,
                        contentScale = ContentScale.Crop, // object-cover
                        modifier = Modifier.fillMaxSize()
                    )
                    // overlay bg-gradient-to-t from-purple-950/40 via-transparent to-transparent
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Transparent,
                                        Purple950.copy(alpha = 0.4f)
                                    )
                                )
                            )
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp) // space-y-1
                ) {
                    Text(
                        text = charName,
                        style = TextStyle(
                            fontFamily = Cinzel,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp, // text-sm
                            color = TavernTextPrimary,
                            letterSpacing = trackingWide,
                            lineHeight = 18.sp // leading-tight
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis // truncate
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) { // space-y-0.5
                        Text(
                            text = classLabel,
                            style = TextStyle(
                                fontFamily = Cinzel,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp, // text-[9px]
                                color = Purple400, // text-purple-400
                                letterSpacing = trackingWider,
                                lineHeight = 12.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "NÍVEL $combatLevel",
                            style = TextStyle(
                                fontFamily = Cinzel,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = TavernGold,
                                letterSpacing = trackingWider,
                                lineHeight = 12.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // XP (pt-0.5 space-y-0.5 max-w-sm)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 384.dp)
                            .padding(top = 2.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "XP COMBATE", // font-serif uppercase (source: "XP Combate")
                                style = TextStyle(
                                    fontFamily = Cinzel,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 8.5.sp,
                                    color = Stone400,
                                    letterSpacing = trackingWidest
                                )
                            )
                            Text(
                                text = "$combatXP / $xpNeeded ($xpPercent%)",
                                style = TextStyle(
                                    fontFamily = JetBrainsMono,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.5.sp,
                                    color = Emerald400
                                )
                            )
                        }
                        // h-1.5 bg-stone-950 border border-stone-800 rounded-full + fill emerald
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Stone950)
                                .border(1.dp, Stone800, RoundedCornerShape(50))
                                .padding(1.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(xpPercent / 100f)
                                    .fillMaxHeight()
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(TavernEmerald600, Emerald400)
                                        )
                                    )
                            )
                        }
                    }
                }
            }

            // ── 3 métricas (grid-cols-3 gap-1.5 mt-3 pt-3 border-t) (TAV-4) ──
            Spacer(modifier = Modifier.height(12.dp)) // mt-3
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(TavernWhite10)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp), // pt-3
                horizontalArrangement = Arrangement.spacedBy(6.dp) // gap-1.5
            ) {
                MetricTile(
                    modifier = Modifier.weight(1f),
                    label = "SEQUÊNCIA",
                    iconRes = LucideR.drawable.lucide_ic_flame,
                    iconTint = TavernOrange500,
                    sub = "Recorde: ${bestStreak}d"
                ) {
                    StreakValue(streak = streak)
                }
                MetricTile(
                    modifier = Modifier.weight(1f),
                    label = "FOCO TOTAL",
                    iconRes = LucideR.drawable.lucide_ic_clock,
                    iconTint = TavernSky400,
                    value = TavernLogic.formatHoursMinutes(totalMinutes),
                    sub = "Tempo dedicado"
                )
                MetricTile(
                    modifier = Modifier.weight(1f),
                    label = "OURO",
                    iconRes = LucideR.drawable.lucide_ic_coins,
                    iconTint = Yellow500,
                    value = TavernLogic.formatGold(gold),
                    sub = "GP no cofre"
                )
            }
        }
    }
}

/**
 * Valor do tile "Sequência": número em Mono Black 14sp + `dia/dias` em Mono Normal 10sp a
 * 70% do gold — React: `{streak} <span class="text-[10px] font-normal text-[#E7C873]/70">dias</span>`,
 * com a pluralização D4.
 */
@Composable
private fun StreakValue(streak: Int) {
    val label = TavernLogic.streakLabel(streak)
    val word = label.substringAfter(' ')
    Text(
        text = buildAnnotatedString {
            append("$streak ")
            withStyle(
                SpanStyle(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal,
                    color = TavernGold.copy(alpha = 0.7f)
                )
            ) {
                append(word)
            }
        },
        style = TextStyle(
            fontFamily = JetBrainsMono,
            fontWeight = FontWeight.Black,
            fontSize = 14.sp,
            color = TavernGold
        )
    )
}

/**
 * Tile de métrica (React: `bg-stone-950/70 border border-stone-800/80 p-2 rounded-lg text-center`).
 * [value] usa Mono Black 14sp; [sub] Mono 7,5sp `stone-500`; o slot [content] é para o valor
 * misto do tile de Sequência.
 */
@Composable
private fun MetricTile(
    modifier: Modifier = Modifier,
    label: String,
    iconRes: Int,
    iconTint: Color,
    value: String? = null,
    sub: String? = null,
    content: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Stone950.copy(alpha = 0.7f))
            .border(1.dp, Stone800.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp), // gap-1
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(12.dp) // w-3 h-3
            )
            Text(
                text = label,
                style = TextStyle(
                    fontFamily = Cinzel,
                    fontWeight = FontWeight.Normal,
                    fontSize = 8.5.sp,
                    color = Stone400,
                    letterSpacing = trackingWider
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        content?.let {
            Box(modifier = Modifier.padding(top = 2.dp)) { it() } // mt-0.5
        }
        value?.let {
            Text(
                text = it,
                modifier = Modifier.padding(top = 2.dp),
                style = TextStyle(
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = TavernGold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        sub?.let {
            Text(
                text = it,
                modifier = Modifier.padding(top = 2.dp),
                style = TextStyle(
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Normal,
                    fontSize = 7.5.sp,
                    color = Stone500
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ---------------------------------------------------------------------------
// 2. ATALHOS 2x2 — TAV-5, TAV-6, TAV-10, TAV-11
// ---------------------------------------------------------------------------

private data class Shortcut(
    val iconRes: Int,
    val iconTint: Color,
    val title: String,
    val pressedTitleColor: Color,
    val description: String,
    val destination: String
)

// TAV-5: destinos `focus`, `habits`, `skills`, `inventory` — sem pílulas nem setas.
private val Shortcuts = listOf(
    Shortcut(
        iconRes = LucideR.drawable.lucide_ic_swords,
        iconTint = TavernGold,
        title = "CÂMARA DE FOCO",
        pressedTitleColor = TavernGold,
        description = "Pomodoro, masmorras e expedições",
        destination = "focus"
    ),
    Shortcut(
        iconRes = LucideR.drawable.lucide_ic_sparkles,
        iconTint = Purple400,
        title = "HÁBITOS",
        pressedTitleColor = Purple300,
        description = "Capela de hábitos, diárias e missões",
        destination = "habits"
    ),
    Shortcut(
        iconRes = LucideR.drawable.lucide_ic_book_open,
        iconTint = TavernSky400,
        title = "GRIMÓRIO DE SKILLS",
        pressedTitleColor = TavernSky300,
        description = "Progresso de estudo e maestria em disciplinas",
        destination = "skills"
    ),
    Shortcut(
        iconRes = LucideR.drawable.lucide_ic_backpack,
        iconTint = Emerald400,
        title = "EQUIPAMENTOS",
        pressedTitleColor = TavernEmerald300,
        description = "Artefatos, poções e equipamentos guardados",
        destination = "inventory"
    )
)

@Composable
private fun ShortcutsSection(onNavigate: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp), // pt-2
        verticalArrangement = Arrangement.spacedBy(10.dp) // space-y-2.5
    ) {
        SectionHeader(
            iconRes = LucideR.drawable.lucide_ic_compass,
            title = "O QUE VOCÊ VAI FAZER HOJE?",
            subtitle = "Escolha seu próximo destino e continue sua jornada."
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { // grid gap-2.5
            ShortcutCard(Shortcuts[0], Modifier.weight(1f), onNavigate)
            ShortcutCard(Shortcuts[1], Modifier.weight(1f), onNavigate)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ShortcutCard(Shortcuts[2], Modifier.weight(1f), onNavigate)
            ShortcutCard(Shortcuts[3], Modifier.weight(1f), onNavigate)
        }
    }
}

@Composable
private fun ShortcutCard(
    shortcut: Shortcut,
    modifier: Modifier = Modifier,
    onNavigate: (String) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val shape = RoundedCornerShape(12.dp) // rounded-xl

    val background = if (pressed) TavernCardBgPressed else TavernCardBg.copy(alpha = 0.95f)
    val borderColor =
        if (pressed) Purple500.copy(alpha = 0.5f) else TavernPurple900.copy(alpha = 0.35f)
    val titleColor = if (pressed) shortcut.pressedTitleColor else TavernTextPrimary

    Column(
        modifier = modifier
            .heightIn(min = 96.dp) // min-h-[96px]
            .graphicsLayer { // active:scale-[0.98]
                scaleX = if (pressed) 0.98f else 1f
                scaleY = if (pressed) 0.98f else 1f
            }
            .clip(shape)
            .background(background)
            .border(1.dp, borderColor, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onNavigate(shortcut.destination) }
            )
            .padding(12.dp), // p-3
        verticalArrangement = Arrangement.Top // justify-start
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 30.dp), // min-h-[30px]: descrição alinhada no topo (TAV-6)
            horizontalArrangement = Arrangement.spacedBy(6.dp), // gap-1.5
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                painter = painterResource(shortcut.iconRes),
                contentDescription = null,
                tint = shortcut.iconTint,
                modifier = Modifier
                    .offset(y = 1.dp) // mt-[1px]
                    .size(14.dp) // w-3.5 h-3.5
            )
            Text(
                text = shortcut.title,
                style = TextStyle(
                    fontFamily = Cinzel,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp, // text-[11px]
                    color = titleColor,
                    letterSpacing = trackingWide,
                    lineHeight = leadingTight
                ),
                maxLines = 2, // quebra em até 2 linhas, sem reticências (TAV-6)
                overflow = TextOverflow.Clip
            )
        }
        Text(
            text = shortcut.description,
            modifier = Modifier.padding(top = 6.dp), // mb-1.5 do cabeçalho
            style = TextStyle(
                fontFamily = Inter,
                fontWeight = FontWeight.Normal,
                fontSize = 10.sp, // text-[10px]
                color = TavernTextMuted,
                lineHeight = leadingSnug
            )
        )
    }
}

// ---------------------------------------------------------------------------
// 3. RECENTES — TAV-7..TAV-10
// ---------------------------------------------------------------------------

@Composable
private fun RecentSection(recentActivity: List<ActivityEntry>, onNavigate: (String) -> Unit) {
    val entries = recentActivity.take(4) // TAV-7: no máximo 4, na ordem do feed

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp), // pt-2
        verticalArrangement = Arrangement.spacedBy(10.dp) // space-y-2.5
    ) {
        SectionHeader(
            iconRes = LucideR.drawable.lucide_ic_scroll_text,
            title = "RECENTES",
            subtitle = "Resumo do registro de atividades e tarefas concluídas."
        )

        if (entries.isEmpty()) {
            EmptyFeedState() // TAV-8
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { // space-y-2
                entries.forEach { entry -> RecentItem(entry = entry) }
            }
        }

        // "Ver registro de atividades" → logs (TAV-9)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp), // pt-1
            horizontalArrangement = Arrangement.End
        ) {
            val interactionSource = remember { MutableInteractionSource() }
            val pressed by interactionSource.collectIsPressedAsState()
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (pressed) TavernGold.copy(alpha = 0.10f) else Color.Transparent
                    )
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { onNavigate("logs") }
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp), // px-2.5 py-1
                horizontalArrangement = Arrangement.spacedBy(4.dp), // gap-1
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ver registro de atividades",
                    style = TextStyle(
                        fontFamily = Cinzel,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp, // text-[11px]
                        color = TavernGold
                    )
                )
                Icon(
                    painter = painterResource(LucideR.drawable.lucide_ic_chevron_right),
                    contentDescription = null,
                    tint = TavernGold,
                    modifier = Modifier.size(14.dp) // w-3.5 h-3.5
                )
            }
        }
    }
}

@Composable
private fun EmptyFeedState() {
    val shape = RoundedCornerShape(12.dp) // rounded-xl
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(TavernCardBg.copy(alpha = 0.7f)) // bg-[#15121A]/70
            .border(1.dp, TavernWhite5, shape) // border-white/5
            .padding(16.dp) // p-4
    ) {
        Text(
            // Texto literal do React, com aspas (TAV-8).
            text = "\"Seu registro de atividades ainda aguarda novos registros. Complete uma missão de foco ou cumpra um ritual para registrar seus feitos.\"",
            style = TextStyle(
                fontFamily = Cinzel,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp, // text-xs
                color = TavernTextDim,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center,
                lineHeight = 17.sp
            )
        )
    }
}

/** Triplo (ícone, fundo, borda) da caixinha de ícone por tipo de entrada. */
private data class IconTrio(
    val iconRes: Int,
    val boxBg: Color,
    val boxBorder: Color,
    val iconTint: Color
)

@Composable
private fun RecentItem(entry: ActivityEntry) {
    val shape = RoundedCornerShape(12.dp) // rounded-xl
    val (iconRes, boxBg, boxBorder, iconTint) = when (entry.type) {
        ActivityType.Focus -> IconTrio(
            LucideR.drawable.lucide_ic_timer,
            Amber500.copy(alpha = 0.10f),
            Amber500.copy(alpha = 0.30f),
            Amber400
        )
        ActivityType.Mission -> IconTrio(
            LucideR.drawable.lucide_ic_circle_check,
            Purple500.copy(alpha = 0.10f),
            Purple500.copy(alpha = 0.30f),
            Purple400
        )
        ActivityType.Ritual -> IconTrio(
            LucideR.drawable.lucide_ic_sparkles,
            Emerald400.copy(alpha = 0.10f),
            Emerald400.copy(alpha = 0.30f),
            Emerald400
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(TavernCardBg.copy(alpha = 0.90f)) // bg-[#15121A]/90
            .border(1.dp, TavernWhite10, shape) // border-white/10
            .padding(10.dp), // p-2.5
        horizontalArrangement = Arrangement.spacedBy(10.dp), // gap-2.5
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp) // w-8 h-8
                .clip(RoundedCornerShape(8.dp)) // rounded-lg
                .background(boxBg)
                .border(1.dp, boxBorder, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(16.dp) // w-4 h-4
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = TavernLogic.entryTitle(entry.type),
                style = TextStyle(
                    fontFamily = Cinzel,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp, // text-xs
                    color = TavernTextPrimary,
                    letterSpacing = trackingWide
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = TavernLogic.entryDetail(entry),
                style = TextStyle(
                    fontFamily = Inter,
                    fontWeight = FontWeight.Normal,
                    fontSize = 10.5.sp, // text-[10.5px]
                    color = TavernTextMuted
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = TavernLogic.formatRelativeTime(entry.at),
                style = TextStyle(
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Normal,
                    fontSize = 9.5.sp, // text-[9.5px]
                    color = TavernTextDim
                )
            )
            TavernLogic.entryReward(entry)?.let { reward ->
                Text(
                    text = reward,
                    style = TextStyle(
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.5.sp,
                        color = TavernGold
                    )
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Cabeçalho de seção — idêntico nas seções 2 e 3 (TAV-10)
// ---------------------------------------------------------------------------

@Composable
private fun SectionHeader(iconRes: Int, title: String, subtitle: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(bottom = 6.dp)) { // pb-1.5
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp), // gap-1.5
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = TavernGold,
                    modifier = Modifier.size(16.dp) // w-4 h-4
                )
                Text(
                    text = title,
                    style = TextStyle(
                        fontFamily = Cinzel,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp, // text-xs
                        color = TavernGold,
                        letterSpacing = trackingWider
                    )
                )
            }
            Text(
                text = subtitle,
                modifier = Modifier.padding(top = 2.dp), // mt-0.5
                style = TextStyle(
                    fontFamily = Inter,
                    fontWeight = FontWeight.Normal,
                    fontSize = 10.5.sp, // text-[10.5px]
                    color = TavernTextMuted
                )
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(TavernWhite10)
        )
    }
}
