package com.example.movieflix.core.utils

import androidx.recyclerview.widget.DiffUtil
import com.example.movieflix.domain.model.MovieResult

class DiffUtilCallback:DiffUtil.ItemCallback<MovieResult>() {

    override fun areItemsTheSame(oldItem: MovieResult, newItem: MovieResult): Boolean {
        return oldItem.id==newItem.id
    }

    override fun areContentsTheSame(oldItem: MovieResult, newItem: MovieResult): Boolean {
return oldItem==newItem
    }
}