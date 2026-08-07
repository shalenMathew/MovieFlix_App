package com.shalenmathew.movieflix.presentation.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.asFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
import com.shalenmathew.movieflix.domain.model.TrackedSeason
import com.shalenmathew.movieflix.domain.model.TVEpisode
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
        adapter = TrackingAdapter(
            onSeriesExpand = { series, callback ->
                seriesTrackingViewModel.getSeasonsForSeries(series.id).observe(viewLifecycleOwner) { seasons ->
                    callback(seasons)
                }
            },
            onSeasonExpand = { season, callback ->
                seriesTrackingViewModel.getEpisodesForSeason(season.id).observe(viewLifecycleOwner) { episodes ->
                    callback(episodes.map { 
                        com.shalenmathew.movieflix.domain.model.TVEpisode(
                            id = it.id,
                            airDate = null,
                            episodeNumber = it.episodeNumber,
                            name = it.name,
                            overview = it.overview,
                            runtime = it.runtime,
                            seasonNumber = it.seasonNumber,
                            stillPath = it.stillPath,
                            voteAverage = null,
                            isWatched = it.isWatched
                        )
                    })
                }
            },
            onUntrackClick = { series ->
                val ctx = context ?: return@TrackingAdapter
                com.google.android.material.dialog.MaterialAlertDialogBuilder(ctx, R.style.TrackingAlertDialog)
                    .setTitle("Stop Tracking Series?")
                    .setMessage("This will remove \"${series.name}\" from your library and delete all cached episodes for offline viewing.")
                    .setPositiveButton("Stop Tracking") { _, _ ->
                        seriesTrackingViewModel.untrackSeries(series.id)
                        com.shalenmathew.movieflix.core.utils.showToast(ctx, "Series removed from tracking")
                    }
                    .setNegativeButton("Keep Tracking", null)
                    .show()
            },
            onEpisodeClick = { series, season, episode ->
                navigateToEpisodeDetails(series, season, episode)
            }
        )
        binding.fragmentTrackingRv.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        binding.fragmentTrackingRv.adapter = adapter
        
        // Disable default animator to prevent clashing with custom TransitionManager
        binding.fragmentTrackingRv.itemAnimator = null
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

    private fun navigateToEpisodeDetails(
        series: TrackedSeries,
        season: TrackedSeason,
        episode: TVEpisode
    ) {
        val seriesId = series.id
        val seasonNumber = season.seasonNumber

        // One-shot fetch using lifecycleScope to prevent accumulating observers
        viewLifecycleOwner.lifecycleScope.launch {
            // Using first() to get the current list once and then stop observing
            val episodes = seriesTrackingViewModel.getEpisodesForSeason(season.id).asFlow().first()
            
            val domainEpisodes = episodes.map {
                com.shalenmathew.movieflix.domain.model.TVEpisode(
                    id = it.id,
                    airDate = null,
                    episodeNumber = it.episodeNumber,
                    name = it.name,
                    overview = it.overview,
                    runtime = it.runtime,
                    seasonNumber = it.seasonNumber,
                    stillPath = it.stillPath,
                    voteAverage = null,
                    isWatched = it.isWatched
                )
            }

            val episodeIndex = domainEpisodes.indexOfFirst { it.id == episode.id }
            if (episodeIndex == -1) return@launch

            // Prepare Data Holder
            com.shalenmathew.movieflix.presentation.episode_details.EpisodeDataHolder.setData(
                episodeList = domainEpisodes,
                season = seasonNumber,
                showName = series.name,
                totalSeasons = 0,
                tvShowId = seriesId,
                onSeasonChange = { _ ->
                    context?.let { com.shalenmathew.movieflix.core.utils.showToast(it, "Swap seasons in the Tracking list") }
                }
            )

            val intent = com.shalenmathew.movieflix.presentation.episode_details.EpisodeDetailsActivity.newIntent(
                requireContext(),
                episodeIndex
            )
            startActivity(intent)
        }
    }
}
