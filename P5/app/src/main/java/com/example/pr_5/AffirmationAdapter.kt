package com.example.pr_5

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AffirmationAdapter(
    private val context: Context,
    private val dataset: List<Affirmation>
) : RecyclerView.Adapter<AffirmationAdapter.AffirmationViewHolder>() {

    class AffirmationViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.affirmationImage)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AffirmationViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_affirmation, parent, false)
        return AffirmationViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: AffirmationViewHolder, position: Int) {
        val item = dataset[position]

        holder.imageView.setImageResource(item.imageResourceId)
    }

    override fun getItemCount() = dataset.size
}
