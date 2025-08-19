package com.example.campus_lost_found

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.example.campus_lost_found.adapter.ItemsAdapter
import com.example.campus_lost_found.model.LostItem
import com.example.campus_lost_found.repository.SupabaseRepository
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class LostItemsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private var noItemsTextView: TextView? = null
    private val itemRepository = SupabaseRepository()
    private val currentUserId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private var lostItems = listOf<LostItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_items_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            // Safe view initialization with null checks
            recyclerView = view.findViewById(R.id.itemsRecyclerView) ?: run {
                Log.e("LostItemsFragment", "RecyclerView not found")
                return
            }

            searchView = view.findViewById(R.id.searchView) ?: run {
                Log.e("LostItemsFragment", "SearchView not found")
                return
            }

            noItemsTextView = view.findViewById(R.id.empty_view)

            setupRecyclerView()
            setupSearch()
            loadLostItems()

        } catch (e: Exception) {
            Log.e("LostItemsFragment", "Error in onViewCreated: ${e.message}")
            // Show error to user instead of crashing
            android.widget.Toast.makeText(requireContext(), "Error loading fragment", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        try {
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.setHasFixedSize(true)

            // Set empty adapter initially to prevent crashes
            recyclerView.adapter = ItemsAdapter(
                items = mutableListOf(),
                isLostItemsList = true,
                currentUserId = currentUserId,
                onItemClick = { },
                onClaimButtonClick = { }
            )
        } catch (e: Exception) {
            Log.e("LostItemsFragment", "Error setting up RecyclerView: ${e.message}")
        }
    }

    private fun setupSearch() {
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
    }

    private fun filterItems(query: String?) {
        if (query.isNullOrBlank()) {
            updateRecyclerView(lostItems)
            return
        }

        val filteredList = lostItems.filter { item ->
            item.name.contains(query, ignoreCase = true) ||
            item.description.contains(query, ignoreCase = true) ||
            item.category.contains(query, ignoreCase = true) ||
            item.location.contains(query, ignoreCase = true) // Use location instead of lastSeenLocation
        }

        updateRecyclerView(filteredList)
    }

    private fun loadLostItems() {
        Log.d("LostItemsFragment", "Loading lost items from Supabase...")

        lifecycleScope.launch {
            try {
                showEmptyState("Loading lost items...")

                val result = itemRepository.getLostItems()

                if (result.isSuccess) {
                    val items = result.getOrNull() ?: emptyList()
                    Log.d("LostItemsFragment", "Successfully loaded ${items.size} lost items")

                    lostItems = items
                    updateRecyclerView(items)

                    if (items.isEmpty()) {
                        showEmptyState("No lost items reported yet.")
                    }
                } else {
                    val error = result.exceptionOrNull()
                    Log.e("LostItemsFragment", "Failed to load lost items: ${error?.message}")
                    showErrorDialog("Failed to load lost items: ${error?.message}")
                    showEmptyState("Failed to load items. Please check your connection.")
                }
            } catch (e: Exception) {
                Log.e("LostItemsFragment", "Exception loading lost items: ${e.message}", e)
                showErrorDialog("Error loading items: ${e.message}")
                showEmptyState("Error loading items. Please try again.")
            }
        }
    }

    private fun updateRecyclerView(items: List<LostItem>) {
        val adapter = ItemsAdapter(
            items = items.toMutableList(),
            isLostItemsList = true,
            currentUserId = currentUserId,
            onItemClick = { item ->
                showItemDetailsDialog(item as LostItem)
            },
            onClaimButtonClick = { item ->
                showFoundConfirmationDialog(item as LostItem)
            }
        )
        recyclerView.adapter = adapter

        if (items.isEmpty()) {
            showEmptyState("No lost items available")
        } else {
            hideEmptyState()
        }
    }

    private fun showItemDetailsDialog(item: LostItem) {
        val message = """
            Name: ${item.name}
            Category: ${item.category}
            Last Seen: ${item.lastSeenLocation}
            Description: ${item.description}
            Date Lost: ${item.dateLost.toDate()}
            Reported by: ${item.reportedByName}
        """.trimIndent()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Item Details")
            .setMessage(message)
            .setPositiveButton("Close", null)
            .show()
    }

    private fun showFoundConfirmationDialog(item: LostItem) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Found This Item?")
            .setMessage("Have you found this item? The owner will be notified of your claim.")
            .setPositiveButton("Yes, I Found It") { _, _ ->
                processFoundClaim(item)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun processFoundClaim(item: LostItem) {
        if (currentUserId.isEmpty()) {
            showErrorDialog("You must be logged in to claim items.")
            return
        }

        lifecycleScope.launch {
            try {
                val currentUserName = FirebaseAuth.getInstance().currentUser?.displayName ?: "Anonymous User"
                val result = itemRepository.markLostItemAsFound(item.id, currentUserId, currentUserName)

                if (result.isSuccess) {
                    showSuccessDialog("Thank you! The owner has been notified that you found their item.")
                    loadLostItems() // Refresh the list
                } else {
                    val error = result.exceptionOrNull()
                    showErrorDialog("Failed to process claim: ${error?.message}")
                }
            } catch (e: Exception) {
                showErrorDialog("Error processing claim: ${e.message}")
            }
        }
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

    override fun onResume() {
        super.onResume()
        loadLostItems() // Refresh data when coming back to this fragment
    }
}
