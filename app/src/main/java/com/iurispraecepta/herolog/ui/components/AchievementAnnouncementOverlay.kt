package com.iurispraecepta.herolog.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iurispraecepta.herolog.logic.achievements.Achievement
import com.iurispraecepta.herolog.ui.theme.Cinzel
import com.iurispraecepta.herolog.ui.theme.JetBrainsMono

/**
 * Overlay global de anúncios de conquistas fora do foco (FR-009/US-04).
 * Mostra uma conquista por vez da fila efêmera. Avançar fecha e remove da fila.
 */
@Composable
fun AchievementAnnouncementOverlay(
    queue: List<Achievement>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentAchievement = queue.firstOrNull()

    AnimatedVisibility(
        visible = currentAchievement != null,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
        modifier = modifier.fillMaxSize()
    ) {
        if (currentAchievement != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .clickable { /* absorve clique para não fechar ao tocar no card */ },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🏆 CONQUISTA DESBLOQUEADA",
                    color = Color(0xFFE5C158),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Ícone central
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .shadow(12.dp, CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFD4AF37).copy(alpha = 0.3f),
                                    Color(0xFFD4AF37).copy(alpha = 0.05f)
                                )
                            )
                        )
                        .border(2.dp, Color(0xFFD4AF37).copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = currentAchievement.icon, fontSize = 40.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = currentAchievement.name,
                    fontFamily = Cinzel,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFFF5F5F4),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = currentAchievement.desc,
                    fontFamily = Cinzel,
                    fontSize = 13.sp,
                    color = Color(0xFFA8A29E),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                if (queue.size > 1) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "1 de ${queue.size}",
                        fontFamily = JetBrainsMono,
                        fontSize = 11.sp,
                        color = Color(0xFF78716C)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botão Continuar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFFD4AF37), Color(0xFFE5C158))
                            )
                        )
                        .clickable(onClick = onDismiss)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CONTINUAR",
                        fontFamily = Cinzel,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp,
                        color = Color(0xFF1C1917)
                    )
                }
            }
        }
        }
    }
}
