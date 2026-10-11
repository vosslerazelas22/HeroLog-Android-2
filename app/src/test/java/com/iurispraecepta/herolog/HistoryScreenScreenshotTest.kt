package com.iurispraecepta.herolog

import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Viewport grande (390×844). Qualifier explícito `w390dp-h844dp` — igual ao
 * `BottomNavDiagnosticTest` — porque `RobolectricDeviceQualifiers.Pixel8` produz
 * 1078×2399 px (≈420dpi), o que tornaria o sufixo `_390` do PNG mentira.
 * Anotações repetidas nas subclasses: `@RunWith`/`@GraphicsMode`/`@Config` não são
 * `@Inherited` (a base fica sem runner e os @Test dela não rodariam).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w390dp-h844dp", sdk = [36])
class HistoryScreenScreenshotTest : HistoryScreenScreenshotBase() {

    override val viewportSuffix: String = "390"
}
