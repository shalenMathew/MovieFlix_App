package com.shalenmathew.movieflix.core.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shalenmathew.movieflix.R
import com.shalenmathew.movieflix.core.utils.Constants
import com.shalenmathew.movieflix.core.utils.DiffUtilCallback
import com.shalenmathew.movieflix.core.utils.loadImage
import com.shalenmathew.movieflix.databinding.ItemListBinding
import com.shalenmathew.movieflix.domain.model.MovieResult

class RecommendationAdapter(private val posterClick:(movieResult:MovieResult)->Unit):ListAdapter<MovieResult, RecommendationAdapter.ViewHolder>(DiffUtilCallback()) {

    private var scheduledMovieIds = setOf<Int>()

    fun updateScheduledMovies(scheduledIds: Set<Int>) {
        scheduledMovieIds = scheduledIds
        notifyDataSetChanged()
    }

  inner  class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
      var binding:ItemListBinding = ItemListBinding.bind(itemView)
      fun bind(movieResult: MovieResult){
          binding.apply {
              itemListRatingTxt.text = String.format("%.1f",movieResult.voteAverage)
              itemListPoster.loadImage(Constants.TMDB_POSTER_IMAGE_BASE_URL_W342.plus(movieResult.posterPath))

              // Show schedule icon if movie is scheduled
              itemListScheduleIcon.visibility = if (scheduledMovieIds.contains(movieResult.id)) {
                  View.VISIBLE
              } else {
                  View.GONE
              }

              root.setOnClickListener(){
                  posterClick(movieResult)
              }
          }
      }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}