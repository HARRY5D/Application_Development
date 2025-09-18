package com.example.sgp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sgp.R
import com.example.sgp.databinding.ItemConversationBinding
import com.example.sgp.model.Conversation
import java.text.SimpleDateFormat
import java.util.*

class ConversationAdapter(
    private val onConversationClick: (Conversation) -> Unit
) : ListAdapter<Conversation, ConversationAdapter.ConversationViewHolder>(ConversationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val binding = ItemConversationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ConversationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ConversationViewHolder(
        private val binding: ItemConversationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(conversation: Conversation) {
            binding.apply {
                // Set conversation name using the correct view ID
                textViewName.text = conversation.name

                // Set last message preview using the correct view ID
                textViewLastMessage.text = conversation.lastMessage ?: "No messages yet"

                // Format and set timestamp using the correct view ID
                val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                textViewTime.text = dateFormat.format(Date(conversation.lastMessageTime))

                // Show unread count using the correct view ID
                if (conversation.unreadCount > 0) {
                    textViewUnreadCount.visibility = android.view.View.VISIBLE
                    textViewUnreadCount.text = conversation.unreadCount.toString()
                } else {
                    textViewUnreadCount.visibility = android.view.View.GONE
                }

                // Show threat indicator by changing text color if conversation has threats
                if (conversation.hasThreat) {
                    val color = ContextCompat.getColor(root.context, R.color.threat_high)
                    textViewLastMessage.setTextColor(color)
                    // Add warning emoji to indicate threat
                    textViewLastMessage.text = "⚠️ ${conversation.lastMessage ?: "Threat detected"}"
                } else {
                    val defaultColor = ContextCompat.getColor(root.context, android.R.color.darker_gray)
                    textViewLastMessage.setTextColor(defaultColor)
                }

                // Show online status by adding indicator to name
                if (conversation.isOnline) {
                    textViewName.text = "${conversation.name} 🟢"
                } else {
                    textViewName.text = conversation.name
                }

                // Set click listener
                root.setOnClickListener {
                    onConversationClick(conversation)
                }
            }
        }
    }
}

class ConversationDiffCallback : DiffUtil.ItemCallback<Conversation>() {
    override fun areItemsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
        return oldItem == newItem
    }
}
