package com.iurispraecepta.herolog.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.R
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
 * Rótulos são sempre visíveis (ativo e inativo) — NAV-6; a Taverna é desenhada num overlay
 * acima da barra sobre a faixa reservada de 16dp (D6). Visual minimalista
 * (spec-bottomnav-minimalista, Android-first — React atualiza depois): sem pill/container nos
 * itens laterais; ativo = `Champagne400` (+ ícone 22dp), inativo = `Zinc400` (+ ícone 20dp) no
 * mesmo drawable outline; Taverna dourada só quando ativa, `Zinc400` quando inativa.
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

// Botão da Taverna (spec-bottomnav-minimalista) — estrutura elevada preservada:
// círculo de 48dp sobre a faixa reservada de 16dp, ícone de 24dp, anel de 2dp.
private val TavernReserveStrip = 16.dp
private val TavernButtonSize = 48.dp
private val TavernIconSize = 24.dp

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
                            .padding(horizontal = horizontalPad, vertical = navItemVerticalPadding),
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
                                // Minimalista: só a cor anima (150–200 ms); o tamanho do ícone
                                // troca sem animação dentro de um slot fixo de 24dp, sem mover
                                // o layout (FR-9). Sem variante filled na lib: mesmo drawable.
                                val itemColor by animateColorAsState(
                                    targetValue = if (isActive) Champagne400 else Zinc400,
                                    animationSpec = tween(durationMillis = 175),
                                    label = "navItemColor"
                                )

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .sizeIn(minHeight = 48.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .selectable(
                                            selected = isActive,
                                            onClick = {
                                                if (hasSubTabs) {
                                                    openDropdown = if (openDropdown == item.id) null else item.id
                                                } else {
                                                    openDropdown = null
                                                    onChangeTab(item.targetTab)
                                                }
                                            },
                                            role = Role.Tab,
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() }
                                        )
                                        .padding(vertical = 4.dp, horizontal = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier.size(24.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                painter = painterResource(item.iconRes),
                                                contentDescription = item.label,
                                                tint = itemColor,
                                                modifier = Modifier.size(if (isActive) 22.dp else 20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = item.label.uppercase(),
                                            color = itemColor,
                                            fontSize = 9.5.sp,
                                            fontFamily = Cinzel,
                                            fontWeight = FontWeight.Normal,
                                            letterSpacing = trackingWider,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
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
 * Item "Taverna" do nav (spec-bottomnav-minimalista): círculo de 48dp elevado sobre a faixa
 * reservada de 16dp, com rótulo sempre visível. Minimalista: fundo chapado + anel de 2dp,
 * sem glow/sombra em nenhum estado; só a cor muda (anel/ícone/rótulo `Champagne400` ativa,
 * `Zinc400` inativa — o hub se distingue pela estrutura, não pela cor). Sem animação de escala.
 */
@Composable
private fun TavernNavItem(
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tavernColor = if (isActive) Champagne400 else Zinc400
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .selectable(
                selected = isActive,
                onClick = onClick,
                role = Role.Tab,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
    ) {
        Box(
            modifier = Modifier
                .size(TavernButtonSize)
                .clip(CircleShape)
                .background(Stone900)
                .border(2.dp, tavernColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.lucide_ic_beer),
                contentDescription = "Taverna (Início)",
                tint = tavernColor,
                modifier = Modifier.size(TavernIconSize)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "TAVERNA",
            color = tavernColor,
            fontSize = 9.5.sp,
            fontFamily = Cinzel,
            fontWeight = FontWeight.Normal,
            letterSpacing = trackingWider,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
