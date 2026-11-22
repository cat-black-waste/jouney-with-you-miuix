package com.catblack.jouney.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catblack.jouney.ui.theme.Brand
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Text

@Composable
fun Ticks(active: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { i ->
            val on = i == active
            val width by animateDpAsState(
                targetValue = if (on) 40.dp else 26.dp,
                animationSpec = tween(450),
                label = "tick$i"
            )
            Box(
                Modifier
                    .padding(horizontal = 3.dp)
                    .width(width)
                    .height(2.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (on) Brand.Ink else Brand.Line)
            )
        }
    }
}

@Composable
fun CtaButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    green: Boolean = false,
    onClick: () -> Unit
) {
    val bg = when {
        !enabled -> Brand.Line
        green -> Brand.Green
        else -> Brand.Ink
    }
    val fg = if (enabled) Color.White else Brand.Muted
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(56.dp),
        enabled = enabled,
        cornerRadius = 28.dp,
        colors = ButtonDefaults.buttonColors(color = bg, disabledColor = Brand.Line)
    ) {
        Text(
            text = text,
            color = fg,
            fontSize = 15.5.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.2.sp
        )
    }
}

@Composable
fun Kicker(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        color = Brand.Faint,
        fontSize = 9.5.sp,
        letterSpacing = 2.6.sp,
        fontWeight = FontWeight.Medium
    )
}

@Composable
fun BrandMini(text: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(4.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(Brand.Accent))
        Box(Modifier.width(7.dp))
        Text(
            text = text,
            color = Brand.Faint,
            fontSize = 9.5.sp,
            letterSpacing = 2.1.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
