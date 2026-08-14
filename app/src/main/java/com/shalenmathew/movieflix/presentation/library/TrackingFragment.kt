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
import com.shalenmathew.movieflix.core.adapters.BannerChoiceAdapter
import com.shalenmathew.movieflix.core.utils.Constants
import com.shalenmathew.movieflix.core.utils.gone
import com.shalenmathew.movieflix.core.utils.visible
import com.shalenmathew.movieflix.databinding.FragmentTrackingBinding
import com.shalenmathew.movieflix.domain.model.TrackedSeries
import com.shalenmathew.movieflix.domain.model.TrackedSeason
import com.shalenmathew.movieflix.domain.model.TVEpisode
import com.shalenmathew.movieflix.presentation.viewmodels.LibrarySearchViewModel
import com.shalenmathew.movieflix.presentation.viewmodels.SeriesTrackingViewModel
import com.shalenmathew.movieflix.presentation.viewmodels.WatchListViewModel
import com.shalenmathew.movieflix.presentation.viewmodels.FavMovieViewModel
import com.shalenmathew.movieflix.domain.model.MovieResult
import com.shalenmathew.movieflix.core.utils.loadImage
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class TrackingFragment : Fragment() {

    private val seriesTrackingViewModel: SeriesTrackingViewModel by viewModels()
    private val librarySearchVm: LibrarySearchViewModel by activityViewModels()
    private val watchListViewModel: WatchListViewModel by viewModels()
    private val favMovieViewModel: FavMovieViewModel by viewModels()

    private var _binding: FragmentTrackingBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: TrackingAdapter
    private var completionDialog: BottomSheetDialog? = null

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
                // Use a standard observer but tied to the series ID to avoid accumulation
                seriesTrackingViewModel.getSeasonsForSeries(series.id).observe(viewLifecycleOwner) { seasons ->
                    callback(seasons)
                }
            },
            onSeasonExpand = { season, callback ->
                seriesTrackingViewModel.getEpisodesForSeason(season.id).observe(viewLifecycleOwner) { episodes ->
                    callback(episodes.map { 
                        com.shalenmathew.movieflix.domain.model.TVEpisode(
                            id = it.id,
                            airDate = it.airDate,
                            episodeNumber = it.episodeNumber,
                            name = it.name,
                            overview = it.overview,
                            runtime = it.runtime,
                            seasonNumber = it.seasonNumber,
                            stillPath = it.stillPath,
                            voteAverage = it.voteAverage,
                            isWatched = it.isWatched
                        )
                    })
                }
            },
            onUntrackClick = { series ->
                val ctx = context ?: return@TrackingAdapter
                com.google.android.material.dialog.MaterialAlertDialogBuilder(ctx, R.style.TrackingAlertDialog)
                    .setTitle(getString(R.string.msg_stop_tracking_title))
                    .setMessage(getString(R.string.msg_stop_tracking_message, series.name))
                    .setPositiveButton(getString(R.string.btn_stop_tracking)) { _, _ ->
                        seriesTrackingViewModel.untrackSeries(series.id)
                        com.shalenmathew.movieflix.core.utils.showToast(ctx, getString(R.string.msg_series_removed_tracking))
                    }
                    .setNegativeButton(getString(R.string.btn_keep_tracking), null)
                    .show()
            },
            onEpisodeClick = { series, season, episode ->
                navigateToEpisodeDetails(series, season, episode)
            },
            onEpisodeWatchedClick = { series, season, episode ->
                val newStatus = !episode.isWatched
                
                if (newStatus) {
                    // AUTO-FILL: Mark this episode AND all previous ones as watched
                    seriesTrackingViewModel.markPreviousEpisodesAsWatched(
                        seriesId = series.id,
                        seasonNumber = season.seasonNumber,
                        episodeNumber = episode.episodeNumber ?: 0
                    )
                    
                    // Update "Last Watched" bookmark
                    seriesTrackingViewModel.updateLastWatchedEpisode(
                        seriesId = series.id,
                        episodeId = episode.id ?: -1,
                        seasonNumber = season.seasonNumber,
                        episodeNumber = episode.episodeNumber ?: 0
                    )
                    
                    // Check for 100% completion
                    checkShowCompletion(series)
                } else {
                    // MANUAL OVERRIDE: Just unmark this specific episode
                    seriesTrackingViewModel.updateEpisodeWatchedStatus(episode.id ?: -1, false)
                }
            },
            onSeriesLongClick = { series ->
                showDemoBottomSheet(series)
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
            binding.peekingLogo.gone()
            binding.peekingShelf.gone()
        } else {
            adapter.submitList(emptyList())
            binding.fragmentTrackingRv.gone()

            if (query.isNullOrBlank()) {
                binding.tvNoResult.gone()
                binding.fragmentTrackingPlaceholder.visible()
                binding.peekingLogo.visible()
                binding.peekingShelf.visible()
            } else {
                binding.tvNoResult.visible()
                binding.fragmentTrackingPlaceholder.gone()
                binding.peekingLogo.gone()
                binding.peekingShelf.gone()
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
                    airDate = it.airDate,
                    episodeNumber = it.episodeNumber,
                    name = it.name,
                    overview = it.overview,
                    runtime = it.runtime,
                    seasonNumber = it.seasonNumber,
                    stillPath = it.stillPath,
                    voteAverage = it.voteAverage,
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
                    context?.let { com.shalenmathew.movieflix.core.utils.showToast(it, getString(R.string.msg_swap_seasons_tracking)) }
                }
            )

            val intent = com.shalenmathew.movieflix.presentation.episode_details.EpisodeDetailsActivity.newIntent(
                requireContext(),
                episodeIndex
            )
            startActivity(intent)
        }
    }

    private fun checkShowCompletion(series: TrackedSeries) {
        if (completionDialog?.isShowing == true) return

        viewLifecycleOwner.lifecycleScope.launch {
            // Small delay to let the database bulk update finish
            kotlinx.coroutines.delay(250)
            
            val seasons = seriesTrackingViewModel.getSeasonsForSeries(series.id).asFlow().first()
            val totalEpisodes = seasons.sumOf { it.episodeCount ?: 0 }
            val watchedEpisodes = seasons.sumOf { it.watchedCount }

            if (totalEpisodes > 0 && watchedEpisodes >= totalEpisodes) {
                showCompletionBottomSheet(series, totalEpisodes)
            }
        }
    }

    private fun showCompletionBottomSheet(series: TrackedSeries, totalCount: Int) {
        if (completionDialog?.isShowing == true) return

        val dialog = BottomSheetDialog(requireContext(), R.style.SheetDialog)
        completionDialog = dialog
        
        val view = layoutInflater.inflate(R.layout.bottom_sheet_series_completed, null)
        
        val banner = view.findViewById<android.widget.ImageView>(R.id.completed_banner_image)
        val subtitle = view.findViewById<android.widget.TextView>(R.id.completed_subtitle_text)
        val watchlistBtn = view.findViewById<View>(R.id.completed_watchlist_btn)
        val watchlistIcon = view.findViewById<android.widget.ImageView>(R.id.completed_watchlist_icon)
        val watchlistText = view.findViewById<android.widget.TextView>(R.id.completed_watchlist_text)
        val favBtn = view.findViewById<View>(R.id.completed_fav_btn)
        val favIcon = view.findViewById<android.widget.ImageView>(R.id.completed_fav_icon)
        val favText = view.findViewById<android.widget.TextView>(R.id.completed_fav_text)
        val untrackBtn = view.findViewById<View>(R.id.completed_untrack_btn)
        val doneBtn = view.findViewById<View>(R.id.completed_done_btn)

        banner.loadImage(Constants.TMDB_IMAGE_BASE_URL_W780.plus(series.backdropPath))
        subtitle.text = getString(R.string.msg_finished_episodes, totalCount, series.name)

        val movieResult = series.toMovieResult()

        // Initial State for Watchlist/Fav
        var isInWatchlist = false
        var isFav = false

        // We need to observe the actual lists to know status
        watchListViewModel.getAllWatchListData().observe(viewLifecycleOwner) { list ->
            isInWatchlist = list.any { it.id == series.id }
            if (isInWatchlist) {
                watchlistIcon.setImageResource(R.drawable.baseline_done_all_24)
                watchlistText.text = getString(R.string.btn_remove_watchlist)
            } else {
                watchlistIcon.setImageResource(R.drawable.ic_add)
                watchlistText.text = getString(R.string.add_to_watchlist)
            }
        }

        favMovieViewModel.getAllMovieData().observe(viewLifecycleOwner) { list ->
            isFav = list.any { it.id == series.id }
            if (isFav) {
                favIcon.setImageResource(R.drawable.fav_red)
                favText.text = getString(R.string.btn_remove_favorites)
            } else {
                favIcon.setImageResource(R.drawable.fav_outline)
                favText.text = getString(R.string.add_to_favorites)
            }
        }

        watchlistBtn.setOnClickListener {
            if (isInWatchlist) {
                watchListViewModel.deleteWatchListData(movieResult)
                com.shalenmathew.movieflix.core.utils.showToast(requireContext(), getString(R.string.msg_removed_watchlist))
            } else {
                watchListViewModel.insertWatchListData(movieResult)
                com.shalenmathew.movieflix.core.utils.showToast(requireContext(), getString(R.string.msg_added_watchlist))
            }
        }

        favBtn.setOnClickListener {
            if (isFav) {
                favMovieViewModel.deleteWatchListData(movieResult)
                com.shalenmathew.movieflix.core.utils.showToast(requireContext(), getString(R.string.msg_removed_favorites))
            } else {
                favMovieViewModel.insertFavMovieData(movieResult)
                com.shalenmathew.movieflix.core.utils.showToast(requireContext(), getString(R.string.msg_added_favorites))
            }
        }

        untrackBtn.setOnClickListener {
            seriesTrackingViewModel.untrackSeries(series.id)
            dialog.dismiss()
            com.shalenmathew.movieflix.core.utils.showToast(requireContext(), getString(R.string.msg_tracking_stopped))
        }

        doneBtn.setOnClickListener { dialog.dismiss() }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun showDemoBottomSheet(series: TrackedSeries) {
        val dialog = BottomSheetDialog(requireContext(), R.style.SheetDialog)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_demo, null)
        
        view.findViewById<android.widget.TextView>(R.id.demo_series_name).text = series.name
        
        view.findViewById<View>(R.id.options_change_banner_btn).setOnClickListener {
            dialog.dismiss()
            showChooseBannerBottomSheet(series)
        }
        
        view.findViewById<View>(R.id.demo_close_btn).setOnClickListener { dialog.dismiss() }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun showChooseBannerBottomSheet(series: TrackedSeries) {
        val dialog = BottomSheetDialog(requireContext(), R.style.SheetDialog)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_choose_banner, null)
        
        val loader = view.findViewById<View>(R.id.choose_banner_loader)
        val rv = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.choose_banner_rv)
        val cancelBtn = view.findViewById<View>(R.id.choose_banner_cancel_btn)
        
        val adapter = BannerChoiceAdapter { selectedPath ->
            seriesTrackingViewModel.updateSeriesBanner(series.id, selectedPath)
            dialog.dismiss()
            com.shalenmathew.movieflix.core.utils.showToast(requireContext(), getString(R.string.msg_banner_updated))
        }
        rv.adapter = adapter

        seriesTrackingViewModel.fetchAvailableBanners(series.id)
        seriesTrackingViewModel.availableBanners.observe(viewLifecycleOwner) { result ->
            when (result) {
                is com.shalenmathew.movieflix.core.utils.NetworkResults.Loading -> {
                    loader.visibility = View.VISIBLE
                    rv.visibility = View.GONE
                }
                is com.shalenmathew.movieflix.core.utils.NetworkResults.Success -> {
                    loader.visibility = View.GONE
                    rv.visibility = View.VISIBLE
                    adapter.submitList(result.data)
                }
                is com.shalenmathew.movieflix.core.utils.NetworkResults.Error -> {
                    loader.visibility = View.GONE
                    com.shalenmathew.movieflix.core.utils.showToast(requireContext(), result.message ?: "Error")
                }
            }
        }

        cancelBtn.setOnClickListener { dialog.dismiss() }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun TrackedSeries.toMovieResult() = MovieResult(
        id = id,
        name = name,
        posterPath = posterPath,
        backdropPath = backdropPath,
        overview = overview,
        mediaType = "tv"
    )
}
