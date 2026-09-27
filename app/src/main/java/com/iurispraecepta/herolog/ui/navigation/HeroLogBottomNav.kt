package com.iurispraecepta.herolog.ui.navigation

import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.R
import com.iurispraecepta.herolog.ui.theme.Amber400
import com.iurispraecepta.herolog.ui.theme.Amber500
import com.iurispraecepta.herolog.ui.theme.Champagne300
import com.iurispraecepta.herolog.ui.theme.Champagne400
import com.iurispraecepta.herolog.ui.theme.Champagne500
import com.iurispraecepta.herolog.ui.theme.Cinzel
import com.iurispraecepta.herolog.ui.theme.Stone900
import com.iurispraecepta.herolog.ui.theme.Stone950
import com.iurispraecepta.herolog.ui.theme.Zinc300
import com.iurispraecepta.herolog.ui.theme.Zinc400
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.HazeMaterials

/**
 * Porte fiel de `BottomNav.tsx` (fonte React, TavernFeat `c89af8f`). 5 módulos top-level nesta
 * ordem: Foco, Rituais, Taverna (botão central elevado), Herói, Reino. Foco e Taverna navegam
 * direto (sem sub-abas); Rituais, Herói e Reino abrem um bottom sheet com as sub-abas do módulo —
 * equivalente nativo ao "sheet" deslizante da fonte (lá é uma `motion.div` fixa na base da tela;
 * aqui é `ModalBottomSheet` do Material 3, que já cobre o dismiss por toque fora e o gesto de
 * arrastar pra fechar).
 *
 * Skills deixou de ser item de topo: passa a ser sub-aba do sheet do Herói (NAV-5). O módulo
 * "missions" da fonte virou "rituals" e o id de aba de destino continua `habits` (ids inalterados).
 *
 * Rótulos são sempre visíveis (ativo e inativo) — NAV-6; só a Taverna tem estados visuais
 * próprios (NAV-7/8/9), desenhada num overlay acima da barra sobre a faixa reservada de 16dp (D6).
 */

// Cores dos ícones de sub-aba, fiéis às classes Tailwind da fonte (text-{cor}-{tom}).
private val Sky400 = Color(0xFF38BDF8)
private val Rose400 = Color(0xFFFB7185)
private val Emerald400 = Color(0xFF34D399)
private val Blue400 = Color(0xFF60A5FA)
private val Slate300 = Color(0xFFCBD5E1)
private val Amber300 = Color(0xFFFCD34D)
private val Orange400 = Color(0xFFFB923C)
private val Yellow400 = Color(0xFFFACC15)
private val Violet400 = Color(0xFFA78BFA)
private val Cyan400 = Color(0xFF22D3EE)
private val Yellow500 = Color(0xFFEAB308)
private val Stone300 = Color(0xFFD6D3D1)
private val Red400 = Color(0xFFF87171)
private val White10 = Color(0x1AFFFFFF)
private val trackingWider = 0.05.em

// Botão da Taverna (NAV-7..9) — literais do React: bg-[#241A33], border/icon #E7C873,
// gradiente #15121A→#0D0B10, borda inativa/rótulo #C9A44C.
private val TavernReserveStrip = 16.dp
private val TavernButtonSize = 48.dp
private val TavernIconSize = 24.dp
private val TavernActiveScale = 1.10f
private val TavernActiveBg = Color(0xFF241A33)
private val TavernGold = Color(0xFFE7C873)
private val TavernBronze = Color(0xFFC9A44C)
private val TavernGradientTop = Color(0xFF15121A)
private val TavernGradientBottom = Color(0xFF0D0B10)

data class SubTabOption(
    val value: String,
    val label: String,
    val iconRes: Int,
    val color: Color
)

data class NavItem(
    val id: String,
    val label: String,
    val iconRes: Int,
    val targetTab: String
)

// value -> id do módulo dono da aba, fiel a getActiveModule() da fonte (TavernFeat c89af8f).
// O módulo "missions" da fonte virou "rituals"; os ids de aba não mudaram.
private val RITUALS_TABS = setOf("habits", "dailies", "todos", "quests", "history")
private val CHARACTER_TABS = setOf("character", "inventory", "skills")

fun getActiveModule(tab: String): String = when {
    tab == "focus" -> "focus"
    tab == "tavern" -> "tavern"
    tab in CHARACTER_TABS -> "character"
    tab in RITUALS_TABS -> "rituals"
    else -> "kingdom"
}

val MODULE_TITLES: Map<String, String> = mapOf(
    "rituals" to "RITUAIS",
    "character" to "HERÓI & EQUIPAMENTOS",
    "kingdom" to "O REINO DE MYSTARA"
)

val SUB_TABS: Map<String, List<SubTabOption>> = mapOf(
    "rituals" to listOf(
        SubTabOption("habits", "Capela de Hábitos", R.drawable.lucide_ic_repeat, Emerald400),
        SubTabOption("dailies", "Tarefas Diárias", R.drawable.lucide_ic_calendar, Blue400),
        SubTabOption("todos", "Missões Avulsas", R.drawable.lucide_ic_clipboard_list, Slate300),
        SubTabOption("quests", "CONTRATOS", R.drawable.lucide_ic_scroll_text, Amber300),
        SubTabOption("history", "Crônicas Diárias", R.drawable.lucide_ic_history, Orange400)
    ),
    "character" to listOf(
        SubTabOption("character", "Ficha do Herói", R.drawable.lucide_ic_shield_user, Sky400),
        SubTabOption("inventory", "Mochila & Equipamentos", R.drawable.lucide_ic_backpack, Rose400),
        SubTabOption("skills", "Grimório de Habilidades", R.drawable.lucide_ic_book_open, Emerald400)
    ),
    "kingdom" to listOf(
        SubTabOption("shop", "Bazar de Mystara", R.drawable.lucide_ic_coins, Yellow400),
        SubTabOption("titles", "TÍTULOS", R.drawable.lucide_ic_medal, Violet400),
        SubTabOption("heatmap", "Heatmap", R.drawable.lucide_ic_grid_3x3, Blue400),
        SubTabOption("stats", "ESTATÍSTICAS DO HERÓI", R.drawable.lucide_ic_chart_column, Cyan400),
        SubTabOption("achievements", "CONQUISTAS", R.drawable.lucide_ic_trophy, Yellow500),
        SubTabOption("logs", "REGISTROS", R.drawable.lucide_ic_file_text, Stone300),
        SubTabOption("guide", "Tutorial", R.drawable.lucide_ic_circle_question_mark, Red400)
    )
)

val NAV_ITEMS = listOf(
    NavItem("focus", "Foco", R.drawable.lucide_ic_timer, "focus"),
    NavItem("rituals", "Rituais", R.drawable.lucide_ic_compass, "habits"),
    NavItem("tavern", "Taverna", R.drawable.lucide_ic_beer, "tavern"),
    NavItem("character", "Herói", R.drawable.lucide_ic_shield_user, "character"),
    NavItem("kingdom", "Reino", R.drawable.lucide_ic_crown, "shop")
)

private val MODULES_WITH_SUBTABS = setOf("rituals", "character", "kingdom")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeroLogBottomNav(
    activeTab: String,
    onChangeTab: (String) -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    var openDropdown by remember { mutableStateOf<String?>(null) }
    val activeModule = getActiveModule(activeTab)
    val isTavernActive = activeModule == "tavern"

    var rowWidthPx by remember { mutableFloatStateOf(0f) }

    val activeIndex = NAV_ITEMS.indexOfFirst { it.id == activeModule }.coerceAtLeast(0)
    val itemWidthPx = if (rowWidthPx > 0f) rowWidthPx / NAV_ITEMS.size else 0f
    val pillTargetX = itemWidthPx * activeIndex

    val animatedPillOffset by animateIntOffsetAsState(
        targetValue = IntOffset(pillTargetX.toInt(), 0),
        animationSpec = tween(durationMillis = 250),
        label = "pillOffset"
    )

    val horizontalPad = 8.dp
    val navItemVerticalPadding = 6.dp

    Box(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // D6: faixa reservada de 16dp no topo do bottomBar — o botão elevado da Taverna
            // vive nela; assim ele fica dentro dos limites do pai (clip + hit-test válidos) e
            // `innerPadding.calculateBottomPadding()` já o inclui no inset sem número mágico.
            Spacer(modifier = Modifier.fillMaxWidth().height(TavernReserveStrip))

            // Barra propriamente dita (haze, borda, clip) — parte inferior (NAV-10/B6 intacto).
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .hazeEffect(state = hazeState, style = HazeMaterials.ultraThin(containerColor = Stone950.copy(alpha = 0.7f)))
                    .shadow(12.dp, shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp))
                    .clip(RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp))
                    .background(Stone950)
                    .drawBehind {
                        val strokePx = 2.dp.toPx()
                        drawLine(
                            color = White10,
                            start = Offset(0f, strokePx / 2f),
                            end = Offset(size.width, strokePx / 2f),
                            strokeWidth = strokePx
                        )
                    }
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = horizontalPad, vertical = navItemVerticalPadding)
                            .onSizeChanged { rowWidthPx = it.width.toFloat() },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NAV_ITEMS.forEach { item ->
                            if (item.id == "tavern") {
                                // Slot central: o botão da Taverna é desenhado no overlay acima,
                                // por isso aqui fica só o espaçador que preserva as 5 colunas.
                                Spacer(modifier = Modifier.weight(1f))
                            } else {
                                val isActive = activeModule == item.id
                                val hasSubTabs = item.id in MODULES_WITH_SUBTABS

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable {
                                            if (hasSubTabs) {
                                                openDropdown = if (openDropdown == item.id) null else item.id
                                            } else {
                                                openDropdown = null
                                                onChangeTab(item.targetTab)
                                            }
                                        }
                                        .padding(vertical = 4.dp, horizontal = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // NAV-6: rótulo sempre visível; o hack .offset(y = 7.dp) do ícone
                                    // inativo caiu junto (existia só pra centralizar sem rótulo).
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.graphicsLayer {
                                            scaleX = if (isActive) 1.05f else 1f
                                            scaleY = if (isActive) 1.05f else 1f
                                        }
                                    ) {
                                        Icon(
                                            painter = painterResource(item.iconRes),
                                            contentDescription = item.label,
                                            tint = if (isActive) Champagne400 else Zinc300.copy(alpha = 0.40f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = item.label.uppercase(),
                                            color = if (isActive) Champagne400 else Zinc400,
                                            fontSize = 9.5.sp,
                                            fontFamily = Cinzel,
                                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                            letterSpacing = trackingWider,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Animated active pill — height = 48.dp (matches nav item Box, replicates React's inset-0).
                    // NAV-8: a fonte não desenha pílula nenhuma quando a Taverna está ativa.
                    if (!isTavernActive) {
                        val horizontalPadPx = with(LocalDensity.current) { horizontalPad.roundToPx() }
                        val navVerticalPadPx = with(LocalDensity.current) { navItemVerticalPadding.roundToPx() }
                        Box(
                            modifier = Modifier
                                .offset { IntOffset(animatedPillOffset.x + horizontalPadPx, navVerticalPadPx) }
                                .size(width = with(LocalDensity.current) { itemWidthPx.toDp() }, height = 48.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Champagne500.copy(alpha = 0.08f))
                                .border(1.dp, Champagne500.copy(alpha = 0.20f), RoundedCornerShape(4.dp))
                        )
                    }
                }
            }
        }

        // NAV-7/NAV-9: botão circular central elevado, irmão da barra pra não sofrer o clip dela
        // (glow/escala vazam pra cima como no React) e ainda assim ficar dentro dos limites do pai
        // — a parte acima da barra continua tocável.
        TavernNavItem(
            isActive = isTavernActive,
            onClick = {
                openDropdown = null
                onChangeTab("tavern")
            },
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }

    val dropdown = openDropdown
    if (dropdown != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { openDropdown = null },
            sheetState = sheetState,
            containerColor = Stone950
        ) {
            // NAV-4: p-3 pb-5 da fonte → 12dp lateral/topo, 20dp no rodapé.
            Column(modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = (MODULE_TITLES[dropdown] ?: "").uppercase(),
                        color = Champagne400,
                        fontFamily = Cinzel,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        letterSpacing = trackingWider
                    )
                    Icon(
                        painter = painterResource(R.drawable.lucide_ic_x),
                        contentDescription = "Fechar",
                        tint = Zinc400,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { openDropdown = null }
                            .padding(2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = White10, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    (SUB_TABS[dropdown] ?: emptyList()).forEach { sub ->
                        val isSelected = activeTab == sub.value
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Champagne500.copy(alpha = 0.15f) else Stone900.copy(alpha = 0.6f))
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Champagne500.copy(alpha = 0.5f) else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    onChangeTab(sub.value)
                                    openDropdown = null
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(sub.iconRes),
                                    contentDescription = null,
                                    tint = sub.color,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = sub.label.uppercase(),
                                    color = if (isSelected) Champagne300 else Zinc300.copy(alpha = 0.70f),
                                    fontFamily = Cinzel,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    letterSpacing = trackingWider,
                                    fontSize = 11.sp
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    painter = painterResource(R.drawable.lucide_ic_check),
                                    contentDescription = null,
                                    tint = Champagne400,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Item "Taverna" do nav (NAV-7..9): círculo de 48dp elevado sobre a faixa reservada de 16dp,
 * com estados ativo/inativo distintos e rótulo sempre visível.
 *
 * Ativo: fundo `#241A33`, borda/ícone/rótulo `#E7C873`, glow `0 0 20px rgba(231,200,115,.35)`.
 * Inativo: gradiente `#15121A → #0D0B10`, borda `#C9A44C`, ícone `#E7C873`, rótulo `#C9A44C`
 * bold, sombra `0 4 16 rgba(0,0,0,.8)` + `0 0 10 rgba(201,164,76,.15)`.
 * Glow/sombra são aproximação radial (`drawBehind`) — o box-shadow do CSS não tem equivalente
 * direto em Compose sem elevação real; registrar como desvio de implementação, não de valor.
 */
@Composable
private fun TavernNavItem(
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(TavernButtonSize)
                .graphicsLayer {
                    scaleX = if (isActive) TavernActiveScale else 1f
                    scaleY = if (isActive) TavernActiveScale else 1f
                }
                .drawBehind { drawTavernAura(isActive) }
                .clip(CircleShape)
                .background(
                    if (isActive) SolidColor(TavernActiveBg)
                    else Brush.verticalGradient(listOf(TavernGradientTop, TavernGradientBottom))
                )
                .border(2.dp, if (isActive) TavernGold else TavernBronze, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.lucide_ic_beer),
                contentDescription = "Taverna (Início)",
                tint = TavernGold,
                modifier = Modifier.size(TavernIconSize)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "TAVERNA",
            color = if (isActive) TavernGold else TavernBronze,
            fontSize = 9.5.sp,
            fontFamily = Cinzel,
            fontWeight = if (isActive) FontWeight.Black else FontWeight.Bold,
            letterSpacing = trackingWider,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

/** Aura do botão da Taverna: glow dourado (ativo) ou sombra deslocada + brilho (inativo). */
private fun DrawScope.drawTavernAura(isActive: Boolean) {
    val circleRadius = size.minDimension / 2f
    if (isActive) {
        // 0 0 20px rgba(231,200,115,.35)
        val radius = circleRadius + 20.dp.toPx()
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(TavernGold.copy(alpha = 0.35f), TavernGold.copy(alpha = 0f)),
                center = center,
                radius = radius
            ),
            radius = radius,
            center = center
        )
    } else {
        // 0 4 16 rgba(0,0,0,.8)
        val shadowRadius = circleRadius + 16.dp.toPx()
        val shadowCenter = Offset(center.x, center.y + 4.dp.toPx())
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.Black.copy(alpha = 0.8f), Color.Black.copy(alpha = 0f)),
                center = shadowCenter,
                radius = shadowRadius
            ),
            radius = shadowRadius,
            center = shadowCenter
        )
        // 0 0 10 rgba(201,164,76,.15)
        val glowRadius = circleRadius + 10.dp.toPx()
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(TavernBronze.copy(alpha = 0.15f), TavernBronze.copy(alpha = 0f)),
                center = center,
                radius = glowRadius
            ),
            radius = glowRadius,
            center = center
        )
    }
}
