package com.iurispraecepta.herolog.ui.kingdom

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.composables.icons.lucide.R as LucideR
import com.iurispraecepta.herolog.data.TitleCategory
import com.iurispraecepta.herolog.data.TitleItem
import com.iurispraecepta.herolog.model.CharacterState
import com.iurispraecepta.herolog.ui.theme.Amber100
import com.iurispraecepta.herolog.ui.theme.Amber500
import com.iurispraecepta.herolog.ui.theme.Amber600
import com.iurispraecepta.herolog.ui.theme.Champagne400
import com.iurispraecepta.herolog.ui.theme.Emerald300
import com.iurispraecepta.herolog.ui.theme.Emerald400
import com.iurispraecepta.herolog.ui.theme.Purple300
import com.iurispraecepta.herolog.ui.theme.Purple400
import com.iurispraecepta.herolog.ui.theme.Purple500
import com.iurispraecepta.herolog.ui.theme.Stone500
import com.iurispraecepta.herolog.ui.theme.Yellow400
import java.text.NumberFormat
import java.util.Locale

// Cor local (não existe token Cyan no tema global) — mesmo hex do TitleShopScreen original.
internal val TitleShopCyan400 = Color(0xFF38BDF8)

internal val titleShopGpFormatter: NumberFormat =
    NumberFormat.getNumberInstance(Locale("pt", "BR"))

internal data class TitleShopSectionHeaderData(
    @param:DrawableRes val iconRes: Int,
    val iconTint: Color,
    val title: String,
    val titleColor: Color,
)

internal val titleShopAcquirableSection = TitleShopSectionHeaderData(
    iconRes = LucideR.drawable.lucide_ic_award,
    iconTint = Amber500,
    title = "Títulos à Venda",
    titleColor = Champagne400,
)

internal val titleShopAchievementsSection = TitleShopSectionHeaderData(
    iconRes = LucideR.drawable.lucide_ic_award,
    iconTint = Purple400,
    title = "Títulos de Conquista",
    titleColor = Purple300,
)

internal val titleShopDropsSection = TitleShopSectionHeaderData(
    iconRes = LucideR.drawable.lucide_ic_award,
    iconTint = Emerald400,
    title = "Títulos de Espólio",
    titleColor = Emerald300,
)

internal data class TitleShopCategoryGroupData(
    val category: TitleCategory,
    val label: String,
    val dotColor: Color,
    val labelColor: Color,
    val sublabel: String? = null,
    val pulseDot: Boolean = false,
)

internal val titleShopAcquirableGroups = listOf(
    TitleShopCategoryGroupData(
        category = TitleCategory.Common,
        label = "COMUM · 150–500 GP",
        dotColor = Stone500,
        labelColor = Amber100.copy(alpha = 0.30f),
    ),
    TitleShopCategoryGroupData(
        category = TitleCategory.Rare,
        label = "RARO · 1.500–6.000 GP",
        dotColor = Amber500,
        labelColor = Amber600,
    ),
    TitleShopCategoryGroupData(
        category = TitleCategory.Epic,
        label = "ÉPICO · 10.000–50.000 GP",
        sublabel = "(concede bônus passivos ao equipar)",
        dotColor = Purple500,
        labelColor = Purple400,
        pulseDot = true,
    ),
    TitleShopCategoryGroupData(
        category = TitleCategory.Legendary,
        label = "LENDÁRIO · 100.000–500.000 GP",
        sublabel = "(concede bônus superiores ao equipar)",
        dotColor = Yellow400,
        labelColor = Yellow400,
        pulseDot = true,
    ),
)

internal fun isTitleOwned(ownedTitles: List<String>, title: TitleItem): Boolean =
    ownedTitles.contains(title.id)

internal fun isTitleAchievementUnlocked(title: TitleItem, state: CharacterState): Boolean =
    title.category == TitleCategory.Achievement && title.checkUnlocked?.invoke(state) == true

internal enum class TitleShopActionKind { Owned, Claimable, Locked, DropOnly, Buy }

internal data class TitleShopActionState(
    val kind: TitleShopActionKind,
    val canAfford: Boolean = false,
)

internal fun resolveTitleShopAction(
    title: TitleItem,
    isOwned: Boolean,
    isAchievementUnlocked: Boolean,
    gold: Int,
): TitleShopActionState = when {
    isOwned -> TitleShopActionState(TitleShopActionKind.Owned)
    title.category == TitleCategory.Achievement ->
        if (isAchievementUnlocked) {
            TitleShopActionState(TitleShopActionKind.Claimable)
        } else {
            TitleShopActionState(TitleShopActionKind.Locked)
        }
    title.category == TitleCategory.Drop -> TitleShopActionState(TitleShopActionKind.DropOnly)
    else -> TitleShopActionState(
        kind = TitleShopActionKind.Buy,
        canAfford = gold >= (title.price ?: 0),
    )
}

internal sealed interface TitleShopFooterState {
    data class Price(val price: Int) : TitleShopFooterState
    data class AchievementRequirement(val text: String, val color: Color) : TitleShopFooterState
    data class DropChance(val text: String) : TitleShopFooterState
    data object Owned : TitleShopFooterState
}

internal fun resolveTitleShopFooter(
    title: TitleItem,
    isOwned: Boolean,
    isAchievementUnlocked: Boolean,
): TitleShopFooterState = when (title.category) {
    TitleCategory.Achievement -> TitleShopFooterState.AchievementRequirement(
        text = if (isOwned) "🔓 Adquirido" else "🔒 ${title.requirementText}",
        color = if (isOwned) Emerald400 else if (isAchievementUnlocked) Amber500 else Stone500,
    )
    TitleCategory.Drop -> TitleShopFooterState.DropChance("♦ ${title.dropChanceText}")
    else -> if (!isOwned && title.price != null) {
        TitleShopFooterState.Price(title.price)
    } else {
        TitleShopFooterState.Owned
    }
}
