package com.catblack.jouney.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catblack.jouney.ui.components.BrandMini
import com.catblack.jouney.ui.components.Kicker
import com.catblack.jouney.ui.mark.JourneyMark
import com.catblack.jouney.ui.pages.ArrivalPage
import com.catblack.jouney.ui.pages.ClearancePage
import com.catblack.jouney.ui.pages.SignaturePage
import com.catblack.jouney.ui.theme.JourneyTheme
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun JourneyApp() {
    JourneyTheme {
        var page by rememberSaveable { mutableIntStateOf(0) }
        BoxWithConstraints(
            Modifier
                .fillMaxSize()
                .background(MiuixTheme.colorScheme.background)
        ) {
            val heightPx = with(LocalDensity.current) { maxHeight.toPx() }
            Ambient()
            AnimatedContent(
                targetState = page,
                transitionSpec = {
                    (
                        fadeIn(tween(460)) +
                            slideInVertically(spring(stiffness = 320f, dampingRatio = 0.9f)) { it / 16 }
                        ).togetherWith(fadeOut(tween(240)))
                },
                label = "page"
            ) { target ->
                when (target) {
                    0 -> ArrivalPage(onNext = { page = 1 })
                    1 -> ClearancePage(onNext = { page = 2 })
                    else -> SignaturePage()
                }
            }
            MarkHost(page = page, heightPx = heightPx, width = maxWidth)
            Row(
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 34.dp, end = 34.dp, top = 26.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Kicker(
                    text = when (page) {
                        0 -> "ARRIVAL"
                        1 -> "CLEARANCE"
                        else -> "SIGNATURE"
                    }
                )
                Box(Modifier.weight(1f))
                BrandMini(
                    text = when (page) {
                        0 -> "SINGLE INSTANCE"
                        1 -> "PERMISSION PROBE"
                        else -> "LICENSE"
                    }
                )
            }
        }
    }
}

@Composable
private fun BoxScope.MarkHost(page: Int, heightPx: Float, width: Dp) {
    val slotY = when (page) {
        0 -> 0.40f
        1 -> 0.155f
        else -> 0.355f
    }
    val slotScale = when (page) {
        0 -> 1f
        1 -> 0.40f
        else -> 0.94f
    }
    val slotAlpha = when (page) {
        0 -> 1f
        1 -> 0.62f
        else -> 1f
    }
    val springSpec = spring<Float>(dampingRatio = 0.86f, stiffness = 200f)
    val scale by animateFloatAsState(slotScale, springSpec, label = "markScale")
    val alpha by animateFloatAsState(slotAlpha, tween(420), label = "markAlpha")
    val translate by animateFloatAsState(slotY * heightPx - heightPx / 2f, springSpec, label = "markY")
    val base = ((width - 54.dp) / 372.dp).coerceIn(0.62f, 1.04f)

    JourneyMark(
        page = page,
        modifier = Modifier
            .align(Alignment.Center)
            .graphicsLayer {
                translationY = translate
                scaleX = base * scale
                scaleY = base * scale
                this.alpha = alpha
            }
    )
}

@Composable
private fun Ambient() {
    Canvas(Modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x22FF8C5F), Color.Transparent),
                center = Offset(size.width * 0.22f, size.height * 0.16f),
                radius = size.width * 0.95f
            )
        )
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x1A96AAFF), Color.Transparent),
                center = Offset(size.width * 0.84f, size.height * 0.08f),
                radius = size.width * 0.85f
            )
        )
    }
}
