package com.shalenmathew.movieflix.presentation.library

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
import com.google.gson.Gson
import com.shalenmathew.movieflix.R
import com.shalenmathew.movieflix.core.adapters.TrackingAdapter
import com.shalenmathew.movieflix.core.utils.Constants
import com.shalenmathew.movieflix.core.utils.gone
import com.shalenmathew.movieflix.core.utils.visible
import com.shalenmathew.movieflix.databinding.FragmentTrackingBinding
import com.shalenmathew.movieflix.domain.model.TrackedSeries
import com.shalenmathew.movieflix.presentation.viewmodels.LibrarySearchViewModel
import com.shalenmathew.movieflix.presentation.viewmodels.SeriesTrackingViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class TrackingFragment : Fragment() {

    private val seriesTrackingViewModel: SeriesTrackingViewModel by viewModels()
    private val librarySearchVm: LibrarySearchViewModel by activityViewModels()

    private var _binding: FragmentTrackingBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: TrackingAdapter

    private var fullList: List<TrackedSeries> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTrackingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inIt()
        observer()
        observeSearch()
    }

    private fun inIt() {
        adapter = TrackingAdapter(onPosterClick = {
            val bundle = Bundle()
            bundle.putString(Constants.MEDIA_SEND_REQUEST_KEY, Gson().toJson(it))
            findNavController().navigate(R.id.movieDetailsFragment, bundle)
        })
        val spanCount = resources.getInteger(R.integer.grid_span_count)
        binding.fragmentTrackingRv.layoutManager = GridLayoutManager(requireContext(), spanCount)
        binding.fragmentTrackingRv.adapter = adapter
    }

    private fun observer() {
        seriesTrackingViewModel.allTrackedSeries.observe(viewLifecycleOwner) { list ->
            fullList = list
            val currentQuery = librarySearchVm.searchQuery.value
            if (currentQuery.isNullOrBlank()) {
                submitAndToggle(list)
            } else {
                applyFilter(currentQuery)
            }
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
        val filtered = fullList.filter { series ->
            series.name.contains(query, ignoreCase = true)
        }
        submitAndToggle(filtered)
    }

    private fun submitAndToggle(list: List<TrackedSeries>) {
        val query = librarySearchVm.searchQuery.value

        if (list.isNotEmpty()) {
            adapter.submitList(list)
            binding.fragmentTrackingRv.visible()
            binding.fragmentTrackingPlaceholder.gone()
        } else {
            adapter.submitList(emptyList())
            binding.fragmentTrackingRv.gone()

            if (query.isNullOrBlank()) {
                binding.tvNoResult.gone()
                binding.fragmentTrackingPlaceholder.visible()
            } else {
                binding.tvNoResult.visible()
                binding.fragmentTrackingPlaceholder.gone()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
