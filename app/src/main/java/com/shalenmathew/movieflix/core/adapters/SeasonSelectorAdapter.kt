package com.shalenmathew.movieflix.core.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shalenmathew.movieflix.databinding.ItemSeasonSelectorBinding
import com.shalenmathew.movieflix.domain.model.TVSeasonBasic

class SeasonSelectorAdapter(
    private val seasons: List<TVSeasonBasic>,
    private val onSeasonSelected: (TVSeasonBasic) -> Unit
) : RecyclerView.Adapter<SeasonSelectorAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemSeasonSelectorBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(season: TVSeasonBasic) {
            binding.apply {
                val seasonNumber = season.seasonNumber ?: 0
                seasonTitle.text = season.name ?: "Season $seasonNumber"
                
                val episodeCount = season.episodeCount ?: 0
                seasonInfo.text = if (episodeCount > 0) {
                    "$episodeCount episode${if (episodeCount != 1) "s" else ""}"
                } else {
                    "No episodes"
                }
                
                root.setOnClickListener {
                    onSeasonSelected(season)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSeasonSelectorBinding.inflate(
            LayoutInflater.from(parent.context), 
            parent, 
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(seasons[position])
    }

    override fun getItemCount() = seasons.size
}
