package com.example.movieflix.core.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.movieflix.R
import com.example.movieflix.databinding.HorizontalFeedItemListBinding
import com.example.movieflix.domain.model.HomeFeed
import com.example.movieflix.domain.model.MovieResult

class HomeAdapter(
    private val onPosterClick:(movieResult:MovieResult)->Unit,
    private val onLoadMore:(categoryTitle:String)->Unit
):ListAdapter<HomeFeed, HomeAdapter.ViewHolder>(
    DiffUtilCallback()
)
{
    private val adapterMap = mutableMapOf<String, HorizontalAdapter>()
    private var scheduledMovieIds = setOf<Int>()

    fun addMoreItemsToCategory(categoryTitle: String, newItems: List<MovieResult>) {
        adapterMap[categoryTitle]?.addMoreItems(newItems)
    }

    fun setLoadingForCategory(categoryTitle: String, isLoading: Boolean) {
        adapterMap[categoryTitle]?.setLoadingState(isLoading)
    }

    fun updateScheduledMovies(scheduledIds: Set<Int>) {
        scheduledMovieIds = scheduledIds
        // Update all existing adapters
        adapterMap.values.forEach { adapter ->
            adapter.updateScheduledMovies(scheduledIds)
        }
    }

    inner class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView) {
        private var binding:HorizontalFeedItemListBinding = HorizontalFeedItemListBinding.bind(itemView)

       fun bind(homeFeed: HomeFeed){
            binding.apply {
                horizontalFeedListItemTitle.text=homeFeed.title

                val horizontalAdapter = adapterMap.getOrPut(homeFeed.title) {
                    HorizontalAdapter(
                        onPosterClick = onPosterClick,
                        onLoadMore = { onLoadMore(homeFeed.title) }
                    ).also { newAdapter ->
                        // Apply scheduled IDs to newly created adapter
                        newAdapter.updateScheduledMovies(scheduledMovieIds)
                    }
                }
                horizontalFeedListItemRv.adapter=horizontalAdapter
                horizontalAdapter.submitList(homeFeed.list)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.horizontal_feed_item_list,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
      holder.bind(getItem(position))
    }

    class DiffUtilCallback : DiffUtil.ItemCallback<HomeFeed>() {

        override fun areItemsTheSame(oldItem: HomeFeed, newItem: HomeFeed): Boolean =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: HomeFeed, newItem: HomeFeed): Boolean =
            oldItem == newItem
    }

}