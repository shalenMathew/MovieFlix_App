package com.example.movieflix.presentation.library

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.movieflix.presentation.favorites.FavFragment
import com.example.movieflix.presentation.watchlist.WatchListFragment

/**
 * Pager adapter that hosts the library tabs for Watchlist and Favourites.
 */
class LibraryPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = TAB_COUNT

    override fun createFragment(position: Int): Fragment = when (position) {
        WATCHLIST_POSITION -> WatchListFragment()
        FAVORITES_POSITION -> FavFragment()
        else -> throw IllegalArgumentException("Unsupported tab index: $position")
    }

    companion object {
        private const val WATCHLIST_POSITION = 0
        private const val FAVORITES_POSITION = 1
        const val TAB_COUNT = 2
    }
}