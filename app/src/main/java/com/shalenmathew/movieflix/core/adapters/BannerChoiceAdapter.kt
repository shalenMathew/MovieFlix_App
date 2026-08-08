package com.shalenmathew.movieflix.core.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shalenmathew.movieflix.R
import com.shalenmathew.movieflix.core.utils.Constants
import com.shalenmathew.movieflix.core.utils.loadImage

class BannerChoiceAdapter(
    private val onBannerClick: (String) -> Unit
) : ListAdapter<String, BannerChoiceAdapter.ViewHolder>(BannerDiffCallback()) {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.banner_choice_image)

        fun bind(path: String) {
            image.loadImage(Constants.TMDB_IMAGE_BASE_URL_W500.plus(path))
            itemView.setOnClickListener { onBannerClick(path) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_banner_choice, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class BannerDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
    }
}
