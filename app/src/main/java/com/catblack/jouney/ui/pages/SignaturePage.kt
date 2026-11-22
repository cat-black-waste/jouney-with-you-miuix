package com.catblack.jouney.ui.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catblack.jouney.ui.components.CtaButton
import com.catblack.jouney.ui.components.Ticks
import com.catblack.jouney.ui.theme.Brand
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.extra.SuperDialog

@Composable
fun SignaturePage() {
    val dialog = remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 34.dp)
            .padding(top = 92.dp, bottom = 30.dp)
    ) {
        Spacer(Modifier.weight(1f))
        Box(Modifier.fillMaxWidth().height(1.dp).background(Brand.Line))
        Spacer(Modifier.height(18.dp))
        Text(
            text = "jouney with you",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            color = Brand.Muted
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "DESIGN · MOTION · KOTLIN",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 9.sp,
            letterSpacing = 2.4.sp,
            textAlign = TextAlign.Center,
            color = Brand.Faint
        )
        Spacer(Modifier.weight(1f))
        Ticks(active = 2)
        Spacer(Modifier.height(22.dp))
        CtaButton(text = "开始使用", onClick = { dialog.value = true })
    }

    SuperDialog(
        show = dialog,
        title = "这是演示版本",
        summary = "“开始使用”是一个假按钮：点下去只弹这个面板，没有真实业务流程。"
    ) {
        TextButton(
            text = "知道了",
            onClick = { dialog.value = false },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
