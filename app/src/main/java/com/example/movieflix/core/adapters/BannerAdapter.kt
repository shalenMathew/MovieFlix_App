package com.example.movieflix.core.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.movieflix.R
import com.example.movieflix.core.utils.Constants.TMDB_IMAGE_BASE_URL_W780
import com.example.movieflix.core.utils.DiffUtilCallback
import com.example.movieflix.core.utils.getGenreListById
import com.example.movieflix.core.utils.loadImage
import com.example.movieflix.databinding.BannerItemBinding
import com.example.movieflix.domain.model.MovieResult
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener

// listAdapter is an enhanced version of recycle view which ses DiffUtil by default
// diff util only updates the the item in a list in which changes where made,unlike recycler view which makes a whole new list if changes were observed

class BannerAdapter():ListAdapter<MovieResult, BannerAdapter.ViewHolder>(DiffUtilCallback()) {



    class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){

        private var youTubePlayerListener: AbstractYouTubePlayerListener? = null
        private var youTubePlayer: YouTubePlayer? = null

        private val binding:BannerItemBinding = BannerItemBinding.bind(itemView)

        fun bind(item:MovieResult)=binding.apply {
            bannerItemTitle.text = item.title
            bannerItemGenere.text = getGenreListById(item.genreIds).joinToString(" • ") {
                it.name
            }
            bannerItemBanner.loadImage(TMDB_IMAGE_BASE_URL_W780.plus(item.backdropPath))

//            initializePlayer(item.)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.banner_item,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}