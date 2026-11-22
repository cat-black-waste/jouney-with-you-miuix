package com.catblack.jouney.ui.pages

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catblack.jouney.ui.components.CtaButton
import com.catblack.jouney.ui.components.Ticks
import com.catblack.jouney.ui.theme.Brand
import kotlinx.coroutines.delay
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun ClearancePage(onNext: () -> Unit) {
    var scanned by remember { mutableIntStateOf(0) }
    var done by remember { mutableStateOf(false) }
    val rows = listOf("存储目录", "通知回执", "位置状态")

    LaunchedEffect(Unit) {
        scanned = 0
        done = false
        delay(760)
        scanned = 1
        delay(440)
        scanned = 2
        delay(440)
        scanned = 3
        done = true
    }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 34.dp)
            .padding(top = 196.dp, bottom = 30.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            ProbeRing(done = done)
        }
        Spacer(Modifier.height(22.dp))
        Text(
            text = if (done) "全部就绪" else "正在检测权限",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = Brand.Ink
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = if (done) "READY · 3/3" else "CHECKING · $scanned/3",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 9.5.sp,
            letterSpacing = 2.4.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = if (done) Brand.Green else Brand.Faint
        )
        Spacer(Modifier.height(34.dp))

        Card(cornerRadius = 22.dp, color = MiuixTheme.colorScheme.surface) {
            Column(Modifier.padding(horizontal = 18.dp)) {
                rows.forEachIndexed { i, label ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.weight(1f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Brand.Ink
                        )
                        Text(
                            text = if (i < scanned) "granted" else "waiting",
                            fontSize = 9.sp,
                            letterSpacing = 2.sp,
                            color = if (i < scanned) Brand.Green else Brand.Faint
                        )
                    }
                    if (i < rows.lastIndex) {
                        Box(Modifier.fillMaxWidth().height(1.dp).background(Brand.Line))
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))
        Ticks(active = 1)
        Spacer(Modifier.height(22.dp))
        CtaButton(
            text = if (done) "下一步进入" else "下一步进入",
            enabled = done,
            green = done,
            onClick = onNext
        )
    }
}

@Composable
private fun ProbeRing(done: Boolean) {
    val pop by animateFloatAsState(
        targetValue = if (done) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 320f),
        label = "pop"
    )
    val check by animateFloatAsState(
        targetValue = if (done) 1f else 0f,
        animationSpec = tween(380, delayMillis = 140),
        label = "check"
    )
    Box(Modifier.size(116.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(color = Color(0x120C0C0D), radius = size.minDimension / 2f - 2f, style = Stroke(4.5.dp.toPx()))
        }
        if (!done) {
            InfiniteProgressIndicator(
                size = 108.dp,
                color = Brand.Accent,
                strokeWidth = 3.dp,
                orbitingDotSize = 2.6.dp
            )
        }
        if (pop > 0.01f) {
            Box(
                Modifier
                    .size(116.dp)
                    .graphicsLayer { scaleX = pop; scaleY = pop; alpha = pop }
                    .clip(CircleShape)
                    .background(Brand.Green),
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier.size(40.dp)) {
                    val w = size.width
                    val h = size.height
                    val path = Path().apply {
                        moveTo(w * 0.20f, h * 0.54f)
                        lineTo(w * 0.43f, h * 0.75f)
                        lineTo(w * 0.80f, h * 0.27f)
                    }
                    val pm = PathMeasure().also { it.setPath(path, false) }
                    val seg = Path()
                    pm.getSegment(0f, pm.length * check, seg, true)
                    drawPath(
                        path = seg,
                        color = Color.White,
                        style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
            }
        }
    }
}
