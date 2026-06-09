package com.dselivetracker.ui.screens.news

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dselivetracker.ui.components.formatBdt
import com.dselivetracker.ui.theme.DarkHeader
import com.dselivetracker.ui.theme.LossRed
import com.dselivetracker.ui.theme.ProfitGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    viewModel: NewsViewModel = viewModel()
) {
    val top20 by viewModel.top20.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Top 20",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkHeader
                )
            )
        }
    ) { innerPadding ->
        if (top20.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No data available",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Pull to refresh or check your connection",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("#", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.width(24.dp))
                        Text("Symbol", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("LTP", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.width(72.dp), textAlign = TextAlign.End)
                        Text("Change%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.width(64.dp), textAlign = TextAlign.End)
                        Text("Volume", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.width(72.dp), textAlign = TextAlign.End)
                    }
                }
                itemsIndexed(top20) { index, entry ->
                    val isProfit = entry.pctChange >= 0
                    val color = if (isProfit) ProfitGreen else LossRed
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${entry.rank}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.width(24.dp),
                                fontWeight = FontWeight.Bold
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = entry.symbol,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "\u09F3${formatBdt(entry.ltp)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "\u09F3${formatBdt(entry.ltp)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.width(72.dp),
                                textAlign = TextAlign.End
                            )
                            Text(
                                text = "${if (isProfit) "+" else ""}${"%.2f".format(kotlin.math.abs(entry.pctChange))}%",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = color,
                                modifier = Modifier.width(64.dp),
                                textAlign = TextAlign.End
                            )
                            Text(
                                text = formatVolume(entry.volume),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.width(72.dp),
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
        }
    }
}

private fun formatVolume(volume: Long): String {
    return when {
        volume >= 1_000_000 -> "${"%.1f".format(volume / 1_000_000.0)}M"
        volume >= 1_000 -> "${"%.1f".format(volume / 1_000.0)}K"
        else -> volume.toString()
    }
}
