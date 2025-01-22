package com.example.movieflix.core.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.movieflix.R
import com.example.movieflix.core.utils.Constants
import com.example.movieflix.core.utils.DiffUtilCallback
import com.example.movieflix.core.utils.isNetworkAvailable
import com.example.movieflix.core.utils.loadImage
import com.example.movieflix.data.local.entity.WatchListEntity
import com.example.movieflix.databinding.ItemListBinding
import com.example.movieflix.databinding.ItemSmallListBinding
import com.example.movieflix.domain.model.MovieResult
import com.example.movieflix.domain.usecases.WatchList

class WatchListAdapter(private var onPosterClick: ((movieResult: MovieResult) -> Unit)):
        ListAdapter<WatchListEntity, WatchListAdapter.ViewHolder>(DiffUtilCallback()) {
    inner class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView) {
       var binding: ItemSmallListBinding
        init {
            binding = ItemSmallListBinding.bind(itemView)
        }
fun bind(watchListEntity: WatchListEntity)=binding.apply{

    val item = watchListEntity.movieResult
    itemListPoster.loadImage(Constants.TMDB_POSTER_IMAGE_BASE_URL_W342.plus(item.posterPath))
    itemListRatingTxt.text = String.format("%.1f", item.voteAverage)

    root.setOnClickListener(){
        if (isNetworkAvailable(root.context)){
            onPosterClick(item)
        }else{
            Toast.makeText(root.context, "Internet req", Toast.LENGTH_SHORT).show()
        }
    }
}
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_small_list,parent,false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    class DiffUtilCallback : DiffUtil.ItemCallback<WatchListEntity>() {
        override fun areItemsTheSame(oldItem: WatchListEntity, newItem: WatchListEntity): Boolean =
            oldItem == newItem
        override fun areContentsTheSame(
            oldItem: WatchListEntity,
            newItem: WatchListEntity
        ): Boolean =
            oldItem == newItem
    }
}