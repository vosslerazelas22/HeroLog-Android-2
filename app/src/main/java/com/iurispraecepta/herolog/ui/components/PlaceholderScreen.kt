package com.iurispraecepta.herolog.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.iurispraecepta.herolog.ui.theme.Amber400
import com.iurispraecepta.herolog.ui.theme.Stone400

/**
 * Placeholder para módulos do React ainda não portados (Reino inteiro: Bazar/Títulos/Heatmap/
 * Estatísticas/Conquistas/Registros/Tutorial; e dentro de Missões: Contratos e Crônicas Diárias).
 * Existe só pra dar destino visual às sub-abas já cadastradas em `HeroLogBottomNav`, sem implicar
 * nenhuma decisão de fidelidade de lógica/visual — remover assim que o módulo real for portado.
 */
@Composable
fun PlaceholderScreen(title: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = Amber400,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Ainda não portado do React.",
                color = Stone400,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
