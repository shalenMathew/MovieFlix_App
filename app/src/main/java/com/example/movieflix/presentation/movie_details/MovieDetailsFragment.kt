package com.example.movieflix.presentation.movie_details

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.net.toUri

import androidx.databinding.DataBindingUtil
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
import com.example.movieflix.domain.model.MovieVideoResult
import com.example.movieflix.presentation.viewmodels.FavMovieViewModel
import com.example.movieflix.presentation.viewmodels.HomeInfoViewModel
import com.example.movieflix.presentation.viewmodels.WatchListViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MovieDetailsFragment : BottomSheetDialogFragment(){
    //remember to add dialog in nav graph or bottom sheet will not work

    private var _binding:FragmentMovieDetailsBinding?=null
    val binding get() = _binding!!
    private val homeInfoViewModel: HomeInfoViewModel by viewModels()
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


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        setStyle(STYLE_NO_FRAME, R.style.SheetDialog)
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= DataBindingUtil.inflate(inflater,R.layout.fragment_movie_details,container,false)
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

            fragmentMovieDetailsWatchBtn.setOnClickListener(){
                whereToWatchLink?.let {
                    customTabsIntent.launchUrl(requireContext(),it.toUri())
                }?: showToast(requireContext(),"No information available")
            }

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

        homeInfoViewModel.movieTrailerList.observe(viewLifecycleOwner){
            when(it){
                is NetworkResults.Success->binding.apply{
                    it.data?.let {result->
                        result.results?.let {videosList->
                            val videosArrayList = videosList as ArrayList
                            val trailerList:List<MovieVideoResult> = videosArrayList.filter {toFilter->
                                // video arraylist response will give us all type of video we only want trailer or teaser from type youtube
                                (toFilter.type==Constants.TRAILER || toFilter.type==Constants.TEASER)&&toFilter.site==Constants.YOUTUBE
                            }
                           try {
                               val movieTrailer = if(trailerList.isEmpty()) videosArrayList[0] else trailerList[0]
                               youtubeUrl="$BASE_YOUTUBE_URL${movieTrailer.key}"
                               binding.ytIcon.setOnClickListener(){
                                   initializePlayer(movieTrailer.key)
                               }
                           }catch (e:Exception){
                               e.printStackTrace()
                           }
                        }

                    }
                }
                is NetworkResults.Loading->{}
                is NetworkResults.Error->{}
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
        binding.posterImage.gone()
        binding.ytIcon.gone()
        binding.apply {

            if (youTubePlayerListener!=null){
                if (key!=null){
                    youTubePlayer?.loadVideo(key,0f)
                }
            }

            youTubePlayerListener=object:AbstractYouTubePlayerListener(){
                override fun onReady(youTubePlayer: YouTubePlayer) {
                    super.onReady(youTubePlayer)
                    key?.let {
                        this@MovieDetailsFragment.youTubePlayer=youTubePlayer
                        this@MovieDetailsFragment.youTubePlayer?.loadVideo(key,0f)
                    }
                }
            }
            fragmentMovieDetailsYt.addYouTubePlayerListener(youTubePlayerListener!!)
        }
    }

    private fun setUpDetailFragment() {
        val result = Gson().fromJson(arguments?.getString(Constants.MEDIA_SEND_REQUEST_KEY),MovieResult::class.java)

        result?.let {
            mediaId=it.id
            movieResult=it

            val genreList:List<Int>? = it.genreIds
            val title = it.title
            val overView = it.overview
            val language = it.originalLanguage
            val rating=it.voteAverage
            val year = it.releaseDate
            val img = it.backdropPath

            binding.apply {
                fragmentMovieDetailsTitle.text=title
                fragmentMovieDetailsGenre.text= getGenreListById(genreList).joinToString {genre->
                    genre.name
                }
                posterImage.loadImage(TMDB_IMAGE_BASE_URL_W780.plus(img))
                fragmentMovieDetailsLang.text=language
                fragmentMovieDetailsOverview.text=overView
                fragmentMovieDetailsRating.text= String.format("%.1f", rating)
                fragmentMovieDetailsYear.text=formatDate(year)
            }

            mediaId?.let {id->
                homeInfoViewModel.getMovieTrailer(id)
                homeInfoViewModel.getRecommendation(id)
                homeInfoViewModel.getWhereToWatchProvider(id)
            }
        }

    }

    override fun onPause() {
        super.onPause()
        youTubePlayer?.pause()
    }

    override fun onResume() {
        super.onResume()
        youTubePlayer?.play()
    }

    override fun onStop() {
        super.onStop()
        youTubePlayer?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.fragmentMovieDetailsYt.release()
        _binding=null
    }

}