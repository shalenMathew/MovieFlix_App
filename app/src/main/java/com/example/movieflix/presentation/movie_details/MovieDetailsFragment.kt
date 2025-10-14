package com.example.movieflix.presentation.movie_details

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.movieflix.R
import com.example.movieflix.core.adapters.RecommendationAdapter
import com.example.movieflix.core.utils.Constants
import com.example.movieflix.core.utils.Constants.BASE_YOUTUBE_URL
import com.example.movieflix.core.utils.Constants.TMDB_IMAGE_BASE_URL_W780
import com.example.movieflix.core.utils.NetworkResults
import com.example.movieflix.core.utils.formatDate
import com.example.movieflix.core.utils.getGenreListById
import com.example.movieflix.core.utils.gone
import com.example.movieflix.core.utils.loadImage
import com.example.movieflix.core.utils.shareMovie
import com.example.movieflix.core.utils.showToast
import com.example.movieflix.databinding.FragmentMovieDetailsBinding
import com.example.movieflix.domain.model.MovieResult
import com.example.movieflix.domain.model.MediaVideoResult
import com.example.movieflix.presentation.viewmodels.FavMovieViewModel
import com.example.movieflix.presentation.viewmodels.HomeInfoViewModel
import com.example.movieflix.presentation.viewmodels.SearchMovieViewModel
import com.example.movieflix.presentation.viewmodels.WatchListViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MovieDetailsFragment : BottomSheetDialogFragment(){
    //remember to add dialog in nav graph or bottom sheet will not work

    private var _binding:FragmentMovieDetailsBinding?=null
    val binding get() = _binding!!
    private val homeInfoViewModel: HomeInfoViewModel by viewModels()
    private val searchMovieViewModel:SearchMovieViewModel by viewModels()

    private var mediaId:Int? = null
    private lateinit var movieResult: MovieResult
    private var youtubeUrl:String = ""
    private var youTubePlayerListener: AbstractYouTubePlayerListener? = null
    private var youTubePlayer: YouTubePlayer? = null
    private lateinit var recommendationAdapter:RecommendationAdapter
    private var whereToWatchLink:String? = null
    private val customTabsIntent by lazy {
        CustomTabsIntent.Builder().setShowTitle(true).build()
    }
    private val watchListViewModel:WatchListViewModel by viewModels()
    private val favMovieViewModel: FavMovieViewModel by viewModels()

    private var isInWatchList:Boolean = false
    private var isFav:Boolean=false

    private var mediaType:String? = null

    private var isPlaying:Boolean = false


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

            fragmentMovieDetailsWatchlistBtn.setOnClickListener(){
                if (!isInWatchList) {
                    watchListViewModel.insertWatchListData(movieResult)
                    addButtonIcon.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.baseline_done_all_24))
                    showToast(requireContext(),"Movie added to watchList")
                }else{
                    watchListViewModel.deleteWatchListData(movieResult)
                    addButtonIcon.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_add))
                    showToast(requireContext(),"Movie removed from watchList")
                }
               isInWatchList=!isInWatchList
            }

            fragmentMovieDetailsFavBtn.setOnClickListener(){

                if (!isFav){
                    favMovieViewModel.insertFavMovieData(movieResult)
                    favIcon.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.fav_red))
                    showToast(requireContext(),"Movie added to Favourites")
                }else{

                    favMovieViewModel.deleteWatchListData(movieResult)
                    favIcon.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.fav_outline))
                    showToast(requireContext(),"Movie removed from Favourites")
                }

                isFav=!isFav

            }


            fragmentMovieDetailsShareBtn.setOnClickListener(){
             shareMovie(requireContext(),movieResult.title.toString(),youtubeUrl)
            }
        }
    }

    private fun iniIt() {
        recommendationAdapter=RecommendationAdapter(posterClick = {
            openDetailFragment(it)
        })
        binding.fragmentMovieDetailsRecommendList.adapter=recommendationAdapter

    }

    private fun openDetailFragment(it: MovieResult) {
        val bundle = Bundle()
        bundle.putString(Constants.MEDIA_SEND_REQUEST_KEY,Gson().toJson(it))

        findNavController().navigate(R.id.action_movieDetailsFragment_self,bundle)
    }

    private fun setUpObservers() {

        homeInfoViewModel.mediaTrailerList.observe(viewLifecycleOwner){
            when(it){
                is NetworkResults.Success->binding.apply{
                    it.data?.let {result->
                        result.results?.let {videosList->
                            val videosArrayList = videosList as ArrayList
                            val trailerList:List<MediaVideoResult> = videosArrayList.filter { toFilter->
                                // video arraylist response will give us all type of video we only want trailer or teaser from type youtube
                                (toFilter.type==Constants.TRAILER || toFilter.type==Constants.TEASER)&&toFilter.site==Constants.YOUTUBE
                            }
                           try {
                               val movieTrailer = if(trailerList.isEmpty()) videosArrayList[0] else trailerList[0]
                               youtubeUrl="$BASE_YOUTUBE_URL${movieTrailer.key}"

                               binding.fragmentMovieDetailsPlayBtn.setOnClickListener {
                                   // If player is ready, toggle play/pause. Otherwise initialize and start playing
                                   if (youTubePlayer != null) {
                                       if (isPlaying) {
                                           youTubePlayer?.pause()
                                           isPlaying = false
                                           binding.fragmentMovieDetailsPlayBtn.text = "Play Trailer"
                                       } else {
                                           youTubePlayer?.play()
                                           isPlaying = true
                                           binding.fragmentMovieDetailsPlayBtn.text = "Pause Trailer"
                                       }
                                   } else {
                                       initializePlayer(movieTrailer.key)
                                   }
                               }

                           }catch (e:Exception){
                               e.printStackTrace()
                           }
                        }

                    }
                }
                is NetworkResults.Loading->{

                    Log.d("YTPlayerBug","LoDING - "+it.message)
                }
                is NetworkResults.Error->{
                    showToast(requireContext(),""+it.message)
                    Log.d("YTPlayerBug",""+it.message)
                }
            }
        }

        homeInfoViewModel.recommendationLiveData.observe(viewLifecycleOwner){movieList->

            when(movieList){
                is NetworkResults.Success -> {
                    movieList.data?.let {resultList->
                        resultList.results.let {
                            if(it.isNotEmpty()){
                                binding.recommendedText.text="More Like this"
                                recommendationAdapter.submitList(it)
                            }else{
                                binding.recommendedText.text="No recommendation"
                            }
                        }
                    }
                }
                is NetworkResults.Error -> {}
                is NetworkResults.Loading -> {}
            }
        }

        homeInfoViewModel.whereToWatchProviders.observe(viewLifecycleOwner){
            when(it){

                is NetworkResults.Loading->{}
                is NetworkResults.Error-> {}
                is NetworkResults.Success-> binding.apply{
                    it.data?.let {result->
                        result.results?.let {results->
                            whereToWatchLink=results.IN?.link
                        }
                    }
                }

            }
        }

        watchListViewModel.getAllWatchListData().observe(viewLifecycleOwner){

            if(it.isNotEmpty()){
                var isInWatchList:Boolean
                for (result in it){
                    isInWatchList = (result.id==mediaId)
                    if(isInWatchList){
                        changeAddToWatchListIcon()
                        break
                    }
                }
            }

        }

        favMovieViewModel.getAllMovieData().observe(viewLifecycleOwner){

            if(it.isNotEmpty()){
                var isFav:Boolean
                for (res in it){
                    isFav = (res.id==mediaId)

                    if(isFav){
                        changeFavIcon()
                        break
                    }
                }
            }

        }

        searchMovieViewModel.searchMovieLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkResults.Success -> {

                    it.data?.let { movieList ->

                        if(movieList.results.isNotEmpty()){
                            mediaType = movieList.results[0].mediaType
                            val id = movieList.results[0].id

                            when(mediaType){
                                "movie" -> {

                                    if (id!=null){
                                        homeInfoViewModel.getMovieTrailer(id)
                                    }else{
                                        showToast(requireContext(),"media id is null")
                                    }
                                }
                                "tv" -> {
                                    if (id!=null){
                                        homeInfoViewModel.getTVTrailer(id)
                                    }else{
                                        showToast(requireContext(),"media id is null")
                                    }
                                }
                            }
                        }
                    }
                }


                is NetworkResults.Error -> {
                    showToast(requireContext(),""+it.message)
                }
                is NetworkResults.Loading -> {
                    Log.d("YTPlayerBug","LoDING - "+it.message)
                }
            }

        }
    }

    private fun changeAddToWatchListIcon() {
        binding.apply {
            isInWatchList=true
            addButtonIcon.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.baseline_done_all_24))
        }
    }

   private fun changeFavIcon(){
       binding.apply {
           isFav=true
           favIcon.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.fav))
       }
   }

    private fun initializePlayer(key: String?) {

        binding.apply {

            posterImage.gone()

                // initialise the player if player is null

                fragmentMovieDetailsYt.getYouTubePlayerWhenReady(object: YouTubePlayerCallback {
                    override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {

                        Log.d("YTPlayerBug","inside on ready")

                        key?.let {
                            this@MovieDetailsFragment.youTubePlayer=youTubePlayer
                            this@MovieDetailsFragment.youTubePlayer?.loadVideo(it,0f)
                            // After first initialization, mark as playing and update button text
                            isPlaying = true
                            binding.fragmentMovieDetailsPlayBtn.text = "Pause Trailer"

                            Log.d("YTPlayerBug","key inside = "+key)

                            Log.d("YTPlayerBug","yt player inside = "+ youTubePlayer)

                        } ?: run {

                            Log.d("YTPlayerBug","key inside run = "+key)
                        }
                    }

                })

            // Attach a listener to keep UI in sync with player state
            if (youTubePlayerListener == null) {
                youTubePlayerListener = object : AbstractYouTubePlayerListener() {
                    override fun onStateChange(youTubePlayer: YouTubePlayer, state: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.PlayerConstants.PlayerState) {
                        when (state) {
                            com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.PlayerConstants.PlayerState.PLAYING -> {
                                isPlaying = true
                                binding.fragmentMovieDetailsPlayBtn.text = "Pause Trailer"
                            }
                            com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.PlayerConstants.PlayerState.PAUSED -> {
                                isPlaying = false
                                binding.fragmentMovieDetailsPlayBtn.text = "Play Trailer"
                            }
                            com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.PlayerConstants.PlayerState.ENDED -> {
                                isPlaying = false
                                binding.fragmentMovieDetailsPlayBtn.text = "Play Trailer"
                            }
                            else -> { /* no-op */ }
                        }
                    }
                }
                fragmentMovieDetailsYt.addYouTubePlayerListener(youTubePlayerListener!!)
            }

            Log.d("YTPlayerBug","yt player listener " + youTubePlayerListener)
            Log.d("YTPlayerBug","yt player : " + youTubePlayer)

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


            binding.apply {
                fragmentMovieDetailsTitle.text = title
                fragmentMovieDetailsGenre.text = getGenreListById(genreList).joinToString { genre ->
                    genre.name
                }
                posterImage.loadImage(TMDB_IMAGE_BASE_URL_W780.plus(img))
                fragmentMovieDetailsLang.text = language
                fragmentMovieDetailsOverview.text = overView
                fragmentMovieDetailsRating.text = String.format("%.1f", rating)
                fragmentMovieDetailsYear.text = formatDate(year)
            }

            mediaId?.let { id ->


                when (it.mediaType) {
                    "movie" -> {
                        homeInfoViewModel.getMovieTrailer(id)
                    }
                    "tv" -> {
                        homeInfoViewModel.getTVTrailer(id)
                    }
                    else -> {

                        if (!title.isNullOrEmpty()) {
                            searchMovieViewModel.fetchSearchMovie(title)
                        }

                    }
                }

                homeInfoViewModel.getRecommendation(id)
                homeInfoViewModel.getWhereToWatchProvider(id)
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

    override fun onPause() {
        super.onPause()
        youTubePlayer?.pause()
    }

    override fun onResume() {
        super.onResume()
        if (isPlaying) {
            youTubePlayer?.play()
        }
    }

    override fun onStop() {
        super.onStop()
        youTubePlayer?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.fragmentMovieDetailsYt.release()
        youTubePlayerListener=null
        _binding=null
    }

}

