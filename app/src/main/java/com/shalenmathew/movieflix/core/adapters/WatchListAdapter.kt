package com.shalenmathew.movieflix.core.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shalenmathew.movieflix.R
import com.shalenmathew.movieflix.core.utils.Constants
import com.shalenmathew.movieflix.core.utils.isNetworkAvailable
import com.shalenmathew.movieflix.core.utils.loadImage
import com.shalenmathew.movieflix.data.local_storage.entity.WatchListEntity
import com.shalenmathew.movieflix.databinding.ItemSmallListBinding
import com.shalenmathew.movieflix.domain.model.MovieResult

class WatchListAdapter(private var onPosterClick: ((movieResult: MovieResult) -> Unit)):
        ListAdapter<WatchListEntity, WatchListAdapter.ViewHolder>(DiffUtilCallback()) {
    
    private var scheduledMovieIds = setOf<Int>()

    fun updateScheduledMovies(scheduledIds: Set<Int>) {
        scheduledMovieIds = scheduledIds
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView) {
       var binding: ItemSmallListBinding
        init {
            binding = ItemSmallListBinding.bind(itemView)
        }
fun bind(watchListEntity: WatchListEntity)=binding.apply{

    val item = watchListEntity.movieResult
    itemListPoster.loadImage(Constants.TMDB_POSTER_IMAGE_BASE_URL_W342.plus(item.posterPath))
    itemListRatingTxt.text = String.format("%.1f", item.voteAverage)

    // Show schedule icon if movie is scheduled
    itemListScheduleIcon.visibility = if (scheduledMovieIds.contains(item.id)) {
        android.view.View.VISIBLE
    } else {
        android.view.View.GONE
    }

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