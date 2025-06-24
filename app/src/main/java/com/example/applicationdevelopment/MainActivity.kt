package com.example.applicationdevelopment

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {
    private lateinit var editTextNumber1: EditText
    private lateinit var editTextNumber2: EditText
    private lateinit var textViewResult: TextView
    private lateinit var buttonAdd: Button
    private lateinit var buttonSubtract: Button
    private lateinit var buttonMultiply: Button
    private lateinit var buttonDivide: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI components
        editTextNumber1 = findViewById(R.id.editTextNumber1)
        editTextNumber2 = findViewById(R.id.editTextNumber2)
        textViewResult = findViewById(R.id.textViewResult)
        buttonAdd = findViewById(R.id.buttonAdd)
        buttonSubtract = findViewById(R.id.buttonSubtract)
        buttonMultiply = findViewById(R.id.buttonMultiply)
        buttonDivide = findViewById(R.id.buttonDivide)

        // Set up click listeners for calculator buttons
        buttonAdd.setOnClickListener {
            performOperation('+')
        }

        buttonSubtract.setOnClickListener {
            performOperation('-')
        }

        buttonMultiply.setOnClickListener {
            performOperation('×')
        }

        buttonDivide.setOnClickListener {
            performOperation('÷')
        }
    }

    private fun performOperation(operator: Char) {
        val input1 = editTextNumber1.text.toString().trim()
        val input2 = editTextNumber2.text.toString().trim()

        // Validate inputs
        if (input1.isEmpty() || input2.isEmpty()) {
            showMessage("Please enter both numbers")
            return
        }

        try {
            val number1 = input1.toDouble()
            val number2 = input2.toDouble()
            var result = 0.0

            when (operator) {
                '+' -> result = number1 + number2
                '-' -> result = number1 - number2
                '×' -> result = number1 * number2
                '÷' -> {
                    if (number2 == 0.0) {
                        showMessage("Cannot divide by zero")
                        return
                    }
                    result = number1 / number2
                }
            }

            // Format result to remove unnecessary decimal zeros
            val resultText = if (result == result.toInt().toDouble()) {
                result.toInt().toString()
            } else {
                result.toString()
            }

            textViewResult.text = resultText
        } catch (e: NumberFormatException) {
            showMessage("Invalid number format")
        }
    }

    private fun showMessage(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show()
    }
}
