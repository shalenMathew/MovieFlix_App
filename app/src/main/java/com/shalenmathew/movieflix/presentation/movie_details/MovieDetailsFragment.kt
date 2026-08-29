package com.shalenmathew.movieflix.presentation.movie_details

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback
import com.shalenmathew.movieflix.R
import com.shalenmathew.movieflix.core.adapters.CastAdapter
import com.shalenmathew.movieflix.core.adapters.EpisodeAdapter
import com.shalenmathew.movieflix.core.adapters.RecommendationAdapter
import com.shalenmathew.movieflix.core.adapters.SeasonSelectorAdapter
import com.shalenmathew.movieflix.core.notifications.NotificationHelper
import com.shalenmathew.movieflix.core.utils.Constants
import com.shalenmathew.movieflix.core.utils.Constants.BASE_YOUTUBE_URL
import com.shalenmathew.movieflix.core.utils.Constants.TMDB_IMAGE_BASE_URL_W780
import com.shalenmathew.movieflix.core.utils.NetworkResults
import com.shalenmathew.movieflix.core.utils.formatDate
import com.shalenmathew.movieflix.core.utils.getGenreListById
import com.shalenmathew.movieflix.core.utils.gone
import com.shalenmathew.movieflix.core.utils.loadImage
import com.shalenmathew.movieflix.core.utils.shareMovie
import com.shalenmathew.movieflix.core.utils.showToast
import com.shalenmathew.movieflix.databinding.FragmentMovieDetailsBinding
import com.shalenmathew.movieflix.domain.model.MediaVideoResult
import com.shalenmathew.movieflix.domain.model.MovieResult
import com.shalenmathew.movieflix.presentation.viewmodels.FavMovieViewModel
import com.shalenmathew.movieflix.presentation.viewmodels.HomeInfoViewModel
import com.shalenmathew.movieflix.presentation.viewmodels.ScheduledViewModel
import com.shalenmathew.movieflix.presentation.viewmodels.SearchMovieViewModel
import com.shalenmathew.movieflix.presentation.viewmodels.SeriesTrackingViewModel
import com.shalenmathew.movieflix.presentation.viewmodels.WatchListViewModel
import com.shalenmathew.movieflix.presentation.viewmodels.CustomListViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

@AndroidEntryPoint
class MovieDetailsFragment : BottomSheetDialogFragment() {
    //remember to add dialog in nav graph or bottom sheet will not work

    private var _binding: FragmentMovieDetailsBinding? = null
    val binding get() = _binding!!
    private val homeInfoViewModel: HomeInfoViewModel by viewModels()
    private val searchMovieViewModel: SearchMovieViewModel by viewModels()

    private var mediaId: Int? = null
    private lateinit var movieResult: MovieResult
    private var youtubeUrl: String = ""
    private var youTubePlayerListener: AbstractYouTubePlayerListener? = null
    private var youTubePlayer: YouTubePlayer? = null
    private lateinit var recommendationAdapter: RecommendationAdapter
    private lateinit var castAdapter: CastAdapter
    private lateinit var episodeAdapter: EpisodeAdapter
    private lateinit var galleryAdapter: com.shalenmathew.movieflix.core.adapters.PersonalGalleryAdapter
    private lateinit var watchProviderAdapter: com.shalenmathew.movieflix.core.adapters.WatchProviderAdapter
    private var whereToWatchLink: String? = null
    private val customTabsIntent by lazy {
        CustomTabsIntent.Builder().setShowTitle(true).build()
    }
    private val watchListViewModel: WatchListViewModel by viewModels()
    private val favMovieViewModel: FavMovieViewModel by viewModels()
    private val scheduledViewModel: ScheduledViewModel by viewModels()
    private val seriesTrackingViewModel: SeriesTrackingViewModel by viewModels()
    private val customListViewModel: CustomListViewModel by viewModels()

    private var isInWatchList: Boolean = false
    private var isFav: Boolean = false
    private var isScheduled: Boolean = false
    private var currentScheduledDate: Long = 0
    private var scheduleCheckRunnable: Runnable? = null
    private val scheduleHandler = Handler(Looper.getMainLooper())

    private var mediaType: String? = null
    private var isOverviewExpanded = false
    private var fullOverviewText = ""

    private var currentSeasonNumber = 1
    private var availableSeasons =
        mutableListOf<com.shalenmathew.movieflix.domain.model.TVSeasonBasic>()
    private var currentEpisodes = listOf<com.shalenmathew.movieflix.domain.model.TVEpisode>()
    private var displayedEpisodesCount = 50
    private val EPISODES_PAGE_SIZE = 50
    private var isLoadingMoreEpisodes = false
    private var isTVShow = false
    private var tvDetailsLoaded = false

    private var isPlaying: Boolean = false
    private var currentSeriesProgressCache: com.shalenmathew.movieflix.domain.model.TrackedSeries? = null

    // Notification permission launcher
    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, proceed with scheduling
            showScheduleDateTimePicker()
        } else {
            context?.let { ctx ->
                showToast(
                    ctx,
                    getString(R.string.msg_notification_permission_required)
                )
            }
        }
    }

    private val pickPosterLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            handleLocalPosterSelection(it)
        }
    }

    private val pickBannerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            handleLocalBannerSelection(it)
        }
    }

    private val pickGalleryImageLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        uris.forEach { uri ->
            handleGalleryImageSelection(uri)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        setStyle(STYLE_NO_FRAME, R.style.SheetDialog)
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMovieDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setUpDetailFragment()
        iniIt()
        setUpObservers()
        handleClickListeners()

    }

    private fun handleClickListeners() {
        binding.apply {

//            fragmentMovieDetailsWatchBtn.setOnClickListener(){
//                whereToWatchLink?.let {
//                    customTabsIntent.launchUrl(requireContext(),it.toUri())
//                }?: showToast(requireContext(),"No information available")
//            }

            fragmentMovieDetailsWatchlistBtn.setOnClickListener() {
                if (!::movieResult.isInitialized) return@setOnClickListener
                val ctx = context ?: return@setOnClickListener

                if (!isInWatchList) {
                    watchListViewModel.insertWatchListData(movieResult)
                    addButtonIcon.setImageDrawable(
                        ContextCompat.getDrawable(
                            ctx,
                            R.drawable.baseline_done_all_24
                        )
                    )
                    showToast(ctx, getString(R.string.msg_added_to_watchlist))
                } else {
                    watchListViewModel.deleteWatchListData(movieResult)
                    addButtonIcon.setImageDrawable(
                        ContextCompat.getDrawable(
                            ctx,
                            R.drawable.ic_add
                        )
                    )
                    showToast(ctx, getString(R.string.msg_removed_from_watchlist))
                }
                isInWatchList = !isInWatchList
                updateScheduleButtonVisibility()
            }

            fragmentMovieDetailsFavBtn.setOnClickListener {
                if (!::movieResult.isInitialized) return@setOnClickListener
                val ctx = context ?: return@setOnClickListener

                if (!isFav) {
                    favMovieViewModel.insertFavMovieData(movieResult)
                    favIcon.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.fav_red))
                    showToast(ctx, getString(R.string.msg_added_to_favorites))
                } else {

                    favMovieViewModel.deleteWatchListData(movieResult)
                    favIcon.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.fav_outline))
                    showToast(ctx, getString(R.string.msg_removed_from_favorites))
                }

                isFav = !isFav
                updateScheduleButtonVisibility()
            }


            fragmentMovieDetailsShareBtn.setOnClickListener() {
                if (!::movieResult.isInitialized) return@setOnClickListener
                val ctx = context ?: return@setOnClickListener
                shareMovie(ctx, movieResult.title.toString(), youtubeUrl)
            }

            fragmentMovieDetailsMoreBtn.setOnClickListener {
                showMoreOptionsBottomSheet()
            }
        }
    }

    private fun iniIt() {
        recommendationAdapter = RecommendationAdapter(posterClick = {
            openDetailFragment(it)
        })
        binding.fragmentMovieDetailsRecommendList.adapter = recommendationAdapter

        castAdapter = CastAdapter()
        binding.fragmentMovieDetailsCastList.adapter = castAdapter

        episodeAdapter = EpisodeAdapter { episode ->
            openEpisodeDetails(episode)
        }
        binding.episodesRecyclerView.adapter = episodeAdapter

        galleryAdapter = com.shalenmathew.movieflix.core.adapters.PersonalGalleryAdapter(
            onImageClick = { image ->
                showImagePreview(image.imagePath)
            },
            onAddClick = { pickGalleryImageLauncher.launch("image/*") },
            onLongClick = { image ->
                context?.let { ctx ->
                    com.google.android.material.dialog.MaterialAlertDialogBuilder(ctx, R.style.TrackingAlertDialog)
                        .setTitle("Delete Image?")
                        .setMessage("Do you want to remove this image from your collection?")
                        .setPositiveButton("Delete") { _, _ ->
                            favMovieViewModel.deleteGalleryImage(image)
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            }
        )
        binding.fragmentMovieDetailsGalleryRv.adapter = galleryAdapter

        watchProviderAdapter =
            com.shalenmathew.movieflix.core.adapters.WatchProviderAdapter { provider ->
                context?.let { ctx ->
                    com.shalenmathew.movieflix.core.utils.StreamingAppUtils.openStreamingApp(
                        ctx,
                        provider,
                        whereToWatchLink
                    )
                    showToast(
                        ctx,
                        com.shalenmathew.movieflix.core.utils.StreamingAppUtils.getAppAvailabilityMessage(
                            ctx,
                            provider
                        )
                    )
                }
            }
        binding.whereToWatchRecyclerView.adapter = watchProviderAdapter

        setupTabLayout()
        setupSeasonDropdown()

        binding.movieDetailsNestedScrollView.setOnScrollChangeListener(
            NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, _ ->
                val totalScrollableHeight = v.getChildAt(0).measuredHeight
                val visibleHeight = v.measuredHeight
                if (scrollY + visibleHeight >= totalScrollableHeight - 300) {
                    if (binding.episodesSection.visibility == View.VISIBLE && !isLoadingMoreEpisodes) {
                        loadMoreEpisodes()
                    }
                }
            }
        )
    }

    private fun openDetailFragment(it: MovieResult) {
        // Update current arguments and reload data
        val bundle = Bundle()
        bundle.putString(Constants.MEDIA_SEND_REQUEST_KEY, Gson().toJson(it))

        // Check if we can use NavController (normal fragment flow)
        try {
            findNavController().navigate(R.id.action_movieDetailsFragment_self, bundle)
        } catch (_: IllegalStateException) {
            // NavController not available - we're shown as a standalone dialog
            // Update arguments and reload the fragment
            arguments = bundle

            // Reset state
            isPlaying = false
            youTubePlayer?.pause()
            tvDetailsLoaded = false

            // Reload data with new movie
            setUpDetailFragment()
        }
    }

    private fun setUpObservers() {

        homeInfoViewModel.mediaTrailerList.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkResults.Success -> binding.apply {
                    it.data?.let { result ->
                        result.results?.let { videosList ->
                            val videosArrayList = videosList as ArrayList
                            val trailerList: List<MediaVideoResult> =
                                videosArrayList.filter { toFilter ->
                                    // video arraylist response will give us all type of video we only want trailer or teaser from type youtube
                                    (toFilter.type == Constants.TRAILER || toFilter.type == Constants.TEASER) && toFilter.site == Constants.YOUTUBE
                                }
                            try {
                                val movieTrailer =
                                    if (trailerList.isEmpty()) videosArrayList[0] else trailerList[0]
                                youtubeUrl = "$BASE_YOUTUBE_URL${movieTrailer.key}"

                                binding.fragmentMovieDetailsPlayBtn.setOnClickListener {

                                    if (!isPlaying) {
                                        // Start or resume playing
                                        if (youTubePlayer == null) {
                                            // First time playing - initialize player
                                            initializePlayer(movieTrailer.key)
                                        } else {
                                            // Resume paused video
                                            youTubePlayer?.play()
                                        }
                                    } else {
                                        // Pause the video
                                        youTubePlayer?.pause()
                                    }
                                }

                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                    }
                }

                is NetworkResults.Loading -> {

                    Log.d("YTPlayerBug", "LoDING - " + it.message)
                }

                is NetworkResults.Error -> {
                    context?.let { ctx -> showToast(ctx, it.message ?: getString(R.string.msg_something_went_wrong)) }
                    Log.d("YTPlayerBug", "" + it.message)
                }
            }
        }

        homeInfoViewModel.recommendationLiveData.observe(viewLifecycleOwner) { movieList ->

            when (movieList) {
                is NetworkResults.Success -> {
                    movieList.data?.let { resultList ->
                        resultList.results.let {
                            if (it.isNotEmpty()) {
                                binding.recommendedText.text = getString(R.string.more_like_this)
                                recommendationAdapter.submitList(it)
                            } else {
                                binding.recommendedText.text = getString(R.string.msg_no_recommendation)
                            }
                        }
                    }
                }

                is NetworkResults.Error -> {}
                is NetworkResults.Loading -> {}
            }
        }

        homeInfoViewModel.whereToWatchProviders.observe(viewLifecycleOwner) {
            when (it) {

                is NetworkResults.Loading -> {}
                is NetworkResults.Error -> {
                    binding.whereToWatchSection.visibility = View.GONE
                }

                is NetworkResults.Success -> binding.apply {
                    it.data?.let { result ->
                        result.results?.let { results ->
                            whereToWatchLink = results.IN?.link

                            // Get all available providers (prioritize flatrate/streaming)
                            val providers = results.IN?.flatrate ?: emptyList()

                            if (providers.isNotEmpty()) {
                                whereToWatchSection.visibility = View.VISIBLE
                                watchProviderAdapter.submitList(providers)
                            } else {
                                whereToWatchSection.visibility = View.GONE
                            }
                        } ?: run {
                            whereToWatchSection.visibility = View.GONE
                        }
                    } ?: run {
                        whereToWatchSection.visibility = View.GONE
                    }
                }

            }
        }

        watchListViewModel.getAllWatchListData().observe(viewLifecycleOwner) { list ->
            isInWatchList = list.any { it.id == mediaId }
            if (isInWatchList) {
                changeAddToWatchListIcon()
                updateScheduleButtonVisibility()
            } else {
                binding.addButtonIcon.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_add))
            }
            updateLastWatchedUI()
        }

        favMovieViewModel.getAllMovieData().observe(viewLifecycleOwner) { list ->
            isFav = list.any { it.id == mediaId }
            if (isFav) {
                changeFavIcon()
                list.find { it.id == mediaId }?.let { res ->
                    setupPersonalNoteView(mediaId!!, res.personalNote)
                }
                binding.fragmentMovieDetailsPersonalNoteLl.isVisible = true
                updateScheduleButtonVisibility()
                
                // Observe gallery images
                mediaId?.let { id ->
                    favMovieViewModel.getGalleryImages(id).observe(viewLifecycleOwner) { images ->
                        binding.fragmentMovieDetailsGalleryLl.isVisible = true
                        val galleryItems = mutableListOf<com.shalenmathew.movieflix.core.adapters.GalleryItem>()
                        galleryItems.add(com.shalenmathew.movieflix.core.adapters.GalleryItem.AddButton)
                        galleryItems.addAll(images.map { com.shalenmathew.movieflix.core.adapters.GalleryItem.Image(it) })
                        galleryAdapter.submitList(galleryItems)
                    }
                }
            } else {
                binding.favIcon.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.fav_outline))
                binding.fragmentMovieDetailsPersonalNoteLl.isVisible = false
                binding.fragmentMovieDetailsGalleryLl.isVisible = false
            }
            updateLastWatchedUI()
        }

        scheduledViewModel.getAllScheduledMovies().observe(viewLifecycleOwner) { scheduledList ->
            // Check if current movie is in the scheduled list
            val scheduledMovie = scheduledList.find { it.id == mediaId }

            // Always sync button state with database state
            isScheduled = scheduledMovie != null
            currentScheduledDate = scheduledMovie?.scheduledDate ?: 0
            updateScheduleButtonIcon()

            // Start checking if scheduled time has passed
            if (isScheduled) {
                startScheduleTimeCheck()
            } else {
                stopScheduleTimeCheck()
            }

            // Update recommendation adapter with scheduled movie IDs
            val ids = scheduledList.mapNotNull { entity -> entity.id }.toSet()
            recommendationAdapter.updateScheduledMovies(ids)
        }

        searchMovieViewModel.searchMovieLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkResults.Success -> {

                    it.data?.let { movieList ->

                        if (movieList.results.isNotEmpty()) {
                            mediaType = movieList.results[0].mediaType
                            val id = movieList.results[0].id

                            when (mediaType) {
                                "movie" -> {
                                    isTVShow = false
                                    binding.tabsSection.visibility = View.GONE
                                    binding.episodesSection.visibility = View.GONE
                                    binding.aboutSection.visibility = View.VISIBLE

                                    if (id != null) {
                                        homeInfoViewModel.getMovieTrailer(id)
                                        // Load secondary data with delay
                                        loadSecondaryData(id)
                                    } else {
                                        context?.let { ctx -> showToast(ctx, getString(R.string.msg_media_id_null)) }
                                    }
                                }

                                "tv" -> {
                                    isTVShow = true
                                    binding.tabsSection.visibility = View.VISIBLE
                                    binding.aboutSection.visibility = View.VISIBLE
                                    binding.episodesSection.visibility = View.GONE

                                    if (id != null) {
                                        homeInfoViewModel.getTVTrailer(id)
                                        // Load secondary data with delay
                                        loadSecondaryData(id)
                                        seriesTrackingViewModel.checkTrackingStatus(id)
                                    } else {
                                        context?.let { ctx -> showToast(ctx, getString(R.string.msg_media_id_null)) }
                                    }
                                }
                            }
                        }
                    }
                }


                is NetworkResults.Error -> {
                    context?.let { ctx -> showToast(ctx, "" + it.message) }
                }

                is NetworkResults.Loading -> {
                    Log.d("YTPlayerBug", "LoDING - " + it.message)
                }
            }

        }

        homeInfoViewModel.castList.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    result.data?.let { castList ->
                        if (castList.isNotEmpty()) {
                            binding.castSectionTitle.visibility = View.VISIBLE
                            binding.fragmentMovieDetailsCastList.visibility = View.VISIBLE
                            castAdapter.submitList(castList)
                        } else {
                            binding.castSectionTitle.visibility = View.GONE
                            binding.fragmentMovieDetailsCastList.visibility = View.GONE
                        }
                    }
                }

                is NetworkResults.Error -> {
                    binding.castSectionTitle.visibility = View.GONE
                    binding.fragmentMovieDetailsCastList.visibility = View.GONE
                }

                is NetworkResults.Loading -> {
                    // Show loading state if needed
                }
            }
        }

        // Observer for TV show details (for episodes feature)
        homeInfoViewModel.tvDetail.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    result.data?.let { tvDetail ->
                        availableSeasons.clear()
                        availableSeasons.addAll(tvDetail.seasons.filter { it.seasonNumber != null && it.seasonNumber > 0 })

                        if (availableSeasons.isNotEmpty()) {
                            // Load first season by default
                            currentSeasonNumber = availableSeasons[0].seasonNumber ?: 1
                            val epCount = availableSeasons[0].episodeCount ?: 0
                            binding.seasonDropdownButton.text =
                                "${getString(R.string.lbl_season_number, currentSeasonNumber)} • ${getString(R.string.msg_episodes_count, epCount)}"
                            mediaId?.let { tvId ->
                                homeInfoViewModel.getTVSeason(tvId, currentSeasonNumber)
                            }
                        }
                    }
                }

                is NetworkResults.Error -> {
                    // Handle error
                }

                is NetworkResults.Loading -> {
                    // Show loading state if needed
                }
            }
        }

        // Observer for TV season episodes
        homeInfoViewModel.tvSeason.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    result.data?.let { season ->
                        currentEpisodes = season.episodes
                        displayedEpisodesCount = EPISODES_PAGE_SIZE
                        submitEpisodeList()
                    }
                }

                is NetworkResults.Error -> {
                    context?.let { ctx ->
                        showToast(
                            ctx,
                            getString(R.string.msg_something_went_wrong) + ": ${result.message}"
                        )
                    }
                }

                is NetworkResults.Loading -> {
                    // Show loading state if needed
                }
            }
        }

        seriesTrackingViewModel.trackingStatus.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    context?.let { showToast(it, getString(R.string.msg_tracking_started)) }
                }
                is NetworkResults.Error -> {
                    context?.let { showToast(it, getString(R.string.msg_something_went_wrong) + ": ${result.message}") }
                }
                is NetworkResults.Loading -> {}
            }
        }

        // Setup Bookmark Text
        seriesTrackingViewModel.currentSeriesProgress.observe(viewLifecycleOwner) { series ->
            currentSeriesProgressCache = series
            updateLastWatchedUI()
        }

        homeInfoViewModel.mediaImages.observe(viewLifecycleOwner) { result ->
            // This will be handled inside the bottom sheet logic via direct observation if needed,
            // or we can keep a reference here.
        }
    }

    private fun openEpisodeDetails(episode: com.shalenmathew.movieflix.domain.model.TVEpisode) {
        val episodeIndex = currentEpisodes.indexOfFirst { it.id == episode.id }
        if (episodeIndex == -1) return

        // Store data in holder to avoid TransactionTooLargeException
        com.shalenmathew.movieflix.presentation.episode_details.EpisodeDataHolder.setData(
            episodeList = currentEpisodes,
            season = currentSeasonNumber,
            showName = movieResult.name ?: movieResult.title,
            totalSeasons = availableSeasons.size,
            tvShowId = mediaId,
            onSeasonChange = { targetSeason ->
                // Switch to the target season when user navigates between seasons
                loadSeason(targetSeason)
            }
        )

        val intent =
            com.shalenmathew.movieflix.presentation.episode_details.EpisodeDetailsActivity.newIntent(
                requireContext(),
                episodeIndex
            )

        startActivity(intent)
    }

    private fun submitEpisodeList() {
        val subset = currentEpisodes.take(displayedEpisodesCount)
        if (currentEpisodes.size > displayedEpisodesCount) {
            val listToSubmit = subset.toMutableList<com.shalenmathew.movieflix.domain.model.TVEpisode?>()
            listToSubmit.add(null)
            episodeAdapter.submitList(listToSubmit)
        } else {
            episodeAdapter.submitList(subset)
        }
    }

    private fun loadMoreEpisodes() {
        if (currentEpisodes.isEmpty()) return
        if (displayedEpisodesCount >= currentEpisodes.size) return

        isLoadingMoreEpisodes = true
        view?.postDelayed({
            if (!isAdded || _binding == null) return@postDelayed
            displayedEpisodesCount = (displayedEpisodesCount + EPISODES_PAGE_SIZE).coerceAtMost(currentEpisodes.size)
            submitEpisodeList()
            isLoadingMoreEpisodes = false
        }, 350)
    }

    private fun loadSeason(seasonNumber: Int) {
        // Update current season number
        currentSeasonNumber = seasonNumber

        // Update dropdown text
        binding.seasonDropdownButton.text = getString(R.string.lbl_season_number, seasonNumber)

        // Load episodes for the new season
        mediaId?.let { tvId ->
            homeInfoViewModel.getTVSeason(tvId, seasonNumber)
        }

        // Switch to Episodes tab if not already there
        if (binding.tabLayout.selectedTabPosition != 1) {
            binding.tabLayout.getTabAt(1)?.select()
        }
    }

    private fun loadSecondaryData(id: Int) {
        // Delay secondary data loading to improve initial page load
        // This allows the main content (title, overview, trailer) to appear instantly
        view?.postDelayed({
            if (isTVShow) {
                homeInfoViewModel.getTVCast(id)
            } else {
                homeInfoViewModel.getMovieCast(id)
            }

            // Load recommendations and watch providers with additional delay
            view?.postDelayed({
                homeInfoViewModel.getRecommendation(id)
                if (isTVShow) {
                    homeInfoViewModel.getTVWhereToWatchProvider(id)
                } else {
                    homeInfoViewModel.getWhereToWatchProvider(id)
                }
            }, 200)
        }, 300)
    }

    private fun changeAddToWatchListIcon() {
        if (!isAdded || _binding == null) return

        binding.apply {
            isInWatchList = true
            context?.let {
                addButtonIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        it,
                        R.drawable.baseline_done_all_24
                    )
                )
            }
        }
    }

    private fun changeFavIcon() {
        if (!isAdded || _binding == null) return

        binding.apply {
            isFav = true
            context?.let {
                favIcon.setImageDrawable(ContextCompat.getDrawable(it, R.drawable.fav))
            }
        }
    }

    private fun updateScheduleButtonIcon() {
        if (!isAdded || _binding == null) return

        try {
            // Only update the main fragment's scheduled status text/icon here
            // The bottom sheet will handle its own UI updates when opened
            updateScheduledDateText()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateScheduledDateText() {
        try {
            if (currentScheduledDate > 0) {
                val dateFormat = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.ENGLISH)
                val formattedDate = dateFormat.format(java.util.Date(currentScheduledDate))

                // Get day suffix (st, nd, rd, th)
                val calendar = java.util.Calendar.getInstance()
                calendar.timeInMillis = currentScheduledDate
                val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
                val daySuffix = getDayOfMonthSuffix(day)

                // Format: "Scheduled on: 28th October 2025, 5:30 PM"
                val simpleDateFormat = SimpleDateFormat("MMMM yyyy, hh:mm a", Locale.ENGLISH)
                val dateStr = simpleDateFormat.format(java.util.Date(currentScheduledDate))

                binding.fragmentMovieDetailsScheduledDate.apply {
                    text = getString(R.string.lbl_scheduled_on, "$day$daySuffix", dateStr)
                    visibility = View.VISIBLE
                }
                binding.fragmentMovieDetailsScheduledIcon.visibility = View.VISIBLE
            } else {
                binding.fragmentMovieDetailsScheduledDate.visibility = View.GONE
                binding.fragmentMovieDetailsScheduledIcon.visibility = View.GONE
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Silently fail if view is not ready
        }
    }

    private fun getDayOfMonthSuffix(day: Int): String {
        return when {
            day in 11..13 -> "th"
            day % 10 == 1 -> "st"
            day % 10 == 2 -> "nd"
            day % 10 == 3 -> "rd"
            else -> "th"
        }
    }

    private fun updateScheduleButtonVisibility() {
        // More button is always visible now
    }

    private fun showMoreOptionsBottomSheet() {
        val ctx = context ?: return
        val bottomSheetDialog = BottomSheetDialog(ctx, R.style.SheetDialog)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_movie_more_options, null)

        val header = view.findViewById<TextView>(R.id.more_options_header)
        
        // Change Poster Item
        val changePosterItem = view.findViewById<View>(R.id.more_options_change_poster_item)
        val changePosterText = view.findViewById<TextView>(R.id.more_options_change_poster_text)
        
        // Change Banner Item
        val changeBannerItem = view.findViewById<View>(R.id.more_options_change_banner_item)
        val changeBannerText = view.findViewById<TextView>(R.id.more_options_change_banner_text)
        
        val scheduleItem = view.findViewById<View>(R.id.more_options_schedule_item)
        val scheduleIcon = view.findViewById<android.widget.ImageView>(R.id.more_options_schedule_icon)
        val scheduleText = view.findViewById<TextView>(R.id.more_options_schedule_text)
        val scheduleSubtitle = view.findViewById<TextView>(R.id.more_options_schedule_subtitle)
        
        val trackItem = view.findViewById<View>(R.id.more_options_track_item)
        val trackIcon = view.findViewById<android.widget.ImageView>(R.id.more_options_track_icon)
        val trackText = view.findViewById<TextView>(R.id.more_options_track_text)
        val trackSubtitle = view.findViewById<TextView>(R.id.more_options_track_subtitle)

        val collectionItem = view.findViewById<View>(R.id.more_options_collection_item)

        header.text = getString(R.string.more_options)

        // Setup Change Poster Option - Limited to Favorites only
        if (isFav) {
            changePosterItem.visibility = View.VISIBLE
            changePosterText.text = if (isTVShow) getString(R.string.btn_change_show_poster) else getString(R.string.btn_change_movie_poster)
            changePosterItem.setOnClickListener {
                bottomSheetDialog.dismiss()
                showChoosePosterBottomSheet()
            }
        } else {
            changePosterItem.visibility = View.GONE
        }

        // Setup Change Banner Option - Limited to Favorites only
        if (isFav) {
            changeBannerItem.visibility = View.VISIBLE
            changeBannerText.text = if (isTVShow) getString(R.string.btn_change_show_banner) else getString(R.string.btn_change_movie_banner)
            changeBannerItem.setOnClickListener {
                bottomSheetDialog.dismiss()
                showChooseBannerBottomSheet()
            }
        } else {
            changeBannerItem.visibility = View.GONE
        }

        // Update UI based on current status
        if (isScheduled) {
            scheduleIcon.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_calendar_check))
            scheduleText.text = getString(R.string.btn_remove_schedule)
            
            // Show formatted date in subtitle
            if (currentScheduledDate > 0) {
                val dateFormat = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
                val dateStr = dateFormat.format(java.util.Date(currentScheduledDate))
                scheduleSubtitle.text = getString(R.string.msg_notified_on, dateStr)
            }
        } else {
            scheduleIcon.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.baseline_calendar_month_24))
            scheduleText.text = getString(R.string.btn_schedule_reminder)
            scheduleSubtitle.text = getString(R.string.msg_schedule_reminder_desc)
        }

        scheduleItem.setOnClickListener {
            bottomSheetDialog.dismiss()
            handleScheduleClick()
        }

        collectionItem.setOnClickListener {
            bottomSheetDialog.dismiss()
            showChooseCustomListBottomSheet()
        }

        // Setup Tracking Item
        if (isTVShow) {
            trackItem.visibility = View.VISIBLE
            
            // Default to "Track" state immediately to avoid showing "Untrack" from a previous show
            trackIcon.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.baseline_playlist_add_check_24))
            trackText.text = getString(R.string.track_series)
            trackSubtitle.text = getString(R.string.track_series_subtitle)

            // Observe tracking status to update UI dynamically
            seriesTrackingViewModel.isCurrentSeriesTracked.observe(viewLifecycleOwner) { isTracked ->
                if (isTracked) {
                    trackIcon.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.baseline_done_all_24))
                    trackText.text = getString(R.string.btn_untrack_series)
                    trackSubtitle.text = getString(R.string.msg_untrack_series_desc)
                } else {
                    trackIcon.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.baseline_playlist_add_check_24))
                    trackText.text = getString(R.string.track_series)
                    trackSubtitle.text = getString(R.string.track_series_subtitle)
                }

                trackItem.setOnClickListener {
                    bottomSheetDialog.dismiss()
                    handleTrackClick(isTracked)
                }
            }
        } else {
            trackItem.visibility = View.GONE
        }

        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()
    }

    private fun handleTrackClick(isTracked: Boolean) {
        val ctx = context ?: return
        if (!::movieResult.isInitialized || mediaId == null) return

        if (!isTracked) {
            seriesTrackingViewModel.trackSeries(mediaId!!)
            showToast(ctx, getString(R.string.msg_tracking_started))
        } else {
            seriesTrackingViewModel.untrackSeries(mediaId!!)
            showToast(ctx, getString(R.string.msg_series_untracked))
        }
    }

    private fun showChooseCustomListBottomSheet() {
        val ctx = context ?: return
        val dialog = BottomSheetDialog(ctx, R.style.SheetDialog)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_choose_custom_list, null)
        
        val rv = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.choose_list_rv)
        val createBtn = view.findViewById<View>(R.id.create_new_list_btn)

        customListViewModel.allLists.observe(viewLifecycleOwner) { lists ->
            viewLifecycleOwner.lifecycleScope.launch {
                val checkList = mutableListOf<Int>()
                for (list in lists) {
                    if (customListViewModel.isMovieInList(list.id, mediaId ?: -1)) {
                        checkList.add(list.id)
                    }
                }
                
                val adapter = com.shalenmathew.movieflix.core.adapters.ChooseCustomListAdapter(
                    onListClick = { selectedList ->
                        viewLifecycleOwner.lifecycleScope.launch {
                            if (customListViewModel.isMovieInList(selectedList.id, mediaId ?: -1)) {
                                customListViewModel.removeMovieFromList(selectedList.id, mediaId ?: -1)
                                showToast(ctx, "Removed from ${selectedList.name}")
                            } else {
                                customListViewModel.addMovieToList(selectedList.id, movieResult)
                                showToast(ctx, "Added to ${selectedList.name}")
                            }
                        }
                    },
                    checkList = checkList
                )
                rv.adapter = adapter
                adapter.submitList(lists)
            }
        }

        createBtn.setOnClickListener {
            dialog.dismiss()
            showCreateListDialog()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun showCreateListDialog() {
        val ctx = context ?: return
        val dialog = BottomSheetDialog(ctx, R.style.SheetDialog)
        val view = layoutInflater.inflate(R.layout.dialog_create_list, null)
        
        val nameEt = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.list_name_et)
        val descEt = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.list_desc_et)
        val createBtn = view.findViewById<View>(R.id.create_list_confirm_btn)

        createBtn.setOnClickListener {
            val name = nameEt.text.toString().trim()
            if (name.isNotEmpty()) {
                customListViewModel.createList(name, descEt.text.toString().trim().takeIf { it.isNotEmpty() })
                dialog.dismiss()
                showChooseCustomListBottomSheet() // Return to selection
            } else {
                nameEt.error = getString(R.string.error_name_empty)
            }
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun handleScheduleClick() {
        val ctx = context ?: return

        if (!isScheduled) {
            // Check notification permission first (Android 13+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                when {
                    NotificationHelper.hasNotificationPermission(ctx) -> {
                        showScheduleDateTimePicker()
                    }
                    shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                        showToast(ctx, getString(R.string.msg_allow_notifications_rationale))
                        requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    else -> {
                        requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            } else {
                showScheduleDateTimePicker()
            }
        } else {
            // Remove schedule
            if (!::movieResult.isInitialized) return
            scheduledViewModel.deleteScheduledMovie(movieResult, currentScheduledDate)
            isScheduled = false
            currentScheduledDate = 0
            updateScheduleButtonIcon()
            showToast(ctx, getString(R.string.msg_schedule_removed))
        }
    }

    private fun showScheduleDateTimePicker() {
        if (!::movieResult.isInitialized) {
            context?.let { ctx -> showToast(ctx, getString(R.string.msg_movie_data_not_loaded)) }
            return
        }

        val ctx = context ?: return
        ScheduleDateTimeDialog.show(ctx) { selectedDateTime ->
            if (!::movieResult.isInitialized) return@show

            scheduledViewModel.insertScheduledMovie(movieResult, selectedDateTime)
            currentScheduledDate = selectedDateTime
            isScheduled = true
            updateScheduleButtonIcon()

            // Start checking if scheduled time has passed
            startScheduleTimeCheck()

            // Format date for toast
            val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
            val formattedDate = dateFormat.format(java.util.Date(selectedDateTime))
            context?.let { c ->
                showToast(
                    c,
                    getString(R.string.msg_scheduled_for, formattedDate)
                )
            }
        }
    }

    private fun setupPersonalNoteView(favId: Int, personalNote: String?) {
        var currentNote = personalNote

        val editorActionListener = TextView.OnEditorActionListener { view, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val newNote = view.text.toString().trim()
                favMovieViewModel.addPersonalNote(favId, newNote.ifEmpty { null })
                currentNote = newNote.ifEmpty { null }
                updateNoteViewState(currentNote)
                hideKeyboard(view)
                return@OnEditorActionListener true
            }
            false
        }

        val openEditorClickListener = View.OnClickListener {
            binding.fragmentMovieDetailsPersonalNoteEditText.setText(currentNote)
            showEditorView()
        }

        // Initial setup
        binding.fragmentMovieDetailsPersonalNoteEditText.apply {
            setOnEditorActionListener(editorActionListener)
            onFocusChangeListener =
                View.OnFocusChangeListener { _, hasFocus ->
                    binding.fragmentMovieDetailsPersonalNoteTextInputLayout.isHintEnabled =
                        !hasFocus
                }
        }
        binding.fragmentMovieDetailsPersonalNoteBtn.setOnClickListener(openEditorClickListener)
        binding.fragmentMovieDetailsPersonalNote.setOnClickListener(openEditorClickListener)

        binding.fragmentMovieDetailsPersonalNoteDeleteBtn.setOnClickListener {
            favMovieViewModel.addPersonalNote(favId, null)
            currentNote = null
            updateNoteViewState(null)
        }

        updateNoteViewState(currentNote)
    }

    /**
     * Updates the visibility of UI components based on whether a note exists.
     */
    private fun updateNoteViewState(currentNote: String?) {
        val noteExists = !currentNote.isNullOrEmpty()
        with(binding) {
            fragmentMovieDetailsPersonalNoteBtn.isVisible = !noteExists
            personalNoteTextLl.isVisible = noteExists

            if (noteExists) {
                fragmentMovieDetailsPersonalNote.apply {
                    text = currentNote
                    isVisible = true
                }
            }

            fragmentMovieDetailsPersonalNoteTextInputLayout.isVisible = false
        }
    }

    /**
     * Shows the note editor and hides other views.
     */
    private fun showEditorView() {
        with(binding) {
            personalNoteTextLl.isVisible = false
            fragmentMovieDetailsPersonalNoteBtn.isVisible = false
            fragmentMovieDetailsPersonalNoteTextInputLayout.isVisible = true
            fragmentMovieDetailsPersonalNoteEditText.requestFocus()
            showKeyboard(fragmentMovieDetailsPersonalNoteEditText)
        }
    }

    private fun hideKeyboard(view: View) {
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun showKeyboard(view: View) {
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }


    private fun setExpandableText(textView: TextView, fullText: String) {
        val maxLines = 3
        fullOverviewText = fullText

        // Reset expanded state
        isOverviewExpanded = false

        // Set initial text with maxLines constraint
        textView.maxLines = maxLines
        textView.text = fullText

        // Use post to ensure TextView is properly laid out
        textView.post {
            if (textView.lineCount > maxLines) {
                // Text needs truncation, add "More" button
                val layout = textView.layout
                if (layout != null) {
                    val truncatedText = getTruncatedText(fullText, textView, maxLines)
                    val moreText = " (More)"
                    val spannableString = SpannableString(truncatedText + moreText)

                    // Make "More" bold and clickable
                    val moreStart = truncatedText.length + 1 // after space
                    val moreEnd = spannableString.length
                    spannableString.setSpan(
                        StyleSpan(Typeface.BOLD),
                        moreStart,
                        moreEnd,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    spannableString.setSpan(
                        object : ClickableSpan() {
                            override fun onClick(widget: View) {
                                expandText(textView)
                            }

                            override fun updateDrawState(ds: TextPaint) {
                                super.updateDrawState(ds)
                                ds.isUnderlineText = false
                                ds.color =
                                    ContextCompat.getColor(requireContext(), R.color.app_color)
                                ds.isFakeBoldText = true
                            }
                        },
                        moreStart,
                        moreEnd,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    textView.maxLines = maxLines
                    textView.text = spannableString
                    textView.movementMethod = LinkMovementMethod.getInstance()
                }
            }
        }
    }

    private fun getTruncatedText(text: String, textView: TextView, maxLines: Int): String {
        val layout = textView.layout ?: return text

        if (layout.lineCount <= maxLines) {
            return text
        }

        // Get the end character index of the line before the last visible line
        val lastVisibleLineIndex = maxLines - 1
        var endIndex = layout.getLineEnd(lastVisibleLineIndex)

        // Reserve space for " (More)" (approximately 7 characters worth of space)
        val moreText = " (More)"
        val paint = textView.paint
        val availableWidth =
            (textView.width - textView.paddingLeft - textView.paddingRight).toFloat()
        val moreWidth = paint.measureText(moreText)

        // Reduce text until "...More" fits
        while (endIndex > 0) {
            val truncated = text.substring(0, endIndex).trimEnd()
            val lineWidth = paint.measureText(
                truncated.substring(
                    truncated.lastIndexOf('\n').let { if (it == -1) 0 else it + 1 })
            )

            if (lineWidth + moreWidth <= availableWidth) {
                return truncated
            }

            endIndex -= 1
        }

        return text.substring(0, endIndex.coerceAtLeast(0))
    }

    private fun expandText(textView: TextView) {
        isOverviewExpanded = true
        textView.maxLines = Integer.MAX_VALUE // Remove line limit

        val lessText = "\n\n(Less)"
        val spannableString = SpannableString(fullOverviewText + lessText)

        // Make "Less" bold and clickable
        val lessStart = fullOverviewText.length
        val lessEnd = spannableString.length
        spannableString.setSpan(
            StyleSpan(Typeface.BOLD),
            lessStart,
            lessEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    collapseText(textView)
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = false
                    ds.color = ContextCompat.getColor(requireContext(), R.color.app_color)
                    ds.isFakeBoldText = true
                }
            },
            lessStart,
            lessEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        textView.text = spannableString
        textView.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun collapseText(textView: TextView) {
        isOverviewExpanded = false
        setExpandableText(textView, fullOverviewText)
    }

    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        // About tab selected
                        binding.aboutSection.visibility = View.VISIBLE
                        binding.episodesSection.visibility = View.GONE
                    }

                    1 -> {
                        // Episodes tab selected - load TV details if not already loaded
                        binding.aboutSection.visibility = View.GONE
                        binding.episodesSection.visibility = View.VISIBLE

                        // Lazy load TV details only when Episodes tab is clicked
                        if (isTVShow && !tvDetailsLoaded) {
                            tvDetailsLoaded = true
                            mediaId?.let { id ->
                                homeInfoViewModel.getTVDetail(id)
                            }
                        }
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupSeasonDropdown() {
        binding.seasonDropdownButton.setOnClickListener {
            if (availableSeasons.isEmpty()) {
                context?.let { ctx -> showToast(ctx, getString(R.string.msg_no_seasons_available)) }
                return@setOnClickListener
            }

            // Change arrow to down when opening
            rotateDropdownArrow(true)

            showSeasonSelectorBottomSheet()
        }
    }

    private fun rotateDropdownArrow(toDown: Boolean) {
        val ctx = context ?: return

        // Simple icon swap with smooth transition
        val newIcon = if (toDown) {
            ContextCompat.getDrawable(ctx, R.drawable.baseline_keyboard_arrow_down_24)
        } else {
            ContextCompat.getDrawable(ctx, R.drawable.baseline_keyboard_arrow_right_24)
        }

        // Apply smooth alpha transition
        binding.seasonDropdownButton.animate()
            .alpha(0.5f)
            .setDuration(125)
            .withEndAction {
                binding.seasonDropdownButton.icon = newIcon
                binding.seasonDropdownButton.animate()
                    .alpha(1f)
                    .setDuration(125)
                    .start()
            }
            .start()
    }

    private fun showSeasonSelectorBottomSheet() {
        val ctx = context ?: return
        val bottomSheetDialog = BottomSheetDialog(ctx, R.style.SheetDialog)
        val bottomSheetView = layoutInflater.inflate(
            R.layout.bottom_sheet_season_selector,
            null
        )

        val recyclerView = bottomSheetView.findViewById<androidx.recyclerview.widget.RecyclerView>(
            R.id.seasons_recycler_view
        )

        val adapter = SeasonSelectorAdapter(availableSeasons) { selectedSeason ->
            currentSeasonNumber = selectedSeason.seasonNumber ?: 1
            binding.seasonDropdownButton.text =
                "${selectedSeason.name} • Episodes ${selectedSeason.episodeCount}"

            // Load episodes for selected season
            mediaId?.let { tvId ->
                homeInfoViewModel.getTVSeason(tvId, currentSeasonNumber)
            }

            bottomSheetDialog.dismiss()
        }

        recyclerView.adapter = adapter

        bottomSheetDialog.setContentView(bottomSheetView)

        // Change arrow back to right when dismissed
        bottomSheetDialog.setOnDismissListener {
            rotateDropdownArrow(false)
        }

        bottomSheetDialog.show()
    }

    private fun initializePlayer(key: String?) {

        binding.apply {

            posterImage.gone()

            // initialise the player if player is null

            fragmentMovieDetailsYt.getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
                override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {

                    Log.d("YTPlayerBug", "inside on ready")

                    key?.let {
                        this@MovieDetailsFragment.youTubePlayer = youTubePlayer

                        // Add listener to track player state changes
                        youTubePlayer.addListener(object : AbstractYouTubePlayerListener() {
                            override fun onStateChange(
                                youTubePlayer: YouTubePlayer,
                                state: PlayerConstants.PlayerState
                            ) {
                                when (state) {
                                    PlayerConstants.PlayerState.PLAYING -> {
                                        isPlaying = true
                                        getString(R.string.btn_pause_trailer).also {
                                            binding.fragmentMovieDetailsPlayBtn.text = it
                                        }
                                        context?.let { ctx ->
                                            binding.fragmentMovieDetailsPlayBtn.icon =
                                                ContextCompat.getDrawable(ctx, R.drawable.ic_pause)
                                        }
                                    }

                                    PlayerConstants.PlayerState.PAUSED -> {
                                        isPlaying = false
                                        getString(R.string.btn_play_trailer).also {
                                            binding.fragmentMovieDetailsPlayBtn.text = it
                                        }
                                        context?.let { ctx ->
                                            binding.fragmentMovieDetailsPlayBtn.icon =
                                                ContextCompat.getDrawable(
                                                    ctx,
                                                    R.drawable.ic_play_arrow
                                                )
                                        }
                                    }

                                    PlayerConstants.PlayerState.ENDED -> {
                                        isPlaying = false
                                        getString(R.string.btn_play_trailer).also {
                                            binding.fragmentMovieDetailsPlayBtn.text = it
                                        }
                                        context?.let { ctx ->
                                            binding.fragmentMovieDetailsPlayBtn.icon =
                                                ContextCompat.getDrawable(
                                                    ctx,
                                                    R.drawable.ic_play_arrow
                                                )
                                        }
                                    }

                                    else -> {}
                                }
                            }
                        })

                        this@MovieDetailsFragment.youTubePlayer?.loadVideo(it, 0f)


                        Log.d("YTPlayerBug", "key inside = $key")

                        Log.d("YTPlayerBug", "yt player inside = $youTubePlayer")

                    } ?: run {

                        Log.d("YTPlayerBug", "key inside run = $key")
                    }
                }

            })

            Log.d("YTPlayerBug", "yt player listener $youTubePlayerListener")
            Log.d("YTPlayerBug", "yt player : $youTubePlayer")

        }
    }

    private fun setUpDetailFragment() {
        // Remove previous observers to prevent stale data from showing when navigating between shows
        seriesTrackingViewModel.isCurrentSeriesTracked.removeObservers(viewLifecycleOwner)
        seriesTrackingViewModel.currentSeriesProgress.removeObservers(viewLifecycleOwner)
        
        // Reset cache
        currentSeriesProgressCache = null
        binding.fragmentMovieDetailsLastWatched.visibility = View.GONE

        val result = Gson().fromJson(
            arguments?.getString(Constants.MEDIA_SEND_REQUEST_KEY),
            MovieResult::class.java
        )

        result?.let {
            mediaId = it.id
            movieResult = it

            val genreList: List<Int>? = it.genreIds
            val title = if (!it.title.isNullOrEmpty()) it.title else it.name

            val overView = it.overview
            val language = it.originalLanguage
            val rating = it.voteAverage
            val year = it.releaseDate
            val img = it.backdropPath
            val releaseDate = it.releaseDate

            binding.apply {
                fragmentMovieDetailsTitle.text = title
                fragmentMovieDetailsGenre.text = getGenreListById(requireContext(), genreList).joinToString { genre ->
                    genre.name
                }
                
                val isLocal = img != null && (img.startsWith("content://") || img.count { it == '/' } > 1)
                val fullUrl = when {
                    isLocal -> img
                    img?.startsWith("http") == true -> img
                    else -> TMDB_IMAGE_BASE_URL_W780.plus(img)
                }
                posterImage.loadImage(fullUrl)
                
                fragmentMovieDetailsLang.text = language
                overView?.let { setExpandableText(fragmentMovieDetailsOverview, it) }
                fragmentMovieDetailsRating.text = String.format("%.1f", rating)
                fragmentMovieDetailsYear.text = formatDate(year)
                releaseDate?.let { rDate ->
                    if (rDate.trim().isNotEmpty()) {
                        fragmentMovieDetailsReleaseDate.visibility = View.VISIBLE
                        fragmentMovieDetailsReleaseDate.text =
                            getString(R.string.release_date, rDate)
                    }
                }

            }

            mediaId?.let { id ->


                when (it.mediaType) {
                    "movie" -> {
                        isTVShow = false
                        binding.tabsSection.visibility = View.GONE
                        binding.episodesSection.visibility = View.GONE
                        binding.aboutSection.visibility = View.VISIBLE

                        homeInfoViewModel.getMovieTrailer(id)
                        // Load cast, recommendations, and watch providers lazily
                        loadSecondaryData(id)
                    }

                    "tv" -> {
                        isTVShow = true
                        binding.tabsSection.visibility = View.VISIBLE
                        binding.aboutSection.visibility = View.VISIBLE
                        binding.episodesSection.visibility = View.GONE

                        homeInfoViewModel.getTVTrailer(id)
                        // Load cast, recommendations, watch providers, and TV details lazily
                        loadSecondaryData(id)
                        seriesTrackingViewModel.checkTrackingStatus(id)
                    }

                    else -> {
                        isTVShow = false
                        binding.tabsSection.visibility = View.GONE
                        binding.episodesSection.visibility = View.GONE
                        binding.aboutSection.visibility = View.VISIBLE

                        if (!title.isNullOrEmpty()) {
                            searchMovieViewModel.fetchSearchMovie(title)
                        }

                    }
                }
                // Removed immediate loading of recommendations and watch providers
            }

        }
    }

    override fun onStart() {

        super.onStart()

        val dialog = dialog as? BottomSheetDialog ?: return

        val bottomSheet = dialog.findViewById<FrameLayout>(
            com.google.android.material.R.id.design_bottom_sheet
        ) ?: return

        val behavior = BottomSheetBehavior.from(bottomSheet)

        // Customize the behavior
        behavior.isHideable = true
        behavior.skipCollapsed = true
        behavior.state = BottomSheetBehavior.STATE_EXPANDED


        // Optional: dismiss on slight swipe down
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) dismiss()
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (slideOffset < 0.1f && behavior.state == BottomSheetBehavior.STATE_DRAGGING) {
                    behavior.state = BottomSheetBehavior.STATE_HIDDEN
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        refreshScheduleStatus()
    }

    private fun refreshScheduleStatus() {
        mediaId?.let { id ->
            lifecycleScope.launch {
                val scheduledEntity = scheduledViewModel.getScheduledMovieById(id)
                isScheduled = scheduledEntity != null
                currentScheduledDate = scheduledEntity?.scheduledDate ?: 0
                withContext(Dispatchers.Main) {
                    updateScheduleButtonIcon()
                    if (isScheduled) {
                        startScheduleTimeCheck()
                    }
                }
            }
        }
    }

    private fun startScheduleTimeCheck() {
        stopScheduleTimeCheck() // Clear any existing check

        scheduleCheckRunnable = object : Runnable {
            override fun run() {
                // Check if fragment is still attached and binding is available
                if (!isAdded || _binding == null) {
                    stopScheduleTimeCheck()
                    return
                }

                if (isScheduled && currentScheduledDate > 0) {
                    val currentTime = System.currentTimeMillis()
                    // If scheduled time has passed by more than 10 seconds, reset the button
                    if (currentTime >= currentScheduledDate + 10000) {
                        // Time has passed, reset the button
                        isScheduled = false
                        currentScheduledDate = 0
                        updateScheduleButtonIcon()

                        // Also delete from database to stay in sync
                        // Only if movieResult is initialized
                        if (::movieResult.isInitialized) {
                            lifecycleScope.launch {
                                try {
                                    val entity =
                                        scheduledViewModel.getScheduledMovieById(mediaId ?: 0)
                                    entity?.let {
                                        scheduledViewModel.deleteScheduledMovie(
                                            movieResult,
                                            it.scheduledDate
                                        )
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }

                        stopScheduleTimeCheck()
                    } else {
                        // Check again in 2 seconds
                        scheduleHandler.postDelayed(this, 2000)
                    }
                } else {
                    stopScheduleTimeCheck()
                }
            }
        }

        // Start checking
        scheduleHandler.postDelayed(scheduleCheckRunnable!!, 2000)
    }

    private fun stopScheduleTimeCheck() {
        scheduleCheckRunnable?.let {
            scheduleHandler.removeCallbacks(it)
            scheduleCheckRunnable = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopScheduleTimeCheck()
        binding.fragmentMovieDetailsYt.release()
        youTubePlayerListener = null
        _binding = null
    }

    private fun updateLastWatchedUI() {
        val series = currentSeriesProgressCache
        if (series != null && series.lastWatchedSeasonNumber != null && series.lastWatchedEpisodeNumber != null) {
            val showBookmark = isInWatchList || isFav
            if (showBookmark) {
                binding.fragmentMovieDetailsLastWatched.apply {
                    val label = getString(
                        R.string.lbl_last_watched,
                        series.lastWatchedSeasonNumber,
                        series.lastWatchedEpisodeNumber
                    )
                    text = SpannableString(label)
                    visibility = View.VISIBLE
                }
            } else {
                binding.fragmentMovieDetailsLastWatched.visibility = View.GONE
            }
        } else {
            binding.fragmentMovieDetailsLastWatched.visibility = View.GONE
        }
    }

    private fun showChoosePosterBottomSheet() {
        val ctx = context ?: return
        val dialog = BottomSheetDialog(ctx, R.style.SheetDialog)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_choose_poster, null)

        val loader = view.findViewById<View>(R.id.choose_poster_loader)
        val rv = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.choose_poster_rv)
        val cancelBtn = view.findViewById<View>(R.id.choose_poster_cancel_btn)
        val galleryBtn = view.findViewById<View>(R.id.choose_poster_from_gallery_btn)

        galleryBtn.setOnClickListener {
            pickPosterLauncher.launch("image/*")
            dialog.dismiss()
        }

        val adapter = com.shalenmathew.movieflix.core.adapters.PosterChoiceAdapter { selectedPath ->
            updateMediaPoster(selectedPath)
            dialog.dismiss()
        }
        rv.adapter = adapter

        val lang = movieResult.originalLanguage
        if (isTVShow) {
            homeInfoViewModel.getTVImages(mediaId ?: -1, lang)
        } else {
            homeInfoViewModel.getMovieImages(mediaId ?: -1, lang)
        }

        homeInfoViewModel.mediaImages.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResults.Loading -> {
                    loader.visibility = View.VISIBLE
                    rv.visibility = View.GONE
                }
                is NetworkResults.Success -> {
                    loader.visibility = View.GONE
                    rv.visibility = View.VISIBLE
                    adapter.submitList(result.data?.posters?.mapNotNull { it.filePath })
                }
                is NetworkResults.Error -> {
                    loader.visibility = View.GONE
                    showToast(ctx, result.message ?: getString(R.string.msg_something_went_wrong))
                }
            }
        }

        cancelBtn.setOnClickListener { dialog.dismiss() }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun updateMediaPoster(posterPath: String) {
        val id = mediaId ?: return
        
        // Update Favorites
        if (isFav) {
            favMovieViewModel.updateFavPoster(id, posterPath)
        }
        
        // Always sync with other lists regardless of where the change was initiated
        // (The option is only visible if isFav is true)
        watchListViewModel.updateWatchListPoster(id, posterPath)
        seriesTrackingViewModel.updateSeriesPoster(id, posterPath)
        customListViewModel.updateMoviePosterAcrossLists(id, posterPath)
        
        showToast(requireContext(), getString(R.string.msg_poster_updated))
    }

    private fun handleLocalPosterSelection(uri: android.net.Uri) {
        val ctx = context ?: return
        val id = mediaId ?: return
        val fileName = "poster_${id}_${System.currentTimeMillis()}.jpg"
        val file = java.io.File(ctx.filesDir, fileName)

        try {
            ctx.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            updateMediaPoster(file.absolutePath)
        } catch (e: Exception) {
            e.printStackTrace()
            showToast(ctx, "Failed to copy image")
        }
    }

    private fun showChooseBannerBottomSheet() {
        val ctx = context ?: return
        val dialog = BottomSheetDialog(ctx, R.style.SheetDialog)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_choose_banner, null)

        val loader = view.findViewById<View>(R.id.choose_banner_loader)
        val rv = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.choose_banner_rv)
        val cancelBtn = view.findViewById<View>(R.id.choose_banner_cancel_btn)
        val galleryBtn = view.findViewById<View>(R.id.choose_banner_from_gallery_btn)

        galleryBtn.setOnClickListener {
            pickBannerLauncher.launch("image/*")
            dialog.dismiss()
        }

        val adapter = com.shalenmathew.movieflix.core.adapters.BannerChoiceAdapter { selectedPath ->
            updateMediaBanner(selectedPath)
            dialog.dismiss()
        }
        rv.adapter = adapter

        val lang = movieResult.originalLanguage
        if (isTVShow) {
            homeInfoViewModel.getTVImages(mediaId ?: -1, lang)
        } else {
            homeInfoViewModel.getMovieImages(mediaId ?: -1, lang)
        }

        homeInfoViewModel.mediaImages.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResults.Loading -> {
                    loader.visibility = View.VISIBLE
                    rv.visibility = View.GONE
                }
                is NetworkResults.Success -> {
                    loader.visibility = View.GONE
                    rv.visibility = View.VISIBLE
                    adapter.submitList(result.data?.backdrops?.mapNotNull { it.filePath })
                }
                is NetworkResults.Error -> {
                    loader.visibility = View.GONE
                    showToast(ctx, result.message ?: getString(R.string.msg_something_went_wrong))
                }
            }
        }

        cancelBtn.setOnClickListener { dialog.dismiss() }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun updateMediaBanner(bannerPath: String?) {
        val id = mediaId ?: return
        if (bannerPath == null) return
        
        // Update Favorites
        if (isFav) {
            favMovieViewModel.updateFavBanner(id, bannerPath)
        }
        
        // Global Sync
        watchListViewModel.updateWatchListBanner(id, bannerPath)
        seriesTrackingViewModel.updateSeriesBanner(id, bannerPath)
        customListViewModel.updateMovieBannerAcrossLists(id, bannerPath)
        
        // Update local UI immediately since this is the banner on the details screen
        val isLocal = bannerPath.startsWith("content://") || bannerPath.count { it == '/' } > 1
        val fullUrl = when {
            isLocal -> bannerPath
            bannerPath.startsWith("http") -> bannerPath
            else -> TMDB_IMAGE_BASE_URL_W780.plus(bannerPath)
        }
        binding.posterImage.loadImage(fullUrl)
        showToast(requireContext(), getString(R.string.msg_banner_updated))
    }

    private fun handleLocalBannerSelection(uri: android.net.Uri) {
        val ctx = context ?: return
        val id = mediaId ?: return
        val fileName = "banner_${id}_${System.currentTimeMillis()}.jpg"
        val file = java.io.File(ctx.filesDir, fileName)

        try {
            ctx.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            updateMediaBanner(file.absolutePath)
        } catch (e: Exception) {
            e.printStackTrace()
            showToast(ctx, "Failed to copy image")
        }
    }

    private fun handleGalleryImageSelection(uri: android.net.Uri) {
        val ctx = context ?: return
        val id = mediaId ?: return
        val fileName = "gallery_${id}_${System.currentTimeMillis()}.jpg"
        val file = java.io.File(ctx.filesDir, fileName)

        try {
            ctx.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            favMovieViewModel.insertGalleryImage(id, file.absolutePath)
        } catch (e: Exception) {
            e.printStackTrace()
            showToast(ctx, "Failed to copy image")
        }
    }

    private fun showImagePreview(imagePath: String) {
        val context = context ?: return
        val dialog = Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(R.layout.dialog_image_preview)
        
        val imageView = dialog.findViewById<ImageView>(R.id.preview_image_view)
        val closeBtn = dialog.findViewById<View>(R.id.preview_close_btn)
        
        imageView.loadImage(imagePath)
        
        closeBtn.setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }

}

