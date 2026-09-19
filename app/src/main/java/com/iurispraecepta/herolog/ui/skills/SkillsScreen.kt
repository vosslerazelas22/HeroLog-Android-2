package com.iurispraecepta.herolog.ui.skills

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.R
import com.iurispraecepta.herolog.logic.SkillLogic
import com.iurispraecepta.herolog.model.Skill
import com.iurispraecepta.herolog.ui.components.FlowRowStable
import com.iurispraecepta.herolog.ui.components.HeroLogModal
import com.iurispraecepta.herolog.ui.components.ModalCountRegistry
import com.iurispraecepta.herolog.ui.components.ModalVariant
import com.iurispraecepta.herolog.ui.navigation.LocalBottomBarInset
import com.iurispraecepta.herolog.ui.theme.Cinzel
import com.iurispraecepta.herolog.ui.theme.Inter
import com.iurispraecepta.herolog.ui.theme.JetBrainsMono
import com.iurispraecepta.herolog.ui.theme.Stone950
import com.iurispraecepta.herolog.ui.theme.Stone900
import com.iurispraecepta.herolog.ui.theme.Stone800
import com.iurispraecepta.herolog.ui.theme.Stone400
import com.iurispraecepta.herolog.ui.theme.Champagne600
import com.iurispraecepta.herolog.ui.theme.Champagne500
import com.iurispraecepta.herolog.ui.theme.Champagne400
import com.iurispraecepta.herolog.ui.theme.Champagne300
import com.iurispraecepta.herolog.ui.theme.Champagne200
import com.iurispraecepta.herolog.ui.theme.Zinc100
import com.iurispraecepta.herolog.ui.theme.Zinc200
import com.iurispraecepta.herolog.ui.theme.Zinc300
import com.iurispraecepta.herolog.ui.theme.Zinc400
import com.iurispraecepta.herolog.ui.theme.Zinc500
import com.iurispraecepta.herolog.ui.theme.Zinc600
import com.iurispraecepta.herolog.ui.theme.Zinc700
import com.iurispraecepta.herolog.ui.theme.Zinc50
import com.iurispraecepta.herolog.ui.theme.Zinc800
import com.iurispraecepta.herolog.ui.theme.Yellow500
import com.iurispraecepta.herolog.ui.theme.Yellow400
import com.iurispraecepta.herolog.ui.theme.Red500
import com.iurispraecepta.herolog.ui.theme.Red400
import com.iurispraecepta.herolog.ui.theme.Emerald400
import com.iurispraecepta.herolog.ui.theme.Amber500
import com.iurispraecepta.herolog.ui.theme.Amber400
import com.iurispraecepta.herolog.ui.theme.Amber200
import com.iurispraecepta.herolog.ui.theme.Amber100

private data class SkillSuggestion(
    val name: String,
    val emoji: String,
)

private val SKILL_SUGGESTIONS =
    listOf(
        SkillSuggestion("Estudos", "📚"),
        SkillSuggestion("Foco Profundo", "🧠"),
        SkillSuggestion("Pesquisa", "🔬"),
        SkillSuggestion("Escrita", "✍️"),
        SkillSuggestion("Idiomas", "🗣️"),
        SkillSuggestion("Leitura", "📖"),
        SkillSuggestion("Programação", "💻"),
        SkillSuggestion("Exercícios", "🏋️"),
        SkillSuggestion("Meditação", "🧘"),
        SkillSuggestion("Artes & Pintura", "🎨"),
        SkillSuggestion("Culinária", "🍳"),
        SkillSuggestion("Finanças", "💰"),
        SkillSuggestion("Música", "🎵"),
        SkillSuggestion("Organização", "📅"),
        SkillSuggestion("Jogos & Estratégia", "🎮"),
        SkillSuggestion("Trabalho", "💼"),
    )

private val SKILL_EMOJIS =
    listOf(
        "📚",
        "💻",
        "🧠",
        "✍️",
        "🗣️",
        "🏋️",
        "🎨",
        "🍳",
        "🔬",
        "🧘",
        "🎵",
        "💰",
        "💼",
        "🧪",
        "🛡️",
        "🎯",
    )

@Composable
private fun Modifier.pressedOverlay(
    onClick: () -> Unit,
    pressedColor: Color = Champagne500.copy(alpha = 0.1f),
    enabled: Boolean = true
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    return this
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick
        )
        .drawBehind {
            if (isPressed && enabled) {
                drawRect(color = pressedColor, size = size)
            }
        }
}

@Composable
private fun SkillSuggestionItem(
    sug: SkillSuggestion,
    alreadyHas: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier =
            modifier
                .clip(RoundedCornerShape(4.dp))
                .background(if (alreadyHas) Color(0x401C1917) else Color(0x800C0A09))
                .border(
                    width = 1.dp,
                    color = if (alreadyHas) Color(0x66292524) else Champagne500.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(4.dp),
                )
                .pressedOverlay(
                    onClick = onClick,
                    pressedColor = Champagne500.copy(alpha = 0.1f),
                    enabled = !alreadyHas
                )
                .padding(horizontal = 8.dp, vertical = 6.dp),
    ) {
        Text(
            text = sug.emoji,
            fontSize = 14.sp,
        )
        Text(
            text = sug.name,
            fontSize = 10.sp,
            fontFamily = Cinzel,
            color = if (alreadyHas) Amber100.copy(alpha = 0.25f) else Champagne200,
        )
    }
}

@Composable
fun SkillsScreen(
    skills: List<Skill>,
    onAddTagToSkill: (skillIdx: Int, newTag: String) -> Unit,
    onRemoveTagFromSkill: (skillIdx: Int, tagIdx: Int) -> Unit,
    onAddCustomSkill: (name: String, emoji: String) -> Unit,
    onDeleteSkill: (idx: Int) -> Unit,
    onPrestigeSkill: (idx: Int) -> Unit,
    onRenameSkill: (idx: Int, newName: String) -> Unit,
    isCreateModalOpen: Boolean,
    onCreateModalOpenChange: (Boolean) -> Unit,
    initialEditingIdx: Int? = null,
    initialDeleteConfirmIdx: Int? = null,
    modifier: Modifier = Modifier,
) {
    var editingIdx by remember(initialEditingIdx) { mutableStateOf<Int?>(initialEditingIdx) }
    var editNameValue by remember(initialEditingIdx) {
        mutableStateOf(initialEditingIdx?.let { skills.getOrNull(it)?.name } ?: "")
    }

    var newSkillNameInput by remember { mutableStateOf("") }
    var selectedNewSkillEmoji by remember { mutableStateOf("📚") }
    var deleteConfirmIdx by remember(initialDeleteConfirmIdx) { mutableStateOf<Int?>(initialDeleteConfirmIdx) }

    val handleSaveRename: (Int) -> Unit = { idx ->
        val trimmed = editNameValue.trim()
        if (trimmed.isNotEmpty()) {
            onRenameSkill(idx, trimmed)
        }
        editingIdx = null
    }

    val handleAddCustom: () -> Unit = {
        val trimmed = newSkillNameInput.trim()
        if (trimmed.isNotEmpty()) {
            onAddCustomSkill(trimmed, selectedNewSkillEmoji)
            newSkillNameInput = ""
            onCreateModalOpenChange(false)
        }
    }

    val handleAddSuggestion: (String, String) -> Unit = { name, emoji ->
        onAddCustomSkill(name, emoji)
        onCreateModalOpenChange(false)
    }

    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = LocalBottomBarInset.current),
        ) {
            items(skills) { sk ->
                val idx = skills.indexOf(sk)
                val reqXP = SkillLogic.requiredXpForLevel(sk.level)
                val targetPercent = (sk.xp.toFloat() / reqXP.toFloat() * 100f).coerceIn(0f, 100f)
                val percent by animateFloatAsState(
                    targetValue = targetPercent,
                    animationSpec = tween(300, easing = androidx.compose.animation.core.LinearEasing)
                )
                val isEditing = editingIdx == idx

                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0x660C0A09)) // bg-stone-950/40
                            .border(1.dp, Zinc800, RoundedCornerShape(4.dp)) // border-zinc-800
                            .padding(12.dp),
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        // Header Row
                        if (isEditing) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text = sk.emoji ?: "🎯",
                                    fontSize = 20.sp,
                                )

                                BasicTextField(
                                    value = editNameValue,
                                    onValueChange = { text ->
                                        if (text.length <= 30) {
                                            editNameValue = text
                                        }
                                    },
                                    modifier =
                                        Modifier
                                            .weight(1f)
                                            .background(Stone950, RoundedCornerShape(4.dp))
                                            .border(1.dp, Zinc700, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                    textStyle =
                                        TextStyle(
                                            color = Zinc100,
                                            fontSize = 12.sp,
                                            fontFamily = Inter,
                                        ),
                                    singleLine = true,
                                    cursorBrush = SolidColor(Champagne400),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    keyboardActions =
                                        KeyboardActions(onDone = {
                                            handleSaveRename(idx)
                                            keyboardController?.hide()
                                        }),
                                )

                                Box(
                                    modifier =
                                        Modifier
                                            .size(28.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0x3310B981))
                                            .pressedOverlay(onClick = { handleSaveRename(idx) }),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.lucide_ic_check),
                                        contentDescription = "Salvar Nome",
                                        tint = Emerald400,
                                        modifier = Modifier.size(14.dp),
                                    )
                                }

                                Box(
                                    modifier =
                                        Modifier
                                            .size(28.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0x33EF4444))
                                            .pressedOverlay(onClick = { editingIdx = null }),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.lucide_ic_x),
                                        contentDescription = "Cancelar",
                                        tint = Red400,
                                        modifier = Modifier.size(14.dp),
                                    )
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top,
                            ) {
                                Row(
                                    modifier =
                                        Modifier
                                            .weight(1f)
                                            .padding(end = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.Top,
                                ) {
                                    Text(
                                        text = sk.emoji ?: "🎯",
                                        fontSize = 20.sp,
                                        modifier = Modifier.padding(top = 2.dp),
                                    )

                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        ) {
Text(
                                            text = sk.name,
                                            fontFamily = Cinzel,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Zinc100,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )

                                            if (sk.prestige != null && sk.prestige > 0) {
                                                Text(
                                                    text = "👑" + "★".repeat(sk.prestige),
                                                    color = Yellow400,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                )
                                            }

Box(
                                                    modifier =
                                                        Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .pressedOverlay(
                                                                onClick = {
                                                                    editingIdx = idx
                                                                    editNameValue = sk.name
                                                                }).padding(2.dp),
                                                ) {
                                                    Icon(
                                                        painter = painterResource(R.drawable.lucide_ic_pen),
                                                        contentDescription = "Renomear Habilidade",
                                                        tint = Color(0x4DFEF3C7),
                                                        modifier = Modifier.size(12.dp),
                                                    )
                                                }
                                        }

                                        Box(
                                            modifier =
                                                Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Champagne500.copy(alpha = 0.1f))
                                                    .border(1.dp, Champagne500.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                        ) {
                                            Text(
                                                text = "Nível ${sk.level}",
                                                color = Champagne400,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = JetBrainsMono,
                                                fontSize = 9.sp,
                                            )
                                        }
                                    }
                                }

                                Box(
                                    modifier =
                                        Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Red500.copy(alpha = 0.04f))
                                            .border(1.dp, Red500.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                            .testTag("forgetSkill_$idx")
                                            .pressedOverlay(onClick = { deleteConfirmIdx = idx }, pressedColor = Red500.copy(alpha = 0.1f))
                                            .padding(horizontal = 6.dp, vertical = 4.dp),
                                ) {
                                    Text(
                                        text = "ESQUECER",
                                        color = Red400.copy(alpha = 0.7f),
                                        fontSize = 9.sp,
                                        fontFamily = Cinzel,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp,
                                    )
                                }
                            }
                        }

                        // Progress Bar Section
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Stone950),
                            ) {
                                Box(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth(percent / 100f)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Champagne500),
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "Progresso: ${sk.xp} / $reqXP XP",
                                    fontFamily = JetBrainsMono,
                                    fontSize = 9.sp,
                                    color = Zinc500,
                                )
                                if (sk.prestige != null && sk.prestige > 0) {
                                    Text(
                                        text = "Bônus: +${sk.prestige * 25}% XP",
                                        fontFamily = Inter,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 9.sp,
                                        color = Yellow500,
                                    )
                                }
                            }
                        }

// Subskills Section
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier =
                                Modifier
                                    .drawBehind {
                                        drawLine(
                                            color = Zinc800,
                                            start = Offset(0f, 0f),
                                            end = Offset(size.width, 0f),
                                            strokeWidth = 1.dp.toPx(),
                                        )
                                    }.padding(top = 8.dp),
                        ) {
                            Text(
                                text = "SUBSKILLS:",
                                fontFamily = Cinzel,
                                fontSize = 9.sp,
                                color = Zinc500,
                                letterSpacing = 0.5.sp,
                            )

                            if (sk.tags.isNullOrEmpty()) {
                                Text(
                                    text = "Nenhuma subskill cadastrada para esta habilidade.",
                                    fontSize = 9.sp,
                                    fontStyle = FontStyle.Italic,
                                    color = Zinc500,
                                )
                            } else {
                                FlowRowStable(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    sk.tags.forEachIndexed { tIdx, tg ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier =
                                                Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Zinc50.copy(alpha = 0.1f))
                                                    .border(1.dp, Zinc700, RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                        ) {
                                            Text(
                                                text = tg,
                                                fontSize = 9.sp,
                                                color = Zinc300,
                                                fontFamily = Inter,
                                            )
Text(
                                                    text = "×",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Zinc500,
                                                    modifier = Modifier
                                                        .padding(start = 4.dp, end = 4.dp)
                                                        .pressedOverlay(onClick = { onRemoveTagFromSkill(idx, tIdx) }, pressedColor = Red500.copy(alpha = 0.1f)),
                                                )
                                        }
                                    }
                                }
                            }

// Subskill Input Row
                            var subskillInput by remember(idx) { mutableStateOf("") }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                BasicTextField(
                                    value = subskillInput,
                                    onValueChange = { subskillInput = it },
                                    modifier =
                                        Modifier
                                            .weight(1f)
                                            .background(Stone950.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                            .border(1.dp, Zinc800, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                    textStyle =
                                        TextStyle(
                                            color = Zinc100,
                                            fontSize = 10.sp,
                                            fontFamily = Inter,
                                        ),
                                    singleLine = true,
                                    cursorBrush = SolidColor(Champagne400),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    keyboardActions =
                                        KeyboardActions(onDone = {
                                            val trimmed = subskillInput.trim()
                                            if (trimmed.isNotEmpty()) {
                                                onAddTagToSkill(idx, trimmed)
                                                subskillInput = ""
                                            }
                                        }),
                                    decorationBox = { innerTextField ->
                                        Box {
                                            if (subskillInput.isEmpty()) {
                                                Text(
                                                    text = "Criar subskill (ex: Direito Processual, React, CSS...)",
                                                    color = Zinc600,
                                                    fontSize = 10.sp,
                                                    fontFamily = Inter,
                                                )
                                            }
                                            innerTextField()
                                        }
                                    },
                                )

                                Box(
                                    modifier =
                                        Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Champagne500.copy(alpha = 0.15f))
                                            .border(1.dp, Champagne500.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
.pressedOverlay(
                                                onClick = {
                                                    val trimmed = subskillInput.trim()
                                                    if (trimmed.isNotEmpty()) {
                                                        onAddTagToSkill(idx, trimmed)
                                                        subskillInput = ""
                                                    }
                                                },
                                                pressedColor = Champagne500.copy(alpha = 0.1f)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "+",
                                        color = Champagne300,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                    )
                                }
                            }
                        }

                        // Prestige Button
                        if (sk.level >= 99) {
                            val infiniteTransition = rememberInfiniteTransition(label = "prestigePulse")
                            val alphaPulse by infiniteTransition.animateFloat(
                                initialValue = 1.0f,
                                targetValue = 0.5f,
                                animationSpec =
                                    infiniteRepeatable(
                                        animation = tween(2000, easing = androidx.compose.animation.core.LinearEasing),
                                        repeatMode = RepeatMode.Reverse,
                                    ),
                                label = "prestigeAlpha",
                            )
                            // DECISÃO CONSCIENTE (D-013): Pausar pulso do Prestígio quando qualquer modal estiver aberto.
                            // O React usa `animate-pulse` contínuo sem pausa. Esta pausa evita distração visual
                            // quando modais estão abertos (ex: modal de criar skill). Registrado em PARIDADE.md.
                            val effectiveAlpha = if (ModalCountRegistry.isAnyModalOpen) 1f else alphaPulse

                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Amber500, Yellow400, Amber400),
                                            ),
                                        ).alpha(effectiveAlpha)
                                        .pressedOverlay(onClick = { onPrestigeSkill(idx) }, pressedColor = Color.White.copy(alpha = 0.1f))
                                        .padding(vertical = 6.dp, horizontal = 8.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "👑 Alcançar Prestígio (Resetar a Nível 1 & Ganhar +25% XP Definitivo)",
                                    color = Stone950,
                                    fontSize = 10.sp,
                                    fontFamily = Cinzel,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp,
                                )
                            }
                        }
                    }
                }
            }
        }

        // Modal Criar Nova Habilidade
        HeroLogModal(
            isOpen = isCreateModalOpen,
            onClose = { onCreateModalOpenChange(false) },
            title = "Criar Nova Habilidade",
            variant = ModalVariant.Amber,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.padding(end = 4.dp),
            ) {
                // Emoji Picker Section
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "SELECIONE UM ÍCONE/EMOJI PARA A HABILIDADE:",
                        fontFamily = Cinzel,
                        fontSize = 10.sp,
                        color = Amber100.copy(alpha = 0.4f),
                        letterSpacing = 1.sp,
                    )

                    FlowRowStable(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(Stone950.copy(alpha = 0.3f))
                                .border(1.dp, Champagne500.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                .padding(8.dp),
                    ) {
                        SKILL_EMOJIS.forEach { em ->
                            val isSelected = selectedNewSkillEmoji == em
                            Box(
                                modifier =
                                    Modifier
                                        .size(32.dp)
                                        .then(
                                            if (isSelected) {
                                                Modifier.shadow(
                                                    elevation = 4.dp,
                                                    shape = RoundedCornerShape(4.dp),
                                                    ambientColor = Amber500.copy(alpha = 0.2f),
                                                    spotColor = Amber500.copy(alpha = 0.2f),
                                                )
                                            } else {
                                                Modifier
                                            },
                                        ).clip(RoundedCornerShape(4.dp))
                                        .background(if (isSelected) Amber500.copy(alpha = 0.2f) else Color(0x731C1917))
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) Amber400 else Color.Transparent,
                                            shape = RoundedCornerShape(4.dp),
                                        ).pressedOverlay(onClick = { selectedNewSkillEmoji = em }, pressedColor = Amber500.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = em,
                                    fontSize = 14.sp,
                                )
                            }
                        }
                    }
                }

                // Custom Skill Name Section
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "NOME DA HABILIDADE DE FOCO:",
                        fontFamily = Cinzel,
                        fontSize = 10.sp,
                        color = Amber100.copy(alpha = 0.4f),
                        letterSpacing = 1.sp,
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        BasicTextField(
                            value = newSkillNameInput,
                            onValueChange = { if (it.length <= 30) newSkillNameInput = it },
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .background(Color(0xCC0C0A09), RoundedCornerShape(4.dp))
                                    .border(1.dp, Champagne500.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                            textStyle =
                                TextStyle(
                                    color = Amber100,
                                    fontSize = 12.sp,
                                    fontFamily = Inter,
                                ),
                            singleLine = true,
                            cursorBrush = SolidColor(Champagne400),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions =
                                KeyboardActions(onDone = {
                                    handleAddCustom()
                                }),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (newSkillNameInput.isEmpty()) {
                                        Text(
                                            text = "Ex: Alquimia de Dados, Exercícios Físicos...",
                                            color = Amber100.copy(alpha = 0.15f),
                                            fontSize = 12.sp,
                                            fontFamily = Inter,
                                        )
                                    }
                                    innerTextField()
                                }
                            },
                        )

                        Box(
                            modifier =
                                Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Champagne500.copy(alpha = 0.15f))
                                    .border(1.dp, Champagne400, RoundedCornerShape(4.dp))
                                    .pressedOverlay(onClick = { handleAddCustom() }, pressedColor = Champagne500.copy(alpha = 0.1f))
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "GRAVAR",
                                color = Champagne300,
                                fontFamily = Cinzel,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                            )
                        }
                    }
                }

                // Quick Suggestions Section
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier =
                        Modifier
                            .drawBehind {
                                drawLine(
                                    color = Champagne500.copy(alpha = 0.1f),
                                    start = Offset(0f, 0f),
                                    end = Offset(size.width, 0f),
                                    strokeWidth = 1.dp.toPx(),
                                )
                            }.padding(top = 16.dp),
                ) {
                    Text(
                        text = "SUGESTÕES RÁPIDAS:",
                        fontFamily = Cinzel,
                        fontSize = 10.sp,
                        color = Amber100.copy(alpha = 0.4f),
                        letterSpacing = 1.sp,
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 160.dp)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        // Render suggestions in 2-column grid (React: grid grid-cols-2)
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            val pairs = SKILL_SUGGESTIONS.chunked(2)
                            pairs.forEach { pair ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.Top,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    pair.forEach { sug ->
                                        val alreadyHas = skills.any { it.name.equals(sug.name, ignoreCase = true) }
                                        SkillSuggestionItem(
                                            sug = sug,
                                            alreadyHas = alreadyHas,
                                            onClick = { handleAddSuggestion(sug.name, sug.emoji) },
                                            modifier = Modifier.weight(1f),
                                        )
                                    }
                                    // If odd number, add spacer for last item
                                    if (pair.size == 1) {
                                        Box(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
            }
        }
        }

        // Modal Confirmação Esquecer Habilidade (irmão do modal de criação,
        // não aninhado: aninhado, ele só compunha com o modal de criação aberto)
        if (deleteConfirmIdx != null) {
            val skillToDelete = skills.getOrNull(deleteConfirmIdx!!)
            HeroLogModal(
                isOpen = true,
                onClose = { deleteConfirmIdx = null },
                title = "Esquecer Habilidade?",
                variant = ModalVariant.Red,
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(horizontal = 4.dp),
                ) {
                    Text(
                        text = skillToDelete?.let { "Tem certeza que deseja esquecer a habilidade \"${it.emoji} ${it.name}\"? Todo o seu aprendizado e XP acumulados nela se perderão permanentemente." } ?: "Tem certeza que deseja esquecer esta habilidade? Todo o seu aprendizado e XP acumulados nela se perderão permanentemente.",
                        fontSize = 14.sp,
                        color = Amber200,
                        fontFamily = Inter,
                        lineHeight = 22.sp,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 12.dp)
                                .testTag("forgetCancelButton")
                                .clickable { deleteConfirmIdx = null }
                                .background(Color.Transparent)
                                .border(1.dp, Zinc700, RoundedCornerShape(4.dp))
                                .padding(horizontal = 24.dp)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "CANCELAR",
                                fontFamily = Cinzel,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                color = Zinc400,
                            )
                        }

                        Text(
                            text = "ESQUECER",
                            fontFamily = Cinzel,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            color = Stone950,
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 12.dp)
                                .testTag("forgetConfirmButton")
                                .clickable {
                                    deleteConfirmIdx?.let { idx ->
                                        onDeleteSkill(idx)
                                        deleteConfirmIdx = null
                                    }
                                }
                                .background(Red500)
                                .clip(RoundedCornerShape(4.dp))
                                .padding(horizontal = 24.dp),
                        )
                    }
                }
            }
        }
}
}
