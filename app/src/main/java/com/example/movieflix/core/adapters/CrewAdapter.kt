package com.example.movieflix.core.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.movieflix.R
import com.example.movieflix.core.utils.Constants
import com.example.movieflix.core.utils.loadImage
import com.example.movieflix.databinding.ItemCastBinding
import com.example.movieflix.domain.model.CrewMember

class CrewAdapter : ListAdapter<CrewMember, CrewAdapter.ViewHolder>(CrewDiffCallback()) {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ItemCastBinding = ItemCastBinding.bind(itemView)
        
        fun bind(crewMember: CrewMember) {
            binding.apply {
                itemCastName.text = crewMember.name
                itemCastCharacter.text = crewMember.job
                
                // Load crew member image
                if (crewMember.profilePath != null) {
                    itemCastProfileImage.loadImage(
                        Constants.TMDB_POSTER_IMAGE_BASE_URL_W342.plus(crewMember.profilePath),
                        placeholder = ContextCompat.getDrawable(itemView.context, R.drawable.poster_bg)
                    )
                } else {
                    itemCastProfileImage.setImageDrawable(
                        ContextCompat.getDrawable(itemView.context, R.drawable.poster_bg)
                    )
                }
                
                // Navigate to actor detail on click
                root.setOnClickListener {
                    navigateToActorDetail(crewMember)
                }
            }
        }
        
        private fun navigateToActorDetail(crewMember: CrewMember) {
            val intent = Intent(itemView.context, com.example.movieflix.presentation.actor_detail.ActorDetailActivity::class.java).apply {
                putExtra(com.example.movieflix.presentation.actor_detail.ActorDetailActivity.EXTRA_PERSON_ID, crewMember.id)
                putExtra(com.example.movieflix.presentation.actor_detail.ActorDetailActivity.EXTRA_PERSON_NAME, crewMember.name)
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
        holder.binding.itemCastProfileImage.setImageDrawable(null)
    }
}

class CrewDiffCallback : DiffUtil.ItemCallback<CrewMember>() {
    override fun areItemsTheSame(oldItem: CrewMember, newItem: CrewMember): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: CrewMember, newItem: CrewMember): Boolean {
        return oldItem == newItem
    }
}
