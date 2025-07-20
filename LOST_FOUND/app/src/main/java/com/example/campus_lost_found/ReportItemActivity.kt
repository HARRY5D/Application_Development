package com.example.campus_lost_found

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.campus_lost_found.model.FoundItem
import com.example.campus_lost_found.model.LostItem
import com.example.campus_lost_found.repository.ItemRepository
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
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

    private val itemRepository = ItemRepository()
    private val storageRef = FirebaseStorage.getInstance().reference
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    private var isLostItem = true
    private var selectedDate: Date = Date()
    private var imageUri: Uri? = null
    private var imageUrl: String = ""
    private var editItemId: String? = null
    private var editingExistingItem = false

    private val currentUserId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private val currentUserName: String
        get() = FirebaseAuth.getInstance().currentUser?.displayName ?: "Anonymous User"

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            imageUri = it
            Glide.with(this)
                .load(uri)
                .centerCrop()
                .into(itemImageView)
            itemImageView.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_item)

        // Get intent data
        isLostItem = intent.getBooleanExtra("isLostItem", true)
        editItemId = intent.getStringExtra("itemId")
        editingExistingItem = editItemId != null

        // Initialize views
        initViews()
        setupUI()

        // Load item data if editing
        if (editingExistingItem) {
            loadItemData()
        }
    }

    private fun initViews() {
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
        // Set title based on mode
        val titleText = if (editingExistingItem) {
            if (isLostItem) "Edit Lost Item" else "Edit Found Item"
        } else {
            if (isLostItem) "Report Lost Item" else "Report Found Item"
        }
        titleTextView.text = titleText
        submitButton.text = if (editingExistingItem) "Update" else "Submit"

        // Setup category spinner
        val categories = arrayOf(
            "Electronics", "Books & Stationery", "ID Cards & Documents",
            "Keys", "Clothing", "Accessories", "Wallet/Purse", "Other"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        // Show/hide kept at field based on item type
        keptAtLayout.visibility = if (isLostItem) View.GONE else View.VISIBLE

        // Date button setup
        updateDateButtonText()
        dateButton.setOnClickListener {
            showDatePicker()
        }

        // Image upload button
        uploadImageButton.setOnClickListener {
            getContent.launch("image/*")
        }

        // Submit button
        submitButton.setOnClickListener {
            if (validateInputs()) {
                if (imageUri != null && imageUrl.isEmpty()) {
                    uploadImageAndSaveItem()
                } else {
                    saveItem()
                }
            }
        }
    }

    private fun loadItemData() {
        if (editItemId.isNullOrEmpty()) return

        if (isLostItem) {
            itemRepository.getLostItem(editItemId!!).addOnSuccessListener { document ->
                val lostItem = document.toObject(LostItem::class.java) ?: return@addOnSuccessListener

                nameEditText.setText(lostItem.name)
                descriptionEditText.setText(lostItem.description)
                setCategorySpinnerSelection(lostItem.category)
                locationEditText.setText(lostItem.location)
                selectedDate = lostItem.dateLost.toDate()
                updateDateButtonText()

                if (lostItem.imageUrl.isNotEmpty()) {
                    imageUrl = lostItem.imageUrl
                    Glide.with(this)
                        .load(imageUrl)
                        .centerCrop()
                        .into(itemImageView)
                    itemImageView.visibility = View.VISIBLE
                }
            }.addOnFailureListener { e ->
                showErrorDialog("Failed to load item: ${e.message}")
            }
        } else {
            itemRepository.getFoundItem(editItemId!!).addOnSuccessListener { document ->
                val foundItem = document.toObject(FoundItem::class.java) ?: return@addOnSuccessListener

                nameEditText.setText(foundItem.name)
                descriptionEditText.setText(foundItem.description)
                setCategorySpinnerSelection(foundItem.category)
                locationEditText.setText(foundItem.location)
                keptAtEditText.setText(foundItem.keptAt)
                selectedDate = foundItem.dateFound.toDate()
                updateDateButtonText()

                if (foundItem.imageUrl.isNotEmpty()) {
                    imageUrl = foundItem.imageUrl
                    Glide.with(this)
                        .load(imageUrl)
                        .centerCrop()
                        .into(itemImageView)
                    itemImageView.visibility = View.VISIBLE
                }
            }.addOnFailureListener { e ->
                showErrorDialog("Failed to load item: ${e.message}")
            }
        }
    }

    private fun setCategorySpinnerSelection(category: String) {
        val adapter = categorySpinner.adapter as ArrayAdapter<String>
        for (i in 0 until adapter.count) {
            if (adapter.getItem(i) == category) {
                categorySpinner.setSelection(i)
                break
            }
        }
    }

    private fun updateDateButtonText() {
        val dateText = if (isLostItem) {
            "Date Lost: ${dateFormat.format(selectedDate)}"
        } else {
            "Date Found: ${dateFormat.format(selectedDate)}"
        }
        dateButton.text = dateText
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(if (isLostItem) "Select Lost Date" else "Select Found Date")
            .setSelection(selectedDate.time)
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            selectedDate = Date(selection)
            updateDateButtonText()
        }

        datePicker.show(supportFragmentManager, "DATE_PICKER")
    }

    private fun validateInputs(): Boolean {
        if (nameEditText.text.isBlank()) {
            showErrorDialog("Please enter the item name")
            return false
        }

        if (locationEditText.text.isBlank()) {
            showErrorDialog("Please enter the location")
            return false
        }

        if (!isLostItem && keptAtEditText.text.isBlank()) {
            showErrorDialog("Please enter where the item is kept")
            return false
        }

        if (currentUserId.isEmpty()) {
            showErrorDialog("You must be signed in to report items")
            return false
        }

        return true
    }

    private fun uploadImageAndSaveItem() {
        val imageRef = storageRef.child("item_images/${UUID.randomUUID()}.jpg")

        imageUri?.let { uri ->
            submitButton.isEnabled = false
            submitButton.text = "Uploading..."

            imageRef.putFile(uri)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        imageUrl = downloadUri.toString()
                        saveItem()
                    }
                }
                .addOnFailureListener { e ->
                    submitButton.isEnabled = true
                    submitButton.text = if (editingExistingItem) "Update" else "Submit"
                    showErrorDialog("Failed to upload image: ${e.message}")
                }
        }
    }

    private fun saveItem() {
        val name = nameEditText.text.toString().trim()
        val description = descriptionEditText.text.toString().trim()
        val category = categorySpinner.selectedItem.toString()
        val location = locationEditText.text.toString().trim()

        if (isLostItem) {
            val lostItem = LostItem(
                id = editItemId ?: "",
                name = name,
                description = description,
                category = category,
                location = location,
                imageUrl = imageUrl,
                reportedBy = currentUserId,
                reportedByName = currentUserName,
                reportedDate = Timestamp.now(),
                dateLost = Timestamp(selectedDate)
            )

            val task = if (editingExistingItem) {
                itemRepository.updateLostItem(lostItem)
            } else {
                itemRepository.addLostItem(lostItem)
            }

            task.addOnSuccessListener {
                showSuccessDialog(if (editingExistingItem) "Item updated successfully" else "Item reported successfully")
            }.addOnFailureListener { e ->
                showErrorDialog("Failed to save item: ${e.message}")
            }
        } else {
            val keptAt = keptAtEditText.text.toString().trim()

            val foundItem = FoundItem(
                id = editItemId ?: "",
                name = name,
                description = description,
                category = category,
                location = location,
                imageUrl = imageUrl,
                reportedBy = currentUserId,
                reportedByName = currentUserName,
                reportedDate = Timestamp.now(),
                keptAt = keptAt,
                claimed = false,
                claimedBy = "",
                claimedByName = "",
                dateFound = Timestamp(selectedDate)
            )

            val task = if (editingExistingItem) {
                itemRepository.updateFoundItem(foundItem)
            } else {
                itemRepository.addFoundItem(foundItem)
            }

            task.addOnSuccessListener {
                showSuccessDialog(if (editingExistingItem) "Item updated successfully" else "Item reported successfully")
            }.addOnFailureListener { e ->
                showErrorDialog("Failed to save item: ${e.message}")
            }
        }
    }

    private fun showSuccessDialog(message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Success")
            .setMessage(message)
            .setPositiveButton("OK") { _, _ ->
                setResult(Activity.RESULT_OK)
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun showErrorDialog(message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    companion object {
        fun createEditIntent(context: Context, isLostItem: Boolean, itemId: String): Intent {
            return Intent(context, ReportItemActivity::class.java).apply {
                putExtra("isLostItem", isLostItem)
                putExtra("itemId", itemId)
            }
        }
    }
}
