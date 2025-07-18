package com.example.tftitemapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GridAdapter(
    private var items: List<GridItem>,
    private val onItemClick: (GridItem) -> Unit
) : RecyclerView.Adapter<GridAdapter.GridViewHolder>() {

    // ViewHolder class - holds references to views
    class GridViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val titleText: TextView = itemView.findViewById(R.id.titleText)
        val placementText: TextView = itemView.findViewById(R.id.placementText)
    }

    // Create new ViewHolder (called when RecyclerView needs a new view)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_grid, parent, false)
        return GridViewHolder(view)
    }

    // Bind data to ViewHolder (called when RecyclerView needs to display data)
    override fun onBindViewHolder(holder: GridViewHolder, position: Int) {
        val item = items[position]

        // Set data to views
        holder.titleText.text = item.itemName
        holder.placementText.text = item.placement
        holder.imageView.setImageResource(item.imageResId)

        // Handle click events
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }

        // Optional: Add long click listener
        holder.itemView.setOnLongClickListener {
            // Handle long click (e.g., show context menu)
            true
        }
    }

    // Return total number of items
    override fun getItemCount(): Int = items.size
}