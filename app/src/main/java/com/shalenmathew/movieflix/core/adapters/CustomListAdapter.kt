package com.shalenmathew.movieflix.core.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shalenmathew.movieflix.R
import com.shalenmathew.movieflix.core.utils.Constants
import com.shalenmathew.movieflix.core.utils.loadImage
import com.shalenmathew.movieflix.domain.model.UserCustomList

class CustomListAdapter(
    private val onListClick: (UserCustomList) -> Unit,
    private val onDeleteClick: (UserCustomList) -> Unit
) : ListAdapter<UserCustomList, CustomListAdapter.ViewHolder>(ListDiffCallback()) {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.list_name)
        val description: TextView = itemView.findViewById(R.id.list_description)
        val deleteBtn: ImageView = itemView.findViewById(R.id.delete_list_btn)
        
        private val posterImages = listOf<ImageView>(
            itemView.findViewById(R.id.poster_1),
            itemView.findViewById(R.id.poster_2),
            itemView.findViewById(R.id.poster_3),
            itemView.findViewById(R.id.poster_4)
        )

        fun bind(list: UserCustomList) {
            name.text = list.name
            description.text = list.description ?: ""
            description.visibility = if (list.description.isNullOrEmpty()) View.GONE else View.VISIBLE
            
            // Bind top 4 posters
            val topPosters = list.topPosters
            posterImages.forEachIndexed { index, imageView ->
                if (index < topPosters.size) {
                    imageView.visibility = View.VISIBLE
                    val path = topPosters[index]
                    val isLocal = path.startsWith("content://") || path.count { it == '/' } > 1
                    if (isLocal) {
                        imageView.loadImage(path)
                    } else {
                        imageView.loadImage(Constants.TMDB_POSTER_IMAGE_BASE_URL_W342.plus(path))
                    }
                } else if (index == 0 && topPosters.isEmpty()) {
                    imageView.visibility = View.VISIBLE
                    imageView.setImageResource(R.drawable.poster_bg)
                } else {
                    imageView.visibility = View.GONE
                }
            }

            itemView.setOnClickListener { onListClick(list) }
            deleteBtn.setOnClickListener { onDeleteClick(list) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_custom_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ListDiffCallback : DiffUtil.ItemCallback<UserCustomList>() {
        override fun areItemsTheSame(oldItem: UserCustomList, newItem: UserCustomList): Boolean =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: UserCustomList, newItem: UserCustomList): Boolean =
            oldItem == newItem
    }
}
