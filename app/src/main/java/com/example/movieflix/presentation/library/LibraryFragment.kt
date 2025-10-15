package com.example.movieflix.presentation.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.movieflix.databinding.FragmentLibraryBinding
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

/**
 * Container fragment that hosts the Library tabs.
 */
@AndroidEntryPoint
class LibraryFragment : Fragment() {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPagerWithTabs()
    }

    private fun setupViewPagerWithTabs() {
        val pagerAdapter = LibraryPagerAdapter(this)
        binding.libraryViewPager.adapter = pagerAdapter
        binding.libraryViewPager.offscreenPageLimit = LibraryPagerAdapter.TAB_COUNT
        binding.libraryViewPager.isUserInputEnabled = true

        TabLayoutMediator(binding.libraryTabs, binding.libraryViewPager) { tab, position ->
            tab.text = when (position) {
                0 -> binding.root.context.getString(com.example.movieflix.R.string.library_tab_watchlist)
                1 -> binding.root.context.getString(com.example.movieflix.R.string.library_tab_favorites)
                else -> ""
            }
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}