package com.dselivetracker.ui.screens.news

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dselivetracker.DseApp
import com.dselivetracker.data.remote.QuotesParser.Top20Entry
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class NewsViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as DseApp
    private val stockRepo = app.stockRepository

    val top20: StateFlow<List<Top20Entry>> = stockRepo.top20
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
