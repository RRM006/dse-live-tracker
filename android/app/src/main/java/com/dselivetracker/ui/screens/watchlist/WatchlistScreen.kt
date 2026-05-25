package com.dselivetracker.ui.screens.watchlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dselivetracker.ui.components.MarketStatusBar
import com.dselivetracker.ui.components.StockCard
import com.dselivetracker.ui.theme.DarkHeader
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistScreen(
    viewModel: WatchlistViewModel = viewModel()
) {
    val watchlistStocks by viewModel.watchlistStocks.collectAsState()
    val symbol by viewModel.symbol.collectAsState()
    val targetPrice by viewModel.targetPrice.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val error by viewModel.error.collectAsState()
    val autocompleteSuggestions by viewModel.autocompleteSuggestions.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val ycpMap by viewModel.ycpMap.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.refresh()
        while (true) {
            delay(30000)
            viewModel.refresh()
        }
    }

    LaunchedEffect(snackbarMessage) {
        if (snackbarMessage != null) {
            snackbarHostState.showSnackbar(snackbarMessage!!)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            TopAppBar(
                title = {
                    Text(
                        text = "Watchlist",
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

            if (watchlistStocks.isEmpty()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Your watchlist is empty",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Add stocks to monitor their live prices",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(watchlistStocks, key = { it.id }) { stock ->
                        val targetHit = stock.targetPrice != null && stock.lastLtp != null &&
                                stock.lastLtp <= stock.targetPrice!!
                        StockCard(
                            symbol = stock.symbol,
                            buyPrice = 0.0,
                            quantity = 1,
                            lastLtp = stock.lastLtp,
                            direction = stock.direction,
                            showRemove = true,
                            onRemove = { viewModel.removeStock(stock.id) },
                            targetHit = targetHit,
                            ycp = ycpMap[stock.symbol]
                        )
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    viewModel.clearError()
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Box {
                    OutlinedTextField(
                        value = symbol,
                        onValueChange = { viewModel.updateSymbol(it) },
                        label = { Text("Stock Symbol") },
                        placeholder = { Text("e.g. GP") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors()
                    )
                    if (autocompleteSuggestions.isNotEmpty()) {
                        DropdownMenu(
                            expanded = true,
                            onDismissRequest = { viewModel.hideAutocomplete() },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            autocompleteSuggestions.forEach { suggestion ->
                                DropdownMenuItem(
                                    text = { Text(suggestion) },
                                    onClick = {
                                        viewModel.selectSymbol(suggestion)
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = targetPrice,
                    onValueChange = { viewModel.updateTargetPrice(it) },
                    label = { Text("Target Price (optional)") },
                    placeholder = { Text("e.g. 350") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.addStock() },
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    enabled = symbol.isNotBlank()
                ) {
                    Text("+ Add to Watchlist")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
