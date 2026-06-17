package com.dselivetracker.ui.screens.holdings

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dselivetracker.ui.components.StockCard
import com.dselivetracker.ui.theme.DarkHeader
import com.dselivetracker.utils.DateUtils
import androidx.compose.foundation.text.KeyboardOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoldingsScreen(
    onNavigateToSearch: (String) -> Unit = {},
    onNavigateToTradeHistory: () -> Unit = {},
    viewModel: HoldingsViewModel = viewModel()
) {
    val sortedStocks by viewModel.sortedStocks.collectAsState()
    val sortMode by viewModel.sortMode.collectAsState()
    val pendingRemove by viewModel.pendingRemove.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val ycpMap by viewModel.ycpMap.collectAsState()
    val stockQuotes by viewModel.stockQuotes.collectAsState()
    val sellStockId by viewModel.sellStockId.collectAsState()
    val sellPrice by viewModel.sellPrice.collectAsState()
    val sellDate by viewModel.sellDate.collectAsState()

    var showSortMenu by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    if (sellStockId != null) {
        val stock = sortedStocks.find { it.id == sellStockId }
        var showDatePicker by remember { mutableStateOf(false) }
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = sellDate ?: System.currentTimeMillis())

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { viewModel.updateSellDate(it) }
                        showDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        AlertDialog(
            onDismissRequest = { viewModel.hideSellDialog() },
            title = { Text("Sell ${stock?.symbol ?: ""}") },
            text = {
                Column {
                    Text("Enter the sell price to sell all ${stock?.quantity ?: 0} shares.")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = sellPrice,
                        onValueChange = { viewModel.updateSellPrice(it) },
                        label = { Text("Sell Price (BDT)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { showDatePicker = true }) {
                        val dateStr = if (sellDate != null) DateUtils.formatTimestamp(sellDate!!) else "Pick Sell Date"
                        Text(if (sellDate != null) "Sell Date: $dateStr" else "Pick Sell Date")
                    }
                    if (stock != null) {
                        val quote = stockQuotes[stock.symbol]
                        val category = quote?.category
                        val settlementDate = if (stock.buyDate != null && !category.isNullOrEmpty())
                            DateUtils.calculateSettlementDate(stock.buyDate, category) else null
                        val currentSellDate = sellDate
                        val sellDateError = if (currentSellDate != null && settlementDate != null) {
                            val sellLocal = java.time.LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(currentSellDate), java.time.ZoneId.of("Asia/Dhaka")).toLocalDate()
                            if (sellLocal.isBefore(settlementDate)) settlementDate else null
                        } else null
                        val estValue = sellPrice.toDoubleOrNull()?.times(stock.quantity) ?: 0.0
                        val brokerFee = estValue * 0.004
                        val dseFee = estValue * 0.00025
                        val aitFee = estValue * 0.0005
                        val estCommission = brokerFee + dseFee + aitFee
                        val buyCommission = stock.commission
                        val estPnl = sellPrice.toDoubleOrNull()?.let { price ->
                            (price - stock.buyPrice) * stock.quantity - buyCommission - estCommission
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Est. value: ৳${com.dselivetracker.ui.components.formatBdt(estValue)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Broker (0.40%): ৳${com.dselivetracker.ui.components.formatBdt(brokerFee)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "DSE (0.025%): ৳${com.dselivetracker.ui.components.formatBdt(dseFee)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "AIT (0.05%): ৳${com.dselivetracker.ui.components.formatBdt(aitFee)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (estPnl != null) {
                            val pnlColor = if (estPnl >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            Text(
                                text = "Est. P&L: ${if (estPnl >= 0) "+" else "-"}৳${com.dselivetracker.ui.components.formatBdt(estPnl)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = pnlColor
                            )
                        }
                        if (sellDateError != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Not yet matured! Earliest sell: ${sellDateError.format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy"))}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            },
            confirmButton = {
                val quote = if (stock != null) stockQuotes[stock.symbol] else null
                val category = quote?.category
                val settlementDate = if (stock != null && stock.buyDate != null && !category.isNullOrEmpty())
                    DateUtils.calculateSettlementDate(stock.buyDate, category) else null
                val currentSellDate = sellDate
                val isNotMatured = if (currentSellDate != null && settlementDate != null) {
                    val sellLocal = java.time.LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(currentSellDate), java.time.ZoneId.of("Asia/Dhaka")).toLocalDate()
                    sellLocal.isBefore(settlementDate)
                } else false

                TextButton(onClick = { viewModel.confirmSell() }, enabled = sellPrice.toDoubleOrNull() != null && !isNotMatured) {
                    Text("Sell")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideSellDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    LaunchedEffect(pendingRemove) {
        if (pendingRemove != null) {
            val result = snackbarHostState.showSnackbar(
                message = "${pendingRemove!!.symbol} removed",
                actionLabel = "Undo",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoRemove()
            } else {
                viewModel.clearPendingRemove()
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    actionOnNewLine = false,
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = "Holdings",
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkHeader
                )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sort:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { showSortMenu = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text(sortMode.label, style = MaterialTheme.typography.bodySmall)
                }
                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    SortMode.entries.forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(mode.label) },
                            onClick = {
                                viewModel.setSortMode(mode)
                                showSortMenu = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${sortedStocks.size} stock${if (sortedStocks.size != 1) "s" else ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onNavigateToTradeHistory,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.height(32.dp),
                    contentPadding = ButtonDefaults.TextButtonContentPadding
                ) {
                    Text("History", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary)
                }
            }

            if (sortedStocks.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "No holdings yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Go to Portfolio tab to add your first stock",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sortedStocks, key = { it.id }) { stock ->
                        val quote = stockQuotes[stock.symbol]
                        StockCard(
                            symbol = stock.symbol,
                            buyPrice = stock.buyPrice,
                            quantity = stock.quantity,
                            lastLtp = stock.lastLtp,
                            direction = stock.direction,
                            showRemove = true,
                            onRemove = { viewModel.removeStock(stock.id) },
                            showSell = true,
                            onSell = { viewModel.showSellDialog(stock.id) },
                            onClick = {
                                onNavigateToSearch(stock.symbol)
                            },
                            ycp = ycpMap[stock.symbol],
                            high = quote?.high,
                            low = quote?.low,
                            closep = quote?.closep,
                            change = quote?.change,
                            upperLimit = quote?.upperLimit,
                            lowerLimit = quote?.lowerLimit,
                            category = quote?.category,
                            buyDate = stock.buyDate,
                            lastUpdated = stock.lastUpdated
                        )
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }
        }
    }
}
