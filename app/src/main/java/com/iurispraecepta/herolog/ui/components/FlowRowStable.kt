package com.iurispraecepta.herolog.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import kotlin.math.max

@Composable
fun FlowRowStable(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    val arrangeHMethod = Arrangement.Horizontal::class.java.getMethod(
        "arrange",
        androidx.compose.ui.unit.Density::class.java,
        Int::class.javaPrimitiveType,
        IntArray::class.java,
        androidx.compose.ui.unit.LayoutDirection::class.java,
        IntArray::class.java
    )
    val arrangeVMethod = Arrangement.Vertical::class.java.getMethod(
        "arrange",
        androidx.compose.ui.unit.Density::class.java,
        Int::class.javaPrimitiveType,
        IntArray::class.java,
        IntArray::class.java
    )

    Layout(
        measurePolicy = { measurables, constraints ->
            val density = this
            val maxWidth = if (constraints.maxWidth == Constraints.Infinity)
                Int.MAX_VALUE else constraints.maxWidth

            val placeables = measurables.map { it.measure(Constraints(maxWidth = maxWidth)) }

            val itemWidths = IntArray(placeables.size) { placeables[it].width }
            val itemHeights = IntArray(placeables.size) { placeables[it].height }

            val hOffsets = IntArray(placeables.size)
            arrangeHMethod.invoke(horizontalArrangement, density, Int.MAX_VALUE, itemWidths, layoutDirection, hOffsets)
            val spacingH = if (placeables.size >= 2) (hOffsets[1] - hOffsets[0] - itemWidths[0]).coerceAtLeast(0) else 0

            val vOffsets = IntArray(placeables.size)
            arrangeVMethod.invoke(verticalArrangement, density, itemHeights.sum(), itemHeights, vOffsets)
            val spacingV = if (placeables.size >= 2) (vOffsets[1] - vOffsets[0] - itemHeights[0]).coerceAtLeast(0) else 0

            val lines = mutableListOf<List<Int>>()
            val lineWidths = mutableListOf<Int>()
            val lineHeights = mutableListOf<Int>()

            var currentLine = mutableListOf<Int>()
            var currentLineWidth = 0
            var currentLineHeight = 0

            for (i in placeables.indices) {
                val w = placeables[i].width
                val h = placeables[i].height

                if (currentLine.isNotEmpty() && currentLineWidth + spacingH + w > maxWidth) {
                    lines.add(currentLine)
                    lineWidths.add(currentLineWidth)
                    lineHeights.add(currentLineHeight)
                    currentLine = mutableListOf()
                    currentLineWidth = 0
                    currentLineHeight = 0
                }

                currentLine.add(i)
                currentLineWidth += if (currentLine.size == 1) w else spacingH + w
                currentLineHeight = max(currentLineHeight, h)
            }

            if (currentLine.isNotEmpty()) {
                lines.add(currentLine)
                lineWidths.add(currentLineWidth)
                lineHeights.add(currentLineHeight)
            }

            val totalHeight = if (lineHeights.isNotEmpty())
                lineHeights.sum() + spacingV * (lineHeights.size - 1) else 0

            layout(
                width = lineWidths.maxOrNull() ?: 0,
                height = totalHeight
            ) {
                var y = 0
                for ((lineIdx, line) in lines.withIndex()) {
                    val lineH = lineHeights[lineIdx]

                    val offsets = IntArray(line.size)
                    val lineSizes = IntArray(line.size) { placeables[line[it]].width }
                    arrangeHMethod.invoke(horizontalArrangement, density, lineWidths[lineIdx], lineSizes, layoutDirection, offsets)

                    for (k in line.indices) {
                        placeables[line[k]].place(IntOffset(offsets[k], y))
                    }

                    y += lineH + spacingV
                }
            }
        },
        content = content,
        modifier = modifier
    )
}
