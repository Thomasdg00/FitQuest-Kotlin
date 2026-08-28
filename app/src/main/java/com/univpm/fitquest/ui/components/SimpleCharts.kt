package com.univpm.fitquest.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.univpm.fitquest.util.FormatUtils
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.round

data class BarChartItem(
    val label: String,
    val value: Double,
)

data class LineChartPoint(
    val x: Double,
    val y: Double,
)

@Composable
fun SimpleBarChart(
    items: List<BarChartItem>,
    valueLabel: (Double) -> String,
    modifier: Modifier = Modifier,
) {
    val maxValue = items.maxOfOrNull { it.value }?.coerceAtLeast(0.0) ?: 0.0
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        items.forEach { item ->
            val fraction = if (maxValue <= 0.0) 0f else (item.value / maxValue).toFloat().coerceIn(0f, 1f)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = item.label, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = valueLabel(item.value),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.primary),
                    )
                }
            }
        }
    }
}

@Composable
fun SimpleLineChart(
    points: List<LineChartPoint>,
    emptyText: String,
    semanticDescription: String,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    xAxisLabel: String? = null,
    yAxisLabel: String? = null,
    xValueLabel: ((Double) -> String)? = null,
    yValueLabel: ((Double) -> String)? = null,
) {
    if (points.size < 2) {
        Text(
            text = emptyText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier.padding(vertical = 8.dp),
        )
        return
    }

    val xTicks = remember(points) {
        buildChartAxisTicks(
            minValue = points.minOf { it.x },
            maxValue = points.maxOf { it.x },
        )
    }
    val yTicks = remember(points) {
        buildChartAxisTicks(
            minValue = points.minOf { it.y },
            maxValue = points.maxOf { it.y },
        )
    }
    val minX = xTicks.first()
    val maxX = xTicks.last()
    val minY = yTicks.first()
    val maxY = yTicks.last()
    val xRange = (maxX - minX).takeIf { it > 0.0 } ?: 1.0
    val yRange = (maxY - minY).takeIf { it > 0.0 } ?: 1.0
    val axisColor = MaterialTheme.colorScheme.outlineVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val resolvedXValueLabel = xValueLabel ?: FormatUtils::formatOneDecimal
    val resolvedYValueLabel = yValueLabel ?: FormatUtils::formatOneDecimal

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        yAxisLabel?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelSmall,
                color = labelColor,
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .width(52.dp)
                    .height(168.dp),
            ) {
                yTicks.asReversed().forEach { tick ->
                    Text(
                        text = resolvedYValueLabel(tick),
                        style = MaterialTheme.typography.labelSmall,
                        color = labelColor,
                    )
                }
            }
            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .height(168.dp)
                    .semantics {
                        contentDescription = semanticDescription
                        role = Role.Image
                    },
            ) {
                val chartPoints = points.map { point ->
                    val x = ((point.x - minX) / xRange).toFloat().coerceIn(0f, 1f) * size.width
                    val y = size.height -
                        (((point.y - minY) / yRange).toFloat().coerceIn(0f, 1f) * size.height)
                    Offset(x, y)
                }

                yTicks.forEach { tick ->
                    val y = size.height - (((tick - minY) / yRange).toFloat() * size.height)
                    drawLine(
                        color = axisColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f,
                    )
                }
                xTicks.forEach { tick ->
                    val x = ((tick - minX) / xRange).toFloat() * size.width
                    drawLine(
                        color = axisColor,
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 1f,
                    )
                }
                drawLine(
                    color = axisColor,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 2f,
                )
                drawLine(
                    color = axisColor,
                    start = Offset(0f, 0f),
                    end = Offset(0f, size.height),
                    strokeWidth = 2f,
                )
                chartPoints.zipWithNext().forEach { (start, end) ->
                    drawLine(
                        color = lineColor,
                        start = start,
                        end = end,
                        strokeWidth = 5f,
                        cap = StrokeCap.Round,
                    )
                }
                chartPoints.forEach { point ->
                    drawCircle(color = lineColor, radius = 5f, center = point)
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.width(60.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.weight(1f),
            ) {
                xTicks.forEach { tick ->
                    Text(
                        text = resolvedXValueLabel(tick),
                        style = MaterialTheme.typography.labelSmall,
                        color = labelColor,
                    )
                }
            }
        }
        xAxisLabel?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelSmall,
                color = labelColor,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
    }
}


internal fun buildChartAxisTicks(
    minValue: Double,
    maxValue: Double,
    preferredTickCount: Int = 4,
): List<Double> {
    val tickCount = preferredTickCount.coerceAtLeast(2)
    val adjustedMin = if (minValue == maxValue) minValue - 1.0 else minValue
    val adjustedMax = if (minValue == maxValue) maxValue + 1.0 else maxValue
    val range = (adjustedMax - adjustedMin).takeIf { it > 0.0 } ?: 1.0
    val step = niceChartStep(range / (tickCount - 1))
    val first = floor(adjustedMin / step) * step
    val last = ceil(adjustedMax / step) * step
    val ticks = mutableListOf<Double>()
    var value = first
    while (value <= last + (step / 2.0)) {
        ticks += normalizeChartTick(value)
        value += step
    }
    return ticks
}

private fun niceChartStep(rawStep: Double): Double {
    val exponent = floor(log10(rawStep.coerceAtLeast(0.000001)))
    val magnitude = 10.0.pow(exponent)
    val normalized = rawStep / magnitude
    val niceNormalized = when {
        normalized <= 1.0 -> 1.0
        normalized <= 2.0 -> 2.0
        normalized <= 5.0 -> 5.0
        else -> 10.0
    }
    return niceNormalized * magnitude
}

private fun normalizeChartTick(value: Double): Double =
    round(value * 1_000_000.0) / 1_000_000.0


fun formatChartPace(value: Double): String {
    val totalSeconds = (value * 60.0).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.ITALIAN, "%d:%02d", minutes, seconds)
}

fun formatChartMeters(value: Double): String = String.format(Locale.ITALIAN, "%.0f", value)
