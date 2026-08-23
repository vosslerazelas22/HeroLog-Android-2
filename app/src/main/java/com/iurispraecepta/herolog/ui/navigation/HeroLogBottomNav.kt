package com.iurispraecepta.herolog.ui.navigation

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Backpack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Castle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iurispraecepta.herolog.ui.theme.Amber400
import com.iurispraecepta.herolog.ui.theme.Amber500
import com.iurispraecepta.herolog.ui.theme.Stone950

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

data class SubTabOption(
    val value: String,
    val label: String,
    val icon: ImageVector,
    val color: Color
)

private data class NavItem(
    val id: String,
    val label: String,
    val icon: ImageVector,
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
        SubTabOption("character", "Status", Icons.Filled.AccountCircle, Sky400), // UserCircle
        SubTabOption("inventory", "Inventário", Icons.Filled.Backpack, Rose400)
    ),
    "missions" to listOf(
        SubTabOption("habits", "Capela de Hábitos", Icons.Filled.Repeat, Emerald400),
        SubTabOption("dailies", "Tarefas Diárias", Icons.Filled.DateRange, Blue400), // Calendar
        SubTabOption("todos", "Missões Avulsas", Icons.Filled.Checklist, Slate300), // ClipboardList
        SubTabOption("quests", "CONTRATOS", Icons.Filled.Description, Amber300), // ScrollText
        SubTabOption("history", "Crônicas Diárias", Icons.Filled.History, Orange400)
    ),
    "kingdom" to listOf(
        SubTabOption("shop", "Bazar de Mystara", Icons.Filled.MonetizationOn, Yellow400), // Coins
        SubTabOption("titles", "TÍTULOS", Icons.Filled.MilitaryTech, Violet400), // Medal
        SubTabOption("heatmap", "Heatmap", Icons.Filled.GridView, Blue400), // Grid3x3
        SubTabOption("stats", "ESTATÍSTICAS DO HERÓI", Icons.Filled.BarChart, Cyan400), // ChartColumn
        SubTabOption("achievements", "CONQUISTAS", Icons.Filled.EmojiEvents, Yellow500), // Trophy
        SubTabOption("logs", "REGISTROS", Icons.Filled.Description, Stone300), // FileText
        SubTabOption("guide", "Tutorial", Icons.AutoMirrored.Filled.HelpOutline, Red400) // HelpCircle
    )
)

private val NAV_ITEMS = listOf(
    NavItem("focus", "Foco", Icons.Filled.Timer, "focus"),
    NavItem("character", "Herói", Icons.Filled.Shield, "character"), // ShieldUser
    NavItem("skills", "Skills", Icons.AutoMirrored.Filled.MenuBook, "skills"), // BookOpen
    NavItem("missions", "Missões", Icons.Filled.Explore, "habits"), // Compass
    NavItem("kingdom", "Reino", Icons.Filled.Castle, "shop")
)

private val MODULES_WITH_SUBTABS = setOf("character", "missions", "kingdom")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeroLogBottomNav(
    activeTab: String,
    onChangeTab: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var openDropdown by remember { mutableStateOf<String?>(null) }
    val activeModule = getActiveModule(activeTab)

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Stone950)
                .border(width = 2.dp, color = Amber500.copy(alpha = 0.20f))
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            NAV_ITEMS.forEach { item ->
                val isActive = activeModule == item.id
                val hasSubTabs = item.id in MODULES_WITH_SUBTABS

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .then(
                            if (isActive) {
                                Modifier
                                    .background(Amber500.copy(alpha = 0.05f))
                                    .border(1.dp, Amber500.copy(alpha = 0.10f), RoundedCornerShape(4.dp))
                            } else {
                                Modifier
                            }
                        )
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = if (isActive) Amber400 else Amber400.copy(alpha = 0.40f),
                            modifier = Modifier.size(20.dp)
                        )
                        if (isActive) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.label.uppercase(),
                                color = Amber400,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
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
                        color = Amber400,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    (SUB_TABS[dropdown] ?: emptyList()).forEach { sub ->
                        val isSelected = activeTab == sub.value
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Amber500.copy(alpha = 0.15f) else Stone950.copy(alpha = 0.6f))
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Amber500.copy(alpha = 0.5f) else Color.Transparent,
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
                                    imageVector = sub.icon,
                                    contentDescription = null,
                                    tint = sub.color,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = sub.label.uppercase(),
                                    color = if (isSelected) Amber300 else Amber400.copy(alpha = 0.60f),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = Amber400,
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