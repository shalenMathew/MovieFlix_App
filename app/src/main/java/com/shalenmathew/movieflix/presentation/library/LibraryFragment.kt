package com.shalenmathew.movieflix.presentation.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.shalenmathew.movieflix.core.utils.Constants.FAVORITES
import com.shalenmathew.movieflix.core.utils.Constants.WATCHLIST
import com.shalenmathew.movieflix.databinding.FragmentLibraryBinding
import com.shalenmathew.movieflix.presentation.favorites.FavFragment
import com.shalenmathew.movieflix.presentation.viewmodels.LibraryViewModel
import com.shalenmathew.movieflix.presentation.watchlist.WatchListFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LibraryFragment : Fragment() {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    private val libraryViewModel: LibraryViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup ViewPager2
        val adapter = LibraryPagerAdapter(this)
        binding.viewPager.adapter = adapter

        // Attach TabLayout
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> WATCHLIST
                1 -> FAVORITES
                else -> ""
            }
        }.attach()

        val selectedTab = libraryViewModel.selectedTab.value ?: 0
        binding.viewPager.currentItem = selectedTab

        // Listen for tab changes
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                libraryViewModel.setSelectedTab(tab?.position ?: 0)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private inner class LibraryPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> WatchListFragment()
                1 -> FavFragment()
                else -> WatchListFragment()
            }
        }
    }
}
