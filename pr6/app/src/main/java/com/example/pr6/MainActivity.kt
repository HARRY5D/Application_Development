package com.example.pr6

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var etTemperature: EditText
    private lateinit var spinnerSourceUnit: Spinner
    private lateinit var spinnerTargetUnit: Spinner
    private lateinit var btnConvert: Button
    private lateinit var tvResult: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etTemperature = findViewById(R.id.etTemperature)
        spinnerSourceUnit = findViewById(R.id.spinnerSourceUnit)
        spinnerTargetUnit = findViewById(R.id.spinnerTargetUnit)
        btnConvert = findViewById(R.id.btnConvert)
        tvResult = findViewById(R.id.tvResult)

        // Populate spinners
        ArrayAdapter.createFromResource(
            this,
            R.array.temperature_units,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerSourceUnit.adapter = adapter
            spinnerTargetUnit.adapter = adapter
        }

        btnConvert.setOnClickListener {
            convertTemperature()
        }
    }

    private fun convertTemperature() {
        val tempString = etTemperature.text.toString()
        if (tempString.isEmpty()) {
            tvResult.text = "Please enter a temperature"
            return
        }

        val temperature = tempString.toDoubleOrNull()
        if (temperature == null) {
            tvResult.text = "Invalid temperature input"
            return
        }

        val sourceUnit = spinnerSourceUnit.selectedItem.toString()
        val targetUnit = spinnerTargetUnit.selectedItem.toString()

        if (sourceUnit == targetUnit) {
            tvResult.text = String.format("%.2f %s", temperature, targetUnit.first())
            return
        }

        val result = performConversion(temperature, sourceUnit, targetUnit)
        tvResult.text = String.format("Result: %.2f %s", result, targetUnit.first())
    }

    private fun performConversion(value: Double, from: String, to: String): Double {
        // First, convert the input 'from' unit to Celsius as a base
        val valueInCelsius = when (from) {
            "Fahrenheit" -> (value - 32) * 5.0 / 9.0
            "Kelvin" -> value - 273.15
            "Celsius" -> value
            else -> throw IllegalArgumentException("Invalid source unit")
        }

        // Then, convert from Celsius to the target 'to' unit
        return when (to) {
            "Fahrenheit" -> (valueInCelsius * 9.0 / 5.0) + 32
            "Kelvin" -> valueInCelsius + 273.15
            "Celsius" -> valueInCelsius
            else -> throw IllegalArgumentException("Invalid target unit")
        }
    }
}