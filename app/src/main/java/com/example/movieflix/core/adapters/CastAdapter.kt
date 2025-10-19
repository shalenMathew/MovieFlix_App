package com.example.movieflix.core.adapters

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.movieflix.R
import com.example.movieflix.core.utils.Constants
import com.example.movieflix.core.utils.loadImage
import com.example.movieflix.databinding.ItemCastBinding
import com.example.movieflix.domain.model.CastMember

class CastAdapter : ListAdapter<CastMember, CastAdapter.ViewHolder>(CastDiffCallback()) {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ItemCastBinding = ItemCastBinding.bind(itemView)
        
        fun bind(castMember: CastMember) {
            binding.apply {
                itemCastName.text = castMember.name
                itemCastCharacter.text = castMember.character
                
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
                
                // Setup social media icons
                setupSocialMedia(castMember)
            }
        }
        
        private fun setupSocialMedia(castMember: CastMember) {
            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()
            
            binding.apply {
                // Instagram
                if (!castMember.instagramId.isNullOrEmpty()) {
                    itemCastInstagram.visibility = View.VISIBLE
                    itemCastInstagram.setOnClickListener {
                        openUrl("https://www.instagram.com/${castMember.instagramId}", customTabsIntent)
                    }
                } else {
                    itemCastInstagram.visibility = View.GONE
                }
                
                // Twitter
                if (!castMember.twitterId.isNullOrEmpty()) {
                    itemCastTwitter.visibility = View.VISIBLE
                    itemCastTwitter.setOnClickListener {
                        openUrl("https://twitter.com/${castMember.twitterId}", customTabsIntent)
                    }
                } else {
                    itemCastTwitter.visibility = View.GONE
                }
                
                // Facebook
                if (!castMember.facebookId.isNullOrEmpty()) {
                    itemCastFacebook.visibility = View.VISIBLE
                    itemCastFacebook.setOnClickListener {
                        openUrl("https://www.facebook.com/${castMember.facebookId}", customTabsIntent)
                    }
                } else {
                    itemCastFacebook.visibility = View.GONE
                }
                
                // Hide container if no social media
                if (castMember.instagramId.isNullOrEmpty() &&
                    castMember.twitterId.isNullOrEmpty() &&
                    castMember.facebookId.isNullOrEmpty()) {
                    itemCastSocialsContainer.visibility = View.GONE
                } else {
                    itemCastSocialsContainer.visibility = View.VISIBLE
                }
            }
        }
        
        private fun openUrl(url: String, customTabsIntent: CustomTabsIntent) {
            try {
                customTabsIntent.launchUrl(itemView.context, Uri.parse(url))
            } catch (e: Exception) {
                e.printStackTrace()
            }
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
