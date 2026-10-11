package com.iurispraecepta.herolog

import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Viewport pequeno (375×667), mesmo qualifier do `BottomNavDiagnosticTest` e dos
 * testes 375 do projeto: sem densidade explícita (mdpi implícito do Robolectric,
 * px = dp), igual aos baselines `bottomnav_diag_*_375x667.png` existentes.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w375dp-h667dp", sdk = [36])
class HistoryScreenScreenshotTest375 : HistoryScreenScreenshotBase() {

    override val viewportSuffix: String = "375"
}
