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
import com.catblack.jouney.ui.theme.Brand
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.exp

private class MarkGlyph(
    val spec: GlyphSpec,
    val path: Path,
    val measure: PathMeasure
)

class MarkRuntime(val count: Int) {
    val progress = List(count) { mutableFloatStateOf(0f) }
    val velocity = FloatArray(count)
    val startMs = FloatArray(count)
    val durMs = FloatArray(count)
    var dragX by mutableFloatStateOf(-1f)
    var dragging by mutableStateOf(false)
    var autoDone by mutableStateOf(false)
    var startedAt by mutableLongStateOf(System.nanoTime())
    var page by mutableIntStateOf(0)
    var energy by mutableFloatStateOf(0f)
    var circle by mutableFloatStateOf(0f)
    var rail by mutableFloatStateOf(0f)
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

/** o first (the circle seed), then the rest of "jouney", then "with you" left to right. */
private val WRITE_ORDER = listOf(1, 0, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)

@Composable
fun JourneyMark(page: Int, modifier: Modifier = Modifier) {
    val density = LocalDensity.current.density
    val glyphs = remember { buildGlyphs() }
    val segments = remember(glyphs) { List(glyphs.size) { Path() } }
    val runtime = remember(glyphs) { MarkRuntime(glyphs.size) }

    SideEffect { runtime.page = page }

    LaunchedEffect(runtime) {
        // constant pen speed: duration follows each letter's real outline length
        var cursor = 900f
        WRITE_ORDER.forEach { idx ->
            val len = glyphs[idx].measure.length
            runtime.startMs[idx] = cursor
            runtime.durMs[idx] = (len / 1.45f).coerceIn(150f, 480f)
            cursor += runtime.durMs[idx] + 70f
        }
        var last = 0L
        while (true) {
            withFrameNanos { now ->
                val dt = if (last == 0L) 0f else ((now - last) / 1_000_000_000f).coerceAtMost(0.05f)
                last = now
                val elapsed = (now - runtime.startedAt) / 1_000_000f
                val drag = runtime.dragX
                for (i in glyphs.indices) {
                    val spec = glyphs[i].spec
                    val target = when {
                        runtime.page != 0 -> 1f
                        runtime.dragging && drag >= 0f ->
                            smooth(((drag - (spec.x - 16f)) / (spec.w + 34f)).coerceIn(0f, 1f))
                        !runtime.autoDone -> {
                            val s = runtime.startMs[i]
                            val d = runtime.durMs[i]
                            if (elapsed <= s) 0f else smooth((elapsed - s) / d)
                        }
                        else -> 1f
                    }
                    val stiff = if (runtime.dragging) 300f else 220f
                    val damp = if (runtime.dragging) 30f else 28f
                    val current = runtime.progress[i].floatValue
                    val v = runtime.velocity[i] + (target - current) * stiff * dt
                    runtime.velocity[i] = v - v * damp * dt
                    runtime.progress[i].floatValue =
                        (current + runtime.velocity[i] * dt).coerceIn(0f, 1f)
                }
                if (runtime.page == 0 && !runtime.autoDone && elapsed > cursor + 200f) {
                    runtime.autoDone = true
                }
                runtime.circle = if (runtime.page != 0 || runtime.dragging) 1f else easeOut(elapsed / 300f)
                runtime.rail = when {
                    runtime.page != 0 -> 1f
                    runtime.dragging -> 1f
                    else -> easeOut((elapsed - 300f) / 560f)
                }
                runtime.energy *= exp(-dt * 9f)
            }
        }
    }

    val scope = rememberCoroutineScope()
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
    // the geometric seed: a circle sitting exactly on the letter o
    if (runtime.circle > 0.001f) {
        val o = glyphs[1]
        val p = runtime.progress[1].floatValue
        val alpha = (1f - smooth(p)) * runtime.circle
        val radius = 22.9f
        val center = Offset(96.9f, 82.1f)
        val reveal = runtime.circle.coerceIn(0f, 1f)
        drawArc(
            color = Brand.Gray,
            startAngle = -90f,
            sweepAngle = 360f * reveal,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = androidx.compose.ui.geometry.Size(radius * 2f, radius * 2f),
            style = Stroke(width = 1.4f),
            alpha = alpha.coerceIn(0f, 1f)
        )
    }

    // the baseline the letters stand on, unrolled from the o in both directions
    val rail = runtime.rail
    if (rail > 0.001f) {
        drawLine(Brand.Line, Offset(96.9f, 104f), Offset(96.9f - 46.9f * rail, 104f), strokeWidth = 1f)
        drawLine(Brand.Line, Offset(96.9f, 104f), Offset(96.9f + 225.1f * rail, 104f), strokeWidth = 1f)
    }

    var activeIndex = -1
    glyphs.forEachIndexed { i, g ->
        val p = runtime.progress[i].floatValue
        if (p <= 0.002f) return@forEachIndexed
        val ink = smooth(p)
        val dry = smooth((p - 0.9f) / 0.1f)

        // measuring tick under each letter, fades as the letter inks in
        drawLine(
            color = Color(0x240C0C0D),
            start = Offset(g.spec.x, 104f),
            end = Offset(g.spec.x, 111f),
            strokeWidth = 1f,
            alpha = (1f - ink).coerceIn(0f, 1f)
        )

        val seg = segments[i]
        seg.reset()
        val total = g.measure.length
        if (total > 0f) {
            g.measure.getSegment(0f, total * p, seg, true)
            if (runtime.energy > 0.03f && p < 0.995f) {
                val trail = Path()
                g.measure.getSegment((total * (p - 0.06f)).coerceAtLeast(0f), total * p, trail, true)
                drawPath(
                    path = trail,
                    color = lerp(Brand.Gray, Brand.Accent, 0.45f),
                    style = Stroke(width = 2.4f, cap = StrokeCap.Round, join = StrokeJoin.Round),
                    alpha = (runtime.energy * 0.5f).coerceIn(0f, 1f)
                )
            }
            drawPath(
                path = seg,
                color = lerp(Brand.Gray, Brand.Ink, ink),
                style = Stroke(width = 1.9f, cap = StrokeCap.Round, join = StrokeJoin.Round)
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
            drawCircle(Brand.Accent, radius = 3.2f, center = tip)
            for (j in 1..3) {
                val pos = g.measure.getPosition(total * (p - j * 0.06f).coerceAtLeast(0f))
                drawCircle(Brand.Accent, radius = 3.2f - j * 0.7f, center = pos, alpha = 0.3f / j)
            }
        }
    }
}
