package com.shalenmathew.movieflix.core.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shalenmathew.movieflix.R
import com.shalenmathew.movieflix.core.utils.Constants
import com.shalenmathew.movieflix.core.utils.loadImage
import com.shalenmathew.movieflix.databinding.ItemSmallListBinding
import com.shalenmathew.movieflix.domain.model.MovieResult
import com.shalenmathew.movieflix.domain.model.TrackedSeries

class TrackingAdapter(private var onPosterClick: ((movieResult: MovieResult) -> Unit)) :
    ListAdapter<TrackedSeries, TrackingAdapter.ViewHolder>(DiffUtilCallback()) {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ItemSmallListBinding = ItemSmallListBinding.bind(itemView)

        fun bind(series: TrackedSeries) = binding.apply {
            itemListPoster.loadImage(Constants.TMDB_POSTER_IMAGE_BASE_URL_W342.plus(series.posterPath))
            // We don't have voteAverage in TrackedSeries entity currently, but we can add it or just show progress
            itemListRatingTxt.visibility = View.GONE
            itemListScheduleIcon.visibility = View.GONE

            // Click logic removed as per user request
            root.setOnClickListener(null)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_small_list, parent, false)
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
