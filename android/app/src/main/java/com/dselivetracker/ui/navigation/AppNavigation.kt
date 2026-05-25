package com.dselivetracker.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dselivetracker.ui.screens.holdings.HoldingsScreen
import com.dselivetracker.ui.screens.portfolio.PortfolioScreen
import com.dselivetracker.ui.screens.search.SearchScreen
import com.dselivetracker.ui.screens.watchlist.WatchlistScreen

sealed class Screen(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Portfolio : Screen("portfolio", "Portfolio", Icons.Filled.Wallet, Icons.Outlined.Wallet)
    data object Holdings : Screen("holdings", "Holdings", Icons.Filled.AccountBalance, Icons.Outlined.AccountBalance)
    data object Watchlist : Screen("watchlist", "Watchlist", Icons.Filled.Star, Icons.Outlined.Star)
    data object Search : Screen("search", "Search", Icons.Filled.Search, Icons.Outlined.Search) {
        const val ROUTE_PATTERN = "search?s={symbol}&b={buyPrice}&q={quantity}"
        fun createRoute(symbol: String = "", buyPrice: String = "", quantity: String = ""): String {
            return "search?s=$symbol&b=$buyPrice&q=$quantity"
        }
    }
}

val bottomNavScreens = listOf(Screen.Portfolio, Screen.Holdings, Screen.Watchlist, Screen.Search)

@Composable
fun DseNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavScreens.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any {
                        it.route == screen.route || it.route == Screen.Search.ROUTE_PATTERN
                    } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            val route = if (screen is Screen.Search) {
                                Screen.Search.createRoute()
                            } else {
                                screen.route
                            }
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = screen.label
                            )
                        },
                        label = { Text(screen.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Portfolio.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Portfolio.route) {
                PortfolioScreen(
                    onNavigateToSearch = { symbol, buyPrice, qty ->
                        navController.navigate(Screen.Search.createRoute(symbol, buyPrice, qty))
                    }
                )
            }
            composable(Screen.Holdings.route) {
                HoldingsScreen(
                    onNavigateToSearch = { symbol, buyPrice, qty ->
                        navController.navigate(Screen.Search.createRoute(symbol, buyPrice, qty))
                    }
                )
            }
            composable(Screen.Watchlist.route) {
                WatchlistScreen()
            }
            composable(
                route = Screen.Search.ROUTE_PATTERN,
                arguments = listOf(
                    navArgument("symbol") { type = NavType.StringType; defaultValue = "" },
                    navArgument("buyPrice") { type = NavType.StringType; defaultValue = "" },
                    navArgument("quantity") { type = NavType.StringType; defaultValue = "" }
                )
            ) { backStackEntry ->
                val symbol = backStackEntry.arguments?.getString("symbol") ?: ""
                val buyPrice = backStackEntry.arguments?.getString("buyPrice") ?: ""
                val quantity = backStackEntry.arguments?.getString("quantity") ?: ""
                SearchScreen(
                    initialSymbol = symbol,
                    initialBuyPrice = buyPrice,
                    initialQuantity = quantity
                )
            }
        }
    }
}
