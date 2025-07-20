package com.example.campus_lost_found

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.campus_lost_found.adapter.ItemsAdapter
import com.example.campus_lost_found.model.LostItem
import com.example.campus_lost_found.repository.ItemRepository
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class LostItemsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private val itemRepository = ItemRepository()
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

        recyclerView = view.findViewById(R.id.itemsRecyclerView)
        searchView = view.findViewById(R.id.searchView)

        setupRecyclerView()
        setupSearch()
        loadLostItems()
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)
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
            item.location.contains(query, ignoreCase = true)
        }

        updateRecyclerView(filteredList)
    }

    private fun loadLostItems() {
        itemRepository.getLostItems().get()
            .addOnSuccessListener { snapshot ->
                val items = snapshot.toObjects(LostItem::class.java)
                lostItems = items
                updateRecyclerView(items)
            }
            .addOnFailureListener { exception ->
                showErrorDialog("Failed to load lost items: ${exception.message}")
            }
    }

    private fun updateRecyclerView(items: List<LostItem>) {
        val adapter = ItemsAdapter(
            items = items,
            isLostItemsList = true,
            currentUserId = currentUserId,
            onItemClick = { item ->
                // Show item details
                showItemDetailsDialog(item as LostItem)
            }
        )
        recyclerView.adapter = adapter
    }

    private fun showItemDetailsDialog(item: LostItem) {
        val message = """
            Name: ${item.name}
            Category: ${item.category}
            Location: ${item.location}
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

    private fun showErrorDialog(message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        loadLostItems() // Refresh data when coming back to this fragment
    }
}
