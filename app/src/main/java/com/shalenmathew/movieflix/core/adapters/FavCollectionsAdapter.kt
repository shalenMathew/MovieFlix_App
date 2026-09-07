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
import com.shalenmathew.movieflix.databinding.ItemFavCollectionBinding
import com.shalenmathew.movieflix.domain.model.UserCustomList

class FavCollectionsAdapter(
    private val onCollectionClick: (UserCustomList) -> Unit
) : ListAdapter<UserCustomList, FavCollectionsAdapter.ViewHolder>(CollectionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFavCollectionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemFavCollectionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: UserCustomList) {
            binding.collectionName.text = item.name
            binding.collectionCount.text = itemView.context.getString(R.string.items_count, item.movieCount)

            val posters = item.topPosters
            val posterViews = listOf(binding.poster1, binding.poster2, binding.poster3)
            val cardViews = listOf(binding.card1, binding.card2, binding.card3)

            posterViews.forEachIndexed { index, imageView ->
                if (index < posters.size) {
                    cardViews[index].visibility = View.VISIBLE
                    val path = posters[index]
                    val isLocal = path.startsWith("content://") || path.count { it == '/' } > 1
                    if (isLocal) {
                        imageView.loadImage(path)
                    } else {
                        imageView.loadImage(Constants.TMDB_POSTER_IMAGE_BASE_URL_W342.plus(path))
                    }
                } else {
                    cardViews[index].visibility = View.GONE
                }
            }
            
            if (posters.isEmpty()) {
                binding.card1.visibility = View.VISIBLE
                binding.poster1.setImageResource(R.drawable.poster_bg)
            }

            itemView.setOnClickListener { onCollectionClick(item) }
        }
    }

    class CollectionDiffCallback : DiffUtil.ItemCallback<UserCustomList>() {
        override fun areItemsTheSame(oldItem: UserCustomList, newItem: UserCustomList): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: UserCustomList, newItem: UserCustomList): Boolean =
            oldItem == newItem
    }
}
