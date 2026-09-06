package com.iurispraecepta.herolog.ui.components

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.em
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import com.iurispraecepta.herolog.model.CharClass
import com.iurispraecepta.herolog.model.OrbConcept
import com.iurispraecepta.herolog.ui.theme.Amber400
import androidx.compose.ui.text.style.TextAlign
import com.iurispraecepta.herolog.ui.theme.Stone900
import com.iurispraecepta.herolog.ui.theme.Stone950
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.em
import android.os.Build
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Divider

// Paleta 1:1 com React
private val QuestPanel = Color(0xFF0B0915)
private val Stone800 = Color(0xFF292524)
private val Champagne400 = Color(0xFFE5C158)
private val Champagne500 = Color(0xFFD4AF37)

@Composable
fun GeneralSettingsModal(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    characterName: String,
    charClass: CharClass,
    orbConcept: OrbConcept,
    onNameChange: (String) -> Unit,
    onClassChange: (CharClass) -> Unit,
    onOrbConceptChange: (OrbConcept) -> Unit,
    coroutineScope: CoroutineScope
) {
    if (!isOpen) return

    var nameInput by remember { mutableStateOf(characterName) }
    val springSpec: AnimationSpec<Float> = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Backdrop (leaf — no children, blur only affects this)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Modifier.blur(8.dp)
                    } else {
                        Modifier
                    }
                )
                .background(Color.Black.copy(alpha = 0.8f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismiss() }
        )

        // Content (sibling of backdrop — NOT blurred)
        Box(
            modifier = Modifier
                .padding(16.dp)
                .widthIn(max = 448.dp)
                .fillMaxWidth()
                .background(QuestPanel, RoundedCornerShape(12.dp))
                .border(2.dp, Champagne500.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                .shadow(16.dp, RoundedCornerShape(12.dp), ambientColor = Color.Black.copy(alpha = 0.5f), spotColor = Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            val screenHeight = LocalConfiguration.current.screenHeightDp.dp
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = screenHeight * 0.7f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ajustes Gerais",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Black,
                        color = Champagne400,
                        fontSize = 14.sp,
                        letterSpacing = 1.5.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar",
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Header/Body divider
                Divider(
                    color = Champagne500.copy(alpha = 0.15f),
                    thickness = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                )

                // Section 1: Perfil do Herói
                SettingsSection(
                    title = "Perfil do Herói",
                    description = "Ajuste a identidade do seu herói e as preferências principais da campanha."
                ) {
                    Column(modifier = androidx.compose.ui.Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Nome do Herói
                        VStack(spacing = 4.dp) {
                            Text(
                                text = "Nome do Herói:",
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFCD34D),
                                fontSize = 9.sp,
                                letterSpacing = 0.2.em
                            )
                            TextField(
                                value = nameInput,
                                onValueChange = { if (it.length <= 24) onNameChange(it) },
                                modifier = androidx.compose.ui.Modifier
                                    .fillMaxWidth()
                                    .height(40.dp),
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 10.sp,
                                    color = Color(0xFFFEF3C7),
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif
                                ),
                                colors = TextFieldDefaults.colors(
                                    unfocusedContainerColor = Stone900.copy(alpha = 0.9f),
                                    focusedContainerColor = Stone900.copy(alpha = 0.9f),
                                    unfocusedIndicatorColor = Champagne500.copy(alpha = 0.2f),
                                    focusedIndicatorColor = Champagne400,
                                    unfocusedTextColor = Color(0xFFFEF3C7),
                                    focusedTextColor = Color(0xFFFEF3C7),
                                    unfocusedPlaceholderColor = Color(0xFFFEF3C7).copy(alpha = 0.2f),
                                    focusedPlaceholderColor = Color(0xFFFEF3C7).copy(alpha = 0.2f),
                                    cursorColor = Champagne400
                                )
                            )
                        }

                        // Seleção de Classe
                        VStack(spacing = 4.dp) {
                            Text(
                                text = "Selecione sua classe:",
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFCD34D),
                                fontSize = 9.sp,
                                letterSpacing = 0.2.em
                            )
                            Row(
                                modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ClassOptionCard(
                                    icon = "🧙",
                                    name = "Mago",
                                    bonus = "+20% XP",
                                    bonusColor = Color(0xFFA855F7),
                                    isSelected = charClass == CharClass.Mage,
                                    onClick = { onClassChange(CharClass.Mage) }
                                )
                                ClassOptionCard(
                                    icon = "⚔️",
                                    name = "Guerreiro",
                                    bonus = "+20% Ouro",
                                    bonusColor = Amber400,
                                    isSelected = charClass == CharClass.Warrior,
                                    onClick = { onClassChange(CharClass.Warrior) }
                                )
                                ClassOptionCard(
                                    icon = "🏹",
                                    name = "Patrulheiro",
                                    bonus = "+15% Streak",
                                    bonusColor = Color(0xFF10B981),
                                    isSelected = charClass == CharClass.Ranger,
                                    onClick = { onClassChange(CharClass.Ranger) }
                                )
                            }
                        }
                    }
                }

                // Section 2: Formato do Núcleo de Foco
                SettingsSection(title = "Formato do Núcleo de Foco") {
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OrbConceptOptionCard(
                                concept = OrbConcept.A,
                                emoji = "⏳",
                                name = "Chrono-Relic",
                                subtitle = "Relíquia Rúnica",
                                isSelected = orbConcept == OrbConcept.A,
                                onClick = { onOrbConceptChange(OrbConcept.A) }
                            )
                            OrbConceptOptionCard(
                                concept = OrbConcept.B,
                                emoji = "🔮",
                                name = "Obsidian Core",
                                subtitle = "Núcleo Cristalino",
                                isSelected = orbConcept == OrbConcept.B,
                                onClick = { onOrbConceptChange(OrbConcept.B) }
                            )
                        }
                        Row(
                            modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OrbConceptOptionCard(
                                concept = OrbConcept.C,
                                emoji = "⏱️",
                                name = "Arcane Dial",
                                subtitle = "Mostrador Arcano",
                                isSelected = orbConcept == OrbConcept.C,
                                onClick = { onOrbConceptChange(OrbConcept.C) }
                            )
                            OrbConceptOptionCard(
                                concept = OrbConcept.D,
                                emoji = "🧪",
                                name = "Alchemist Flask",
                                subtitle = "Frasco Alquimista",
                                isSelected = orbConcept == OrbConcept.D,
                                onClick = { onOrbConceptChange(OrbConcept.D) }
                            )
                        }
                    }
                }

                // Close button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Stone900,
                        contentColor = Champagne400
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Concluir e Retornar",
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        letterSpacing = 1.5.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    description: String? = null,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Stone950.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .border(1.dp, Champagne500.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Black,
                color = Color(0xFFFCD34D),
                fontSize = 10.sp,
                letterSpacing = 0.24.em
            )
            if (description != null) {
                Text(
                    text = description,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 9.sp,
                    color = Color(0xFFFEF3C7).copy(alpha = 0.55f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        content()
    }
}

@Composable
private fun RowScope.ClassOptionCard(
    icon: String,
    name: String,
    bonus: String,
    bonusColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) {
        Amber400.copy(alpha = 0.08f)
    } else {
        Stone900.copy(alpha = 0.7f)
    }
    val borderColor = if (isSelected) Amber400 else Champagne500.copy(alpha = 0.1f)
    val opacity = if (isSelected) 1f else 0.7f

    Box(
        modifier = androidx.compose.ui.Modifier
            .fillMaxWidth()
            .weight(1f)
            .height(96.dp)
            .background(bgColor, RoundedCornerShape(8.dp))
            .border(if (isSelected) 1.dp else 0.5.dp, borderColor, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .alpha(opacity)
    ) {
        Column(
            modifier = androidx.compose.ui.Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isSelected) {
                Box(
                    modifier = androidx.compose.ui.Modifier
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .background(
                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                listOf(
                                    Amber400.copy(alpha = 0f),
                                    Color(0xFFFDE047),
                                    Amber400.copy(alpha = 0f)
                                )
                            )
                        )
                )
            }
            Text(text = icon, fontSize = 20.sp)
            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(4.dp))
            Text(
                text = name.uppercase(),
                fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFDE68A),
                fontSize = 8.sp,
                letterSpacing = 0.16.em
            )
            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(4.dp))
            Text(
                text = bonus,
                color = bonusColor,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun RowScope.OrbConceptOptionCard(
    concept: OrbConcept,
    emoji: String,
    name: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) {
        Champagne500.copy(alpha = 0.08f)
    } else {
        Stone900.copy(alpha = 0.7f)
    }
    val borderColor = if (isSelected) Champagne500 else Color.White.copy(alpha = 0.1f)
    val opacity = if (isSelected) 1f else 0.7f

    Box(
        modifier = androidx.compose.ui.Modifier
            .fillMaxWidth()
            .weight(1f)
            .height(96.dp)
            .background(bgColor, RoundedCornerShape(8.dp))
            .border(if (isSelected) 1.dp else 0.5.dp, borderColor, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .alpha(opacity)
    ) {
        Column(
            modifier = androidx.compose.ui.Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isSelected) {
                Box(
                    modifier = androidx.compose.ui.Modifier
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .background(
                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                listOf(
                                    Champagne500.copy(alpha = 0f),
                                    Color(0xFFFDE047),
                                    Champagne500.copy(alpha = 0f)
                                )
                            )
                        )
                )
            }
            Text(text = emoji, fontSize = 20.sp)
            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(4.dp))
            Text(
                text = name,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFDE68A),
                fontSize = 8.sp,
                letterSpacing = 0.14.em
            )
            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = Color(0xFF9CA3AF),
                fontSize = 7.5.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun VStack(
    modifier: Modifier = Modifier,
    spacing: androidx.compose.ui.unit.Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(spacing), content = content)
}