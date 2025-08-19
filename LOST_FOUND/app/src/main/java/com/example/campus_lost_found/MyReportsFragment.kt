package com.example.campus_lost_found

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope
import com.example.campus_lost_found.adapter.ItemsAdapter
import com.example.campus_lost_found.model.FoundItem
import com.example.campus_lost_found.model.Item
import com.example.campus_lost_found.model.LostItem
import com.example.campus_lost_found.repository.SupabaseRepository
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MyReportsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private var tabLayout: TabLayout? = null
    private var noItemsTextView: TextView? = null
    private val itemRepository = SupabaseRepository()
    private val currentUserId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private var myLostItems = listOf<LostItem>()
    private var myFoundItems = listOf<FoundItem>()
    private var displayingLostItems = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_items_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            Log.d("MyReportsFragment", "Starting onViewCreated")

            // Initialize required views
            recyclerView = view.findViewById(R.id.itemsRecyclerView) ?: run {
                Log.e("MyReportsFragment", "RecyclerView not found")
                return
            }

            searchView = view.findViewById(R.id.searchView) ?: run {
                Log.e("MyReportsFragment", "SearchView not found")
                return
            }

            // Initialize optional views safely
            tabLayout = view.findViewById(R.id.myReportsTabLayout)
            noItemsTextView = view.findViewById(R.id.empty_view)

            if (tabLayout == null) {
                Log.w("MyReportsFragment", "TabLayout not found in layout")
            }

            if (noItemsTextView == null) {
                Log.w("MyReportsFragment", "Empty view not found in layout")
            }

            setupViews()
            loadMyItems()

        } catch (e: Exception) {
            Log.e("MyReportsFragment", "Error in onViewCreated: ${e.message}", e)
            // Show error to user instead of crashing
            android.widget.Toast.makeText(requireContext(), "Error loading fragment", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupViews() {
        try {
            // Setup RecyclerView
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.setHasFixedSize(true)

            // Set empty adapter initially
            recyclerView.adapter = ItemsAdapter(
                items = mutableListOf(),
                isLostItemsList = true,
                currentUserId = currentUserId,
                onItemClick = { },
                onClaimButtonClick = { }
            )

            // Setup TabLayout if available
            tabLayout?.let { tabLayout ->
                tabLayout.visibility = View.VISIBLE
                tabLayout.removeAllTabs()
                tabLayout.addTab(tabLayout.newTab().setText("Lost Items"))
                tabLayout.addTab(tabLayout.newTab().setText("Found Items"))

                tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                    override fun onTabSelected(tab: TabLayout.Tab?) {
                        tab?.let {
                            displayingLostItems = it.position == 0
                            updateDisplayedItems()
                        }
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab?) {}
                    override fun onTabReselected(tab: TabLayout.Tab?) {}
                })
            }

            // Setup SearchView
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    filterItems(query)
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    filterItems(newText)
                    return false
                }
            })

        } catch (e: Exception) {
            Log.e("MyReportsFragment", "Error setting up views: ${e.message}")
        }
    }

    private fun loadMyItems() {
        if (currentUserId.isEmpty()) {
            Log.w("MyReportsFragment", "No user logged in")
            updateRecyclerView(emptyList())
            return
        }

        lifecycleScope.launch {
            try {
                showEmptyState("Loading your reports...")

                // Load both lost and found items
                val lostResult = itemRepository.getLostItemsByUser(currentUserId)
                val foundResult = itemRepository.getFoundItemsByUser(currentUserId)

                if (lostResult.isSuccess) {
                    myLostItems = lostResult.getOrNull() ?: emptyList()
                    Log.d("MyReportsFragment", "Loaded ${myLostItems.size} lost items")
                } else {
                    Log.e("MyReportsFragment", "Failed to load lost items: ${lostResult.exceptionOrNull()?.message}")
                }

                if (foundResult.isSuccess) {
                    myFoundItems = foundResult.getOrNull() ?: emptyList()
                    Log.d("MyReportsFragment", "Loaded ${myFoundItems.size} found items")
                } else {
                    Log.e("MyReportsFragment", "Failed to load found items: ${foundResult.exceptionOrNull()?.message}")
                }

                updateDisplayedItems()

            } catch (e: Exception) {
                Log.e("MyReportsFragment", "Exception loading items: ${e.message}", e)
                showErrorDialog("Failed to load your reports: ${e.message}")
                updateRecyclerView(emptyList())
            }
        }
    }

    private fun updateDisplayedItems() {
        val itemsToShow = if (displayingLostItems) myLostItems else myFoundItems
        updateRecyclerView(itemsToShow)
    }

    private fun filterItems(query: String?) {
        val itemsToFilter = if (displayingLostItems) myLostItems else myFoundItems

        if (query.isNullOrBlank()) {
            updateRecyclerView(itemsToFilter)
            return
        }

        val filteredList = itemsToFilter.filter { item ->
            when (item) {
                is LostItem -> item.name.contains(query, ignoreCase = true) ||
                    item.description.contains(query, ignoreCase = true) ||
                    item.category.contains(query, ignoreCase = true) ||
                    item.location.contains(query, ignoreCase = true) // Use location instead of lastSeenLocation
                is FoundItem -> item.name.contains(query, ignoreCase = true) ||
                    item.description.contains(query, ignoreCase = true) ||
                    item.category.contains(query, ignoreCase = true) ||
                    item.location.contains(query, ignoreCase = true) ||
                    item.keptAt.contains(query, ignoreCase = true)
                else -> false
            }
        }

        updateRecyclerView(filteredList)
    }

    private fun updateRecyclerView(items: List<Item>) {
        try {
            val adapter = ItemsAdapter(
                items = items.toMutableList(),
                isLostItemsList = displayingLostItems,
                currentUserId = currentUserId,
                onItemClick = { item -> showItemDetailsDialog(item) },
                onClaimButtonClick = { item -> showDeleteConfirmationDialog(item) }
            )
            recyclerView.adapter = adapter

            // Update empty state
            if (items.isEmpty()) {
                val message = if (displayingLostItems) {
                    "No lost items reported yet"
                } else {
                    "No found items reported yet"
                }
                showEmptyState(message)
            } else {
                hideEmptyState()
            }

        } catch (e: Exception) {
            Log.e("MyReportsFragment", "Error updating RecyclerView: ${e.message}")
        }
    }

    private fun showEmptyState(message: String) {
        noItemsTextView?.let {
            it.text = message
            it.visibility = View.VISIBLE
        }
        recyclerView.visibility = View.GONE
    }

    private fun hideEmptyState() {
        noItemsTextView?.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }

    private fun showItemDetailsDialog(item: Item) {
        val message = when (item) {
            is LostItem -> """
                Name: ${item.name}
                Category: ${item.category}
                Location: ${item.lastSeenLocation}
                Description: ${item.description}
                Date Lost: ${item.dateLost.toDate()}
            """.trimIndent()

            is FoundItem -> {
                val claimStatus = if (item.claimed) {
                    "Claimed by: ${item.claimedByName}"
                } else {
                    "Not claimed"
                }

                """
                Name: ${item.name}
                Category: ${item.category}
                Location: ${item.location}
                Description: ${item.description}
                Date Found: ${item.dateFound.toDate()}
                Kept at: ${item.keptAt}
                Status: $claimStatus
                """.trimIndent()
            }

            else -> "Item details unavailable"
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Item Details")
            .setMessage(message)
            .setPositiveButton("Close", null)
            .show()
    }

    private fun showDeleteConfirmationDialog(item: Item) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Confirm Deletion")
            .setMessage("Are you sure you want to delete this item report? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteItem(item)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteItem(item: Item) {
        lifecycleScope.launch {
            try {
                val result = when (item) {
                    is LostItem -> itemRepository.deleteLostItem(item.id)
                    is FoundItem -> itemRepository.deleteFoundItem(item.id)
                    else -> Result.failure(Exception("Unknown item type"))
                }

                if (result.isSuccess) {
                    showSuccessDialog("Item deleted successfully")
                    loadMyItems()
                } else {
                    val error = result.exceptionOrNull()
                    showErrorDialog("Failed to delete item: ${error?.message}")
                }
            } catch (e: Exception) {
                showErrorDialog("Error deleting item: ${e.message}")
            }
        }
    }

    private fun showLoginRequiredDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Login Required")
            .setMessage("You must be logged in to view your reports.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showSuccessDialog(message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Success")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showErrorDialog(message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        loadMyItems() // Refresh data when coming back to this fragment
    }
}
