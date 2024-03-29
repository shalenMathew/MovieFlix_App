package com.example.movieflix.core.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.movieflix.R
import com.example.movieflix.core.utils.Constants.TMDB_POSTER_IMAGE_BASE_URL_W342
import com.example.movieflix.core.utils.DiffUtilCallback
import com.example.movieflix.core.utils.isNetworkAvailable
import com.example.movieflix.core.utils.loadImage
import com.example.movieflix.core.utils.showToast
import com.example.movieflix.databinding.ItemListBinding
import com.example.movieflix.domain.model.MovieResult

class HorizontalAdapter(private var onPosterClick: ((movieResult: MovieResult) -> Unit)? = null)
    :ListAdapter<MovieResult, HorizontalAdapter.ViewHolder>(DiffUtilCallback()) {

    inner class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView) {

   private val binding:ItemListBinding
    init {
        binding=ItemListBinding.bind(itemView)
    }

        fun bind(item:MovieResult){
            binding.apply {
                val context = binding.root.context
                itemListPoster.loadImage(TMDB_POSTER_IMAGE_BASE_URL_W342.plus(item.posterPath))
                itemListRatingTxt.text = String.format("%.1f", item.voteAverage)

                itemListPoster.setOnClickListener(){
                    if(isNetworkAvailable(context)){
                        onPosterClick?.invoke(item)
                    }else{
                        showToast(context,"Internet connection req")
                    }
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