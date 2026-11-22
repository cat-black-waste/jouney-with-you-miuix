package com.catblack.jouney.ui.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catblack.jouney.ui.components.CtaButton
import com.catblack.jouney.ui.components.Ticks
import com.catblack.jouney.ui.theme.Brand
import top.yukonga.miuix.kmp.basic.Text
import java.time.LocalTime

@Composable
fun ArrivalPage(onNext: () -> Unit) {
    val hour = remember { LocalTime.now().hour }
    val greeting = when {
        hour < 5 -> "夜深了"
        hour < 11 -> "早上好"
        hour < 14 -> "中午好"
        hour < 18 -> "下午好"
        hour < 23 -> "晚上好"
        else -> "夜深了"
    }
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 34.dp)
            .padding(top = 92.dp, bottom = 30.dp)
    ) {
        Spacer(Modifier.weight(1f))
        Text(
            text = "hi，$greeting",
            fontSize = 31.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.6).sp,
            color = Brand.Ink
        )
        Spacer(Modifier.height(9.dp))
        Text(
            text = "在字标上横向拖动，笔尖会把灰阶的字一笔一笔描出来，描完的地方慢慢吃墨。",
            fontSize = 13.5.sp,
            lineHeight = 22.sp,
            color = Brand.Muted
        )
        Spacer(Modifier.height(26.dp))
        Ticks(active = 0)
        Spacer(Modifier.height(22.dp))
        CtaButton(text = "继续", onClick = onNext)
    }
}
