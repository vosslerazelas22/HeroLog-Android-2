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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.iurispraecepta.herolog.data.TITLE_CATALOG
import com.iurispraecepta.herolog.data.TitleCategory
import com.iurispraecepta.herolog.data.TitleItem
import com.iurispraecepta.herolog.data.createInitialCharacterState
import com.iurispraecepta.herolog.model.CharacterState
import com.iurispraecepta.herolog.ui.navigation.LocalBottomBarInset
import com.iurispraecepta.herolog.ui.theme.Amber100
import com.iurispraecepta.herolog.ui.theme.Amber300
import com.iurispraecepta.herolog.ui.theme.Amber500
import com.iurispraecepta.herolog.ui.theme.Amber600
import com.iurispraecepta.herolog.ui.theme.Cinzel
import com.iurispraecepta.herolog.ui.theme.Emerald400
import com.iurispraecepta.herolog.ui.theme.HeroLogTheme
import com.iurispraecepta.herolog.ui.theme.Inter
import com.iurispraecepta.herolog.ui.theme.JetBrainsMono
import com.iurispraecepta.herolog.ui.theme.QuestPanel
import com.iurispraecepta.herolog.ui.theme.RitualTextSecondary
import com.iurispraecepta.herolog.ui.theme.Stone500
import com.iurispraecepta.herolog.ui.theme.Stone800
import com.iurispraecepta.herolog.ui.theme.Stone900

/**
 * Sub-aba "Títulos" do Bazar — Loja de Títulos em **livro-caixa denso**, opção V2 aprovada
 * na exploração visual da issue #51.
 *
 * Cada título vira uma linha de catálogo (1 coluna em todas as categorias): medalhão de emoji
 * à esquerda, nome/perks/requisito no centro e ação à direita. Sem caixa por item; a separação
 * é uma linha fina e uma espinha lateral na cor da raridade. A ação é sempre um alvo de 48dp.
 *
 * Comportamento preservado da tela original: mesmas condições de posse, conquista desbloqueada,
 * preço acessível, perks opcionais, textos de rodapé e ações
 * (Comprar/Resgatar/Bloqueado/Espólio raro/Já adquirido). Apenas UI; nenhuma chamada de lógica nova.
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
            .padding(bottom = 24.dp + LocalBottomBarInset.current),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        TitleShopIntro()

        TitleShopSectionHeader(titleShopAcquirableSection)
        titleShopAcquirableGroups.forEach { group ->
            CategorySubgroup(group) {
                TitleLedger(
                    items = catalog.filter { it.category == group.category },
                    state = state,
                    ownedTitles = ownedTitles,
                    onBuyTitle = onBuyTitle,
                    onClaimAchievementTitle = onClaimAchievementTitle,
                )
            }
        }

        TitleShopSectionHeader(titleShopAchievementsSection)
        TitleLedger(
            items = catalog.filter { it.category == TitleCategory.Achievement },
            state = state,
            ownedTitles = ownedTitles,
            onBuyTitle = onBuyTitle,
            onClaimAchievementTitle = onClaimAchievementTitle,
        )

        TitleShopSectionHeader(titleShopDropsSection)
        TitleLedger(
            items = catalog.filter { it.category == TitleCategory.Drop },
            state = state,
            ownedTitles = ownedTitles,
            onBuyTitle = onBuyTitle,
            onClaimAchievementTitle = onClaimAchievementTitle,
        )
    }
}

@Composable
private fun TitleShopIntro() {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
        Text(
            text = "Adquira títulos com ouro acumulado ou resgate recompensas por conquistas.",
            fontFamily = Inter,
            fontWeight = FontWeight.Normal,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            color = RitualTextSecondary,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Amber500.copy(alpha = 0.10f)),
        )
    }
}

@Composable
private fun TitleShopSectionHeader(header: TitleShopSectionHeaderData) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(header.iconRes),
                contentDescription = null,
                tint = header.iconTint,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = header.title.uppercase(),
                fontFamily = Cinzel,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 0.08.em,
                color = header.titleColor,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Amber500.copy(alpha = 0.10f)),
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun CategorySubgroup(
    group: TitleShopCategoryGroupData,
    content: @Composable () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val dotAlpha = if (group.pulseDot) {
                val infiniteTransition = rememberInfiniteTransition(label = "title_shop_group_dot_pulse")
                val alphaAnim by infiniteTransition.animateFloat(
                    initialValue = 1.0f,
                    targetValue = 0.35f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(900),
                        repeatMode = RepeatMode.Reverse,
                    ),
                    label = "title_shop_dot_alpha",
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
                    .background(group.dotColor),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = group.label,
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 0.08.em,
                color = group.labelColor,
            )
        }
        if (group.sublabel != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = group.sublabel,
                fontFamily = Cinzel,
                fontSize = 10.sp,
                color = TitleShopCyan400,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        content()
        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun TitleLedger(
    items: List<TitleItem>,
    state: CharacterState,
    ownedTitles: List<String>,
    onBuyTitle: (String, Int) -> Unit,
    onClaimAchievementTitle: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        items.forEach { title ->
            TitleLedgerRow(
                title = title,
                isOwned = isTitleOwned(ownedTitles, title),
                gold = state.gold,
                state = state,
                onBuy = { onBuyTitle(title.id, title.price ?: 0) },
                onClaim = { onClaimAchievementTitle(title.id) },
            )
        }
    }
}

@Composable
private fun TitleLedgerRow(
    title: TitleItem,
    isOwned: Boolean,
    gold: Int,
    state: CharacterState,
    onBuy: () -> Unit,
    onClaim: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isAchievementUnlocked = isTitleAchievementUnlocked(title, state)
    val footer = resolveTitleShopFooter(title, isOwned, isAchievementUnlocked)
    val action = resolveTitleShopAction(title, isOwned, isAchievementUnlocked, gold)
    val spineColor = if (isOwned) Emerald400 else title.visualStyle.borderColor

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(title.visualStyle.backgroundColor.copy(alpha = 0.35f))
            .padding(start = 10.dp, end = 10.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .heightIn(min = 40.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(spineColor),
        )
        Spacer(modifier = Modifier.width(9.dp))
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(title.visualStyle.backgroundColor)
                .border(1.dp, title.visualStyle.borderColor, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = title.emoji, fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title.name.uppercase(),
                fontFamily = Cinzel,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 0.06.em,
                color = title.visualStyle.glowTextColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (!title.perks.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    title.perks.forEach { perk ->
                        Text(
                            text = perk,
                            fontFamily = JetBrainsMono,
                            fontSize = 10.sp,
                            color = TitleShopCyan400,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            TitleFooterLeft(footer)
        }
        Spacer(modifier = Modifier.width(10.dp))
        TitleFooterAction(
            action = action,
            onBuy = onBuy,
            onClaim = onClaim,
            modifier = Modifier.widthIn(min = 96.dp),
        )
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Amber500.copy(alpha = 0.05f)),
    )
}

@Composable
private fun TitleFooterLeft(footer: TitleShopFooterState) {
    when (footer) {
        is TitleShopFooterState.Price -> Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = titleShopGpFormatter.format(footer.price),
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = Amber500,
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = "GP",
                fontFamily = JetBrainsMono,
                fontSize = 10.sp,
                color = Amber500.copy(alpha = 0.70f),
            )
        }
        is TitleShopFooterState.AchievementRequirement -> Text(
            text = footer.text,
            fontFamily = JetBrainsMono,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = footer.color,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        is TitleShopFooterState.DropChance -> Text(
            text = footer.text,
            fontFamily = Cinzel,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = Amber500.copy(alpha = 0.60f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        TitleShopFooterState.Owned -> Text(
            text = "Adquirido",
            fontFamily = JetBrainsMono,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = Emerald400,
        )
    }
}

@Composable
private fun TitleFooterAction(
    action: TitleShopActionState,
    onBuy: () -> Unit,
    onClaim: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (action.kind) {
        TitleShopActionKind.Owned -> ActionPill(
            label = "Já adquirido",
            color = Emerald400,
            background = Emerald400.copy(alpha = 0.04f),
            border = Emerald400.copy(alpha = 0.20f),
            modifier = modifier,
        )
        TitleShopActionKind.Claimable -> {
            val infiniteTransition = rememberInfiniteTransition(label = "title_shop_claim_button_pulse")
            val buttonAlpha by infiniteTransition.animateFloat(
                initialValue = 1.0f,
                targetValue = 0.55f,
                animationSpec = infiniteRepeatable(
                    animation = tween(900),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "title_shop_claim_alpha",
            )
            Box(
                modifier = modifier
                    .heightIn(min = 48.dp)
                    .alpha(buttonAlpha)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Amber600)
                    .clickable(onClick = onClaim),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Resgatar",
                    fontFamily = Cinzel,
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    letterSpacing = 0.06.em,
                    color = Amber100,
                    textAlign = TextAlign.Center,
                )
            }
        }
        TitleShopActionKind.Locked -> ActionPill(
            label = "Bloqueado",
            color = Stone500,
            background = Stone900,
            border = Stone800,
            modifier = modifier,
        )
        TitleShopActionKind.DropOnly -> ActionPill(
            label = "Espólio raro",
            color = Stone500,
            background = Stone900,
            border = Stone800,
            modifier = modifier,
        )
        TitleShopActionKind.Buy -> {
            val canAfford = action.canAfford
            Box(
                modifier = modifier
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (canAfford) Color(0xFF40301A) else Stone900)
                    .border(
                        width = 1.dp,
                        color = if (canAfford) Amber500.copy(alpha = 0.20f) else Stone800,
                        shape = RoundedCornerShape(4.dp),
                    )
                    .then(if (canAfford) Modifier.clickable(onClick = onBuy) else Modifier),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Comprar",
                    fontFamily = Cinzel,
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    letterSpacing = 0.06.em,
                    color = if (canAfford) Amber300 else Stone500,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun ActionPill(
    label: String,
    color: Color,
    background: Color,
    border: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(background)
            .border(1.dp, border, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontFamily = Cinzel,
            fontWeight = FontWeight.Black,
            fontSize = 10.sp,
            letterSpacing = 0.06.em,
            color = color,
            textAlign = TextAlign.Center,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────────────────────

private fun titleShopPreviewState(
    gold: Int = 200_000,
    ownedTitles: List<String> = listOf(
        "APPRENTICE",
        "CHAMPION",
        "STORMBORN",
        "LEGEND_GP",
        "VOIDWALKER",
        "THE_WATCHER",
        "THE_ETERNAL_SCHOLAR",
        "IRON_WILL",
        "BLESSED",
    ),
    bestStreak: Int = 365,
    totalSessions: Int = 1_000,
    totalMinutes: Int = 30_000,
    totalXP: Int = 5_000_000,
    combo: Int = 50,
    dungeonProgress: Int = 25,
    wildernessWins: Int = 50,
): CharacterState = createInitialCharacterState().copy(
    gold = gold,
    ownedTitles = ownedTitles,
    bestStreak = bestStreak,
    totalSessions = totalSessions,
    totalMinutes = totalMinutes,
    totalXP = totalXP,
    combo = combo,
    dungeonProgress = dungeonProgress,
    wildernessWins = wildernessWins,
)

@Preview(name = "Loja Títulos — com dados", showBackground = true, backgroundColor = 0xFF0B0915, heightDp = 900)
@Composable
private fun TitleShopPreviewComDados() {
    HeroLogTheme(darkTheme = true) {
        Box(modifier = Modifier.background(QuestPanel).padding(horizontal = 16.dp)) {
            TitleShopScreen(
                state = titleShopPreviewState(),
                onBuyTitle = { _, _ -> },
                onClaimAchievementTitle = {},
            )
        }
    }
}

@Preview(name = "Loja Títulos — conta nova (0 GP)", showBackground = true, backgroundColor = 0xFF0B0915, heightDp = 900)
@Composable
private fun TitleShopPreviewContaNova() {
    HeroLogTheme(darkTheme = true) {
        Box(modifier = Modifier.background(QuestPanel).padding(horizontal = 16.dp)) {
            TitleShopScreen(
                state = createInitialCharacterState().copy(gold = 0),
                onBuyTitle = { _, _ -> },
                onClaimAchievementTitle = {},
            )
        }
    }
}

@Preview(name = "Loja Títulos — tudo adquirido (47)", showBackground = true, backgroundColor = 0xFF0B0915, heightDp = 900)
@Composable
private fun TitleShopPreviewTudoAdquirido() {
    HeroLogTheme(darkTheme = true) {
        Box(modifier = Modifier.background(QuestPanel).padding(horizontal = 16.dp)) {
            TitleShopScreen(
                state = titleShopPreviewState(
                    gold = 500_000,
                    ownedTitles = TITLE_CATALOG.map { it.id },
                ),
                onBuyTitle = { _, _ -> },
                onClaimAchievementTitle = {},
            )
        }
    }
}
