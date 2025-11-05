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
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
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
import com.shalenmathew.movieflix.presentation.viewmodels.WatchListViewModel
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
    private lateinit var watchProviderAdapter: com.shalenmathew.movieflix.core.adapters.WatchProviderAdapter
    private var whereToWatchLink: String? = null
    private val customTabsIntent by lazy {
        CustomTabsIntent.Builder().setShowTitle(true).build()
    }
    private val watchListViewModel: WatchListViewModel by viewModels()
    private val favMovieViewModel: FavMovieViewModel by viewModels()
    private val scheduledViewModel: ScheduledViewModel by viewModels()

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
    private var isTVShow = false
    private var tvDetailsLoaded = false

    private var isPlaying: Boolean = false

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
                    "Notification permission is required for scheduled reminders"
                )
            }
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
        setUpObservers()
        iniIt()
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
                    showToast(ctx, "Movie added to watchList")
                } else {
                    watchListViewModel.deleteWatchListData(movieResult)
                    addButtonIcon.setImageDrawable(
                        ContextCompat.getDrawable(
                            ctx,
                            R.drawable.ic_add
                        )
                    )
                    showToast(ctx, "Movie removed from watchList")
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
                    showToast(ctx, "Movie added to Favourites")
                } else {

                    favMovieViewModel.deleteWatchListData(movieResult)
                    favIcon.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.fav_outline))
                    showToast(ctx, "Movie removed from Favourites")
                }

                isFav = !isFav
                updateScheduleButtonVisibility()

            }


            fragmentMovieDetailsShareBtn.setOnClickListener() {
                if (!::movieResult.isInitialized) return@setOnClickListener
                val ctx = context ?: return@setOnClickListener
                shareMovie(ctx, movieResult.title.toString(), youtubeUrl)
            }

            fragmentMovieDetailsScheduleBtn.setOnClickListener {
                val ctx = context ?: return@setOnClickListener

                if (!isScheduled) {
                    // Check notification permission first (Android 13+)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        when {
                            NotificationHelper.hasNotificationPermission(ctx) -> {
                                showScheduleDateTimePicker()
                            }

                            shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                                // Show explanation
                                showToast(
                                    ctx,
                                    "Allow notifications to get reminders for scheduled movies"
                                )
                                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }

                            else -> {
                                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    } else {
                        // No permission needed for older Android versions
                        showScheduleDateTimePicker()
                    }
                } else {
                    // Remove schedule
                    if (!::movieResult.isInitialized) return@setOnClickListener
                    scheduledViewModel.deleteScheduledMovie(movieResult, currentScheduledDate)
                    isScheduled = false
                    currentScheduledDate = 0
                    updateScheduleButtonIcon()
                    showToast(ctx, "Schedule removed")
                }
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
                    context?.let { ctx -> showToast(ctx, "" + it.message) }
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
                                binding.recommendedText.text = "More Like this"
                                recommendationAdapter.submitList(it)
                            } else {
                                binding.recommendedText.text = "No recommendation"
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

        watchListViewModel.getAllWatchListData().observe(viewLifecycleOwner) {

            if (it.isNotEmpty()) {
                var isInWatchList: Boolean
                for (result in it) {
                    isInWatchList = (result.id == mediaId)
                    if (isInWatchList) {
                        changeAddToWatchListIcon()
                        updateScheduleButtonVisibility()
                        break
                    }
                }
            }

        }

        favMovieViewModel.getAllMovieData().observe(viewLifecycleOwner) {

            if (it.isNotEmpty()) {
                var isFav: Boolean
                for (res in it) {
                    isFav = (res.id == mediaId)

                    if (isFav) {
                        changeFavIcon()
                        setupPersonalNoteView(mediaId!!, res.personalNote)
                        binding.fragmentMovieDetailsPersonalNoteLl.isVisible = true
                        updateScheduleButtonVisibility()
                        break
                    }
                    binding.fragmentMovieDetailsPersonalNoteLl.isVisible = false
                }
            }

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
                                        context?.let { ctx -> showToast(ctx, "media id is null") }
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
                                    } else {
                                        context?.let { ctx -> showToast(ctx, "media id is null") }
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
                                "Season $currentSeasonNumber • Episodes ${epCount}"
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
                        episodeAdapter.submitList(season.episodes)
                    }
                }

                is NetworkResults.Error -> {
                    context?.let { ctx ->
                        showToast(
                            ctx,
                            "Error loading episodes: ${result.message}"
                        )
                    }
                }

                is NetworkResults.Loading -> {
                    // Show loading state if needed
                }
            }
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

    private fun loadSeason(seasonNumber: Int) {
        // Update current season number
        currentSeasonNumber = seasonNumber

        // Update dropdown text
        binding.seasonDropdownButton.text = "Season $seasonNumber"

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
            context?.let { ctx ->
                binding.apply {
                    if (isScheduled) {
                        scheduleIcon.setImageDrawable(
                            ContextCompat.getDrawable(
                                ctx,
                                R.drawable.ic_calendar_check
                            )
                        )
                        // Show and update scheduled date text
                        updateScheduledDateText()
                    } else {
                        scheduleIcon.setImageDrawable(
                            ContextCompat.getDrawable(
                                ctx,
                                R.drawable.baseline_calendar_month_24
                            )
                        )
                        // Hide scheduled date text when not scheduled
                        fragmentMovieDetailsScheduledDate.visibility = View.GONE
                        fragmentMovieDetailsScheduledIcon.visibility = View.GONE
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Silently fail if view is not ready
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
                    text = "Scheduled on: $day$daySuffix $dateStr"
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
        binding.fragmentMovieDetailsScheduleBtnContainer.visibility =
            if (isInWatchList || isFav) View.VISIBLE else View.GONE
    }

    private fun showScheduleDateTimePicker() {
        if (!::movieResult.isInitialized) {
            context?.let { ctx -> showToast(ctx, "Movie data not loaded yet. Please try again.") }
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
                    "Scheduled for $formattedDate. You'll get a notification!"
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
                context?.let { ctx -> showToast(ctx, "No seasons available") }
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
                                        "Pause Trailer".also {
                                            binding.fragmentMovieDetailsPlayBtn.text = it
                                        }
                                        context?.let { ctx ->
                                            binding.fragmentMovieDetailsPlayBtn.icon =
                                                ContextCompat.getDrawable(ctx, R.drawable.ic_pause)
                                        }
                                    }

                                    PlayerConstants.PlayerState.PAUSED -> {
                                        isPlaying = false
                                        "Play Trailer".also {
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
                                        "Play Trailer".also {
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
                fragmentMovieDetailsGenre.text = getGenreListById(genreList).joinToString { genre ->
                    genre.name
                }
                posterImage.loadImage(TMDB_IMAGE_BASE_URL_W780.plus(img))
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

}

