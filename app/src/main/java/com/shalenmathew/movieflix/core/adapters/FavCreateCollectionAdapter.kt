package com.shalenmathew.movieflix.core.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shalenmathew.movieflix.databinding.ItemFavCreateCollectionBinding

class FavCreateCollectionAdapter(
    private val onClick: () -> Unit
) : RecyclerView.Adapter<FavCreateCollectionAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFavCreateCollectionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.setOnClickListener { onClick() }
    }

    override fun getItemCount(): Int = 1

    class ViewHolder(binding: ItemFavCreateCollectionBinding) : RecyclerView.ViewHolder(binding.root)
}
