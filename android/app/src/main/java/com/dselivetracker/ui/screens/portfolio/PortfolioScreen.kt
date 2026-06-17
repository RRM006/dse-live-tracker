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
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dselivetracker.ui.components.AddStockForm
import com.dselivetracker.ui.components.MarketStatusBar
import com.dselivetracker.ui.components.PieChart
import com.dselivetracker.ui.components.SummaryCard
import com.dselivetracker.ui.theme.DarkHeader
import com.dselivetracker.utils.DateUtils
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioScreen(
    onNavigateToSearch: (String) -> Unit = {},
    viewModel: PortfolioViewModel = viewModel()
) {
    val summary by viewModel.summary.collectAsState()
    val symbol by viewModel.symbol.collectAsState()
    val buyPrice by viewModel.buyPrice.collectAsState()
    val quantity by viewModel.quantity.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val lastUpdated by viewModel.lastUpdated.collectAsState()
    val error by viewModel.error.collectAsState()
    val marketStatus by viewModel.marketStatus.collectAsState()
    val autocompleteSuggestions by viewModel.autocompleteSuggestions.collectAsState()
    val buyDate by viewModel.buyDate.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.refresh()
        while (true) {
            delay(30000)
            viewModel.refresh()
        }
    }

    LaunchedEffect(lastUpdated) {
        if (lastUpdated != null) {
            val message = if (lastUpdated!!.startsWith("Update failed"))
                "Refresh failed \u2014 showing cached data"
            else
                "Data refreshed at ${lastUpdated!!.substringAfter("at ")}"
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
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
                        status = marketStatus,
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
                    if (summary!!.pieSlices.size >= 2) {
                        Spacer(modifier = Modifier.height(12.dp))
                        PieChart(slices = summary!!.pieSlices)
                    }
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

                var showBuyDatePicker by remember { mutableStateOf(false) }
                val buyDatePickerState = rememberDatePickerState(initialSelectedDateMillis = buyDate ?: System.currentTimeMillis())

                if (showBuyDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { showBuyDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                buyDatePickerState.selectedDateMillis?.let { viewModel.updateBuyDate(it) }
                                showBuyDatePicker = false
                            }) { Text("OK") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showBuyDatePicker = false }) { Text("Cancel") }
                        }
                    ) {
                        DatePicker(state = buyDatePickerState)
                    }
                }

                AddStockForm(
                    symbol = symbol,
                    onSymbolChange = { viewModel.updateSymbol(it) },
                    buyPrice = buyPrice,
                    onBuyPriceChange = { viewModel.updateBuyPrice(it) },
                    quantity = quantity,
                    onQuantityChange = { viewModel.updateQuantity(it) },
                    onAdd = { viewModel.addStock() },
                    autocompleteSuggestions = autocompleteSuggestions,
                    onSelectSuggestion = { viewModel.selectSymbol(it) },
                    onDismissAutocomplete = { viewModel.hideAutocomplete() }
                )
                Spacer(modifier = Modifier.height(4.dp))
                TextButton(onClick = { showBuyDatePicker = true }) {
                    val dateStr = if (buyDate != null) DateUtils.formatTimestamp(buyDate!!) else "Add Buy Date (optional)"
                    Text(
                        text = if (buyDate != null) "Buy Date: $dateStr" else "Add Buy Date (optional)",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

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
}
