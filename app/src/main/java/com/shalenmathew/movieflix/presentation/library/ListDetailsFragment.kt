package com.shalenmathew.movieflix.presentation.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.gson.Gson
import com.shalenmathew.movieflix.R
import androidx.databinding.DataBindingUtil
import com.shalenmathew.movieflix.core.adapters.HorizontalAdapter
import com.shalenmathew.movieflix.core.utils.Constants
import com.shalenmathew.movieflix.databinding.FragmentListDetailsBinding
import com.shalenmathew.movieflix.domain.model.MovieResult
import com.shalenmathew.movieflix.presentation.viewmodels.CustomListViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ListDetailsFragment : Fragment() {

    private val viewModel: CustomListViewModel by viewModels()
    private var _binding: FragmentListDetailsBinding? = null
    private val mBinding get() = _binding!!

    // We'll use navArgs to get the list ID and name
    // For now, let's just use arguments directly until we update navigation.xml
    private var listId: Int = -1
    private var listName: String = ""
    private var listDesc: String? = null

    private lateinit var adapter: HorizontalAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DataBindingUtil.inflate<FragmentListDetailsBinding>(inflater, com.shalenmathew.movieflix.R.layout.fragment_list_details, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        listId = arguments?.getInt("listId") ?: -1
        listName = arguments?.getString("listName") ?: ""
        listDesc = arguments?.getString("listDesc")

        setupUI()
        setupRecyclerView()
        observeData()
    }

    private fun setupUI() {
        mBinding.listDetailsName.text = listName
        mBinding.listDetailsDesc.text = listDesc ?: ""
        mBinding.listDetailsDesc.visibility = if (listDesc.isNullOrEmpty()) View.GONE else View.VISIBLE
        
        mBinding.listDetailsBackBtn.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupRecyclerView() {
        adapter = HorizontalAdapter(onPosterClick = { movie ->
            val bundle = Bundle()
            bundle.putString(Constants.MEDIA_SEND_REQUEST_KEY, Gson().toJson(movie))
            findNavController().navigate(R.id.movieDetailsFragment, bundle)
        })
        mBinding.listMoviesRv.adapter = adapter
    }

    private fun observeData() {
        if (listId != -1) {
            viewModel.getMoviesInList(listId).observe(viewLifecycleOwner) { movies ->
                val movieResults = movies.map { 
                    MovieResult(
                        id = it.mediaId,
                        name = it.name,
                        title = it.name,
                        posterPath = it.posterPath,
                        backdropPath = it.backdropPath,
                        overview = it.overview,
                        mediaType = it.mediaType,
                        voteAverage = it.voteAverage,
                        releaseDate = it.releaseDate,
                        originalLanguage = it.originalLanguage
                    )
                }
                adapter.submitList(movieResults)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
