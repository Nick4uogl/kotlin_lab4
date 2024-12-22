package com.example.myapplication

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlin.math.pow
import kotlin.math.sqrt

@Composable
fun ShortCircuitCalculator() {
    // State for user input
    var shortCircuitPower by remember { mutableStateOf("200") }
    // State for calculated results
    var results by remember { mutableStateOf<ShortCircuitResults?>(null) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Input field for short-circuit power (Sk)
        ShortCircuitInputField(
            value = shortCircuitPower,
            onValueChange = { shortCircuitPower = it },
            label = "Short-Circuit Power (Sk) [MVA]"
        )

        // Button to trigger calculation
        Button(
            onClick = {
                val skValue = shortCircuitPower.toDoubleOrNull() ?: 0.0
                results = calculateShortCircuitParameters(skValue)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Calculate")
        }

        // Display results if available
        results?.let { result ->
            DisplayResult(label = "Xc", value = result.reactorImpedance)
            DisplayResult(label = "Xt", value = result.transformerImpedance)
            DisplayResult(label = "Total Resistance", value = result.totalImpedance)
            DisplayResult(label = "Initial Three-Phase SC Current", value = result.initialShortCircuitCurrent)
        }
    }
}

@Composable
fun ShortCircuitInputField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun DisplayResult(label: String, value: String) {
    Text("$label: $value")
}

fun calculateShortCircuitParameters(sk: Double): ShortCircuitResults {
    // Reactor impedance calculation
    val xc = 10.5.pow(2) / sk
    // Transformer impedance calculation
    val xt = (10.5 / 100) * (10.5.pow(2) / 6.3)
    // Total impedance
    val totalImpedance = xc + xt
    // Initial three-phase short-circuit current
    val initialSCCurrent = 10.5 / (sqrt(3.0) * totalImpedance)

    return ShortCircuitResults(
        reactorImpedance = String.format("%.2f", xc),
        transformerImpedance = String.format("%.2f", xt),
        totalImpedance = String.format("%.2f", totalImpedance),
        initialShortCircuitCurrent = String.format("%.1f", initialSCCurrent)
    )
}

data class ShortCircuitResults(
    val reactorImpedance: String,
    val transformerImpedance: String,
    val totalImpedance: String,
    val initialShortCircuitCurrent: String
)
