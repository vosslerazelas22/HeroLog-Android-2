// app/src/main/java/com/iurispraecepta/herolog/ui/kingdom/TitleShopScreen.kt
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.ShoppingCart
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.iurispraecepta.herolog.data.TITLE_CATALOG
import com.iurispraecepta.herolog.data.TitleCategory
import com.iurispraecepta.herolog.data.TitleItem
import com.iurispraecepta.herolog.model.CharacterState
import com.iurispraecepta.herolog.ui.theme.Amber500
import com.iurispraecepta.herolog.ui.theme.Cinzel
import com.iurispraecepta.herolog.ui.theme.JetBrainsMono
import com.iurispraecepta.herolog.ui.theme.Stone800
import com.iurispraecepta.herolog.ui.theme.Stone900
import com.iurispraecepta.herolog.ui.theme.Stone950
import java.util.Locale

// ─── Cores sem equivalente nomeado no tema global (mesmo padrão de ShopScreen.kt/GuideScreen.kt) ───
private val Champagne300 = Color(0xFFF5DFA0)
private val Champagne400 = Color(0xFFE5C158)
private val Amber100Half = Color(0xFFFEF3C7)
private val AmberD97706 = Color(0xFFD97706) // cor exata usada só no rótulo RARE, fora da escala nomeada
private val Purple300 = Color(0xFFD8B4FE)
private val Purple400 = Color(0xFFC084FC)
private val Purple500 = Color(0xFFA855F7)
private val Emerald300 = Color(0xFF6EE7B7)
private val Emerald400 = Color(0xFF34D399)
private val Yellow400 = Color(0xFFFACC15)
private val Stone400 = Color(0xFFA8A29E)
private val Stone500 = Color(0xFF78716C)
private val Amber300 = Color(0xFFFCD34D) // amber-300 exato — cor do texto "Comprar" na fonte
private val Amber400 = Color(0xFFFBBF24) // amber-400 exato — cor do header intro na fonte
private val Cyan400 = Color(0xFF38BDF8)

/**
 * Sub-aba "Selos & Títulos Reais" do Bazar — porte de `TitleShop.tsx` (`src/modules/kingdom/`).
 *
 * Substitui o placeholder temporário deixado em `ShopScreen.kt` (Bloco E) na sub-aba `titles`
 * do `shopSubTab`. **Não confundir com `TitleSelectorScreen.kt`** (rota `activeTab === 'titles'`
 * da navegação inferior, componente diferente — `TitleSelector.tsx` — que serve pra *equipar* um
 * título já possuído; esta tela aqui serve pra *comprar/resgatar* títulos novos). Ver nota de
 * nomenclatura no `PARIDADE.md`/`DEV_LOG_ANDROID.md` sobre a colisão do rótulo "Bloco C" entre
 * chats paralelos — este trabalho ficou sem bloco numerado atribuído até este ponto.
 *
 * Sem lógica nova: reusa `TITLE_CATALOG` (Bloco 8) e `TitleLogic.buyTitle`/`claimAchievementTitle`
 * (Bloco 7, já com os 2 bugs do React corrigidos — `buyTitle` não desconta ouro de título já
 * possuído, `claimAchievementTitle` idempotente). `onBuyTitle`/`onClaimAchievementTitle` chamam a
 * função correspondente no `HeroLogViewModel`, que precisa existir (não existia antes deste
 * arquivo — ver `buyTitle`/`claimAchievementTitle` adicionados junto).
 *
 * Fiel à fonte: 4 seções de compra (Common/Rare/Epic/Legendary) + Achievement + Drop, cada uma
 * filtrando `TITLE_CATALOG` por `category`. Grid de 2 colunas para Common/Rare (breakpoint móvel
 * real da fonte, `grid-cols-2`) e 1 coluna para Epic/Legendary/Achievement/Drop (`grid-cols-1`
 * no mobile) — os breakpoints maiores (`sm:`/`lg:`) da fonte não se aplicam a uma tela de
 * celular. Grid implementado sem `LazyVerticalGrid` (linhas fixas via `chunked`), já que esta
 * tela inteira já vive dentro de um `verticalScroll` — aninhar um `LazyVerticalGrid` dentro de
 * uma coluna rolável quebraria em runtime (altura infinita).
 *
 * **Simplificações conscientes** (mesmo precedente de `GuideScreen.kt`/`HeatmapScreen.kt`): sem
 * o `title=` de tooltip HTML no botão "Loot Raro" (não há hover em touch); preço formatado com
 * separador de milhar `pt-BR` (`NumberFormat`), já que `toLocaleString()` da fonte não fixa
 * locale explícito mas o app inteiro é em português — mesma suposição de localidade já usada em
 * outros pontos do port (datas `dd/MM/yyyy`).
 *
 * Ícones: `ShoppingBag → ShoppingCart`, `Award → WorkspacePremium` (mesma escolha já usada em
 * `AchievementsScreen.kt`/`TitleSelectorScreen.kt`), `Sparkles → AutoAwesome` (idem
 * `AchievementsScreen.kt`/`GuideScreen.kt`), `Coins` da fonte não usado nesta tela (só aparece no
 * import do React, não é renderizado em nenhum lugar do JSX real — confirmado na leitura).
 */
@Composable
fun TitleShopScreen(
    state: CharacterState,
    onBuyTitle: (titleId: String, price: Int) -> Unit,
    onClaimAchievementTitle: (titleId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val ownedTitles = state.ownedTitles ?: emptyList()
    val catalog = TITLE_CATALOG

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ═══ Cabeçalho introdutório ═══
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp)
        ) {
            Text(
                text = "MERCADO DE TÍTULOS NOBRES: ADQUIRA BRASÕES COM SEU GP ACUMULADO OU " +
                    "RESGATE SUAS MARCAS DE FEITOS GLORIOSOS.",
                fontFamily = Cinzel,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 0.14.em,
                color = Amber400
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Amber500.copy(alpha = 0.10f))
            )
        }

        // ═══ SEÇÃO 1: TÍTULOS ADQUIRÍVEIS (GP) ═══
        TitleShopSectionHeader(
            icon = Icons.Filled.ShoppingCart,
            iconTint = Amber500,
            title = "Títulos Adquiríveis (GP)",
            titleColor = Champagne400
        )

        CategorySubgroup(
            label = "COMMON · 150 - 500 GP",
            dotColor = Stone500,
            labelColor = Amber100Half.copy(alpha = 0.30f)
        ) {
            TitleGrid(
                items = catalog.filter { it.category == TitleCategory.Common },
                columns = 2,
                state = state,
                ownedTitles = ownedTitles,
                onBuyTitle = onBuyTitle,
                onClaimAchievementTitle = onClaimAchievementTitle
            )
        }

        CategorySubgroup(
            label = "RARE · 1.500 - 6.000 GP",
            dotColor = Amber500,
            labelColor = AmberD97706
        ) {
            TitleGrid(
                items = catalog.filter { it.category == TitleCategory.Rare },
                columns = 2,
                state = state,
                ownedTitles = ownedTitles,
                onBuyTitle = onBuyTitle,
                onClaimAchievementTitle = onClaimAchievementTitle
            )
        }

        CategorySubgroup(
            label = "EPIC · 10.000 - 50.000 GP",
            sublabel = "(concede atributos passivos ao equipar)",
            dotColor = Purple500,
            labelColor = Purple400,
            pulseDot = true
        ) {
            TitleGrid(
                items = catalog.filter { it.category == TitleCategory.Epic },
                columns = 1,
                state = state,
                ownedTitles = ownedTitles,
                onBuyTitle = onBuyTitle,
                onClaimAchievementTitle = onClaimAchievementTitle
            )
        }

        CategorySubgroup(
            label = "LEGENDARY · 100.000 - 500.000 GP",
            sublabel = "(concede atributos supremos ao equipar)",
            dotColor = Yellow400,
            labelColor = Yellow400,
            pulseDot = true
        ) {
            TitleGrid(
                items = catalog.filter { it.category == TitleCategory.Legendary },
                columns = 1,
                state = state,
                ownedTitles = ownedTitles,
                onBuyTitle = onBuyTitle,
                onClaimAchievementTitle = onClaimAchievementTitle
            )
        }

        // ═══ SEÇÃO 2: ACHIEVEMENT TITLES ═══
        TitleShopSectionHeader(
            icon = Icons.Filled.WorkspacePremium,
            iconTint = Purple400,
            title = "Achievements Titles · Resgate por Milestones de Conquista",
            titleColor = Purple300
        )
        TitleGrid(
            items = catalog.filter { it.category == TitleCategory.Achievement },
            columns = 1,
            state = state,
            ownedTitles = ownedTitles,
            onBuyTitle = onBuyTitle,
            onClaimAchievementTitle = onClaimAchievementTitle
        )

        // ═══ SEÇÃO 3: RARE DROP TITLES ═══
        TitleShopSectionHeader(
            icon = Icons.Filled.AutoAwesome,
            iconTint = Emerald400,
            title = "Rare Drop Titles · Sorteados ao Completar Sessões de Estudo",
            titleColor = Emerald300
        )
        TitleGrid(
            items = catalog.filter { it.category == TitleCategory.Drop },
            columns = 1,
            state = state,
            ownedTitles = ownedTitles,
            onBuyTitle = onBuyTitle,
            onClaimAchievementTitle = onClaimAchievementTitle
        )
    }
}

@Composable
private fun TitleShopSectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    titleColor: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title.uppercase(),
                fontFamily = Cinzel,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 0.08.em,
                color = titleColor
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Amber500.copy(alpha = 0.10f))
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun CategorySubgroup(
    label: String,
    dotColor: Color,
    labelColor: Color,
    sublabel: String? = null,
    pulseDot: Boolean = false,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val dotAlpha = if (pulseDot) {
                val infiniteTransition = rememberInfiniteTransition(label = "category_dot_pulse")
                val alphaAnim by infiniteTransition.animateFloat(
                    initialValue = 1.0f,
                    targetValue = 0.35f,
                    animationSpec = infiniteRepeatable(animation = tween(900), repeatMode = RepeatMode.Reverse),
                    label = "dot_alpha"
                )
                alphaAnim
            } else {
                1.0f
            }

            Box(
                modifier = Modifier
                    .size(6.dp)
                    .alpha(dotAlpha)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                letterSpacing = 0.08.em,
                color = labelColor
            )
            if (sublabel != null) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = sublabel,
                    fontFamily = Cinzel,
                    fontSize = 8.sp,
                    color = Cyan400
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        content()
        Spacer(modifier = Modifier.height(4.dp))
    }
}

/**
 * Grid não-lazy (linhas via `chunked`) — evita o crash de `LazyVerticalGrid` aninhado dentro do
 * `verticalScroll` da tela toda. `columns` = 2 (Common/Rare) ou 1 (Epic/Legendary/Achievement/
 * Drop), espelhando o breakpoint móvel mais estreito da fonte (`grid-cols-2`/`grid-cols-1`).
 */
@Composable
private fun TitleGrid(
    items: List<TitleItem>,
    columns: Int,
    state: CharacterState,
    ownedTitles: List<String>,
    onBuyTitle: (String, Int) -> Unit,
    onClaimAchievementTitle: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.chunked(columns).forEach { rowItems ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowItems.forEach { title ->
                    Box(modifier = Modifier.weight(1f)) {
                        TitleCard(
                            title = title,
                            isOwned = ownedTitles.contains(title.id),
                            gold = state.gold,
                            state = state,
                            onBuy = { onBuyTitle(title.id, title.price ?: 0) },
                            onClaim = { onClaimAchievementTitle(title.id) }
                        )
                    }
                }
                // Preenche a linha se sobrar espaço (última linha incompleta) pra manter o
                // alinhamento dos cards já existentes na mesma largura.
                repeat(columns - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun TitleCard(
    title: TitleItem,
    isOwned: Boolean,
    gold: Int,
    state: CharacterState,
    onBuy: () -> Unit,
    onClaim: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isAchievementUnlocked = title.category == TitleCategory.Achievement &&
        title.checkUnlocked?.invoke(state) == true

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(title.visualStyle.backgroundColor)
            .border(1.dp, title.visualStyle.borderColor, RoundedCornerShape(6.dp))
            .padding(12.dp)
    ) {
        // ─── Cabeçalho: emoji + nome ───
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = title.emoji, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title.name.uppercase(),
                fontFamily = Cinzel,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 0.06.em,
                color = title.visualStyle.glowTextColor
            )
        }

        // ─── Perks (Epic/Legendary/Achievement/Drop) ───
        if (!title.perks.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                title.perks.forEach { perk ->
                    Text(
                        text = perk,
                        fontFamily = JetBrainsMono,
                        fontSize = 9.sp,
                        color = Cyan400
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Amber500.copy(alpha = 0.05f))
        )
        Spacer(modifier = Modifier.height(8.dp))

        // ─── Rodapé: preço/requisito + botão ───
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TitleFooterLeft(title = title, isOwned = isOwned, isAchievementUnlocked = isAchievementUnlocked)
            TitleFooterAction(
                title = title,
                isOwned = isOwned,
                isAchievementUnlocked = isAchievementUnlocked,
                gold = gold,
                onBuy = onBuy,
                onClaim = onClaim
            )
        }
    }
}

private val gpFormatter = java.text.NumberFormat.getNumberInstance(Locale("pt", "BR"))

@Composable
private fun TitleFooterLeft(title: TitleItem, isOwned: Boolean, isAchievementUnlocked: Boolean) {
    when (title.category) {
        TitleCategory.Achievement -> {
            val color = if (isOwned) Emerald400 else if (isAchievementUnlocked) Amber500 else Stone500
            Text(
                text = if (isOwned) "🔓 Adquirido" else "🔒 ${title.requirementText}",
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                color = color
            )
        }
        TitleCategory.Drop -> {
            Text(
                text = "♦ ${title.dropChanceText}",
                fontFamily = Cinzel,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                color = Amber500.copy(alpha = 0.60f)
            )
        }
        else -> {
            if (!isOwned && title.price != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = gpFormatter.format(title.price),
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = Amber500
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "GP",
                        fontFamily = JetBrainsMono,
                        fontSize = 8.sp,
                        color = Amber500.copy(alpha = 0.70f)
                    )
                }
            } else {
                Text(
                    text = "Adquirido",
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    color = Emerald400
                )
            }
        }
    }
}

@Composable
private fun TitleFooterAction(
    title: TitleItem,
    isOwned: Boolean,
    isAchievementUnlocked: Boolean,
    gold: Int,
    onBuy: () -> Unit,
    onClaim: () -> Unit
) {
    when {
        isOwned -> {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Emerald400.copy(alpha = 0.04f))
                    .border(1.dp, Emerald400.copy(alpha = 0.20f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Desbloqueado",
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Bold,
                    fontSize = 8.sp,
                    letterSpacing = 0.05.em,
                    color = Emerald400.copy(alpha = 0.80f)
                )
            }
        }
        title.category == TitleCategory.Achievement -> {
            if (isAchievementUnlocked) {
                val infiniteTransition = rememberInfiniteTransition(label = "claim_button_pulse")
                val buttonAlpha by infiniteTransition.animateFloat(
                    initialValue = 1.0f,
                    targetValue = 0.55f,
                    animationSpec = infiniteRepeatable(animation = tween(900), repeatMode = RepeatMode.Reverse),
                    label = "claim_alpha"
                )
                Box(
                    modifier = Modifier
                        .alpha(buttonAlpha)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFD97706))
                        .clickable(onClick = onClaim)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "Resgatar",
                        fontFamily = Cinzel,
                        fontWeight = FontWeight.Black,
                        fontSize = 8.sp,
                        letterSpacing = 0.06.em,
                        color = Amber100Half
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Stone900)
                        .border(1.dp, Stone800, RoundedCornerShape(4.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "Bloqueado",
                        fontFamily = Cinzel,
                        fontWeight = FontWeight.Black,
                        fontSize = 8.sp,
                        letterSpacing = 0.06.em,
                        color = Stone500
                    )
                }
            }
        }
        title.category == TitleCategory.Drop -> {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Stone900)
                    .border(1.dp, Stone800, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "Loot Raro",
                    fontFamily = Cinzel,
                    fontWeight = FontWeight.Black,
                    fontSize = 8.sp,
                    letterSpacing = 0.06.em,
                    color = Stone500
                )
            }
        }
        else -> {
            val canAfford = gold >= (title.price ?: 0)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (canAfford) Color(0xFF40301A) else Stone900)
                    .border(
                        width = 1.dp,
                        color = if (canAfford) Amber500.copy(alpha = 0.20f) else Stone800,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .then(if (canAfford) Modifier.clickable(onClick = onBuy) else Modifier)
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "Comprar",
                    fontFamily = Cinzel,
                    fontWeight = FontWeight.Black,
                    fontSize = 8.sp,
                    letterSpacing = 0.06.em,
                    color = if (canAfford) Amber300 else Stone500
                )
            }
        }
    }
}
