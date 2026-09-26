package com.iurispraecepta.herolog.ui.focus

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max

/**
 * Distribuição vertical da aba Foco — port da estrutura de espaço do React:
 *
 * - content `p-4 flex-1 flex flex-col gap-2 py-2 pb-[80px]` (`App.tsx:2383`)
 * - `timer-container-responsive flex-1 justify-center` (`App.tsx:2477`)
 * - padding do timer 1.25rem; 0.5rem se `max-height <= 700px` (`index.css:122-131`)
 *
 * Slots:
 * - [top]: SkillInlineCarousel + respiro fixo até a região do timer;
 * - [timer]: FocusOrb — recebe **todo** o espaço vertical leftover da viewport e
 *   centraliza o orb (equivalente a `flex-1` + `justify-center`);
 * - [bottom]: CTA / botões de sessão + status + quick actions — altura intrínseca.
 *
 * Se `top + timerMínimo + bottom > viewport`, o layout cresce além da área e o
 * `verticalScroll` do pai mantém o bottom alcançável (paridade com o scroll de
 * página do React em altura reduzida).
 *
 * `weight` dentro de `verticalScroll` não expande (eixo principal sem limite);
 * por isso a altura da viewport é passada em [availableHeight] e o cálculo é
 * feito num `Layout` de passo único.
 */
@Composable
fun FocusTimerViewport(
    availableHeight: Dp,
    modifier: Modifier = Modifier,
    top: @Composable () -> Unit,
    timer: @Composable () -> Unit,
    bottom: @Composable () -> Unit,
) {
    Layout(
        modifier = modifier,
        content = {
            // top/bottom empilham verticalmente (Column): o caller passa vários
            // filhos por slot (ex.: carousel + Spacer, CTA + status + actions).
            // Box sobreporia todos no mesmo ponto — foi a causa do CTA invisível
            // (issue #22 follow-up). O timer tem filho único e mede intrinsecamente
            // para centralização no placeRelative do Layout.
            Column(modifier = Modifier.fillMaxWidth()) { top() }
            Box { timer() }
            Column(modifier = Modifier.fillMaxWidth()) { bottom() }
        }
    ) { measurables, constraints ->
        require(measurables.size == 3) {
            "FocusTimerViewport espera exatamente 3 slots (top, timer, bottom)"
        }

        // minWidth=0: sem isso o slot do timer herda o minWidth do fillMaxWidth do
        // Layout e vira full-width — placeRelative não consegue centralizar o orb.
        val loose = constraints.copy(
            minWidth = 0,
            minHeight = 0,
            maxHeight = Constraints.Infinity
        )
        val topPlaceable = measurables[0].measure(loose)
        val timerPlaceable = measurables[1].measure(loose)
        val bottomPlaceable = measurables[2].measure(loose)

        val availPx = availableHeight.roundToPx()
        // index.css:122-131 — py-5 base; py-2 em viewport com altura <= 700px
        val padPx =
            if (availPx <= 700.dp.roundToPx()) 8.dp.roundToPx() else 20.dp.roundToPx()

        val fixedPx = topPlaceable.height + bottomPlaceable.height
        val leftoverPx = availPx - fixedPx
        val minTimerPx = timerPlaceable.height + 2 * padPx
        val timerRegionPx = max(leftoverPx, minTimerPx)

        val totalHeightPx = topPlaceable.height + timerRegionPx + bottomPlaceable.height
        val layoutWidth =
            if (constraints.hasBoundedWidth) constraints.maxWidth
            else max(topPlaceable.width, max(timerPlaceable.width, bottomPlaceable.width))

        layout(layoutWidth, totalHeightPx) {
            topPlaceable.placeRelative((layoutWidth - topPlaceable.width) / 2, 0)
            val timerY = topPlaceable.height + (timerRegionPx - timerPlaceable.height) / 2
            timerPlaceable.placeRelative((layoutWidth - timerPlaceable.width) / 2, timerY)
            bottomPlaceable.placeRelative(
                (layoutWidth - bottomPlaceable.width) / 2,
                topPlaceable.height + timerRegionPx
            )
        }
    }
}

/**
 * Área de conteúdo da aba Foco sob o banner (equivalente ao `div.flex-1` de
 * `App.tsx:2383`): ocupa o leftover da aba via `weight` no caller, mede a
 * viewport, aplica padding horizontal 16dp / top 8dp (`p-4` + `py-2`) e monta
 * [FocusTimerViewport] dentro de `verticalScroll` (overflow em altura curta).
 */
@Composable
fun FocusTabBody(
    modifier: Modifier = Modifier,
    top: @Composable () -> Unit,
    timer: @Composable () -> Unit,
    bottom: @Composable () -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize().then(modifier)) {
        val viewportHeight = maxHeight
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, top = 8.dp, end = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FocusTimerViewport(
                availableHeight = viewportHeight - 8.dp,
                modifier = Modifier.fillMaxWidth(),
                top = top,
                timer = timer,
                bottom = bottom,
            )
        }
    }
}
