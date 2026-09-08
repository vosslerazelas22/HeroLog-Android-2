package com.iurispraecepta.herolog.ui.navigation

import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
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
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.HazeMaterials

/**
 * Porte fiel de `BottomNav.tsx` (fonte React). 5 módulos top-level: Foco, Herói, Skills,
 * Missões, Reino. Foco e Skills navegam direto (sem sub-abas). Herói, Missões e Reino abrem um
 * bottom sheet com as sub-abas do módulo — equivalente nativo ao "sheet" deslizante da fonte
 * (lá é uma `motion.div` fixa na base da tela; aqui é `ModalBottomSheet` do Material 3, que já
 * cobre o dismiss por toque fora e o gesto de arrastar pra fechar).
 *
 * NOTA DE FIDELIDADE: os ícones lucide-react da fonte não têm equivalente 1:1 em Material Icons.
 * Mapeamento por aproximação semântica registrado em cada `Icons.Filled.*` abaixo — ver comentário
 * ao lado de cada um. `Castle` e `Checklist` não foram confirmados por build real (sem acesso a
 * Gradle/Android SDK neste ambiente) — se o build falhar nesses dois símbolos, os substitutos
 * sugeridos são `Icons.Filled.AccountBalance` (Castle) e `Icons.Filled.List` (Checklist).
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
private val Zinc400 = Color(0xFF9CA3AF)
private val White10 = Color(0x1AFFFFFF)
private val trackingWider = 0.05.em

data class SubTabOption(
    val value: String,
    val label: String,
    val iconRes: Int,
    val color: Color
)

private data class NavItem(
    val id: String,
    val label: String,
    val iconRes: Int,
    val targetTab: String
)

// value -> id do módulo dono da aba, fiel a getActiveModule() da fonte.
private val MISSIONS_TABS = setOf("habits", "dailies", "todos", "quests", "history")
private val CHARACTER_TABS = setOf("character", "inventory")

fun getActiveModule(tab: String): String = when {
    tab == "focus" -> "focus"
    tab in CHARACTER_TABS -> "character"
    tab == "skills" -> "skills"
    tab in MISSIONS_TABS -> "missions"
    else -> "kingdom"
}

val MODULE_TITLES: Map<String, String> = mapOf(
    "character" to "Herói",
    "missions" to "Missões",
    "kingdom" to "Reino"
)

val SUB_TABS: Map<String, List<SubTabOption>> = mapOf(
    "character" to listOf(
        SubTabOption("character", "Status", R.drawable.lucide_ic_circle_user, Sky400),
        SubTabOption("inventory", "Inventário", R.drawable.lucide_ic_backpack, Rose400)
    ),
    "missions" to listOf(
        SubTabOption("habits", "Capela de Hábitos", R.drawable.lucide_ic_repeat, Emerald400),
        SubTabOption("dailies", "Tarefas Diárias", R.drawable.lucide_ic_calendar, Blue400),
        SubTabOption("todos", "Missões Avulsas", R.drawable.lucide_ic_clipboard_list, Slate300),
        SubTabOption("quests", "CONTRATOS", R.drawable.lucide_ic_scroll_text, Amber300),
        SubTabOption("history", "Crônicas Diárias", R.drawable.lucide_ic_history, Orange400)
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

private val NAV_ITEMS = listOf(
    NavItem("focus", "Foco", R.drawable.lucide_ic_timer, "focus"),
    NavItem("character", "Herói", R.drawable.lucide_ic_shield_user, "character"),
    NavItem("skills", "Skills", R.drawable.lucide_ic_book_open, "skills"),
    NavItem("missions", "Missões", R.drawable.lucide_ic_compass, "habits"),
    NavItem("kingdom", "Reino", R.drawable.lucide_ic_castle, "shop")
)

private val MODULES_WITH_SUBTABS = setOf("character", "missions", "kingdom")

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

    var pillTargetX by remember { mutableFloatStateOf(0f) }
    var pillWidth by remember { mutableFloatStateOf(0f) }

    val animatedPillOffset by animateIntOffsetAsState(
        targetValue = IntOffset(pillTargetX.toInt(), 0),
        animationSpec = tween(durationMillis = 250),
        label = "pillOffset"
    )

    Column(modifier = modifier
        .hazeEffect(state = hazeState, style = HazeMaterials.ultraThin(containerColor = Stone950.copy(alpha = 0.7f)))
        .shadow(12.dp, shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp))
        .clip(RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Stone950)
                    .border(width = 2.dp, color = White10)
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NAV_ITEMS.forEach { item ->
                    val isActive = activeModule == item.id
                    val hasSubTabs = item.id in MODULES_WITH_SUBTABS

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .onGloballyPositioned { coordinates ->
                                if (isActive) {
                                    pillTargetX = coordinates.positionInRoot().x
                                    pillWidth = coordinates.size.width.toFloat()
                                }
                            }
                            .clip(RoundedCornerShape(4.dp))
                            .clickable {
                                if (hasSubTabs) {
                                    openDropdown = if (openDropdown == item.id) null else item.id
                                } else {
                                    openDropdown = null
                                    onChangeTab(item.targetTab)
                                }
                            }
                            .padding(vertical = 4.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val contentOffset by animateDpAsState(
                            targetValue = if (isActive) 0.dp else 7.dp,
                            animationSpec = tween(durationMillis = 200),
                            label = "contentOffset"
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .offset(y = contentOffset)
                                .graphicsLayer {
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
                            if (isActive) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.label.uppercase(),
                                    color = Champagne400,
                                    fontSize = 10.sp,
                                    fontFamily = Cinzel,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = trackingWider,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // Animated active pill
            Box(
                modifier = Modifier
                    .offset { animatedPillOffset }
                    .size(width = pillWidth.dp, height = 40.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Champagne500.copy(alpha = 0.05f))
                    .border(1.dp, Champagne500.copy(alpha = 0.10f), RoundedCornerShape(4.dp))
            )
        }
    }

    val dropdown = openDropdown
    if (dropdown != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { openDropdown = null },
            sheetState = sheetState,
            containerColor = Stone950
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
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
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(sub.iconRes),
                                    contentDescription = null,
                                    tint = sub.color,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = sub.label.uppercase(),
                                    color = if (isSelected) Champagne300 else Zinc300.copy(alpha = 0.60f),
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
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}