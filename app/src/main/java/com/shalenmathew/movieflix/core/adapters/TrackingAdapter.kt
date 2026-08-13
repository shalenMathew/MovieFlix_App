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
    private val onUntrackClick: (TrackedSeries) -> Unit,
    private val onEpisodeClick: (TrackedSeries, TrackedSeason, TVEpisode) -> Unit,
    private val onEpisodeWatchedClick: (TrackedSeries, TrackedSeason, TVEpisode) -> Unit,
    private val onSeriesLongClick: (TrackedSeries) -> Unit
) : ListAdapter<TrackedSeries, TrackingAdapter.ViewHolder>(DiffUtilCallback()) {

    private var expandedSeriesId: Int? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bannerCard: View = itemView.findViewById(R.id.tracking_series_banner_card)
        val bannerImage: ImageView = itemView.findViewById(R.id.tracking_banner_image)
        val bannerTitle: TextView = itemView.findViewById(R.id.tracking_banner_title)
        val lastWatchedText: TextView = itemView.findViewById(R.id.tracking_banner_last_watched)
        val bannerArrow: ImageView = itemView.findViewById(R.id.tracking_banner_arrow)
        val untrackIcon: ImageView = itemView.findViewById(R.id.tracking_untrack_icon)
        val seasonsRv: RecyclerView = itemView.findViewById(R.id.tracking_seasons_rv)
        
        private val seasonAdapter = TrackingSeasonAdapter(
            onSeasonExpand = onSeasonExpand,
            onEpisodeClick = { season, episode ->
                onEpisodeClick(getItem(bindingAdapterPosition), season, episode)
            },
            onEpisodeWatchedClick = { season, episode ->
                onEpisodeWatchedClick(getItem(bindingAdapterPosition), season, episode)
            }
        )

        init {
            seasonsRv.layoutManager = LinearLayoutManager(itemView.context)
            seasonsRv.adapter = seasonAdapter
            seasonsRv.isNestedScrollingEnabled = false
            seasonsRv.itemAnimator = null
        }

        fun bind(series: TrackedSeries) {
            bannerImage.loadImage(Constants.TMDB_IMAGE_BASE_URL_W780.plus(series.backdropPath))
            bannerTitle.text = series.name
            
            // Set last watched info
            if (series.lastWatchedSeasonNumber != null && series.lastWatchedEpisodeNumber != null) {
                lastWatchedText.text = itemView.context.getString(R.string.lbl_last_watched, series.lastWatchedSeasonNumber, series.lastWatchedEpisodeNumber)
                lastWatchedText.visibility = View.VISIBLE
            } else {
                lastWatchedText.visibility = View.GONE
            }
            
            updateExpansionState(series)

            bannerCard.setOnClickListener {
                toggleExpansion(series)
            }

            untrackIcon.setOnClickListener {
                onUntrackClick(series)
            }

            bannerCard.setOnLongClickListener {
                onSeriesLongClick(series)
                true
            }
        }

        fun updateExpansionState(series: TrackedSeries) {
            val isExpanded = expandedSeriesId == series.id
            
            seasonsRv.visibility = if (isExpanded) View.VISIBLE else View.GONE
            bannerArrow.rotation = if (isExpanded) 90f else 0f
            
            if (isExpanded) {
                onSeriesExpand(series) { seasons ->
                    seasonAdapter.submitList(seasons)
                }
            } else {
                seasonAdapter.submitList(null)
            }
        }

        fun updateBookmarkInfo(series: TrackedSeries) {
            if (series.lastWatchedSeasonNumber != null && series.lastWatchedEpisodeNumber != null) {
                lastWatchedText.text = itemView.context.getString(R.string.lbl_last_watched, series.lastWatchedSeasonNumber, series.lastWatchedEpisodeNumber)
                lastWatchedText.visibility = View.VISIBLE
            } else {
                lastWatchedText.visibility = View.GONE
            }
        }

        fun updateBanner(series: TrackedSeries) {
            bannerImage.loadImage(Constants.TMDB_IMAGE_BASE_URL_W780.plus(series.backdropPath))
        }

        private fun toggleExpansion(series: TrackedSeries) {
            if (series.syncStatus == "PENDING") {
                com.shalenmathew.movieflix.core.utils.showToast(
                    itemView.context, 
                    itemView.context.getString(R.string.msg_syncing_episodes)
                )
                return
            }
            
            val position = bindingAdapterPosition
            if (position == RecyclerView.NO_POSITION) return

            val oldExpandedId = expandedSeriesId
            expandedSeriesId = if (expandedSeriesId == series.id) null else series.id

            (itemView.parent as? ViewGroup)?.let { parent ->
                val transition = TransitionSet()
                    .addTransition(ChangeBounds())
                    .setOrdering(TransitionSet.ORDERING_TOGETHER)
                    .setDuration(250)
                TransitionManager.beginDelayedTransition(parent, transition)
            }

            notifyItemChanged(position, "EXPANSION_CHANGE")
            
            if (oldExpandedId != null && oldExpandedId != series.id) {
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
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tracking_banner, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            val combinedPayloads = payloads.flatMap { if (it is List<*>) it else listOf(it) }
            
            if (combinedPayloads.contains("EXPANSION_CHANGE")) {
                holder.updateExpansionState(getItem(position))
            }
            if (combinedPayloads.contains("BOOKMARK_CHANGE")) {
                holder.updateBookmarkInfo(getItem(position))
            }
            if (combinedPayloads.contains("BANNER_CHANGE")) {
                holder.updateBanner(getItem(position))
            }
        }
    }

    class DiffUtilCallback : DiffUtil.ItemCallback<TrackedSeries>() {
        override fun areItemsTheSame(oldItem: TrackedSeries, newItem: TrackedSeries): Boolean =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: TrackedSeries, newItem: TrackedSeries): Boolean =
            oldItem == newItem

        override fun getChangePayload(oldItem: TrackedSeries, newItem: TrackedSeries): Any? {
            val payloads = mutableListOf<String>()
            if (oldItem.lastWatchedEpisodeId != newItem.lastWatchedEpisodeId) {
                payloads.add("BOOKMARK_CHANGE")
            }
            if (oldItem.backdropPath != newItem.backdropPath) {
                payloads.add("BANNER_CHANGE")
            }
            return if (payloads.isNotEmpty()) payloads else null
        }
    }
}
