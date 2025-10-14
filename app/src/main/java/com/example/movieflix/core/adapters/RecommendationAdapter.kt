package com.example.movieflix.core.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.movieflix.R
import com.example.movieflix.core.utils.Constants
import com.example.movieflix.core.utils.DiffUtilCallback
import com.example.movieflix.core.utils.loadImage
import com.example.movieflix.databinding.ItemListBinding
import com.example.movieflix.domain.model.MovieResult

class RecommendationAdapter(private val posterClick:(movieResult:MovieResult)->Unit):ListAdapter<MovieResult, RecommendationAdapter.ViewHolder>(DiffUtilCallback()) {

  inner  class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
      var binding:ItemListBinding = ItemListBinding.bind(itemView)
      fun bind(movieResult: MovieResult){
          binding.apply {
              itemListRatingTxt.text = String.format("%.1f",movieResult.voteAverage)
              itemListPoster.loadImage(Constants.TMDB_POSTER_IMAGE_BASE_URL_W342.plus(movieResult.posterPath))

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