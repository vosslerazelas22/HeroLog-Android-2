package com.iurispraecepta.herolog.ui.components

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.iurispraecepta.herolog.model.InventoryItem
import com.iurispraecepta.herolog.model.Rarity
import com.iurispraecepta.herolog.ui.theme.Amber100
import com.iurispraecepta.herolog.ui.theme.Amber950
import com.iurispraecepta.herolog.ui.theme.Champagne300
import com.iurispraecepta.herolog.ui.theme.Champagne400
import com.iurispraecepta.herolog.ui.theme.Cinzel
import com.iurispraecepta.herolog.ui.theme.JetBrainsMono
import com.iurispraecepta.herolog.ui.theme.Zinc300
import kotlinx.coroutines.delay

enum class ItemInspectVariant { Primary, Danger, Success, Stone, Amber }

data class ItemInspectAction(
    val label: String,
    val onClick: () -> Unit,
    val variant: ItemInspectVariant = ItemInspectVariant.Stone
)

// Paridade com Motion 12.x (defaults do React, ItemInspectModal.tsx L59-70):
// opacidade em 0,3s com ease [0.25, 0.1, 0.35, 1]; escala em spring
// (stiffness 550, damping 30 -> dampingRatio = 30 / (2 * sqrt(550)) ~= 0.64).
// O dampingRatio é cálculo teórico — exige validação visual em device.
private const val ExitAnimDurationMs = 300L
private val OpacityEasing = CubicBezierEasing(0.25f, 0.1f, 0.35f, 1f)
private const val ScaleStiffness = 550f
private const val ScaleDampingRatio = 0.64f

@Composable
fun ItemInspectModal(
    item: InventoryItem?,
    onClose: () -> Unit,
    actions: List<ItemInspectAction> = emptyList(),
    showSlotSelector: Boolean = false,
    onSelectSlot: ((Int) -> Unit)? = null
) {
    var shouldRenderDialog by remember { mutableStateOf(item != null) }
    var animatedVisible by remember { mutableStateOf(item != null) }
    var currentItem by remember { mutableStateOf(item) }

    DisposableEffect(item != null) {
        if (item != null) {
            ModalCountRegistry.increment()
        }
        onDispose {
            if (item != null) {
                ModalCountRegistry.decrement()
            }
        }
    }

    LaunchedEffect(item) {
        if (item != null) {
            currentItem = item
            shouldRenderDialog = true
            animatedVisible = true
        } else {
            animatedVisible = false
            delay(ExitAnimDurationMs)
            shouldRenderDialog = false
        }
    }

    if (!shouldRenderDialog) return
    val displayItem = item ?: currentItem ?: return

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Backdrop (D5, decisão do mantenedor: só escurece, sem blur.
            // `backdrop-blur-sm` do React registrado como melhoria futura no
            // PARIDADE.md; o `Modifier.blur` anterior era aplicado no próprio
            // Box preto e não desfocava nada atrás). O backdrop anima junto
            // (fade, React L59-61/68-70).
            AnimatedVisibility(
                visible = animatedVisible,
                enter = fadeIn(tween(300, easing = OpacityEasing)),
                exit = fadeOut(tween(300, easing = OpacityEasing))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.8f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onClose()
                        }
                )
            }

            // Animated Modal Container
            AnimatedVisibility(
                visible = animatedVisible,
                enter = fadeIn(tween(300, easing = OpacityEasing)) +
                        scaleIn(
                            initialScale = 0.95f,
                            animationSpec = spring(
                                dampingRatio = ScaleDampingRatio,
                                stiffness = ScaleStiffness
                            )
                        ),
                exit = fadeOut(tween(300, easing = OpacityEasing)) +
                        scaleOut(
                            targetScale = 0.95f,
                            animationSpec = spring(
                                dampingRatio = ScaleDampingRatio,
                                stiffness = ScaleStiffness
                            )
                        )
            ) {
                val screenHeight = LocalConfiguration.current.screenHeightDp.dp
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .widthIn(max = 384.dp)
                        .fillMaxWidth()
                        .heightIn(max = screenHeight * 0.85f)
                        .shadow(24.dp, RoundedCornerShape(8.dp)) // shadow-2xl (D9)
                        .background(Color(0xFF0C0A09), RoundedCornerShape(8.dp)) // stone-950
                        .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Header (React L73: p-4 gradient stone-900 -> stone-950,
                        // border-b amber-500/10)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFF1C1917), Color(0xFF0C0A09))
                                    )
                                )
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = displayItem.emoji,
                                fontSize = 30.sp,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Column {
                                Text(
                                    text = displayItem.name.uppercase(),
                                    style = TextStyle(
                                        fontFamily = Cinzel,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = Champagne400, // text-champagne-400 (D2)
                                        letterSpacing = 0.1.em
                                    )
                                )
                                val subtitleText = if (displayItem.isEquipment == true) {
                                    when (displayItem.rarity) {
                                        Rarity.Especial -> "🛡️ Equipamento Especial"
                                        Rarity.Comum -> "🛡️ Equipamento Comum"
                                        null -> "🛡️ Equipamento"
                                    }
                                } else {
                                    "🎒 Relíquia Colecionável"
                                }
                                Text(
                                    text = subtitleText.uppercase(),
                                    style = TextStyle(
                                        fontFamily = JetBrainsMono,
                                        fontSize = 9.sp,
                                        color = Color(0xFFFEF3C7).copy(alpha = 0.4f)
                                    )
                                )
                            }
                        }
                        // Divisória do header consome 1dp de layout (D13;
                        // border-b do React; drawBehind não consumia layout)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0xFFF59E0B).copy(alpha = 0.1f))
                        )

                        // Content (D7, melhoria consciente: scroll interno com
                        // weight para o footer "Voltar" nunca ser empurrado
                        // para fora com conteúdo alto)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false)
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = displayItem.desc,
                                style = TextStyle(
                                    fontFamily = Cinzel,
                                    fontSize = 12.sp,
                                    color = Color(0xFFFEF3C7).copy(alpha = 0.8f),
                                    lineHeight = 19.5.sp // leading-relaxed (D11)
                                )
                            )

                            if (displayItem.isEquipment == true) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            color = Color(0xFFF59E0B).copy(alpha = 0.04f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "📦 INFORMAÇÕES DE DURABILIDADE",
                                        style = TextStyle(
                                            fontFamily = Cinzel,
                                            fontSize = 10.sp,
                                            color = Champagne400, // text-champagne-400 (D2)
                                            letterSpacing = 0.05.em,
                                            textAlign = TextAlign.Center
                                        )
                                    )
                                    val charges = displayItem.charges ?: 0
                                    val maxCharges = displayItem.maxCharges ?: 8
                                    Text(
                                        text = "🔋 $charges / $maxCharges Cargas",
                                        style = TextStyle(
                                            fontFamily = JetBrainsMono,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF34D399), // emerald-400
                                            textAlign = TextAlign.Center
                                        )
                                    )
                                    Text(
                                        text = "Perde 1 de durabilidade toda vez que for ativado ao completar uma sessão de foco.",
                                        style = TextStyle(
                                            fontFamily = Cinzel,
                                            fontSize = 9.sp,
                                            color = Color(0xFFFEF3C7).copy(alpha = 0.5f),
                                            textAlign = TextAlign.Center
                                        )
                                    )
                                }
                            }

                            if ((showSlotSelector && displayItem.isEquipment == true && onSelectSlot != null) || actions.isNotEmpty()) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (showSlotSelector && displayItem.isEquipment == true && onSelectSlot != null) {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "SELECIONE O ESPAÇO PARA EQUIPAR:",
                                                style = TextStyle(
                                                    fontFamily = JetBrainsMono,
                                                    fontSize = 10.sp,
                                                    color = Color(0xFFFEF3C7).copy(alpha = 0.3f)
                                                )
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                for (slotIdx in 0..2) {
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(Color(0xFF1C1917))
                                                            .border(
                                                                1.dp,
                                                                Color(0xFFF59E0B).copy(alpha = 0.2f),
                                                                RoundedCornerShape(4.dp)
                                                            )
                                                            .clickable { onSelectSlot(slotIdx) }
                                                            .padding(vertical = 6.dp, horizontal = 8.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = "SLOT ${slotIdx + 1}",
                                                            style = TextStyle(
                                                                fontFamily = Cinzel,
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 10.sp,
                                                                color = Amber100, // herda text-amber-100 (D3)
                                                                letterSpacing = 0.05.em
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    if (actions.isNotEmpty()) {
                                        // React L133: `flex gap-2 pt-2 border-t`
                                        // (align-items: stretch — D12; borda consome
                                        // 1px de layout — D13)
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(1.dp)
                                                    .background(Color(0xFFF59E0B).copy(alpha = 0.05f))
                                            )
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(IntrinsicSize.Max),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                            val isSingle = actions.size == 1
                                            actions.forEach { action ->
                                                val isGrowing = when (action.variant) {
                                                    ItemInspectVariant.Success, ItemInspectVariant.Primary, ItemInspectVariant.Amber -> true
                                                    ItemInspectVariant.Danger -> isSingle
                                                    ItemInspectVariant.Stone -> false
                                                }

                                                var buttonModifier: Modifier = Modifier.fillMaxHeight()
                                                if (isGrowing) {
                                                    buttonModifier = buttonModifier.weight(1f)
                                                }

                                                val style = getActionStyle(action.variant)

                                                Box(
                                                    modifier = buttonModifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(style.bgColor)
                                                        .border(1.dp, style.borderColor, RoundedCornerShape(4.dp))
                                                        .clickable { action.onClick() }
                                                        .padding(vertical = 8.dp, horizontal = 12.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = action.label.uppercase(),
                                                        style = TextStyle(
                                                            fontFamily = Cinzel,
                                                            fontWeight = if (style.isBold) FontWeight.Bold else FontWeight.Normal,
                                                            fontSize = 12.sp,
                                                            color = style.textColor,
                                                            letterSpacing = 0.05.em,
                                                            textAlign = TextAlign.Center
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        } // fim do wrapper ações/slots (L319)

                        // Footer (React L148: p-3 bg-stone-900/40 border-t;
                        // borda consome 1px de layout — D13)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1C1917).copy(alpha = 0.4f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color(0xFFF59E0B).copy(alpha = 0.1f))
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF1C1917))
                                    .border(
                                        1.dp,
                                        Color(0xFFF59E0B).copy(alpha = 0.2f),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .clickable { onClose() }
                                    .padding(vertical = 6.dp, horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "VOLTAR",
                                    style = TextStyle(
                                        fontFamily = Cinzel,
                                        fontSize = 12.sp,
                                        color = Color(0xFFFEF3C7).copy(alpha = 0.7f),
                                        letterSpacing = 0.05.em
                                    )
                                )
                            }
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class ActionStyle(
    val bgColor: Color,
    val borderColor: Color,
    val textColor: Color,
    val isBold: Boolean
)

private fun getActionStyle(variant: ItemInspectVariant): ActionStyle {
    return when (variant) {
        ItemInspectVariant.Success -> ActionStyle(
            bgColor = Color(0xFF022C22).copy(alpha = 0.4f), // emerald-950/40
            borderColor = Color(0xFF10B981).copy(alpha = 0.3f), // emerald-500/30
            textColor = Color(0xFF6EE7B7), // emerald-300
            isBold = true
        )
        ItemInspectVariant.Danger -> ActionStyle(
            bgColor = Color(0xFF450A0A).copy(alpha = 0.4f), // red-950/40
            borderColor = Color(0xFFEF4444).copy(alpha = 0.3f), // red-500/30
            textColor = Color(0xFFFCA5A5), // red-300
            isBold = false
        )
        ItemInspectVariant.Primary, ItemInspectVariant.Amber -> ActionStyle(
            // D10 (decisão do mantenedor: corrigir, não replicar o bug do
            // React onde `bg-champagne-950/40` não é gerado — index.css não
            // define champagne-950/900 — usando o Amber950 real)
            bgColor = Amber950.copy(alpha = 0.4f),
            borderColor = Color(0xFFF59E0B).copy(alpha = 0.3f), // amber-500/30
            textColor = Champagne300, // text-champagne-300
            isBold = false
        )
        ItemInspectVariant.Stone -> ActionStyle(
            // D10: fiel a getButtonClass 'stone' (React L49): bg-stone-900,
            // borda white/10, texto zinc-300/70
            bgColor = Color(0xFF1C1917), // stone-900
            borderColor = Color.White.copy(alpha = 0.1f), // white/10
            textColor = Zinc300.copy(alpha = 0.7f), // zinc-300/70
            isBold = false
        )
    }
}
