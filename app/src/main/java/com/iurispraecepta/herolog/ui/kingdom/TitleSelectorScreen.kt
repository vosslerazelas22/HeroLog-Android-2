package com.iurispraecepta.herolog.ui.kingdom

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.R as LucideR
import com.iurispraecepta.herolog.data.TITLE_CATALOG
import com.iurispraecepta.herolog.data.TitleItem
import com.iurispraecepta.herolog.ui.theme.Amber100
import com.iurispraecepta.herolog.ui.theme.Amber500
import com.iurispraecepta.herolog.ui.theme.Cinzel
import com.iurispraecepta.herolog.ui.navigation.LocalBottomBarInset
import com.iurispraecepta.herolog.ui.theme.JetBrainsMono
import com.iurispraecepta.herolog.ui.theme.QuestPanel
import com.iurispraecepta.herolog.ui.theme.Stone900
import com.iurispraecepta.herolog.ui.theme.Stone950

private val Champagne300 = Color(0xFFF5DFA0) // champagne-300 real do index.css do React (#f5dfa0)
private val Champagne400 = Color(0xFFE5C158)
private val Champagne500 = Color(0xFFD4AF37)
private val Stone400 = Color(0xFFA8A29E)
private val Stone700 = Color(0xFF44403C)
private val Stone300 = Color(0xFFD6D3D1) // stone-300 exato — cor do texto "Remover" na fonte
private val Stone800 = Color(0xFF292524)

/**
 * Porte fiel de TitleSelector.tsx (src/modules/character/TitleSelector.tsx) + o cabeçalho de
 * painel do wrapper em App.tsx (activeTab === 'titles').
 *
 * ⚠️ ACHADO DE ESCOPO (24/08): este é o componente correto pra rota `"titles"` do
 * `MainActivity.kt`. `TitleShop.tsx` — que o `MOC_reino_pre_port.md` e o plano original do
 * Bloco C listavam como "Loja de Títulos" — **não é essa tela**. Na fonte real, `TitleShop` é
 * renderizado como sub-aba (`shopSubTab === 'titles'`) *dentro* da tela do Bazar
 * (`activeTab === 'shop'`), ao lado de `ShopTab`, alternado por um toggle local (ver `App.tsx`
 * linhas ~3404-3436). `TitleShop.tsx` fica de fora deste Bloco — pertence ao Bloco E
 * (`ShopScreen.kt`), que vai precisar de um toggle "Consumíveis & Equipamentos" / "Selos e
 * Títulos Reais" para acomodar as duas sub-abas.
 *
 * Também não confundir com `TitleEquipModal.kt` (já portado, Bloco 12) — mesma função
 * (equipar título) mas UI diferente (modal categorizado por raridade, chamado de outro lugar,
 * provavelmente do Personagem). `TitleSelector.tsx` é uma tela cheia, lista plana com contador
 * "Seus Títulos Desbloqueados (N)", sem agrupamento por categoria — texto e estrutura diferentes,
 * portados aqui literalmente a partir da fonte real (não reaproveitado do modal).
 *
 * Sem lógica nova — reusa `TITLE_CATALOG` (Bloco 8) e a ação de equipar já existe em
 * `HeroLogViewModel.equipTitle` (chama `TitleLogic.equipTitle`, já com o bug de posse corrigido).
 *
 * Nota sobre ícones: `Award` (header + "Sua estante de brasões está vazia") e `Shield`
 * via Lucide (`LucideR.drawable.lucide_ic_award` / `lucide_ic_shield`, `painterResource`),
 * literais do `TitleSelector.tsx` — sem aproximação Material.
 */
@Composable
fun TitleSelectorScreen(
    ownedTitles: List<String>,
    equippedTitle: String?,
    onEquipTitle: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val unlockedTitles = TITLE_CATALOG.filter { ownedTitles.contains(it.id) }

    // A tela inteira rola (no React a página rola, não só a lista). O inset inferior vem do
    // Scaffold via `LocalBottomBarInset` — sem ele o fim da lista ficava sob a bottom nav
    // (mesmo padrão de ShopScreen/StatsScreen/AchievementsScreen/FocusCompletionFlow).
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 12.dp, bottom = 24.dp + LocalBottomBarInset.current)
    ) {
        // Wrapper do painel (App.tsx, activeTab === 'titles'):
        // `bg-quest-panel border border-amber-500/15 rounded-lg overflow-hidden p-5`.
        // No mobile a fonte usa `px-0` (painel de borda a borda), por isso sem margem lateral.
        // Sombra `shadow-[0_12px_40px_rgba(0,0,0,0.7)]` não portada (preta sobre fundo escuro).
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(QuestPanel)
                .border(1.dp, Amber500.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                .padding(20.dp)
        ) {
            // ═══ CABEÇALHO DO PAINEL (vem do wrapper em App.tsx) ═══
            // `pb-2.5 border-b border-amber-500/10 mb-4 flex justify-center items-center min-h-[35px]`
            // (min-h inclui o padding e a borda: 34dp + 1dp de linha).
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
                        painter = painterResource(LucideR.drawable.lucide_ic_award),
                        contentDescription = null,
                        tint = Champagne500,
                        modifier = Modifier.height(16.dp).width(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TÍTULOS",
                        fontFamily = Cinzel,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        letterSpacing = 0.05.em,
                        color = Champagne400
                    )
                }
            }
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Amber500.copy(alpha = 0.10f)))
            Spacer(modifier = Modifier.height(16.dp))

            // `space-y-6` entre o bloco de intro e o conteúdo
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                // Intro: `text-xs uppercase tracking-[0.14em] font-bold` + `pb-4 border-b`
                Column {
                    Text(
                        text = "Escolha o título que deseja exibir no seu perfil e ative os benefícios vinculados a ele.".uppercase(),
                        fontFamily = Cinzel,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        letterSpacing = 0.14.em,
                        color = Champagne400
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Amber500.copy(alpha = 0.10f)))
                }

                if (unlockedTitles.isEmpty()) {
                    EmptyTitlesState()
                } else {
                    // `space-y-4` entre o cabeçalho da seção e a lista
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // `flex items-center gap-2 border-b border-amber-500/10 pb-1.5`
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    painter = painterResource(LucideR.drawable.lucide_ic_shield),
                                    contentDescription = null,
                                    tint = Champagne400,
                                    modifier = Modifier.height(16.dp).width(16.dp)
                                )
                                Text(
                                    text = "SEUS TÍTULOS DESBLOQUEADOS (${unlockedTitles.size})",
                                    fontFamily = Cinzel,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    letterSpacing = 0.10.em,
                                    color = Champagne300
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Amber500.copy(alpha = 0.10f)))
                        }

                        // `grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4` — num celular (< 640dp, o
                        // breakpoint `sm`) a fonte renderiza 1 coluna. Sem LazyVerticalGrid (crash
                        // quando aninhado em verticalScroll).
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            unlockedTitles.forEach { title ->
                                TitleCard(
                                    title = title,
                                    isEquipped = equippedTitle == title.id,
                                    onEquip = { onEquipTitle(title.id) },
                                    onUnequip = { onEquipTitle(null) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyTitlesState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Stone900.copy(alpha = 0.20f))
            .border(1.dp, Amber500.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
            .padding(vertical = 48.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "empty_titles_pulse")
        val iconAlpha by infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 0.5f, // animate-pulse do Tailwind: opacity 1 → .5 em 2s, cubic-bezier(.4,0,.6,1)
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = CubicBezierEasing(0.4f, 0f, 0.6f, 1f)),
                repeatMode = RepeatMode.Reverse
            ),
            label = "icon_alpha"
        )
        Icon(
            painter = painterResource(LucideR.drawable.lucide_ic_award),
            contentDescription = null,
            tint = Amber500.copy(alpha = 0.20f),
            modifier = Modifier.height(48.dp).width(48.dp).alpha(iconAlpha)
        )
        Spacer(modifier = Modifier.height(12.dp))
        // `text-xs ... uppercase tracking-wider` + `text-center` herdado do container
        Text(
            text = "Sua estante de brasões está vazia!".uppercase(),
            fontFamily = Cinzel,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            letterSpacing = 0.05.em,
            textAlign = TextAlign.Center,
            color = Champagne400
        )
        Spacer(modifier = Modifier.height(4.dp))
        // `text-[10px] mt-1 max-w-sm` (24rem = 384dp) com `leading-relaxed` (1.625 × 10 = 16.25sp)
        Text(
            text = "Nenhum título foi conquistado ainda. Cultive sua força de vontade nas Missões de Foco ou compre patentes de prestígio no Bazar de Mystara!",
            fontFamily = Cinzel,
            fontSize = 10.sp,
            lineHeight = 16.25.sp,
            textAlign = TextAlign.Center,
            color = Amber100.copy(alpha = 0.40f),
            modifier = Modifier.widthIn(max = 384.dp)
        )
    }
}

@Composable
private fun TitleCard(
    title: TitleItem,
    isEquipped: Boolean,
    onEquip: () -> Unit,
    onUnequip: () -> Unit
) {
    val borderColor = if (isEquipped) Amber500 else title.visualStyle.borderColor
    val backgroundColor = if (isEquipped) Stone900.copy(alpha = 0.90f) else title.visualStyle.backgroundColor

    Box {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(backgroundColor)
                .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = title.emoji, fontSize = 16.sp)
                    Text(
                        text = title.name.uppercase(),
                        fontFamily = Cinzel,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 0.06.em,
                        color = title.visualStyle.glowTextColor
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Stone950.copy(alpha = 0.40f))
                        .border(1.dp, Stone800.copy(alpha = 0.45f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = title.category.name.uppercase(), // `uppercase` do React
                        fontFamily = JetBrainsMono,
                        fontSize = 8.sp,
                        letterSpacing = 0.10.em,
                        color = Stone400
                    )
                }
                if (!title.perks.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        title.perks.forEach { perk ->
                            Text(
                                text = "⚡ $perk",
                                fontFamily = JetBrainsMono,
                                fontSize = 9.sp,
                                color = Color(0xFF38BDF8)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Amber500.copy(alpha = 0.05f)))
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (isEquipped) {
                    Text(
                        text = "REMOVER",
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Stone800)
                            .border(1.dp, Stone700.copy(alpha = 0.50f), RoundedCornerShape(4.dp))
                            .clickable(onClick = onUnequip)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        fontFamily = Cinzel,
                        fontWeight = FontWeight.Black,
                        fontSize = 8.sp,
                        letterSpacing = 0.10.em, // tracking-widest
                        color = Stone300
                    )
                } else {
                    Text(
                        text = "EQUIPAR",
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Amber500)
                            .border(1.dp, Color(0xFFFBBF24), RoundedCornerShape(4.dp))
                            .clickable(onClick = onEquip)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        fontFamily = Cinzel,
                        fontWeight = FontWeight.Black,
                        fontSize = 8.sp,
                        letterSpacing = 0.10.em, // tracking-widest
                        color = Stone950
                    )
                }
            }
        }

        if (isEquipped) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 8.dp, y = (-10).dp) // `-top-2.5 -right-2`
                    .clip(RoundedCornerShape(4.dp))
                    .background(Amber500)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "ATIVO",
                    fontFamily = Cinzel,
                    fontWeight = FontWeight.Black,
                    fontSize = 8.sp,
                    letterSpacing = 0.05.em, // tracking-wider
                    color = Stone950
                )
            }
        }
    }
}
