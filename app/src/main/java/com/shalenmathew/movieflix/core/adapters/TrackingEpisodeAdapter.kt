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
    private val onEpisodeClick: (TVEpisode) -> Unit,
    private val onWatchedClick: (TVEpisode) -> Unit
) : ListAdapter<TVEpisode, TrackingEpisodeAdapter.ViewHolder>(EpisodeDiffCallback()) {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val infoContainer: View = itemView.findViewById(R.id.item_episode_info_container)
        val thumbnail: ImageView = itemView.findViewById(R.id.item_episode_thumbnail)
        val name: TextView = itemView.findViewById(R.id.item_episode_name)
        val number: TextView = itemView.findViewById(R.id.item_episode_number)
        val watchedBtn: ImageView = itemView.findViewById(R.id.item_episode_watched_btn)
        val watchedContainer: View = itemView.findViewById(R.id.item_episode_watched_container)

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

            updateWatchedStatus(episode)

            infoContainer.setOnClickListener {
                onEpisodeClick(getItem(bindingAdapterPosition))
            }

            watchedContainer.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onWatchedClick(getItem(position))
                }
            }
        }

        fun updateWatchedStatus(episode: TVEpisode) {
            val drawableRes = if (episode.isWatched) {
                R.drawable.check_box
            } else {
                R.drawable.check_box_unmarked
            }
            watchedBtn.setImageResource(drawableRes)
            
            // Handle the "overlay" look: Dim the item when watched
            val alpha = if (episode.isWatched) 0.5f else 1.0f
            thumbnail.alpha = alpha
            name.alpha = alpha
            number.alpha = alpha
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tracking_episode, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else if (payloads.contains("WATCHED_CHANGE")) {
            holder.updateWatchedStatus(getItem(position))
        }
    }

    class EpisodeDiffCallback : DiffUtil.ItemCallback<TVEpisode>() {
        override fun areItemsTheSame(oldItem: TVEpisode, newItem: TVEpisode): Boolean =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: TVEpisode, newItem: TVEpisode): Boolean =
            oldItem == newItem

        override fun getChangePayload(oldItem: TVEpisode, newItem: TVEpisode): Any? {
            return if (oldItem.id == newItem.id && oldItem.isWatched != newItem.isWatched) {
                "WATCHED_CHANGE"
            } else null
        }
    }
}
