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
fun PowerNetworkCalculator() {
    var rsnInput by remember { mutableStateOf("10.65") }
    var xsnInput by remember { mutableStateOf("24.02") }
    var rsnMinInput by remember { mutableStateOf("34.88") }
    var xsnMinInput by remember { mutableStateOf("65.68") }

    var results by remember { mutableStateOf<NetworkResults?>(null) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        InputField(label = "Rsn (Ω)", value = rsnInput) { rsnInput = it }
        InputField(label = "Xsn (Ω)", value = xsnInput) { xsnInput = it }
        InputField(label = "Rsn min (Ω)", value = rsnMinInput) { rsnMinInput = it }
        InputField(label = "Xsn min (Ω)", value = xsnMinInput) { xsnMinInput = it }

        Button(
            onClick = {
                results = calculateNetwork(
                    rsn = rsnInput.toDoubleOrNull() ?: 0.0,
                    xsn = xsnInput.toDoubleOrNull() ?: 0.0,
                    rsnMin = rsnMinInput.toDoubleOrNull() ?: 0.0,
                    xsnMin = xsnMinInput.toDoubleOrNull() ?: 0.0
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Calculate")
        }

        results?.let { displayResults(it) }
    }
}

@Composable
fun InputField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun displayResults(results: NetworkResults) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("110kV bus SC currents (normal/minimum):")
        Text("Three-phase: ${results.iSh3}/${results.iSh3Min} A")
        Text("Two-phase: ${results.iSh2}/${results.iSh2Min} A")

        Text("\n10kV bus SC currents (normal/minimum):")
        Text("Three-phase: ${results.iShN3}/${results.iShN3Min} A")
        Text("Two-phase: ${results.iShN2}/${results.iShN2Min} A")

        Text("\nPoint 10 SC currents (normal/minimum):")
        Text("Three-phase: ${results.iLN3}/${results.iLN3Min} A")
        Text("Two-phase: ${results.iLN2}/${results.iLN2Min} A")
    }
}

fun calculateNetwork(rsn: Double, xsn: Double, rsnMin: Double, xsnMin: Double): NetworkResults {
    val xt = calculateTransformerReactance()
    val normal = calculateImpedances(rsn, xsn, xt)
    val minimum = calculateImpedances(rsnMin, xsnMin, xt)

    val currents110kV = calculateCurrents(115.0, normal, minimum)
    val currents10kV = calculateCurrents(11.0, normal.transformed(), minimum.transformed())
    val currentsPoint10 = calculatePoint10Currents(normal, minimum)

    return NetworkResults(
        iSh3 = currents110kV.threePhaseNormal,
        iSh2 = currents110kV.twoPhaseNormal,
        iSh3Min = currents110kV.threePhaseMin,
        iSh2Min = currents110kV.twoPhaseMin,
        iShN3 = currents10kV.threePhaseNormal,
        iShN2 = currents10kV.twoPhaseNormal,
        iShN3Min = currents10kV.threePhaseMin,
        iShN2Min = currents10kV.twoPhaseMin,
        iLN3 = currentsPoint10.threePhaseNormal,
        iLN2 = currentsPoint10.twoPhaseNormal,
        iLN3Min = currentsPoint10.threePhaseMin,
        iLN2Min = currentsPoint10.twoPhaseMin
    )
}

data class Impedance(val resistance: Double, val reactance: Double) {
    val impedance = sqrt(resistance.pow(2) + reactance.pow(2))
    fun transformed(): Impedance {
        val kpr = 11.0.pow(2) / 115.0.pow(2)
        return Impedance(resistance * kpr, reactance * kpr)
    }
}

data class Currents(
    val threePhaseNormal: String,
    val twoPhaseNormal: String,
    val threePhaseMin: String,
    val twoPhaseMin: String
)

fun calculateTransformerReactance(): Double = (11.1 * 115.0.pow(2)) / (100 * 6.3)

fun calculateImpedances(resistance: Double, reactance: Double, transformerReactance: Double): Impedance {
    return Impedance(resistance, reactance + transformerReactance)
}

fun calculateCurrents(voltage: Double, normal: Impedance, minimum: Impedance): Currents {
    val threePhaseNormal = formatCurrent(voltage / (sqrt(3.0) * normal.impedance))
    val twoPhaseNormal = formatCurrent(threePhaseNormal.toDouble() * (sqrt(3.0) / 2))
    val threePhaseMin = formatCurrent(voltage / (sqrt(3.0) * minimum.impedance))
    val twoPhaseMin = formatCurrent(threePhaseMin.toDouble() * (sqrt(3.0) / 2))
    return Currents(threePhaseNormal, twoPhaseNormal, threePhaseMin, twoPhaseMin)
}

fun calculatePoint10Currents(normal: Impedance, minimum: Impedance): Currents {
    val lineResistance = 12.52 // Total resistance of the line
    val lineReactance = 6.88 // Total reactance of the line

    val normalTotal = Impedance(normal.resistance + lineResistance, normal.reactance + lineReactance)
    val minimumTotal = Impedance(minimum.resistance + lineResistance, minimum.reactance + lineReactance)

    return calculateCurrents(11.0, normalTotal, minimumTotal)
}

fun formatCurrent(current: Double): String = String.format("%.1f", current)

data class NetworkResults(
    val iSh3: String,
    val iSh2: String,
    val iSh3Min: String,
    val iSh2Min: String,
    val iShN3: String,
    val iShN2: String,
    val iShN3Min: String,
    val iShN2Min: String,
    val iLN3: String,
    val iLN2: String,
    val iLN3Min: String,
    val iLN2Min: String
)
