package com.example.movieflix.presentation.search

import android.net.Network
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.movieflix.R
import com.example.movieflix.core.adapters.HorizontalAdapter
import com.example.movieflix.core.adapters.TrendingMovieAdapter
import com.example.movieflix.core.utils.Constants
import com.example.movieflix.core.utils.NetworkResults
import com.example.movieflix.core.utils.gone
import com.example.movieflix.core.utils.isNetworkAvailable
import com.example.movieflix.core.utils.openNetworkSettings
import com.example.movieflix.core.utils.visible
import com.example.movieflix.databinding.FragmentSearchBinding
import com.example.movieflix.presentation.base.BaseFragment
import com.example.movieflix.presentation.viewmodels.SearchMovieViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@AndroidEntryPoint
class SearchFragment : BaseFragment() {

    private var _binding:FragmentSearchBinding? = null
    private val binding get() = _binding!!

   private val searchMovieViewModel:SearchMovieViewModel by viewModels()

    private lateinit var horizontalAdapter: HorizontalAdapter
    private lateinit var trendingMovieAdapter: TrendingMovieAdapter

    private var query:String? = null

    private var job:Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding=DataBindingUtil.inflate(inflater,R.layout.fragment_search,container,false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        intit()
        setUpObservers()
        clickHandlers()
    }

    private fun clickHandlers() {
        binding.fragmentHomeNetworkCheck.layoutNetworkBtn.setOnClickListener(){
            openNetworkSettings(requireContext())
        }
    }

    private fun intit() {
        searchMovieViewModel.fetchTrendingMovies()

        trendingMovieAdapter=TrendingMovieAdapter(onClick = {
            val bundle = Bundle()
            bundle.putString(Constants.MEDIA_SEND_REQUEST_KEY,Gson().toJson(it))
            findNavController().navigate(R.id.action_searchFragment_to_movieDetailsFragment,bundle)
        })

        horizontalAdapter = HorizontalAdapter(onPosterClick = {
            val bundle =Bundle()
            bundle.putString(Constants.MEDIA_SEND_REQUEST_KEY,Gson().toJson(it))
            findNavController().navigate(R.id.action_searchFragment_to_movieDetailsFragment,bundle)
        })
        binding.apply {
           fragmentSearchSearchResult.adapter=horizontalAdapter
            fragmentSearchTrendRv.adapter=trendingMovieAdapter
            fragmentSearchEt.doOnTextChanged { text,_,_,_ ->
                text?.let {searchText->
                    query=searchText.trim().toString()
                    if(searchText.isNotEmpty() && searchText.isNotBlank()){
                        performSearch(searchText.trim().toString())
                    }else{
                        searchMovieViewModel.fetchTrendingMovies()
                        job?.cancel()
                    }
                }
            }
        }
    }

    private fun performSearch(searchedMovie: String) {
        // job is a way to control coroutine, we can run multiple coroutine  concurrently or we can cancel a coroutine using a job
        // here what we want is we will put a delay of 500ms before displaying the search result
        // so jobs can be used to pause or put some delay before displaying the result
        job?.cancel()
        job = viewLifecycleOwner.lifecycleScope.launch {
            // lifecyclescope is a type of scope where the the lifecycle of coroutine is tied with the lifecycle of a component
            // viewLifeCycleOwner represents the life cycle of a fragment
            // so  viewLifecycleOwner.lifecycleScope mean launching a life cycle which will live as long as the fragment
            delay(500)
            searchMovieViewModel.fetchSearchMovie(searchedMovie)
        }
    }

    private fun setUpObservers() {
        searchMovieViewModel.searchMovieLiveData.observe(viewLifecycleOwner){
            when(it){
                is NetworkResults.Success->binding.apply{

                    it.data?.let {movieList->
                       if (movieList.results.isNotEmpty()){
                           fragmentSearchPb.gone()
                           fragmentSearchTrendRv.gone()
                           fragmentSearchSearchResult.visible()
                           horizontalAdapter.submitList(movieList.results)
                       }else{
                           Toast.makeText(context, "No movie found", Toast.LENGTH_SHORT).show()
                       }
                    }

                }
                is NetworkResults.Loading-> binding.apply{
                    fragmentSearchPb.visible()
                }
                is NetworkResults.Error-> binding.apply{
                    fragmentSearchPb.gone()
                    Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        searchMovieViewModel.trendingMovies.observe(viewLifecycleOwner){
            when(it){
                is NetworkResults.Success->{
                    binding.apply {
                        fragmentSearchPb.gone()
                        fragmentSearchSearchResult.gone()
                        fragmentSearchTrendRv.visible()
                        trendingMovieAdapter.submitList(it.data?.results)
                    }
                }
                is NetworkResults.Loading-> {
                    binding.fragmentSearchPb.visible()
                }
                is NetworkResults.Error-> binding.apply{

                    fragmentSearchPb.gone()
                    Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    override fun onResume() {
        super.onResume()
        checkNetworkAvailability()
    }

    private fun checkNetworkAvailability() {
        if(isNetworkAvailable(requireContext())){
            binding.fragmentHomeNetworkCheck.layoutNtwContainer.gone()
        }else{
            binding.fragmentHomeNetworkCheck.layoutNtwContainer.visible()
        }
    }

    override fun onNetworkAvailable(network: Network) {
        super.onNetworkAvailable(network)
        requireActivity().runOnUiThread{

            binding.fragmentHomeNetworkCheck.layoutNtwContainer.gone()
        }
    }

    override fun onNetworkLost(network: Network?) {
        super.onNetworkLost(network)
        requireActivity().runOnUiThread{
            binding.fragmentHomeNetworkCheck.layoutNtwContainer.visible()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding=null
    }

}