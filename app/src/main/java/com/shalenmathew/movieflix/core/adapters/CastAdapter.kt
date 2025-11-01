package com.shalenmathew.movieflix.core.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shalenmathew.movieflix.R
import com.shalenmathew.movieflix.core.utils.Constants
import com.shalenmathew.movieflix.core.utils.loadImage
import com.shalenmathew.movieflix.databinding.ItemCastBinding
import com.shalenmathew.movieflix.domain.model.CastMember

class CastAdapter : ListAdapter<CastMember, CastAdapter.ViewHolder>(CastDiffCallback()) {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ItemCastBinding = ItemCastBinding.bind(itemView)
        
        fun bind(castMember: CastMember) {
            binding.apply {
                itemCastName.text = castMember.name
                // Handle missing character name - display in uppercase
                itemCastCharacter.text = if (castMember.character.isNullOrBlank()) {
                    "CAST MEMBER"
                } else {
                    castMember.character.uppercase()
                }
                
                // Clear previous image and load new one to prevent recycling issues
                if (castMember.profilePath != null) {
                    itemCastProfileImage.loadImage(
                        Constants.TMDB_POSTER_IMAGE_BASE_URL_W342.plus(castMember.profilePath),
                        placeholder = ContextCompat.getDrawable(itemView.context, R.drawable.poster_bg)
                    )
                } else {
                    // Set default placeholder when no profile image
                    itemCastProfileImage.setImageDrawable(
                        ContextCompat.getDrawable(itemView.context, R.drawable.poster_bg)
                    )
                }
                
                // Navigate to actor detail on click
                root.setOnClickListener {
                    navigateToActorDetail(castMember)
                }
            }
        }
        
        private fun navigateToActorDetail(castMember: CastMember) {
            val intent = Intent(itemView.context, com.shalenmathew.movieflix.presentation.actor_detail.ActorDetailActivity::class.java).apply {
                putExtra(com.shalenmathew.movieflix.presentation.actor_detail.ActorDetailActivity.EXTRA_PERSON_ID, castMember.id)
                putExtra(com.shalenmathew.movieflix.presentation.actor_detail.ActorDetailActivity.EXTRA_PERSON_NAME, castMember.name)
            }
            itemView.context.startActivity(intent)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cast, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        // Clear the image when view is recycled to prevent wrong images showing
        holder.binding.itemCastProfileImage.setImageDrawable(null)
    }
}

class CastDiffCallback : DiffUtil.ItemCallback<CastMember>() {
    override fun areItemsTheSame(oldItem: CastMember, newItem: CastMember): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: CastMember, newItem: CastMember): Boolean {
        return oldItem == newItem
    }
}
