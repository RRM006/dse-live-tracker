package com.dselivetracker.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun AddStockForm(
    symbol: String,
    onSymbolChange: (String) -> Unit,
    buyPrice: String,
    onBuyPriceChange: (String) -> Unit,
    quantity: String,
    onQuantityChange: (String) -> Unit,
    onAdd: () -> Unit,
    targetPrice: String? = null,
    onTargetPriceChange: ((String) -> Unit)? = null,
    buttonText: String = "+ Add to Portfolio",
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(12.dp)) {
        Text(
            text = if (targetPrice != null) "Track Stock" else "Add Stock",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = symbol,
            onValueChange = { onSymbolChange(it.uppercase()) },
            label = { Text("Stock Symbol") },
            placeholder = { Text("e.g. GP") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            colors = OutlinedTextFieldDefaults.colors()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = buyPrice,
            onValueChange = onBuyPriceChange,
            label = { Text("Buy Price (BDT)") },
            placeholder = { Text("e.g. 300") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
            colors = OutlinedTextFieldDefaults.colors()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = quantity,
            onValueChange = onQuantityChange,
            label = { Text("Quantity") },
            placeholder = { Text("e.g. 100") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onAdd() }),
            colors = OutlinedTextFieldDefaults.colors()
        )

        if (targetPrice != null && onTargetPriceChange != null) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = targetPrice,
                onValueChange = onTargetPriceChange,
                label = { Text("Target Price (optional)") },
                placeholder = { Text("e.g. 350") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onAdd() }),
                colors = OutlinedTextFieldDefaults.colors()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onAdd,
            modifier = Modifier.fillMaxWidth().height(42.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            enabled = symbol.isNotBlank() && buyPrice.isNotBlank()
        ) {
            Text(buttonText)
        }
    }
}
