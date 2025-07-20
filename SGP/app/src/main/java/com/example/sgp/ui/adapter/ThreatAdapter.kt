package com.example.sgp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sgp.databinding.ItemThreatBinding
import com.example.sgp.model.ThreatItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ThreatAdapter : ListAdapter<ThreatItem, ThreatAdapter.ThreatViewHolder>(ThreatDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThreatViewHolder {
        val binding = ItemThreatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ThreatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ThreatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ThreatViewHolder(private val binding: ItemThreatBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ThreatItem) {
            // Format timestamp
            val dateFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
            val date = Date(item.timestamp)

            binding.apply {
                tvThreatTimestamp.text = dateFormat.format(date)
                tvThreatType.text = item.threatType
                tvSourceApp.text = item.sourceApp
                tvSender.text = if (item.sender.isNotEmpty()) "• ${item.sender}" else ""
                tvMessageContent.text = item.messageContent
                tvConfidenceScore.text = "${item.confidenceScore}%"
                progressConfidence.progress = item.confidenceScore
            }
        }
    }

    class ThreatDiffCallback : DiffUtil.ItemCallback<ThreatItem>() {
        override fun areItemsTheSame(oldItem: ThreatItem, newItem: ThreatItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ThreatItem, newItem: ThreatItem): Boolean {
            return oldItem == newItem
        }
    }
}
