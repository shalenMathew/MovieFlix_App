package com.shalenmathew.movieflix.core.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shalenmathew.movieflix.R
import com.shalenmathew.movieflix.core.utils.loadImage
import com.shalenmathew.movieflix.data.local_storage.entity.PersonalGalleryEntity

sealed class GalleryItem {
    object AddButton : GalleryItem()
    data class Image(val entity: PersonalGalleryEntity) : GalleryItem()
}

class PersonalGalleryAdapter(
    private val onImageClick: (PersonalGalleryEntity) -> Unit,
    private val onAddClick: () -> Unit,
    private val onLongClick: (PersonalGalleryEntity) -> Unit
) : ListAdapter<GalleryItem, RecyclerView.ViewHolder>(GalleryDiffCallback()) {

    private val TYPE_ADD = 0
    private val TYPE_IMAGE = 1

    inner class AddViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener { onAddClick() }
        }
    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.item_gallery_image)
        fun bind(image: PersonalGalleryEntity) {
            imageView.loadImage(image.imagePath)
            itemView.setOnClickListener { onImageClick(image) }
            itemView.setOnLongClickListener {
                onLongClick(image)
                true
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is GalleryItem.AddButton -> TYPE_ADD
            is GalleryItem.Image -> TYPE_IMAGE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_ADD) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_gallery_add, parent, false)
            AddViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_personal_gallery, parent, false)
            ImageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        if (holder is ImageViewHolder && item is GalleryItem.Image) {
            holder.bind(item.entity)
        }
    }

    class GalleryDiffCallback : DiffUtil.ItemCallback<GalleryItem>() {
        override fun areItemsTheSame(oldItem: GalleryItem, newItem: GalleryItem): Boolean {
            return if (oldItem is GalleryItem.Image && newItem is GalleryItem.Image) {
                oldItem.entity.id == newItem.entity.id
            } else {
                oldItem is GalleryItem.AddButton && newItem is GalleryItem.AddButton
            }
        }

        override fun areContentsTheSame(oldItem: GalleryItem, newItem: GalleryItem): Boolean {
            return oldItem == newItem
        }
    }
}
