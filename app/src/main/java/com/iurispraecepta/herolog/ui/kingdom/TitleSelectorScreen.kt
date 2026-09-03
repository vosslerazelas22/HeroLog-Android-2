package com.iurispraecepta.herolog.ui.kingdom

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.iurispraecepta.herolog.data.TITLE_CATALOG
import com.iurispraecepta.herolog.data.TitleItem
import com.iurispraecepta.herolog.ui.theme.Amber100
import com.iurispraecepta.herolog.ui.theme.Amber500
import com.iurispraecepta.herolog.ui.theme.Cinzel
import com.iurispraecepta.herolog.ui.theme.JetBrainsMono
import com.iurispraecepta.herolog.ui.theme.Stone900
import com.iurispraecepta.herolog.ui.theme.Stone950

private val Champagne300 = Color(0xFFE9CF7C)
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
 * Nota sobre ícones: `Award` (header + "Sua estante de brasões está vazia") aproximado como
 * `WorkspacePremium` (mesma escolha do Bloco C em `AchievementsScreen.kt`, não confirmado por
 * build real ainda). `Shield` exato.
 */
@Composable
fun TitleSelectorScreen(
    ownedTitles: List<String>,
    equippedTitle: String?,
    onEquipTitle: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val unlockedTitles = TITLE_CATALOG.filter { ownedTitles.contains(it.id) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Stone950)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // ═══ CABEÇALHO DO PAINEL (vem do wrapper em App.tsx) ═══
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.WorkspacePremium,
                contentDescription = null,
                tint = Champagne500,
                modifier = Modifier.height(16.dp).width(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "TÍTULOS",
                fontFamily = Cinzel,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                letterSpacing = 0.10.em,
                color = Champagne400
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            // Intro
            Text(
                text = "Escolha o título que deseja exibir no seu perfil e ative os benefícios vinculados a ele.",
                fontFamily = Cinzel,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 0.14.em,
                color = Champagne400
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Amber500.copy(alpha = 0.10f)))
            Spacer(modifier = Modifier.height(16.dp))

            if (unlockedTitles.isEmpty()) {
                EmptyTitlesState()
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Shield,
                            contentDescription = null,
                            tint = Champagne400,
                            modifier = Modifier.height(14.dp).width(14.dp)
                        )
                        Text(
                            text = "SEUS TÍTULOS DESBLOQUEADOS (${unlockedTitles.size})",
                            fontFamily = Cinzel,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.08.em,
                            color = Champagne300
                        )
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Amber500.copy(alpha = 0.10f)))
                    Spacer(modifier = Modifier.height(2.dp))

                    // grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 — telas Android tratadas como
                    // "sm" (2 colunas), consistente com o resto do módulo Reino
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth().height(600.dp)
                    ) {
                        items(unlockedTitles) { title ->
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
            targetValue = 0.4f,
            animationSpec = infiniteRepeatable(animation = tween(1000), repeatMode = RepeatMode.Reverse),
            label = "icon_alpha"
        )
        Icon(
            imageVector = Icons.Filled.WorkspacePremium,
            contentDescription = null,
            tint = Amber500.copy(alpha = 0.20f),
            modifier = Modifier.height(48.dp).width(48.dp).alpha(iconAlpha)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Sua estante de brasões está vazia!",
            fontFamily = Cinzel,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            letterSpacing = 0.05.em,
            color = Champagne400
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Nenhum título honorífico foi conquistado ainda. Cultive sua força de vontade nas Missões de Foco ou compre patentes de prestígio no Bazar de Mystara!",
            fontFamily = Cinzel,
            fontSize = 10.sp,
            lineHeight = 15.sp,
            color = Amber100.copy(alpha = 0.40f),
            modifier = Modifier.widthIn(max = 320.dp)
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
                        text = title.name,
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
                        text = title.category.name.lowercase(),
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
                        letterSpacing = 0.12.em,
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
                        letterSpacing = 0.12.em,
                        color = Stone950
                    )
                }
            }
        }

        if (isEquipped) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-6).dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Amber500)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "ATIVO",
                    fontFamily = Cinzel,
                    fontWeight = FontWeight.Black,
                    fontSize = 8.sp,
                    letterSpacing = 0.08.em,
                    color = Stone950
                )
            }
        }
    }
}
