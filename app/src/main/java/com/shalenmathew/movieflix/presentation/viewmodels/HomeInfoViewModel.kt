package com.shalenmathew.movieflix.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shalenmathew.movieflix.core.utils.NetworkResults
import com.shalenmathew.movieflix.domain.model.CastMember
import com.shalenmathew.movieflix.domain.model.CrewMember
import com.shalenmathew.movieflix.domain.model.HomeFeedData
import com.shalenmathew.movieflix.domain.model.MovieList
import com.shalenmathew.movieflix.domain.model.MediaVideoResultList
import com.shalenmathew.movieflix.domain.model.TVDetail
import com.shalenmathew.movieflix.domain.model.TVSeason
import com.shalenmathew.movieflix.domain.model.WatchProviders
import com.shalenmathew.movieflix.domain.usecases.GetMovieInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeInfoViewModel @Inject constructor(private val getMovieInfo: GetMovieInfo):ViewModel() {

    private var _homeFeedList:MutableLiveData<NetworkResults<HomeFeedData>> = MutableLiveData()
    var homeFeedList:LiveData<NetworkResults<HomeFeedData>> = _homeFeedList

    private var _mediaTrailerList = MutableLiveData<NetworkResults<MediaVideoResultList>>()
    val mediaTrailerList:LiveData<NetworkResults<MediaVideoResultList>> = _mediaTrailerList

    private var _recommendationList=MutableLiveData<NetworkResults<MovieList>>()
    val recommendationLiveData:LiveData<NetworkResults<MovieList>> = _recommendationList

    private var _whereToWatchProvider=MutableLiveData<NetworkResults<WatchProviders>>()
    val whereToWatchProviders:LiveData<NetworkResults<WatchProviders>> = _whereToWatchProvider

    private var _castList = MutableLiveData<NetworkResults<List<CastMember>>>()
    val castList: LiveData<NetworkResults<List<CastMember>>> = _castList

    private var _crewList = MutableLiveData<NetworkResults<List<CrewMember>>>()
    val crewList: LiveData<NetworkResults<List<CrewMember>>> = _crewList

    private var _tvDetail = MutableLiveData<NetworkResults<TVDetail>>()
    val tvDetail: LiveData<NetworkResults<TVDetail>> = _tvDetail

    private var _tvSeason = MutableLiveData<NetworkResults<TVSeason>>()
    val tvSeason: LiveData<NetworkResults<TVSeason>> = _tvSeason

    // Pagination support
    private var _loadMoreMovies = MutableLiveData<NetworkResults<Pair<String, MovieList>>>()
    val loadMoreMovies: LiveData<NetworkResults<Pair<String, MovieList>>> = _loadMoreMovies

    private val categoryPages = mutableMapOf<String, Int>()
    private val isLoadingMore = mutableMapOf<String, Boolean>()

    init {
        getMovieInfoData() // call the function as soon the viewmodel is initialized
    }

 fun getMovieInfoData(){

     if (_homeFeedList.value is NetworkResults.Loading || _homeFeedList.value is NetworkResults.Success) {
         return
     }

    viewModelScope.launch {
        getMovieInfo.getMovieInfo().onEach {  // onEach -> each time a data is emitted the below block will run
            _homeFeedList.value=it
        }.launchIn(this) // -> launchIn is used to launch the collection of the Flow inside a specific coroutine scope.
    // Instead of manually calling 'collect', launchIn automatically starts collecting the Flow in the scope you specify.

        // launchIn vs Collect
        // launchIn is a non blocking in nature which mean code below launchIn will still run even when all values are not emitted
        // whereas the code below or next line below .collect() won't run until all emitted values is being /
    }
 }

    fun getMovieTrailer(movieId:Int){
        viewModelScope.launch {
            getMovieInfo.getMovieTrailer(movieId).onEach {
                _mediaTrailerList.value = it
            }.launchIn(this)
        }
    }


    fun getTVTrailer(tvId:Int){
        viewModelScope.launch {
            getMovieInfo.getTVTrailer(tvId).onEach {
                _mediaTrailerList.value = it
            }.launchIn(this)
        }
    }

    fun getRecommendation(movieId: Int){
        viewModelScope.launch {
            getMovieInfo.getRecommendation(movieId).onEach {
                _recommendationList.value=it
            }.launchIn(this)
        }
    }

    fun getWhereToWatchProvider(movieId:Int){
        viewModelScope.launch {
            getMovieInfo.getWhereToWatchProviders(movieId).onEach {
                _whereToWatchProvider.value=it
            }.launchIn(this)
        }
    }

    fun getTVWhereToWatchProvider(tvId:Int){
        viewModelScope.launch {
            getMovieInfo.getTVWhereToWatchProviders(tvId).onEach {
                _whereToWatchProvider.value=it
            }.launchIn(this)
        }
    }

    fun loadMoreMoviesForCategory(categoryTitle: String) {
        // Check if already loading for this category
        if (isLoadingMore[categoryTitle] == true) return

        // Get next page number (starts from 2 since initial load is page 1)
        val nextPage = categoryPages.getOrDefault(categoryTitle, 1) + 1

        isLoadingMore[categoryTitle] = true

        viewModelScope.launch {
            getMovieInfo.loadMoreMoviesForCategory(categoryTitle, nextPage).onEach { result ->
                when (result) {
                    is NetworkResults.Success -> {
                        result.data?.let { movieList ->
                            categoryPages[categoryTitle] = nextPage
                            _loadMoreMovies.value = NetworkResults.Success(Pair(categoryTitle, movieList))
                        }
                        isLoadingMore[categoryTitle] = false
                    }
                    is NetworkResults.Error -> {
                        _loadMoreMovies.value = NetworkResults.Error(result.message ?: "Error loading more")
                        isLoadingMore[categoryTitle] = false
                    }
                    is NetworkResults.Loading -> {
                        // Keep loading state
                    }
                }
            }.launchIn(this)
        }
    }

    fun isLoadingCategory(categoryTitle: String): Boolean {
        return isLoadingMore[categoryTitle] == true
    }

    fun getMovieCast(movieId: Int) {
        viewModelScope.launch {
            getMovieInfo.getMovieCast(movieId).onEach {
                _castList.value = it
            }.launchIn(this)
        }
    }

    fun getTVCast(tvId: Int) {
        viewModelScope.launch {
            getMovieInfo.getTVCast(tvId).onEach {
                _castList.value = it
            }.launchIn(this)
        }
    }

    fun getMovieCrew(movieId: Int) {
        viewModelScope.launch {
            getMovieInfo.getMovieCrew(movieId).onEach {
                _crewList.value = it
            }.launchIn(this)
        }
    }

    fun getTVCrew(tvId: Int) {
        viewModelScope.launch {
            getMovieInfo.getTVCrew(tvId).onEach {
                _crewList.value = it
            }.launchIn(this)
        }
    }

    fun getTVDetail(tvId: Int) {
        viewModelScope.launch {
            getMovieInfo.getTVDetail(tvId).onEach {
                _tvDetail.value = it
            }.launchIn(this)
        }
    }

    fun getTVSeason(tvId: Int, seasonNumber: Int) {
        viewModelScope.launch {
            getMovieInfo.getTVSeason(tvId, seasonNumber).onEach {
                _tvSeason.value = it
            }.launchIn(this)
        }
    }

}
