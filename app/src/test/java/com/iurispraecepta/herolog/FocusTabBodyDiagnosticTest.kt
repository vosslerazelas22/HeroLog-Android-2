package com.iurispraecepta.herolog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.iurispraecepta.herolog.ui.focus.FocusTabBody
import com.iurispraecepta.herolog.ui.navigation.LocalBottomBarInset
import com.iurispraecepta.herolog.ui.theme.HeroLogTheme
import com.iurispraecepta.herolog.ui.theme.QuestPanel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Diagnóstico Fase 1 (issue #22 — follow-up): monta [FocusTabBody] no arranjo real
 * (banner fixo + body com `weight(1f)`), com alturas realistas do conteúdo da aba Foco
 * idle em 390x844, e verifica se CTA + quick actions estão visíveis ou alcançáveis
 * por scroll. NÃO desliga `mainClock.autoAdvance` (precisamos de scroll real).
 *
 * Referências de altura:
 * - banner ~52dp: `FocusHeaderBanner` (p-3.5 + conteúdo, `App.tsx:2339`)
 * - carousel 72dp + spacer 22dp: gap-2 + notifier h-1.5 + gap-2 (`App.tsx:2383,2396`)
 * - orb 256dp: `FocusOrbSize.STANDARD` (`FocusOrb.kt:1346`)
 * - inset 80dp: `pb-[80px]` do content (`App.tsx:2383`)
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w390dp-h844dp", sdk = [36])
class FocusTabBodyDiagnosticTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun focusTabBody_diag_390x844() {
        var outerSize: IntSize? = null
        var bodySize: IntSize? = null
        var ctaBounds: Rect? = null
        var qaBounds: Rect? = null

        composeTestRule.setContent {
            // darkTheme fixo: o app real roda em tema escuro; em claro os stubs
            // champagne/branco sobre fundo claro ficam ilegíveis.
            HeroLogTheme(darkTheme = true) {
                // pb-[80px] do React (App.tsx:2383) via CompositionLocal, como a MainActivity
                // faz com innerPadding.calculateBottomPadding() (MainActivity.kt:342)
                CompositionLocalProvider(LocalBottomBarInset provides 80.dp) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(QuestPanel)
                            .onSizeChanged { outerSize = it }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .background(Color(0x33E5C158)),
                            contentAlignment = Alignment.Center
                        ) { Text("banner", color = Color.White) }
                        FocusTabBody(
                            modifier = Modifier
                                .weight(1f)
                                .onSizeChanged { bodySize = it },
                            top = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(72.dp)
                                        .background(Color(0x33E5C158)),
                                    contentAlignment = Alignment.Center
                                ) { Text("carousel", color = Color.White) }
                                Spacer(modifier = Modifier.height(22.dp))
                            },
                            timer = {
                                Box(
                                    modifier = Modifier
                                        .height(256.dp)
                                        .background(Color(0x66A855F7)),
                                    contentAlignment = Alignment.Center
                                ) { Text("ORB", color = Color.White) }
                            },
                            bottom = {
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .background(Color(0x33E5C158))
                                        .onGloballyPositioned { ctaBounds = it.boundsInRoot() },
                                    contentAlignment = Alignment.Center
                                ) { Text("CTA-INICIAR-MISSAO", color = Color.White) }
                                Spacer(modifier = Modifier.height(10.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(96.dp)
                                        .background(Color(0x22FFFFFF)),
                                    contentAlignment = Alignment.Center
                                ) { Text("RAID", color = Color.White) }
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(64.dp)
                                        .background(Color(0x22FFFFFF))
                                        .onGloballyPositioned { qaBounds = it.boundsInRoot() },
                                    contentAlignment = Alignment.Center
                                ) { Text("QA-MODO-SOM-AJUSTES-TELA", color = Color.White) }
                                Spacer(modifier = Modifier.height(LocalBottomBarInset.current))
                            }
                        )
                    }
                }
            }
        }
        composeTestRule.waitForIdle()

        // Screenshot do estado inicial (antes de qualquer scroll/assert)
        composeTestRule.onRoot().captureRoboImage(
            filePath = "src/test/screenshots/focus_tabbody_diag_390x844.png"
        )

        val outer = outerSize
        val body = bodySize
        val cta0 = ctaBounds
        val qa0 = qaBounds
        // Altura esperada do scroll-container ≈ body − padding top 8dp. Em px depende da
        // densidade do Robolectric; o que importa aqui são as comparações relativas.
        println("DIAG outer=$outer body(FocusTabBody)=$body")
        println("DIAG ctaBoundsInRoot(antes)=$cta0")
        println("DIAG qaBoundsInRoot(antes)=$qa0")

        val ctaVisibleBefore: Boolean = try {
            composeTestRule.onNodeWithText("CTA-INICIAR-MISSAO").assertIsDisplayed()
            true
        } catch (e: AssertionError) {
            println("DIAG CTA assertIsDisplayed(antes) FALHOU: ${e.message?.take(300)}")
            false
        }
        val qaVisibleBefore: Boolean = try {
            composeTestRule.onNodeWithText("QA-MODO-SOM-AJUSTES-TELA").assertIsDisplayed()
            true
        } catch (e: AssertionError) {
            println("DIAG QA assertIsDisplayed(antes) FALHOU: ${e.message?.take(300)}")
            false
        }
        println("DIAG visibleAntes: cta=$ctaVisibleBefore qa=$qaVisibleBefore")

        // Tentativa de scroll até o CTA — distingue "abaixo da dobra + scroll OK"
        // de "scroll quebrado" (H1).
        var scrollOk = false
        try {
            composeTestRule.onNodeWithText("CTA-INICIAR-MISSAO").performScrollTo()
            composeTestRule.waitForIdle()
            scrollOk = true
        } catch (e: Exception) {
            println("DIAG performScrollTo(CTA) FALHOU: ${e.javaClass.simpleName}: ${e.message?.take(300)}")
        }
        println("DIAG scrollOk=$scrollOk ctaBoundsInRoot(depois)=$ctaBounds qaBoundsInRoot(depois)=$qaBounds")
        composeTestRule.onRoot().captureRoboImage(
            filePath = "src/test/screenshots/focus_tabbody_diag_390x844_scrolled.png"
        )

        val ctaVisibleAfter: Boolean = try {
            composeTestRule.onNodeWithText("CTA-INICIAR-MISSAO").assertIsDisplayed()
            true
        } catch (e: AssertionError) {
            println("DIAG CTA assertIsDisplayed(depois) FALHOU: ${e.message?.take(300)}")
            false
        }
        println("DIAG visibleDepois: cta=$ctaVisibleAfter scrollOk=$scrollOk")

        // Veredito do diagnóstico (falha informativa se o bug reproduzir):
        // - CTA visível de cara → sem regressão neste arranjo.
        // - CTA só visível após scroll → overflow com scroll funcional (H1-parcial).
        // - CTA invisível mesmo após scroll → scroll quebrado ou clip (H1/H2/H3).
        if (!ctaVisibleAfter) {
            throw AssertionError(
                "DIAG: CTA inalcançável em 390x844 " +
                    "(antes=$ctaVisibleBefore, scrollOk=$scrollOk, depois=$ctaVisibleAfter; " +
                    "outer=$outer body=$body cta=$ctaBounds qa=$qaBounds). " +
                    "Ver screenshots focus_tabbody_diag_390x844[_scrolled].png"
            )
        }
    }

    /**
     * Contrato de overflow do KDoc de `FocusTimerViewport`: se
     * `top + timerMínimo + bottom > viewport`, o layout cresce além da área e o
     * `verticalScroll` mantém o bottom alcançável. Força overflow com um bottom
     * de ~600dp e exige que o CTA seja alcançável via scroll.
     */
    @Test
    fun focusTabBody_overflow_scrollsToCTA_390x844() {
        composeTestRule.setContent {
            HeroLogTheme(darkTheme = true) {
                CompositionLocalProvider(LocalBottomBarInset provides 80.dp) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(QuestPanel)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .background(Color(0x33E5C158)),
                            contentAlignment = Alignment.Center
                        ) { Text("banner", color = Color.White) }
                        FocusTabBody(
                            modifier = Modifier.weight(1f),
                            top = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(72.dp)
                                        .background(Color(0x33E5C158)),
                                    contentAlignment = Alignment.Center
                                ) { Text("carousel", color = Color.White) }
                                Spacer(modifier = Modifier.height(22.dp))
                            },
                            timer = {
                                Box(
                                    modifier = Modifier
                                        .height(256.dp)
                                        .background(Color(0x66A855F7)),
                                    contentAlignment = Alignment.Center
                                ) { Text("ORB", color = Color.White) }
                            },
                            bottom = {
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .background(Color(0x33E5C158)),
                                    contentAlignment = Alignment.Center
                                ) { Text("CTA-OVERFLOW", color = Color.White) }
                                // Bloco alto de propósito: força top+min+bottom > viewport
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(500.dp)
                                        .background(Color(0x22FFFFFF)),
                                    contentAlignment = Alignment.Center
                                ) { Text("TALL", color = Color.White) }
                                Spacer(modifier = Modifier.height(LocalBottomBarInset.current))
                            }
                        )
                    }
                }
            }
        }
        composeTestRule.waitForIdle()

        var scrolled = false
        try {
            composeTestRule.onNodeWithText("CTA-OVERFLOW").performScrollTo()
            composeTestRule.waitForIdle()
            scrolled = true
        } catch (e: Exception) {
            println("DIAG-OVERFLOW performScrollTo FALHOU: ${e.javaClass.simpleName}: ${e.message?.take(300)}")
        }
        val visible: Boolean = try {
            composeTestRule.onNodeWithText("CTA-OVERFLOW").assertIsDisplayed()
            true
        } catch (e: AssertionError) {
            println("DIAG-OVERFLOW assertIsDisplayed FALHOU: ${e.message?.take(300)}")
            false
        }
        println("DIAG-OVERFLOW scrolled=$scrolled visible=$visible")
        if (!visible) {
            throw AssertionError(
                "DIAG-OVERFLOW: CTA inalcançável com overflow (scrolled=$scrolled, visible=$visible)"
            )
        }
    }
}
