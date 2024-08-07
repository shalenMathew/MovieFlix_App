package com.example.movieflix.presentation.watchlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.movieflix.R
import com.example.movieflix.core.adapters.WatchListAdapter
import com.example.movieflix.core.utils.Constants
import com.example.movieflix.core.utils.gone
import com.example.movieflix.core.utils.visible
import com.example.movieflix.databinding.FragmentWatchListBinding
import com.example.movieflix.presentation.viewmodels.FavMovieViewModel
import com.example.movieflix.presentation.viewmodels.WatchListViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WatchListFragment : Fragment() {

private val watchListViewModel:WatchListViewModel by viewModels()

    private var _binding:FragmentWatchListBinding? = null
    private val binding get() =  _binding!!
    private lateinit var adapter: WatchListAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(inflater,R.layout.fragment_watch_list,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inIt()
        observer()
    }

    private fun inIt() {
        watchListViewModel.getAllWatchListData()
        adapter=WatchListAdapter (onPosterClick = {
            val bundle = Bundle()
            bundle.putString(Constants.MEDIA_SEND_REQUEST_KEY,Gson().toJson(it))
            findNavController().navigate(R.id.action_watchListFragment_to_movieDetailsFragment,bundle)
        })

        binding.fragmentWatchListRv.adapter=adapter
    }

    private fun observer() {
        watchListViewModel.getAllWatchListData().observe(viewLifecycleOwner){

            if (it.isNotEmpty()){
                adapter.submitList(it)
                binding.fragmentWatchListNoMovie.gone()
            }else{
                binding.fragmentWatchListRv.gone()
                binding.fragmentWatchListNoMovie.visible()
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding=null
    }

}