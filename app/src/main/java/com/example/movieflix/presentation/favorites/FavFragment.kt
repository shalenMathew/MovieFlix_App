package com.example.movieflix.presentation.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.movieflix.R
import com.example.movieflix.core.adapters.FavAdapters
import com.example.movieflix.core.utils.Constants
import com.example.movieflix.core.utils.gone
import com.example.movieflix.core.utils.visible
import com.example.movieflix.databinding.FragmentFavBinding
import com.example.movieflix.presentation.viewmodels.FavMovieViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class FavFragment : Fragment() {
    private val favMovieViewModel:FavMovieViewModel by viewModels()

    private var _binding: FragmentFavBinding? = null
    private val binding get() =  _binding!!
    private lateinit var adapter: FavAdapters

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentFavBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inIt()
        observer()
    }


    private fun inIt() {
        favMovieViewModel.getAllMovieData()
        adapter = FavAdapters(onPosterClick = { selectedItem ->
            val bundle = Bundle().apply {
                putString(Constants.MEDIA_SEND_REQUEST_KEY, Gson().toJson(selectedItem))
            }
            findNavController().navigate(R.id.action_libraryFragment_to_movieDetailsFragment, bundle)
        })

        binding.fragmentFavRv.adapter = adapter
    }

    private fun observer() {
        favMovieViewModel.getAllMovieData().observe(viewLifecycleOwner) { favouriteItems ->
            adapter.submitList(favouriteItems)

            if (favouriteItems.isNullOrEmpty()) {
                binding.fragmentFavRv.gone()
                binding.fragmentFavPlaceholder.visible()
                binding.randomEmoji.visible()
            } else {
                binding.fragmentFavRv.visible()
                binding.fragmentFavPlaceholder.gone()
                binding.randomEmoji.gone()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding=null
    }

}