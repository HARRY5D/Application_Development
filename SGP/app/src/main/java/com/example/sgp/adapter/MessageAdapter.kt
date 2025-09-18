package com.example.sgp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sgp.R
import com.example.sgp.databinding.ItemMessageBinding
import com.example.sgp.model.Message
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(
    private val onMessageClick: (Message) -> Unit
) : ListAdapter<Message, MessageAdapter.MessageViewHolder>(MessageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemMessageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MessageViewHolder(
        private val binding: ItemMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message) {
            binding.apply {
                // Set message content
                textMessageContent.text = message.content

                // Format and set timestamp
                val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                textTimestamp.text = dateFormat.format(Date(message.timestamp))

                // Show threat indicator if message is classified as threat
                if (message.isThreat) {
                    threatIndicator.visibility = android.view.View.VISIBLE
                    threatType.visibility = android.view.View.VISIBLE
                    threatType.text = message.threatType.uppercase()

                    // Set threat color based on type
                    val color = when (message.threatType) {
                        "phishing" -> ContextCompat.getColor(root.context, R.color.threat_high)
                        "spam" -> ContextCompat.getColor(root.context, R.color.threat_medium)
                        "abusive" -> ContextCompat.getColor(root.context, R.color.threat_high)
                        else -> ContextCompat.getColor(root.context, R.color.threat_low)
                    }
                    threatIndicator.setColorFilter(color)
                    threatType.setTextColor(color)

                    // Show confidence score
                    confidenceScore.visibility = android.view.View.VISIBLE
                    confidenceScore.text = "${(message.confidenceScore * 100).toInt()}%"
                } else {
                    threatIndicator.visibility = android.view.View.GONE
                    threatType.visibility = android.view.View.GONE
                    confidenceScore.visibility = android.view.View.GONE
                }

                // Set read status
                if (message.isRead) {
                    root.alpha = 0.7f
                } else {
                    root.alpha = 1.0f
                }

                // Set click listener
                root.setOnClickListener {
                    onMessageClick(message)
                }
            }
        }
    }
}

class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
    override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem == newItem
    }
}
