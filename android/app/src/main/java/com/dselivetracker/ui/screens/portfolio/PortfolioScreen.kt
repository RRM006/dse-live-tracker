package com.dselivetracker.ui.screens.portfolio

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dselivetracker.ui.components.AddStockForm
import com.dselivetracker.ui.components.MarketStatusBar
import com.dselivetracker.ui.components.SummaryCard
import com.dselivetracker.ui.theme.DarkHeader
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioScreen(
    onNavigateToSearch: (String, String, String) -> Unit = { _, _, _ -> },
    viewModel: PortfolioViewModel = viewModel()
) {
    val summary by viewModel.summary.collectAsState()
    val symbol by viewModel.symbol.collectAsState()
    val buyPrice by viewModel.buyPrice.collectAsState()
    val quantity by viewModel.quantity.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val lastUpdated by viewModel.lastUpdated.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refresh()
        while (true) {
            delay(30000)
            viewModel.refresh()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = "DSE Tracker",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            },
            actions = {
                IconButton(onClick = {
                    if (!isRefreshing) viewModel.refresh()
                }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                MarketStatusBar(
                    modifier = Modifier.padding(end = 12.dp)
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = DarkHeader
            )
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            if (summary != null) {
                Spacer(modifier = Modifier.height(12.dp))
                SummaryCard(summary = summary!!)
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (error != null) {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                viewModel.clearError()
            }

            AddStockForm(
                symbol = symbol,
                onSymbolChange = { viewModel.updateSymbol(it) },
                buyPrice = buyPrice,
                onBuyPriceChange = { viewModel.updateBuyPrice(it) },
                quantity = quantity,
                onQuantityChange = { viewModel.updateQuantity(it) },
                onAdd = { viewModel.addStock() }
            )

            if (lastUpdated != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Last updated: ${lastUpdated!!}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
