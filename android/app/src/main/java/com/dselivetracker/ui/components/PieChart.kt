package com.dselivetracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dselivetracker.ui.theme.Blue400
import com.dselivetracker.ui.theme.Blue500
import com.dselivetracker.ui.theme.Blue600
import com.dselivetracker.ui.theme.LossRed
import com.dselivetracker.ui.theme.ProfitGreen

data class PieSlice(
    val label: String,
    val value: Double,
    val color: Color
)

val pieColors = listOf(
    Blue500, ProfitGreen, LossRed, Blue600, Blue400,
    Color(0xFF8B5CF6), Color(0xFFEC4899), Color(0xFFF59E0B),
    Color(0xFF10B981), Color(0xFF6366F1), Color(0xFF14B8A6),
    Color(0xFFF97316)
)

@Composable
fun PieChart(
    slices: List<PieSlice>,
    modifier: Modifier = Modifier
) {
    if (slices.isEmpty()) return

    val total = slices.sumOf { it.value }.let { if (it <= 0) 1.0 else it }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Canvas(modifier = Modifier.size(200.dp)) {
                val diameter = size.minDimension
                val arcSize = Size(diameter, diameter)
                val topLeft = Offset(
                    (size.width - diameter) / 2f,
                    (size.height - diameter) / 2f
                )
                var startAngle = -90f
                slices.forEachIndexed { index, slice ->
                    val sweepAngle = (slice.value / total * 360).toFloat()
                    drawArc(
                        color = slice.color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        topLeft = topLeft,
                        size = arcSize
                    )
                    startAngle += sweepAngle

                    if (sweepAngle > 5f) {
                        val midAngle = Math.toRadians((startAngle - sweepAngle / 2).toDouble())
                        val labelRadius = diameter / 3f
                        val cx = size.width / 2f + (labelRadius * kotlin.math.cos(midAngle)).toFloat()
                        val cy = size.height / 2f + (labelRadius * kotlin.math.sin(midAngle)).toFloat()
                        drawContext.canvas.nativeCanvas.apply {
                            val paint = android.graphics.Paint().apply {
                                color = android.graphics.Color.WHITE
                                textSize = 28f
                                textAlign = android.graphics.Paint.Align.CENTER
                                isFakeBoldText = true
                            }
                            val pct = "%.0f%%".format(slice.value / total * 100)
                            drawText(pct, cx, cy + 10f, paint)
                        }
                    }
                }
                drawCircle(
                    color = MaterialTheme.colorScheme.surface,
                    radius = diameter * 0.3f,
                    center = Offset(size.width / 2f, size.height / 2f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        slices.forEach { slice ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Canvas(modifier = Modifier.size(10.dp)) {
                    drawCircle(slice.color)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = slice.label,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "\u09F3${formatBdt(slice.value)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
