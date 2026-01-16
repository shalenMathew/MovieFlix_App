package com.shalenmathew.movieflix.presentation.watchlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.shalenmathew.movieflix.R
import com.shalenmathew.movieflix.core.adapters.WatchListAdapter
import com.shalenmathew.movieflix.core.utils.Constants
import com.shalenmathew.movieflix.core.utils.gone
import com.shalenmathew.movieflix.core.utils.visible
import com.shalenmathew.movieflix.databinding.FragmentWatchListBinding
import com.shalenmathew.movieflix.data.local_storage.entity.WatchListEntity
import com.shalenmathew.movieflix.presentation.viewmodels.LibrarySearchViewModel
import com.shalenmathew.movieflix.presentation.viewmodels.ScheduledViewModel
import com.shalenmathew.movieflix.presentation.viewmodels.WatchListViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
@AndroidEntryPoint
class WatchListFragment : Fragment() {

    private val watchListViewModel: WatchListViewModel by viewModels()
    private val scheduledViewModel: ScheduledViewModel by viewModels()
    private val librarySearchVm: LibrarySearchViewModel by activityViewModels()

    private var _binding: FragmentWatchListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: WatchListAdapter

    private var fullList: List<WatchListEntity> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentWatchListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inIt()
        observer()
        observeSearch()
    }

    private fun inIt() {
        watchListViewModel.getAllWatchListData()
        adapter = WatchListAdapter(onPosterClick = {
            val bundle = Bundle()
            bundle.putString(Constants.MEDIA_SEND_REQUEST_KEY, Gson().toJson(it))
            findNavController().navigate(R.id.movieDetailsFragment, bundle)
        })
        binding.fragmentWatchListRv.layoutManager = GridLayoutManager(requireContext(),3)
        binding.fragmentWatchListRv.adapter = adapter
    }

    private fun observer() {
        watchListViewModel.getAllWatchListData().observe(viewLifecycleOwner) { list ->
            fullList = list
            val currentQuery = librarySearchVm.searchQuery.value
            if (currentQuery.isNullOrBlank()) {
                submitAndToggle(list)
            } else {
                applyFilter(currentQuery)
            }
        }
        scheduledViewModel.getAllScheduledMovies().observe(viewLifecycleOwner) { scheduledList ->
            val ids = scheduledList.mapNotNull { entity -> entity.id }.toSet()
            adapter.updateScheduledMovies(ids)
        }
    }

    private fun observeSearch() {
        lifecycleScope.launchWhenStarted {
            librarySearchVm.searchQuery.collectLatest { query ->
                if (query.isNullOrBlank()) {
                    submitAndToggle(fullList)
                } else {
                    applyFilter(query)
                }
            }
        }
    }

    private fun applyFilter(query: String) {
        val filtered = fullList.filter { entity ->
            val title = entity.movieResult?.title ?: entity.movieResult?.name ?: ""
            title.contains(query, ignoreCase = true)
        }
        submitAndToggle(filtered)
    }

    private fun submitAndToggle(list: List<WatchListEntity>) {
        val query = librarySearchVm.searchQuery.value

        if (list.isNotEmpty()) {
            adapter.submitList(list)
            binding.fragmentWatchListRv.visible()
            binding.fragmentWatchListPlaceholder.gone()
            binding.randomEmoji.gone()
        }
        else {
            adapter.submitList(emptyList())
            binding.fragmentWatchListRv.gone()

            if (query.isBlank()) {
                binding.tvNoResult.gone()
                binding.fragmentWatchListPlaceholder.visible()
                binding.randomEmoji.visible()
            }
            else {
                binding.tvNoResult.visible()
                binding.fragmentWatchListPlaceholder.gone()
                binding.randomEmoji.gone()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}