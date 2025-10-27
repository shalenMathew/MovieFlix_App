package com.example.movieflix.core.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.movieflix.R
import com.example.movieflix.core.utils.Constants
import com.example.movieflix.core.utils.loadImage
import com.example.movieflix.databinding.ItemWatchProviderBinding
import com.example.movieflix.domain.model.Provider

class WatchProviderAdapter(
    private val onProviderClick: (Provider) -> Unit
) : ListAdapter<Provider, WatchProviderAdapter.ViewHolder>(ProviderDiffCallback()) {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ItemWatchProviderBinding = ItemWatchProviderBinding.bind(itemView)
        
        fun bind(provider: Provider) {
            binding.apply {
                // Load provider logo
                if (provider.logoPath != null) {
                    itemWatchProviderLogo.loadImage(
                        Constants.TMDB_POSTER_IMAGE_BASE_URL_W342.plus(provider.logoPath),
                        placeholder = ContextCompat.getDrawable(itemView.context, R.drawable.poster_bg)
                    )
                } else {
                    // Set default placeholder when no logo
                    itemWatchProviderLogo.setImageDrawable(
                        ContextCompat.getDrawable(itemView.context, R.drawable.poster_bg)
                    )
                }
                
                // Set click listener on the entire item
                itemView.setOnClickListener {
                    onProviderClick(provider)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_watch_provider, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        // Clear the image when view is recycled to prevent wrong images showing
        holder.binding.itemWatchProviderLogo.setImageDrawable(null)
    }
}

class ProviderDiffCallback : DiffUtil.ItemCallback<Provider>() {
    override fun areItemsTheSame(oldItem: Provider, newItem: Provider): Boolean {
        return oldItem.providerId == newItem.providerId
    }

    override fun areContentsTheSame(oldItem: Provider, newItem: Provider): Boolean {
        return oldItem == newItem
    }
}
