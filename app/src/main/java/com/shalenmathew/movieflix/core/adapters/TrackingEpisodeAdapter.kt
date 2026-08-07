package com.shalenmathew.movieflix.core.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shalenmathew.movieflix.R
import com.shalenmathew.movieflix.core.utils.loadImage
import com.shalenmathew.movieflix.domain.model.TVEpisode

class TrackingEpisodeAdapter(
    private val onEpisodeClick: (TVEpisode) -> Unit
) : ListAdapter<TVEpisode, TrackingEpisodeAdapter.ViewHolder>(EpisodeDiffCallback()) {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thumbnail: ImageView = itemView.findViewById(R.id.item_episode_thumbnail)
        val name: TextView = itemView.findViewById(R.id.item_episode_name)
        val number: TextView = itemView.findViewById(R.id.item_episode_number)

        fun bind(episode: TVEpisode) {
            name.text = episode.name ?: "Episode ${episode.episodeNumber}"
            number.text = "Episode ${episode.episodeNumber}"
            
            if (episode.stillPath != null) {
                thumbnail.loadImage(
                    "https://image.tmdb.org/t/p/w300${episode.stillPath}",
                    placeholder = ContextCompat.getDrawable(itemView.context, R.drawable.poster_bg)
                )
            } else {
                thumbnail.setImageDrawable(
                    ContextCompat.getDrawable(itemView.context, R.drawable.poster_bg)
                )
            }

            itemView.setOnClickListener {
                onEpisodeClick(episode)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tracking_episode, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class EpisodeDiffCallback : DiffUtil.ItemCallback<TVEpisode>() {
        override fun areItemsTheSame(oldItem: TVEpisode, newItem: TVEpisode): Boolean =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: TVEpisode, newItem: TVEpisode): Boolean =
            oldItem == newItem
    }
}
