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
import com.shalenmathew.movieflix.databinding.ItemTrackingBannerBinding
import com.shalenmathew.movieflix.domain.model.MovieResult
import com.shalenmathew.movieflix.domain.model.TrackedSeries

class TrackingAdapter(private var onPosterClick: ((movieResult: MovieResult) -> Unit)) :
    ListAdapter<TrackedSeries, TrackingAdapter.ViewHolder>(DiffUtilCallback()) {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ItemTrackingBannerBinding = ItemTrackingBannerBinding.bind(itemView)

        fun bind(series: TrackedSeries) = binding.apply {
            trackingBannerImage.loadImage(Constants.TMDB_IMAGE_BASE_URL_W780.plus(series.backdropPath))
            trackingBannerTitle.text = series.name

            // Click logic removed as per user request
            this.root.setOnClickListener(null)
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
