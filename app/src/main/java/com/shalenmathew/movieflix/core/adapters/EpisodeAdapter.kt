package com.shalenmathew.movieflix.core.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shalenmathew.movieflix.R
import com.shalenmathew.movieflix.core.utils.loadImage
import com.shalenmathew.movieflix.databinding.ItemEpisodeBinding
import com.shalenmathew.movieflix.domain.model.TVEpisode

class EpisodeAdapter(
    private val onEpisodeClick: ((TVEpisode) -> Unit)? = null
) : ListAdapter<TVEpisode, EpisodeAdapter.ViewHolder>(EpisodeDiffCallback()) {

    inner class ViewHolder(private val binding: ItemEpisodeBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(episode: TVEpisode) {
            binding.apply {
                // Episode name
                itemEpisodeName.text = episode.name ?: "Episode ${episode.episodeNumber}"
                
                // Episode number
                itemEpisodeNumber.text = "Episode ${episode.episodeNumber}"
                
                // Episode description
                itemEpisodeDescription.text = episode.overview?.takeIf { it.isNotBlank() }
                    ?: "No description available"
                
                // Episode duration
                val runtime = episode.runtime
                itemEpisodeDuration.text = if (runtime != null && runtime > 0) {
                    "${runtime}m"
                } else {
                    ""
                }
                
                // Episode thumbnail - use W300 instead of W780 for better performance
                if (episode.stillPath != null) {
                    itemEpisodeThumbnail.loadImage(
                        "https://image.tmdb.org/t/p/w300${episode.stillPath}",
                        placeholder = ContextCompat.getDrawable(root.context, R.drawable.poster_bg)
                    )
                } else {
                    itemEpisodeThumbnail.setImageDrawable(
                        ContextCompat.getDrawable(root.context, R.drawable.poster_bg)
                    )
                }
                
                // Click listener
                root.setOnClickListener {
                    onEpisodeClick?.invoke(episode)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEpisodeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        // Image recycling is handled automatically by Glide
    }
}

class EpisodeDiffCallback : DiffUtil.ItemCallback<TVEpisode>() {
    override fun areItemsTheSame(oldItem: TVEpisode, newItem: TVEpisode): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: TVEpisode, newItem: TVEpisode): Boolean {
        return oldItem == newItem
    }
}
