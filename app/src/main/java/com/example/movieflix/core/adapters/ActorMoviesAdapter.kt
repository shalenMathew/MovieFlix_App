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
import com.example.movieflix.databinding.ItemSmallListBinding
import com.example.movieflix.domain.model.MovieResult

class ActorMoviesAdapter(
    private val onMovieClick: (MovieResult) -> Unit
) : ListAdapter<MovieResult, ActorMoviesAdapter.ViewHolder>(DiffUtilCallback()) {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ItemSmallListBinding = ItemSmallListBinding.bind(itemView)

        fun bind(movie: MovieResult) {
            binding.apply {
                itemListRatingTxt.text = String.format("%.1f", movie.voteAverage ?: 0.0)
                itemListPoster.loadImage(
                    Constants.TMDB_POSTER_IMAGE_BASE_URL_W342.plus(movie.posterPath)
                )

                root.setOnClickListener {
                    onMovieClick(movie)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_small_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
