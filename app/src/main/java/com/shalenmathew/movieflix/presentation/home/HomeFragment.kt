package com.shalenmathew.movieflix.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.SnapHelper
import com.shalenmathew.movieflix.R
import com.shalenmathew.movieflix.core.adapters.BannerAdapter
import com.shalenmathew.movieflix.core.adapters.HomeAdapter
import com.shalenmathew.movieflix.core.utils.Constants
import com.shalenmathew.movieflix.core.utils.NetworkResults
import com.shalenmathew.movieflix.core.utils.gone
import com.shalenmathew.movieflix.core.utils.openNetworkSettings
import com.shalenmathew.movieflix.core.utils.visible
import com.shalenmathew.movieflix.databinding.FragmentHomeBinding
import com.shalenmathew.movieflix.domain.model.HomeFeedData
import com.shalenmathew.movieflix.domain.model.MovieResult
import com.shalenmathew.movieflix.presentation.base.BaseFragment
import com.shalenmathew.movieflix.presentation.viewmodels.HomeInfoViewModel
import com.shalenmathew.movieflix.presentation.viewmodels.ScheduledViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HomeFragment : BaseFragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val homeInfoViewModel: HomeInfoViewModel by viewModels() // initializing viewmodel
    private val scheduledViewModel: ScheduledViewModel by viewModels()
    private lateinit var bannerAdapter: BannerAdapter
    private lateinit var homeAdapter: HomeAdapter
    private var bannerList: List<MovieResult> = arrayListOf()
    private var snapHelper: SnapHelper = PagerSnapHelper()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        handleClickListeners()
        observer()
    }

    private fun handleClickListeners() {
        binding.apply {

            fragmentHomeNetworkCheck.layoutNetworkBtn.setOnClickListener {
                openNetworkSettings(requireContext())
            }

            fragmentHomeWatchNowBtn.setOnClickListener {
                val layoutManger =
                    binding.fragmentHomeBannerImgRv.layoutManager as LinearLayoutManager
                val firstVisibleItem = layoutManger.findFirstVisibleItemPosition()
                openDetailFragment(bannerList[firstVisibleItem])
            }
        }
    }

    private fun initView() {
        bannerAdapter = BannerAdapter()
        homeAdapter = HomeAdapter(
            onPosterClick = { openDetailFragment(it) },
            onLoadMore = { categoryTitle ->
                homeInfoViewModel.loadMoreMoviesForCategory(categoryTitle)
            }
        )

        binding.apply {
            fragmentHomeBannerImgRv.adapter = bannerAdapter
            fragmentHomeHomeFeedRv.adapter = homeAdapter
            snapHelper.attachToRecyclerView(fragmentHomeBannerImgRv)
            fragmentHomeIndicator.attachToRecyclerView(fragmentHomeBannerImgRv)
        }
    }

    object ClickHandler{
        private var lastClick = 0L;

        fun isClickAllowed(): Boolean {
            val allowUser = System.currentTimeMillis() - lastClick > 1000L
            if(allowUser){
                lastClick = System.currentTimeMillis()
            }
            return allowUser;

        }
    }

    private fun openDetailFragment(it: MovieResult) {

        if(!ClickHandler.isClickAllowed()){
            return
        }
        val bundle = Bundle()
        bundle.putString(Constants.MEDIA_SEND_REQUEST_KEY, Gson().toJson(it))

        findNavController().navigate(R.id.action_homeFragment_to_movieDetailsFragment, bundle)
    }



    private fun observer() {
        homeInfoViewModel.homeFeedList.observe(viewLifecycleOwner) {
            it

            when (it) {
                is NetworkResults.Success -> binding.apply {
                    shimmerLoading.root.gone()
                    it.data?.let { homeFeedData: HomeFeedData ->
                        fragmentHomeHomeFeedLayout.visible()
                        bannerList = homeFeedData.bannerMovie
                        bannerAdapter.submitList(homeFeedData.bannerMovie)
                        homeAdapter.submitList(homeFeedData.homeFeedResponseList)
                    }
                }

                is NetworkResults.Error -> binding.apply {
                    Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                    shimmerLoading.root.gone()
                    fragmentHomeHomeFeedLayout.gone()
                }

                is NetworkResults.Loading -> binding.apply {
                    shimmerLoading.root.visible()
                    fragmentHomeHomeFeedLayout.gone()
                }
            }
        }

        // Observe pagination results
        homeInfoViewModel.loadMoreMovies.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    result.data?.let { (categoryTitle, movieList) ->
                        homeAdapter.addMoreItemsToCategory(categoryTitle, movieList.results)
                        homeAdapter.setLoadingForCategory(categoryTitle, false)
                    }
                }

                is NetworkResults.Error -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                }

                is NetworkResults.Loading -> {
                    // Loading state is handled by adapter
                }
            }
        }

        // Observe scheduled movies and update icon visibility
        scheduledViewModel.getAllScheduledMovies().observe(viewLifecycleOwner) { scheduledList ->
            val ids = scheduledList.mapNotNull { entity -> entity.id }.toSet()
            homeAdapter.updateScheduledMovies(ids)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onNetworkLost() {
        super.onNetworkLost()
        requireActivity().runOnUiThread {
            binding.fragmentHomeNetworkCheck.layoutNtwContainer.visible()
        }
    }

    override fun onNetworkAvailable() {
        super.onNetworkAvailable()
        requireActivity().runOnUiThread {
            binding.fragmentHomeNetworkCheck.layoutNtwContainer.gone()
            // This logic is still correct.
            homeInfoViewModel.getMovieInfoData()
        }
    }
}   