package com.example.p4

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    // UI Elements
    private lateinit var categorySpinner: Spinner
    private lateinit var electronicsLayout: LinearLayout
    private lateinit var clothingLayout: LinearLayout
    private lateinit var groceryLayout: LinearLayout
    private lateinit var calculateButton: Button
    private lateinit var clearButton: Button
    private lateinit var totalRewardsText: TextView

    // Selected category
    private var selectedCategory = 0

    // Item checkboxes
    private lateinit var cbSmartphone: CheckBox
    private lateinit var cbLaptop: CheckBox
    private lateinit var cbHeadphones: CheckBox
    private lateinit var cbTshirt: CheckBox
    private lateinit var cbJeans: CheckBox
    private lateinit var cbJacket: CheckBox
    private lateinit var cbFruits: CheckBox
    private lateinit var cbVegetables: CheckBox
    private lateinit var cbDairy: CheckBox

    // Reward points for each item
    private val rewardPoints = mapOf(
        "smartphone" to 50,
        "laptop" to 100,
        "headphones" to 30,
        "tshirt" to 20,
        "jeans" to 25,
        "jacket" to 35,
        "fruits" to 15,
        "vegetables" to 15,
        "dairy" to 10
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize UI elements
        initializeViews()
        setupSpinner()
        setupListeners()

        // Set initial rewards text
        updateRewardsText(0)
    }

    private fun initializeViews() {
        // Categories
        categorySpinner = findViewById(R.id.category_spinner)

        // Item layouts
        electronicsLayout = findViewById(R.id.electronics_items)
        clothingLayout = findViewById(R.id.clothing_items)
        groceryLayout = findViewById(R.id.grocery_items)

        // Electronics items
        cbSmartphone = findViewById(R.id.cb_smartphone)
        cbLaptop = findViewById(R.id.cb_laptop)
        cbHeadphones = findViewById(R.id.cb_headphones)

        // Clothing items
        cbTshirt = findViewById(R.id.cb_tshirt)
        cbJeans = findViewById(R.id.cb_jeans)
        cbJacket = findViewById(R.id.cb_jacket)

        // Grocery items
        cbFruits = findViewById(R.id.cb_fruits)
        cbVegetables = findViewById(R.id.cb_vegetables)
        cbDairy = findViewById(R.id.cb_dairy)

        // Buttons and text
        calculateButton = findViewById(R.id.calculate_button)
        clearButton = findViewById(R.id.clear_button)
        totalRewardsText = findViewById(R.id.total_rewards)
    }

    private fun setupSpinner() {
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            R.array.categories_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            categorySpinner.adapter = adapter
        }
    }

    private fun setupListeners() {
        // Spinner listener for category selection
        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Save selected position
                selectedCategory = position

                // Hide all item layouts first
                electronicsLayout.visibility = View.GONE
                clothingLayout.visibility = View.GONE
                groceryLayout.visibility = View.GONE

                // Show appropriate layout based on selection
                when (position) {
                    0 -> electronicsLayout.visibility = View.VISIBLE
                    1 -> clothingLayout.visibility = View.VISIBLE
                    2 -> groceryLayout.visibility = View.VISIBLE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Hide all layouts when nothing is selected
                electronicsLayout.visibility = View.GONE
                clothingLayout.visibility = View.GONE
                groceryLayout.visibility = View.GONE
            }
        }

        // Calculate button listener
        calculateButton.setOnClickListener {
            calculateRewards()
        }

        // Clear button listener
        clearButton.setOnClickListener {
            clearSelections()
        }
    }

    private fun calculateRewards(): Int {
        var totalPoints = 0

        // Add points for electronics items
        if (cbSmartphone.isChecked) totalPoints += rewardPoints["smartphone"] ?: 0
        if (cbLaptop.isChecked) totalPoints += rewardPoints["laptop"] ?: 0
        if (cbHeadphones.isChecked) totalPoints += rewardPoints["headphones"] ?: 0

        // Add points for clothing items
        if (cbTshirt.isChecked) totalPoints += rewardPoints["tshirt"] ?: 0
        if (cbJeans.isChecked) totalPoints += rewardPoints["jeans"] ?: 0
        if (cbJacket.isChecked) totalPoints += rewardPoints["jacket"] ?: 0

        // Add points for grocery items
        if (cbFruits.isChecked) totalPoints += rewardPoints["fruits"] ?: 0
        if (cbVegetables.isChecked) totalPoints += rewardPoints["vegetables"] ?: 0
        if (cbDairy.isChecked) totalPoints += rewardPoints["dairy"] ?: 0

        // Apply category bonus (10% extra if any items from the category are selected)
        val categoryBonus = when (selectedCategory) {
            0 -> { // Electronics
                if (cbSmartphone.isChecked || cbLaptop.isChecked || cbHeadphones.isChecked)
                    (totalPoints * 0.1).toInt() else 0
            }
            1 -> { // Clothing
                if (cbTshirt.isChecked || cbJeans.isChecked || cbJacket.isChecked)
                    (totalPoints * 0.1).toInt() else 0
            }
            2 -> { // Grocery
                if (cbFruits.isChecked || cbVegetables.isChecked || cbDairy.isChecked)
                    (totalPoints * 0.1).toInt() else 0
            }
            else -> 0
        }

        totalPoints += categoryBonus

        // Update the rewards text
        updateRewardsText(totalPoints)

        return totalPoints
    }

    private fun updateRewardsText(points: Int) {
        totalRewardsText.text = getString(R.string.total_rewards, points)
    }

    private fun clearSelections() {
        // Reset spinner to first item
        categorySpinner.setSelection(0)

        // Hide clothing and grocery layouts, show electronics (first category)
        electronicsLayout.visibility = View.VISIBLE
        clothingLayout.visibility = View.GONE
        groceryLayout.visibility = View.GONE

        // Uncheck all checkboxes
        cbSmartphone.isChecked = false
        cbLaptop.isChecked = false
        cbHeadphones.isChecked = false
        cbTshirt.isChecked = false
        cbJeans.isChecked = false
        cbJacket.isChecked = false
        cbFruits.isChecked = false
        cbVegetables.isChecked = false
        cbDairy.isChecked = false

        // Reset rewards text
        updateRewardsText(0)
    }
}