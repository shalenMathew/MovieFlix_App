package com.example.movieflix.core.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.movieflix.R
import com.example.movieflix.core.utils.Constants
import com.example.movieflix.core.utils.DiffUtilCallback
import com.example.movieflix.core.utils.isNetworkAvailable
import com.example.movieflix.core.utils.loadImage
import com.example.movieflix.databinding.ItemTopMovieBinding
import com.example.movieflix.domain.model.MovieResult

class TrendingMovieAdapter(private val onClick:(movie:MovieResult)-> Unit): ListAdapter<MovieResult,TrendingMovieAdapter.ViewHolder>(DiffUtilCallback()) {

   inner  class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){

         private val binding:ItemTopMovieBinding
         init {
             binding = ItemTopMovieBinding.bind(itemView)
         }
        fun bind(movie:MovieResult){
            binding.apply {
                val context = root.context
                movieNameText.text=movie.title
                movieImage.loadImage(Constants.TMDB_IMAGE_BASE_URL_W500.plus(movie.backdropPath))

                root.setOnClickListener(){
                   if(isNetworkAvailable(context)){
                       onClick(movie)
                   }else{
                       Toast.makeText(context, "Check ur internet connection ", Toast.LENGTH_SHORT).show()
                   }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_top_movie,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


}