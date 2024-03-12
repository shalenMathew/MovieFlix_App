package com.example.movieflix.presentation.randomRecomm

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.movieflix.R
import com.example.movieflix.core.utils.Constants
import com.example.movieflix.core.utils.NetworkResults
import com.example.movieflix.core.utils.getGenreListById
import com.example.movieflix.core.utils.getRandomChar
import com.example.movieflix.core.utils.gone
import com.example.movieflix.core.utils.isNetworkAvailable
import com.example.movieflix.core.utils.loadImage
import com.example.movieflix.core.utils.visible
import com.example.movieflix.databinding.FragmentRandomRecommendationBinding
import com.example.movieflix.presentation.viewmodels.RandomRecommendationViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RandomRecommendationFragment : Fragment() {

    private var _binding:FragmentRandomRecommendationBinding? =null
    private val binding get() =_binding!!

    private val randomRecommendationViewModel:RandomRecommendationViewModel by viewModels()

    private var job:Job? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(inflater,R.layout.fragment_random_recommendation,container,false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
       handleClickListeners()
        observers()
    }

    private fun observers() {
        randomRecommendationViewModel.searchMovieLiveData.observe(viewLifecycleOwner){
            when(it){
                is NetworkResults.Success->binding.apply{

                    fragmentRrAnime.cancelAnimation()
                    fragmentRrAnime.gone()
                    fragmentRrAnimeBtn.gone()
                    fragmentRrTv.gone()
                    fragmentRrCard.visible()
                    fragmentRrTitle.visible()
                    fragmentRrOverview.visible()
                    fragmentRrRatingDetails.visible()
                    fragmentRrRollAgainBtn.visible()
                    progressBar.gone()


                    it.data?.results?.let {list->
                        fragmentRrImg.loadImage(Constants.TMDB_IMAGE_BASE_URL_W780.plus(list[0].backdropPath))
                        fragmentRrTitle.text=list[0].title
                        fragmentRrRatingDetails.text = getGenreListById(list[0].genreIds).joinToString { genre ->
                            genre.name
                        }
                        fragmentRrOverview.text=list[0].overview
                    }

                    fragmentRrRollAgainBtn.setOnClickListener(){
                        val query = getRandomChar()
                        randomRecommendationViewModel.fetchSearchMovie(query)
                    }
                }
                is NetworkResults.Loading->binding.apply{
                    progressBar.visible()
                }
                is NetworkResults.Error-> binding.apply{

                    progressBar.gone()
                    Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun handleClickListeners() {
        binding.apply {
                fragmentRrAnimeBtn.setOnClickListener(){
                    if (isNetworkAvailable(requireContext())){
                        startSearch()
                    }else{
                        Toast.makeText(requireContext(), "Internet is req", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun startSearch() {
binding.apply {
    fragmentRrAnime.playAnimation()
}
        val query = getRandomChar()
        job?.cancel()
        job = viewLifecycleOwner.lifecycleScope.launch {
            delay(2000)
            randomRecommendationViewModel.fetchSearchMovie(query)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding=null
    }

}