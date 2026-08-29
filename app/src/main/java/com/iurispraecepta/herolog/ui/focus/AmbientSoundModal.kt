package com.iurispraecepta.herolog.ui.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.R
import com.iurispraecepta.herolog.ui.components.HeroLogModal
import com.iurispraecepta.herolog.ui.components.ModalVariant

private val Amber400 = Color(0xFFFBBF24)
private val Amber500 = Color(0xFFF59E0B)
private val Stone900 = Color(0xFF1C1917)
private val Stone800 = Color(0xFF292524)
private val Stone700 = Color(0xFF44403C)
private val Stone500 = Color(0xFF78716C)
private val Stone300 = Color(0xFFD6D3D1)

@Composable
fun AmbientSoundModal(
    isOpen: Boolean,
    onClose: () -> Unit,
    selectedTrack: String?,
    volume: Int,
    onSelectTrack: (String?) -> Unit,
    onSetVolume: (Int) -> Unit,
    tracks: List<AmbientTrack> = AMBIENT_SOUNDS
) {
    HeroLogModal(
        isOpen = isOpen,
        onClose = onClose,
        title = "Sons do Santuário",
        variant = ModalVariant.Amber
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Volume Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Stone900)
                    .border(1.dp, Stone800, RoundedCornerShape(8.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = Amber400,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Volume",
                            color = Stone300,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = "$volume%",
                        color = Amber400,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Slider(
                    value = volume.toFloat(),
                    onValueChange = { onSetVolume(it.toInt()) },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = Amber400,
                        activeTrackColor = Amber500,
                        inactiveTrackColor = Stone700
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Lista de Trilhas
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tracks.forEach { track ->
                    val isSelected = selectedTrack == track.id
                    AmbientTrackRow(
                        track = track,
                        isSelected = isSelected,
                        onClick = { onSelectTrack(track.id) }
                    )
                }
            }

            // Rodapé informativo
            Text(
                text = "Os sussurros do santuário reverberam somente enquanto sua mente estiver focada de fato (durante sessões de foco ativas, não pausadas).",
                color = Stone500,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun AmbientTrackRow(
    track: AmbientTrack,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) Amber400 else Stone800
    val bgColor = if (isSelected) Stone900 else Stone900.copy(alpha = 0.5f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = track.icone,
                fontSize = 20.sp
            )
            Column {
                Text(
                    text = track.nome,
                    color = if (isSelected) Amber400 else Color(0xFFE7E5E4),
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
                if (isSelected) {
                    Text(
                        text = "Ativo e em Sintonia",
                        color = Amber500,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Icon(
            painter = painterResource(
                if (isSelected) R.drawable.lucide_ic_toggle_right else R.drawable.lucide_ic_toggle_left
            ),
            contentDescription = null,
            tint = if (isSelected) Color(0xFFE5C158) else Color(0xFFA1A1AA)
        )
    }
}
