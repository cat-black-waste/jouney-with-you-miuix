package com.catblack.jouney.ui.mark

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.awaitPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.catblack.jouney.R
import com.catblack.jouney.ui.theme.Brand
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.exp

private class MarkGlyph(val spec: GlyphSpec, val path: Path, val measure: PathMeasure)

class MarkRuntime(val count: Int) {
    val progress = List(count) { mutableFloatStateOf(0f) }
    val velocity = FloatArray(count)
    var dragX by mutableFloatStateOf(-1f)
    var dragging by mutableStateOf(false)
    var autoDone by mutableStateOf(false)
    var startedAt by mutableLongStateOf(System.nanoTime())
    var page by mutableIntStateOf(0)
    var energy by mutableFloatStateOf(0f)
    var direction by mutableFloatStateOf(1f)
    var rule by mutableFloatStateOf(0f)
}

private fun smooth(t: Float): Float {
    val x = t.coerceIn(0f, 1f)
    return x * x * (3f - 2f * x)
}

private fun easeOut(t: Float): Float {
    val x = t.coerceIn(0f, 1f)
    val inv = 1f - x
    return 1f - inv * inv * inv
}

@Composable
fun JourneyMark(page: Int, modifier: Modifier = Modifier) {
    val density = LocalDensity.current.density
    val glyphs = remember { buildGlyphs() }
    val segments = remember(glyphs) { List(glyphs.size) { Path() } }
    val runtime = remember(glyphs) { MarkRuntime(glyphs.size) }
    val scope = rememberCoroutineScope()

    SideEffect { runtime.page = page }

    LaunchedEffect(runtime) {
        var last = 0L
        while (true) {
            withFrameNanos { now ->
                val dt = if (last == 0L) 0f else ((now - last) / 1_000_000_000f).coerceAtMost(0.05f)
                last = now
                val elapsedMs = (now - runtime.startedAt) / 1_000_000f
                val drag = runtime.dragX
                for (i in glyphs.indices) {
                    val spec = glyphs[i].spec
                    val target = when {
                        runtime.page != 0 -> 1f
                        runtime.dragging && drag >= 0f -> {
                            val raw = ((drag - (spec.x - spec.w * 0.1f)) / (spec.w + 52f)).coerceIn(0f, 1f)
                            smooth(raw)
                        }
                        !runtime.autoDone -> easeOut((elapsedMs - (210f + i * 44f)) / 380f)
                        else -> 1f
                    }
                    val stiff = if (runtime.dragging) 300f else 210f
                    val damp = if (runtime.dragging) 30f else 27f
                    val current = runtime.progress[i].floatValue
                    var v = runtime.velocity[i]
                    v += (target - current) * stiff * dt
                    v -= v * damp * dt
                    runtime.velocity[i] = v
                    runtime.progress[i].floatValue = (current + v * dt).coerceIn(0f, 1f)
                }
                if (runtime.page == 0 && !runtime.autoDone && elapsedMs > 2300f) {
                    runtime.autoDone = true
                }
                runtime.rule = easeOut(elapsedMs / 300f)
                runtime.energy *= exp(-dt * 9f)
            }
        }
    }

    Box(
        modifier = modifier
            .size(372.dp, 192.dp)
            .pointerInput(Unit) {
                awaitEachGesture {
                    if (runtime.page != 0) return@awaitEachGesture
                    val down = awaitFirstDown()
                    runtime.dragging = true
                    runtime.autoDone = true
                    runtime.dragX = down.position.x / density
                    var lastX = down.position.x
                    var lastT = down.uptimeMillis
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: break
                        val xPx = change.position.x
                        val dtMs = (change.uptimeMillis - lastT).coerceAtLeast(1L)
                        val vx = (xPx - lastX) / dtMs * 16f
                        runtime.energy = (runtime.energy + abs(vx) / (26f * density)).coerceAtMost(1f)
                        if (vx > 2f) runtime.direction = 1f else if (vx < -2f) runtime.direction = -1f
                        runtime.dragX = xPx / density
                        lastX = xPx
                        lastT = change.uptimeMillis
                        change.consume()
                        if (!change.pressed) break
                    }
                    runtime.dragging = false
                    runtime.dragX = -1f
                    scope.launch {
                        delay(420)
                        if (runtime.page == 0) runtime.autoDone = true
                    }
                }
            }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            scale(density, density, pivot = Offset.Zero) {
                drawWordmark(glyphs, segments, runtime)
            }
        }
    }
}

private fun buildGlyphs(): List<MarkGlyph> = Wordmark.glyphs.map { spec ->
    val parsed = androidx.compose.ui.graphics.vector.PathParser().parsePathString(spec.d).toPath()
    val matrix = Matrix().also { m ->
        m[0, 0] = spec.s
        m[1, 1] = -spec.s
        m[3, 0] = spec.x
        m[3, 1] = spec.y
    }
    parsed.transform(matrix)
    MarkGlyph(spec, parsed, PathMeasure().also { it.setPath(parsed, false) })
}

private fun DrawScope.drawWordmark(
    glyphs: List<MarkGlyph>,
    segments: List<Path>,
    runtime: MarkRuntime
) {
    drawLine(
        color = Color(0x2E0C0C0D),
        start = Offset(50f, 185f),
        end = Offset(50f + 272f * runtime.rule, 185f),
        strokeWidth = 1f
    )

    var activeIndex = -1
    glyphs.forEachIndexed { i, g ->
        val p = runtime.progress[i].floatValue
        if (p <= 0.002f) return@forEachIndexed
        val ink = smooth(p)
        val dry = smooth((p - 0.86f) / 0.14f)
        val seg = segments[i]
        seg.reset()
        val total = g.measure.length
        if (total > 0f) {
            g.measure.getSegment(0f, total * p, seg, true)
            val strokeColor = lerp(Brand.Gray, Brand.Ink, ink)
            if (runtime.energy > 0.03f && p < 0.995f) {
                val trail = Path()
                g.measure.getSegment((total * (p - 0.06f)).coerceAtLeast(0f), total * p, trail, true)
                drawPath(
                    path = trail,
                    color = lerp(Brand.Gray, Brand.Accent, 0.45f),
                    style = Stroke(width = 2.6f, cap = StrokeCap.Round, join = StrokeJoin.Round),
                    alpha = (runtime.energy * 0.5f).coerceIn(0f, 1f)
                )
            }
            drawPath(
                path = seg,
                color = strokeColor,
                style = Stroke(width = 2.1f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
        if (dry > 0f) {
            drawPath(
                path = g.path,
                color = lerp(lerp(Brand.Gray, Brand.Ink, ink), Brand.Ink, dry),
                alpha = dry
            )
        }
        if (p > 0.002f && p < 0.995f && activeIndex < 0) activeIndex = i
    }

    if (activeIndex >= 0) {
        val g = glyphs[activeIndex]
        val p = runtime.progress[activeIndex].floatValue
        val total = g.measure.length
        if (total > 0f) {
            val tip = g.measure.getPosition(total * p)
            drawCircle(color = Brand.Accent, radius = 3.4f, center = tip)
            for (j in 1..3) {
                val d = (p - j * 0.055f).coerceAtLeast(0f)
                val pos = g.measure.getPosition(total * d)
                drawCircle(
                    color = Brand.Accent,
                    radius = 3.4f - j * 0.7f,
                    center = pos,
                    alpha = 0.28f / j
                )
            }
        }
    }
}
