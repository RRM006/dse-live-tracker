package com.dselivetracker.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dselivetracker.ui.theme.LossRed
import com.dselivetracker.ui.theme.ProfitGreen

data class PortfolioSummary(
    val invested: Double,
    val currentValue: Double,
    val pnl: Double,
    val pnlPercent: Double,
    val stockCount: Int,
    val countWithData: Int,
    val realizedPnl: Double = 0.0,
    val unrealizedPnl: Double = 0.0,
    val totalCommission: Double = 0.0,
    val pieSlices: List<PieSlice> = emptyList()
)

@Composable
fun SummaryCard(
    summary: PortfolioSummary,
    modifier: Modifier = Modifier
) {
    val isProfit = summary.pnl >= 0
    val sign = if (isProfit) "+" else "-"
    val color = if (isProfit) ProfitGreen else LossRed

    val realizedIsProfit = summary.realizedPnl >= 0
    val unrealizedIsProfit = summary.unrealizedPnl >= 0

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Portfolio",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${summary.stockCount} stock${if (summary.stockCount > 1) "s" else ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = "Invested",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "৳${formatBdt(summary.invested)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Current${if (summary.countWithData < summary.stockCount) " (${summary.countWithData} updated)" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "৳${formatBdt(summary.currentValue)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Unrealized P/L",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(1f))
                val uSign = if (unrealizedIsProfit) "+" else "-"
                Text(
                    text = "$uSign৳${formatBdt(kotlin.math.abs(summary.unrealizedPnl))}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (unrealizedIsProfit) ProfitGreen else LossRed
                )
            }

            if (summary.realizedPnl != 0.0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Realized P/L",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    val rSign = if (realizedIsProfit) "+" else "-"
                    Text(
                        text = "$rSign৳${formatBdt(kotlin.math.abs(summary.realizedPnl))}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (realizedIsProfit) ProfitGreen else LossRed
                    )
                }
            }

            if (summary.totalCommission > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Commission",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "৳${formatBdt(summary.totalCommission)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total P/L",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "$sign৳${formatBdt(kotlin.math.abs(summary.pnl))} ($sign${"%.2f".format(kotlin.math.abs(summary.pnlPercent))}%)",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }
}

fun formatBdt(value: Double): String {
    val formatter = java.text.NumberFormat.getNumberInstance(java.util.Locale("en", "BD"))
    formatter.minimumFractionDigits = 2
    formatter.maximumFractionDigits = 2
    return formatter.format(kotlin.math.abs(value))
}
