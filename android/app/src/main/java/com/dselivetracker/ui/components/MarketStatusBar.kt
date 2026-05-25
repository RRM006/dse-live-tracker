package com.dselivetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.dselivetracker.ui.theme.LossRed
import com.dselivetracker.ui.theme.ProfitGreen
import java.util.Calendar

@Composable
fun MarketStatusBar(
    modifier: Modifier = Modifier
) {
    val isOpen = remember {
        val now = Calendar.getInstance()
        val day = now.get(Calendar.DAY_OF_WEEK)
        val hours = now.get(Calendar.HOUR_OF_DAY)
        val mins = now.get(Calendar.MINUTE)
        val totalMins = hours * 60 + mins

        if (day >= Calendar.SUNDAY && day <= Calendar.THURSDAY) {
            val open = 10 * 60
            val close = 14 * 60 + 30
            totalMins >= open && totalMins < close
        } else false
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(if (isOpen) ProfitGreen else LossRed)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = if (isOpen) "Open" else "Closed",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
