package com.example.campus_lost_found.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.campus_lost_found.R
import com.example.campus_lost_found.model.FoundItem
import com.example.campus_lost_found.model.Item
import com.example.campus_lost_found.model.LostItem
import java.text.SimpleDateFormat
import java.util.Locale

class ItemsAdapter(
    private val items: List<Item>,
    private val isLostItemsList: Boolean,
    private val currentUserId: String,
    private val onClaimButtonClick: ((Item) -> Unit)? = null,
    private val onItemClick: ((Item) -> Unit)? = null
) : RecyclerView.Adapter<ItemsAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_card, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, isLostItemsList, currentUserId, onClaimButtonClick, onItemClick)
    }

    override fun getItemCount() = items.size

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val itemImage: ImageView = itemView.findViewById(R.id.itemImage)
        private val itemName: TextView = itemView.findViewById(R.id.itemName)
        private val itemCategory: TextView = itemView.findViewById(R.id.itemCategory)
        private val itemLocation: TextView = itemView.findViewById(R.id.itemLocation)
        private val itemDate: TextView = itemView.findViewById(R.id.itemDate)
        private val itemReportedBy: TextView = itemView.findViewById(R.id.itemReportedBy)
        private val btnAction: Button = itemView.findViewById(R.id.btnAction)
        private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

        fun bind(
            item: Item,
            isLostItemsList: Boolean,
            currentUserId: String,
            onClaimButtonClick: ((Item) -> Unit)?,
            onItemClick: ((Item) -> Unit)?
        ) {
            // Set basic item details
            itemName.text = item.name
            itemCategory.text = item.category
            itemLocation.text = item.location
            itemReportedBy.text = itemView.context.getString(R.string.reported_by, item.reportedByName)

            // Load image if available
            if (item.imageUrl.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(item.imageUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(itemImage)
            } else {
                itemImage.setImageResource(R.drawable.ic_launcher_foreground)
            }

            // Set date based on item type
            when (item) {
                is LostItem -> {
                    val formattedDate = dateFormat.format(item.dateLost.toDate())
                    itemDate.text = itemView.context.getString(R.string.lost_on, formattedDate)
                }
                is FoundItem -> {
                    val formattedDate = dateFormat.format(item.dateFound.toDate())
                    itemDate.text = itemView.context.getString(R.string.found_on, formattedDate)
                }
            }

            // Handle claim button visibility and functionality
            if (item is FoundItem && !isLostItemsList) {
                if (item.claimed) {
                    btnAction.setText(R.string.claimed)
                    btnAction.isEnabled = false
                } else {
                    btnAction.setText(R.string.claim)
                    btnAction.isEnabled = currentUserId != item.reportedBy
                    btnAction.setOnClickListener {
                        onClaimButtonClick?.invoke(item)
                    }
                }
                btnAction.visibility = View.VISIBLE
            } else {
                btnAction.visibility = View.GONE
            }

            // Set click listener for the whole item
            itemView.setOnClickListener {
                onItemClick?.invoke(item)
            }
        }
    }
}
