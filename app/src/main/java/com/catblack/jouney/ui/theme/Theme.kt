package com.catblack.jouney.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.darkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme

object Brand {
    val Paper = Color(0xFFFBFBF9)
    val PaperDim = Color(0xFF0B0B0C)
    val Ink = Color(0xFF0C0C0D)
    val InkSoft = Color(0xFF15151A)
    val Graphite = Color(0xFF3A3A3E)
    val Gray = Color(0xFFC4C4CA)
    val Muted = Color(0xFF8A8A90)
    val Faint = Color(0xFFB6B6BC)
    val Accent = Color(0xFFFF5B37)
    val Green = Color(0xFF2FB45A)
    val Line = Color(0x140C0C0D)
}

@Composable
fun JourneyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        darkColorScheme(
            primary = Brand.Accent,
            onPrimary = Color.White,
            primaryVariant = Brand.Accent,
            background = Brand.PaperDim,
            onBackground = Color(0xFFF4F2EE),
            onBackgroundVariant = Color(0xFF9A9AA2),
            surface = Color(0xFF161618),
            onSurface = Color(0xFFF4F2EE),
            surfaceVariant = Color(0xFF161618),
            outline = Color(0xFF303036),
            dividerLine = Color(0xFF25252A),
            secondaryVariant = Color(0xFF1E1E22),
            onSecondaryVariant = Color(0xFFE7E5E1),
            surfaceContainer = Color(0xFF151517),
            onSurfaceContainer = Color(0xFFD6D4D0),
            tertiaryContainer = Color(0xFF2A1B16),
            onTertiaryContainer = Brand.Accent,
        )
    } else {
        lightColorScheme(
            primary = Brand.Accent,
            onPrimary = Color.White,
            primaryVariant = Brand.Accent,
            background = Brand.Paper,
            onBackground = Brand.Ink,
            onBackgroundVariant = Brand.Muted,
            surface = Color.White,
            onSurface = Brand.Ink,
            surfaceVariant = Color.White,
            outline = Color(0xFFE4E0D8),
            dividerLine = Color(0xFFE9E5DD),
            secondaryVariant = Color(0xFFF1EEE7),
            onSecondaryVariant = Brand.Ink,
            surfaceContainer = Color(0xFFF1EEE7),
            onSurfaceContainer = Color(0xFF323232),
            tertiaryContainer = Color(0xFFFFEDE7),
            onTertiaryContainer = Brand.Accent,
            disabledPrimaryButton = Color(0xFFE9E5DD),
        )
    }
    MiuixTheme(colors = colors, content = content)
}
