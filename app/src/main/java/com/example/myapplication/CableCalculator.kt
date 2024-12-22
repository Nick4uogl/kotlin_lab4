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

@Composable
fun CableCalculator() {
    // State variables for user input
    var sm by remember { mutableStateOf("1300") }
    var ik by remember { mutableStateOf("2500") }
    var tf by remember { mutableStateOf("2.5") }

    // State variable for the results
    var results by remember { mutableStateOf<CableResults?>(null) }

    // Column to hold UI components vertically
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Input fields for Sm (MVA), Ik (A), and tf (s)
        CableInputField(value = sm, onValueChange = { sm = it }, label = "Sm (MVA)")
        CableInputField(value = ik, onValueChange = { ik = it }, label = "Ik (A)")
        CableInputField(value = tf, onValueChange = { tf = it }, label = "tf (s)")

        // Button to trigger calculation
        Button(
            onClick = {
                results = calculateCableParameters(
                    sm.toDoubleOrNull() ?: 0.0,
                    ik.toDoubleOrNull() ?: 0.0,
                    tf.toDoubleOrNull() ?: 0.0
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Calculate")
        }

        // Display the results if available
        results?.let { res ->
            ResultText("Normal mode current: ${res.normalCurrent} A")
            ResultText("Post-emergency current: ${res.postEmergencyCurrent} A")
            ResultText("Economic cross-section: ${res.economicCrossSection} mm²")
            ResultText("Minimum cross-section: ${res.minimumCrossSection} mm²")
        }
    }
}

// Composable function for the input fields
@Composable
fun CableInputField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth()
    )
}

// Composable function to display the results text
@Composable
fun ResultText(text: String) {
    Text(text)
}

// Calculator function to compute cable parameters
fun calculateCableParameters(sm: Double, ik: Double, tf: Double): CableResults {
    val im = (sm / 2) / (kotlin.math.sqrt(3.0) * 10)
    val imPa = 2 * im
    val sEk = im / 1.4
    val sVsS = (ik * kotlin.math.sqrt(tf)) / 92

    return CableResults(
        normalCurrent = String.format("%.1f", im),
        postEmergencyCurrent = String.format("%.0f", imPa),
        economicCrossSection = String.format("%.1f", sEk),
        minimumCrossSection = String.format("%.0f", sVsS)
    )
}

// Data class to hold the results
data class CableResults(
    val normalCurrent: String,
    val postEmergencyCurrent: String,
    val economicCrossSection: String,
    val minimumCrossSection: String
)
