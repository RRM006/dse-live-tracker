package com.dselivetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dselivetracker.ui.theme.LossRed
import com.dselivetracker.ui.theme.ProfitGreen
import com.dselivetracker.ui.theme.TextMuted
import com.dselivetracker.utils.DateUtils
import com.dselivetracker.utils.StockUtils

val BuySignalGreen = Color(0xFF1B5E20)
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
    showSell: Boolean = false,
    onSell: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    targetHit: Boolean = false,
    targetPrice: Double? = null,
    ycp: Double? = null,
    high: Double? = null,
    low: Double? = null,
    closep: Double? = null,
    change: Double? = null,
    upperLimit: Double? = null,
    lowerLimit: Double? = null,
    category: String? = null,
    buyDate: Long? = null,
    lastUpdated: Long? = null,
    totalQty: Int? = null
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
            .then(
                if (targetHit) Modifier.background(
                    BuySignalGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) else Modifier
            )
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        colors = CardDefaults.cardColors(
            containerColor = if (targetHit)
                BuySignalGreen.copy(alpha = 0.15f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = symbol,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (!category.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = category,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier
                                    .background(
                                        when (category) {
                                            "A" -> ProfitGreen.copy(alpha = 0.8f)
                                            "B" -> MaterialTheme.colorScheme.primary
                                            "Z" -> LossRed.copy(alpha = 0.8f)
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        if (buyDate != null && !category.isNullOrEmpty() && DateUtils.isMature(buyDate, category)) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "\u2713 Matured",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier
                                    .background(ProfitGreen, shape = RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    val stockName = StockUtils.getStockName(symbol)
                    if (stockName != symbol) {
                        Text(
                            text = stockName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
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
                if (showSell && onSell != null) {
                    Spacer(modifier = Modifier.padding(start = 4.dp))
                    Button(
                        onClick = onSell,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.height(28.dp),
                        contentPadding = ButtonDefaults.TextButtonContentPadding
                    ) {
                        Text("Sell", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onError)
                    }
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
                    color = if (targetHit) ProfitGreen else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(1f))
                if (change != null) {
                    val changeColor = if (change >= 0) ProfitGreen else LossRed
                    Text(
                        text = "${if (change >= 0) "+" else ""}\u09F3${formatBdt(change)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = changeColor
                    )
                }
                if (pct != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "(${if (isProfit == true) "+" else ""}${"%.2f".format(kotlin.math.abs(pct))}%)",
                        style = MaterialTheme.typography.bodySmall,
                        color = color
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (high != null) {
                    Text(
                        text = "H: \u09F3${formatBdt(high)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (high != null && low != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                }
                if (low != null) {
                    Text(
                        text = "L: \u09F3${formatBdt(low)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (closep != null) {
                Text(
                    text = "CLOSEP: \u09F3${formatBdt(closep)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (ycp != null) {
                Text(
                    text = "YCP: \u09F3${formatBdt(ycp)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (upperLimit != null && upperLimit > 0) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Circuit: \u09F3${formatBdt(lowerLimit ?: 0.0)} - \u09F3${formatBdt(upperLimit)}",
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
                if (totalQty != null && totalQty > quantity) {
                    Text(
                        text = "Total: $totalQty shares",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (buyDate != null && !category.isNullOrEmpty()) {
                    val settlementStr = DateUtils.formatSettlementDate(buyDate, category)
                    if (settlementStr != null) {
                        Text(
                            text = "Settlement: $settlementStr",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            if (targetPrice != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Target: \u09F3${formatBdt(targetPrice)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
            if (lastLtp != null && lastUpdated != null) {
                val timeStr = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(lastUpdated))
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Data: $timeStr",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }
    }
}
