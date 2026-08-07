package com.shalenmathew.movieflix.core.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shalenmathew.movieflix.R
import com.shalenmathew.movieflix.domain.model.TVEpisode
import com.shalenmathew.movieflix.domain.model.TrackedSeason
import androidx.transition.TransitionManager
import androidx.transition.ChangeBounds
import androidx.transition.Fade
import androidx.transition.TransitionSet

class TrackingSeasonAdapter(
    private val onSeasonExpand: (TrackedSeason, (List<TVEpisode>) -> Unit) -> Unit,
    private val onEpisodeClick: (TrackedSeason, TVEpisode) -> Unit
) : ListAdapter<TrackedSeason, TrackingSeasonAdapter.ViewHolder>(DiffUtilCallback()) {

    private var expandedSeasonId: Int? = null
    private val episodesCache = mutableMapOf<Int, List<TVEpisode>>()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val seasonHeader: View = itemView.findViewById(R.id.tracking_season_header)
        val seasonTitle: TextView = itemView.findViewById(R.id.tracking_season_title)
        val seasonArrow: ImageView = itemView.findViewById(R.id.tracking_season_arrow)
        val episodesRv: RecyclerView = itemView.findViewById(R.id.tracking_episodes_rv)
        
        private val episodeAdapter = TrackingEpisodeAdapter { episode ->
            onEpisodeClick(getItem(bindingAdapterPosition), episode)
        }

        init {
            episodesRv.layoutManager = LinearLayoutManager(itemView.context)
            episodesRv.adapter = episodeAdapter
            episodesRv.isNestedScrollingEnabled = false
        }

        fun bind(season: TrackedSeason) {
            seasonTitle.text = season.name ?: "Season ${season.seasonNumber}"
            
            updateExpansionState(season)

            seasonHeader.setOnClickListener {
                toggleExpansion(season)
            }
        }

        fun updateExpansionState(season: TrackedSeason) {
            val isExpanded = expandedSeasonId == season.id
            
            // Sync UI state before any async loading
            episodesRv.visibility = if (isExpanded) View.VISIBLE else View.GONE
            seasonArrow.rotation = if (isExpanded) 90f else 0f

            if (isExpanded) {
                val cached = episodesCache[season.id]
                if (cached != null) {
                    episodeAdapter.submitList(cached)
                } else {
                    // Show empty immediately to wipe any recycled data
                    episodeAdapter.submitList(null)
                    onSeasonExpand(season) { episodes ->
                        episodesCache[season.id] = episodes
                        // Double check expansion state hasn't changed during async call
                        if (expandedSeasonId == season.id) {
                            episodeAdapter.submitList(episodes)
                        }
                    }
                }
            } else {
                // Instantly clear data to prevent recycling issues
                episodeAdapter.submitList(null)
            }
        }

        private fun toggleExpansion(season: TrackedSeason) {
            val position = bindingAdapterPosition
            if (position == RecyclerView.NO_POSITION) return

            val wasExpanded = expandedSeasonId == season.id
            
            // Collapse previous if needed
            val oldExpandedId = expandedSeasonId
            expandedSeasonId = if (wasExpanded) null else season.id
            
            // Clean Sliding Transition with Fade In for episodes
            (itemView.parent as? ViewGroup)?.let { parent ->
                val transition = TransitionSet()
                    .addTransition(ChangeBounds())
                    .addTransition(Fade(Fade.IN).addTarget(episodesRv))
                    .setOrdering(TransitionSet.ORDERING_TOGETHER)
                    .setDuration(250)
                TransitionManager.beginDelayedTransition(parent, transition)
            }
            
            // Notify only expansion changes
            notifyItemChanged(position, "EXPANSION_CHANGE")

            if (oldExpandedId != null && oldExpandedId != season.id) {
                for (i in 0 until itemCount) {
                    if (getItem(i).id == oldExpandedId) {
                        notifyItemChanged(i, "EXPANSION_CHANGE")
                        break
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tracking_season, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            // Partial update for expansion state only
            holder.updateExpansionState(getItem(position))
        }
    }

    class DiffUtilCallback : DiffUtil.ItemCallback<TrackedSeason>() {
        override fun areItemsTheSame(oldItem: TrackedSeason, newItem: TrackedSeason): Boolean =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: TrackedSeason, newItem: TrackedSeason): Boolean =
            oldItem == newItem

        override fun getChangePayload(oldItem: TrackedSeason, newItem: TrackedSeason): Any? {
            return if (oldItem.id == newItem.id) {
                "EXPANSION_CHANGE"
            } else null
        }
    }
}
