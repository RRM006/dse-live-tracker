package com.dselivetracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dselivetracker.ui.theme.LossRed
import com.dselivetracker.ui.theme.ProfitGreen
import com.dselivetracker.ui.theme.TextMuted

val SkyBlue = Color(0xFF00BFFF)
val DirectionUp = Color(0xFF4CAF50)
val DirectionDown = Color(0xFFF44336)

@Composable
fun StockCard(
    symbol: String,
    buyPrice: Double,
    quantity: Int,
    lastLtp: Double?,
    direction: String?,
    modifier: Modifier = Modifier,
    showRemove: Boolean = false,
    onRemove: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    targetHit: Boolean = false,
    ycp: Double? = null
) {
    val pnl = if (lastLtp != null) (lastLtp - buyPrice) * quantity else null
    val pct = if (lastLtp != null && buyPrice > 0) ((lastLtp - buyPrice) / buyPrice) * 100 else null
    val isProfit = if (pnl != null) pnl >= 0 else null
    val color = when {
        isProfit == null -> MaterialTheme.colorScheme.onSurface
        isProfit -> ProfitGreen
        else -> LossRed
    }

    val arrow = if (lastLtp != null && ycp != null) {
        if (lastLtp > ycp) "\u2191"
        else if (lastLtp < ycp) "\u2193"
        else "\u2192"
    } else ""

    val arrowColor = when (arrow) {
        "\u2191" -> DirectionUp
        "\u2193" -> DirectionDown
        else -> TextMuted
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (targetHit) Modifier.drawBehind {
                drawRect(
                    color = SkyBlue,
                    size = Size(6f, size.height)
                )
            } else Modifier)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
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
                    text = symbol,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (buyPrice > 0 && pnl != null) {
                    Text(
                        text = "${if (isProfit == true) "+" else ""}\u09F3${formatBdt(pnl)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
                if (arrow.isNotEmpty()) {
                    Spacer(modifier = Modifier.padding(start = 2.dp))
                    Text(
                        text = " $arrow",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = arrowColor
                    )
                }
                if (showRemove && onRemove != null) {
                    Spacer(modifier = Modifier.padding(start = 4.dp))
                    Text(
                        text = "\u2715",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable { onRemove() }
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LTP: ${if (lastLtp != null) "\u09F3${formatBdt(lastLtp)}" else "Awaiting data..."}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (targetHit) SkyBlue else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(1f))
                if (pct != null) {
                    Text(
                        text = "${if (isProfit == true) "+" else ""}${"%.2f".format(kotlin.math.abs(pct))}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = color
                    )
                }
            }
            if (ycp != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "YCP: \u09F3${formatBdt(ycp)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (buyPrice > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Buy: \u09F3${formatBdt(buyPrice)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = " \u00d7 $quantity",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = " = \u09F3${formatBdt(buyPrice * quantity)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (targetHit) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "\u2705 BUY SIGNAL",
                    style = MaterialTheme.typography.labelSmall,
                    color = ProfitGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
