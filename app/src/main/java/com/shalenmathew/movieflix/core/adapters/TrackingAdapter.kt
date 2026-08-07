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
import com.shalenmathew.movieflix.core.utils.Constants
import com.shalenmathew.movieflix.core.utils.loadImage
import com.shalenmathew.movieflix.domain.model.TrackedSeason
import com.shalenmathew.movieflix.domain.model.TrackedSeries
import com.shalenmathew.movieflix.domain.model.TVEpisode
import androidx.transition.TransitionManager
import androidx.transition.ChangeBounds
import androidx.transition.TransitionSet

class TrackingAdapter(
    private val onSeriesExpand: (TrackedSeries, (List<TrackedSeason>) -> Unit) -> Unit,
    private val onSeasonExpand: (TrackedSeason, (List<TVEpisode>) -> Unit) -> Unit,
    private val onUntrackClick: (TrackedSeries) -> Unit
) : ListAdapter<TrackedSeries, TrackingAdapter.ViewHolder>(DiffUtilCallback()) {

    private var expandedSeriesId: Int? = null
    private val seasonsCache = mutableMapOf<Int, List<TrackedSeason>>()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bannerCard: View = itemView.findViewById(R.id.tracking_series_banner_card)
        val bannerImage: ImageView = itemView.findViewById(R.id.tracking_banner_image)
        val bannerTitle: TextView = itemView.findViewById(R.id.tracking_banner_title)
        val bannerArrow: ImageView = itemView.findViewById(R.id.tracking_banner_arrow)
        val untrackIcon: ImageView = itemView.findViewById(R.id.tracking_untrack_icon)
        val seasonsRv: RecyclerView = itemView.findViewById(R.id.tracking_seasons_rv)
        
        private val seasonAdapter = TrackingSeasonAdapter(onSeasonExpand)

        init {
            seasonsRv.layoutManager = LinearLayoutManager(itemView.context)
            seasonsRv.adapter = seasonAdapter
            seasonsRv.isNestedScrollingEnabled = false
        }

        fun bind(series: TrackedSeries) {
            bannerImage.loadImage(Constants.TMDB_IMAGE_BASE_URL_W780.plus(series.backdropPath))
            bannerTitle.text = series.name

            val isExpanded = expandedSeriesId == series.id
            
            // Sync UI state before any async loading
            seasonsRv.visibility = if (isExpanded) View.VISIBLE else View.GONE
            bannerArrow.rotation = if (isExpanded) 90f else 0f
            
            // Untrack icon is now on the banner, always visible
            
            if (isExpanded) {
                val cached = seasonsCache[series.id]
                if (cached != null) {
                    seasonAdapter.submitList(cached)
                } else {
                    // Show empty immediately to wipe any recycled data
                    seasonAdapter.submitList(null)
                    onSeriesExpand(series) { seasons ->
                        seasonsCache[series.id] = seasons
                        // Double check expansion state hasn't changed during async call
                        if (expandedSeriesId == series.id) {
                            seasonAdapter.submitList(seasons)
                        }
                    }
                }
            } else {
                // Instantly clear data to prevent recycling issues
                seasonAdapter.submitList(null)
            }

            bannerCard.setOnClickListener {
                if (series.syncStatus == "PENDING") {
                    com.shalenmathew.movieflix.core.utils.showToast(
                        itemView.context, 
                        "Syncing episodes... please wait."
                    )
                    return@setOnClickListener
                }
                
                val position = bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION) return@setOnClickListener

                val wasExpanded = isExpanded
                expandedSeriesId = if (wasExpanded) null else series.id

                // Clean Sliding Transition (No Fade)
                (itemView.parent as? ViewGroup)?.let { parent ->
                    val transition = TransitionSet()
                        .addTransition(ChangeBounds())
                        .setOrdering(TransitionSet.ORDERING_TOGETHER)
                        .setDuration(250)
                    TransitionManager.beginDelayedTransition(parent, transition)
                }

                notifyItemChanged(position)
            }

            untrackIcon.setOnClickListener {
                onUntrackClick(series)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tracking_banner, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffUtilCallback : DiffUtil.ItemCallback<TrackedSeries>() {
        override fun areItemsTheSame(oldItem: TrackedSeries, newItem: TrackedSeries): Boolean =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: TrackedSeries, newItem: TrackedSeries): Boolean =
            oldItem == newItem
    }
}
