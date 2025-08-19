package com.example.campus_lost_found

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.campus_lost_found.model.FoundItem
import com.example.campus_lost_found.model.LostItem
import com.example.campus_lost_found.repository.SupabaseItemRepository
import com.example.campus_lost_found.repository.SupabaseRepository
import com.example.campus_lost_found.utils.SupabaseManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ReportItemActivity : AppCompatActivity() {

    private lateinit var titleTextView: TextView
    private lateinit var nameEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var locationEditText: EditText
    private lateinit var dateButton: Button
    private lateinit var keptAtLayout: View
    private lateinit var keptAtEditText: EditText
    private lateinit var itemImageView: ImageView
    private lateinit var uploadImageButton: Button
    private lateinit var submitButton: Button

    private val itemRepository = SupabaseRepository()
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    private var isLostItem = true
    private var selectedDate: Date = Date()
    private var imageUri: Uri? = null
    private var imageUrl: String = ""
    private var editItemId: String? = null
    private var editingExistingItem = false

    companion object {
        const val EXTRA_IS_LOST_ITEM = "is_lost_item"
        const val EXTRA_EDIT_ITEM_ID = "edit_item_id"

        fun createIntent(context: Context, isLostItem: Boolean): Intent {
            return Intent(context, ReportItemActivity::class.java).apply {
                putExtra(EXTRA_IS_LOST_ITEM, isLostItem)
            }
        }

        fun createEditIntent(context: Context, isLostItem: Boolean, itemId: String): Intent {
            return Intent(context, ReportItemActivity::class.java).apply {
                putExtra(EXTRA_IS_LOST_ITEM, isLostItem)
                putExtra(EXTRA_EDIT_ITEM_ID, itemId)
            }
        }
    }

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            imageUri = it
            itemImageView.setImageURI(it)
            itemImageView.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_item)

        // Get intent extras
        isLostItem = intent.getBooleanExtra(EXTRA_IS_LOST_ITEM, true)
        editItemId = intent.getStringExtra(EXTRA_EDIT_ITEM_ID)
        editingExistingItem = editItemId != null

        initializeViews()
        setupUI()
        setupEventListeners()

        if (editingExistingItem) {
            loadItemForEditing()
        }
    }

    private fun initializeViews() {
        titleTextView = findViewById(R.id.reportTitleTextView)
        nameEditText = findViewById(R.id.itemNameEditText)
        descriptionEditText = findViewById(R.id.itemDescriptionEditText)
        categorySpinner = findViewById(R.id.categorySpinner)
        locationEditText = findViewById(R.id.locationEditText)
        dateButton = findViewById(R.id.dateButton)
        keptAtLayout = findViewById(R.id.keptAtLayout)
        keptAtEditText = findViewById(R.id.keptAtEditText)
        itemImageView = findViewById(R.id.itemImageView)
        uploadImageButton = findViewById(R.id.uploadImageButton)
        submitButton = findViewById(R.id.submitButton)
    }

    private fun setupUI() {
        // Set title and button text based on item type and editing state
        if (editingExistingItem) {
            titleTextView.text = if (isLostItem) "Edit Lost Item" else "Edit Found Item"
            submitButton.text = "Update Item"
        } else {
            titleTextView.text = if (isLostItem) "Report Lost Item" else "Report Found Item"
            submitButton.text = "Submit Report"
        }

        // Setup category spinner
        val categories = arrayOf("Electronics", "Clothing", "Books", "Keys", "Wallet", "Jewelry", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        // Setup location label and kept at visibility
        if (isLostItem) {
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.locationLayout).hint = "Last Seen Location"
            keptAtLayout.visibility = View.GONE
        } else {
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.locationLayout).hint = "Found Location"
            keptAtLayout.visibility = View.VISIBLE
        }

        // Setup date button
        updateDateButton()
    }

    private fun setupEventListeners() {
        dateButton.setOnClickListener { showDatePicker() }
        uploadImageButton.setOnClickListener { selectImage() }
        submitButton.setOnClickListener { submitItem() }
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(if (isLostItem) "Select Date Lost" else "Select Date Found")
            .setSelection(selectedDate.time)
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            selectedDate = Date(selection)
            updateDateButton()
        }

        datePicker.show(supportFragmentManager, "date_picker")
    }

    private fun updateDateButton() {
        val label = if (isLostItem) "Date Lost: " else "Date Found: "
        dateButton.text = label + dateFormat.format(selectedDate)
    }

    private fun selectImage() {
        imagePickerLauncher.launch("image/*")
    }

    private fun loadItemForEditing() {
        editItemId?.let { itemId ->
            lifecycleScope.launch {
                try {
                    if (isLostItem) {
                        val result = itemRepository.getLostItemsByUser(getCurrentUserId())
                        if (result.isSuccess) {
                            val items = result.getOrNull() ?: emptyList()
                            val item = items.find { it.id == itemId }
                            item?.let { populateFieldsForLostItem(it) }
                        }
                    } else {
                        val result = itemRepository.getFoundItemsByUser(getCurrentUserId())
                        if (result.isSuccess) {
                            val items = result.getOrNull() ?: emptyList()
                            val item = items.find { it.id == itemId }
                            item?.let { populateFieldsForFoundItem(it) }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ReportItemActivity", "Error loading item for editing", e)
                    showError("Failed to load item: ${e.message}")
                }
            }
        }
    }

    private fun populateFieldsForLostItem(item: LostItem) {
        nameEditText.setText(item.name)
        descriptionEditText.setText(item.description)
        locationEditText.setText(item.lastSeenLocation)
        selectedDate = item.dateLost.toDate()
        updateDateButton()

        // Set category spinner
        val categories = (categorySpinner.adapter as ArrayAdapter<String>)
        val position = (0 until categories.count).find { categories.getItem(it) == item.category } ?: 0
        categorySpinner.setSelection(position)

        // Load image if available
        if (item.imageUrl.isNotEmpty()) {
            imageUrl = item.imageUrl
            Glide.with(this)
                .load(item.imageUrl)
                .into(itemImageView)
            itemImageView.visibility = View.VISIBLE
        }
    }

    private fun populateFieldsForFoundItem(item: FoundItem) {
        nameEditText.setText(item.name)
        descriptionEditText.setText(item.description)
        locationEditText.setText(item.location)
        keptAtEditText.setText(item.keptAt)
        selectedDate = item.dateFound.toDate()
        updateDateButton()

        // Set category spinner
        val categories = (categorySpinner.adapter as ArrayAdapter<String>)
        val position = (0 until categories.count).find { categories.getItem(it) == item.category } ?: 0
        categorySpinner.setSelection(position)

        // Load image if available
        if (item.imageUrl.isNotEmpty()) {
            imageUrl = item.imageUrl
            Glide.with(this)
                .load(item.imageUrl)
                .into(itemImageView)
            itemImageView.visibility = View.VISIBLE
        }
    }

    private fun submitItem() {
        if (!validateInputs()) return

        submitButton.isEnabled = false
        submitButton.text = "Submitting..."

        lifecycleScope.launch {
            try {
                // Upload image if selected
                if (imageUri != null) {
                    uploadImageThenSubmit()
                } else {
                    submitItemToDatabase()
                }
            } catch (e: Exception) {
                Log.e("ReportItemActivity", "Error submitting item", e)
                showError("Failed to submit item: ${e.message}")
                resetSubmitButton()
            }
        }
    }

    private suspend fun uploadImageThenSubmit() {
        try {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                showError("You must be logged in to upload images")
                resetSubmitButton()
                return
            }

            // Use the static method with correct parameters
            val fileName = "${currentUser.uid}_${System.currentTimeMillis()}.jpg"
            val result = SupabaseManager.uploadImage(this, imageUri!!, fileName)

            if (result.isSuccess) {
                imageUrl = result.getOrNull() ?: ""
                submitItemToDatabase()
            } else {
                val error = result.exceptionOrNull()
                showError("Failed to upload image: ${error?.message}")
                resetSubmitButton()
            }
        } catch (e: Exception) {
            Log.e("ReportItemActivity", "Error uploading image", e)
            showError("Error uploading image: ${e.message}")
            resetSubmitButton()
        }
    }

    private suspend fun submitItemToDatabase() {
        try {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                showError("You must be logged in to submit items")
                resetSubmitButton()
                return
            }

            val result = if (isLostItem) {
                submitLostItem(currentUser.uid, currentUser.displayName ?: "Anonymous")
            } else {
                submitFoundItem(currentUser.uid, currentUser.displayName ?: "Anonymous")
            }

            if (result.isSuccess) {
                showSuccess(if (editingExistingItem) "Item updated successfully!" else "Item reported successfully!")
                finish()
            } else {
                val error = result.exceptionOrNull()
                showError("Failed to submit item: ${error?.message}")
                resetSubmitButton()
            }
        } catch (e: Exception) {
            Log.e("ReportItemActivity", "Error submitting to database", e)
            showError("Error submitting item: ${e.message}")
            resetSubmitButton()
        }
    }

    private suspend fun submitLostItem(userId: String, userName: String): Result<String> {
        val lostItem = LostItem(
            id = editItemId ?: java.util.UUID.randomUUID().toString(),
            name = nameEditText.text.toString().trim(),
            description = descriptionEditText.text.toString().trim(),
            category = categorySpinner.selectedItem.toString(),
            location = locationEditText.text.toString().trim(),
            imageUrl = imageUrl,
            reportedBy = userId,
            reportedByName = userName,
            reportedDate = Timestamp(Date()),
            dateLost = Timestamp(selectedDate)
        )

        return itemRepository.addLostItem(lostItem)
    }

    private suspend fun submitFoundItem(userId: String, userName: String): Result<String> {
        val foundItem = FoundItem(
            id = editItemId ?: java.util.UUID.randomUUID().toString(),
            name = nameEditText.text.toString().trim(),
            description = descriptionEditText.text.toString().trim(),
            category = categorySpinner.selectedItem.toString(),
            location = locationEditText.text.toString().trim(),
            keptAt = keptAtEditText.text.toString().trim(),
            imageUrl = imageUrl,
            reportedBy = userId,
            reportedByName = userName,
            reportedDate = Timestamp(Date()),
            dateFound = Timestamp(selectedDate)
        )

        return itemRepository.addFoundItem(foundItem)
    }

    private fun validateInputs(): Boolean {
        if (nameEditText.text.toString().trim().isEmpty()) {
            nameEditText.error = "Item name is required"
            return false
        }

        if (locationEditText.text.toString().trim().isEmpty()) {
            locationEditText.error = if (isLostItem) "Last seen location is required" else "Found location is required"
            return false
        }

        if (!isLostItem && keptAtEditText.text.toString().trim().isEmpty()) {
            keptAtEditText.error = "Please specify where the item is kept"
            return false
        }

        return true
    }

    private fun getCurrentUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }

    private fun resetSubmitButton() {
        submitButton.isEnabled = true
        submitButton.text = if (editingExistingItem) "Update Item" else "Submit Report"
    }

    private fun showError(message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showSuccess(message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Success")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}
