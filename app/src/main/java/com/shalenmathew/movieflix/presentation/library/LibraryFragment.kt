package com.shalenmathew.movieflix.presentation.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.shalenmathew.movieflix.databinding.FragmentLibraryBinding
import com.shalenmathew.movieflix.presentation.favorites.FavFragment
import com.shalenmathew.movieflix.presentation.watchlist.WatchListFragment
import com.google.android.material.tabs.TabLayoutMediator
import com.shalenmathew.movieflix.core.utils.DataStoreReference
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LibraryFragment : Fragment() {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

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

        lifecycleScope.launch {
            val lastTab = DataStoreReference.getLastSelectedLibraryTab(requireContext()).first()

            binding.viewPager.setCurrentItem(lastTab, false)

            TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
                tab.text = when (position) {
                    0 -> "Watchlist"
                    1 -> "Favorites"
                    else -> ""
                }
            }.attach()
        }

        // Listen for tab changes
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                lifecycleScope.launch(Dispatchers.IO) {
                    DataStoreReference.setLastSelectedLibraryTab(
                        requireContext(),
                        tab?.position ?: 0
                    )
                }
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
