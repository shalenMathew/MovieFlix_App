package com.example.movieflix.presentation.home

import android.net.Network
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.SnapHelper
import com.example.movieflix.R
import com.example.movieflix.core.adapters.BannerAdapter
import com.example.movieflix.core.adapters.HomeAdapter
import com.example.movieflix.core.utils.Constants
import com.example.movieflix.core.utils.NetworkResults
import com.example.movieflix.core.utils.gone
import com.example.movieflix.core.utils.isNetworkAvailable
import com.example.movieflix.core.utils.openNetworkSettings
import com.example.movieflix.core.utils.visible
import com.example.movieflix.databinding.FragmentHomeBinding
import com.example.movieflix.domain.model.HomeFeedData
import com.example.movieflix.domain.model.MovieResult
import com.example.movieflix.presentation.base.BaseFragment
import com.example.movieflix.presentation.viewmodels.HomeInfoViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HomeFragment : BaseFragment() {

    private var _binding:FragmentHomeBinding?=null
    private val binding get() =_binding!!
    private val homeInfoViewModel: HomeInfoViewModel by viewModels() // initializing viewmodel
    private lateinit var  bannerAdapter: BannerAdapter
    private lateinit var homeAdapter: HomeAdapter
    private var bannerList:List<MovieResult> = arrayListOf()
    private var snapHelper: SnapHelper = PagerSnapHelper()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding=DataBindingUtil.inflate(inflater,R.layout.fragment_home,container,false)
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

    fragmentHomeNetworkCheck.layoutNetworkBtn.setOnClickListener(){
        openNetworkSettings(requireContext())
    }

    fragmentHomeWatchNowBtn.setOnClickListener(){
        val layoutManger=binding.fragmentHomeBannerImgRv.layoutManager as LinearLayoutManager
        val firstVisibleItem = layoutManger.findFirstVisibleItemPosition()
        openDetailFragment(bannerList[firstVisibleItem])
    }

}
    }

    private fun initView() {
        bannerAdapter= BannerAdapter()
        homeAdapter= HomeAdapter{
            openDetailFragment(it)
        }

        binding.apply {
            fragmentHomeBannerImgRv.adapter = bannerAdapter
            fragmentHomeHomeFeedRv.adapter=homeAdapter
            snapHelper.attachToRecyclerView(fragmentHomeBannerImgRv)
            fragmentHomeIndicator.attachToRecyclerView(fragmentHomeBannerImgRv)
        }
    }

    private fun openDetailFragment(it: MovieResult) {
        val bundle = Bundle()
        bundle.putString(Constants.MEDIA_SEND_REQUEST_KEY,Gson().toJson(it))

        findNavController().navigate(R.id.action_homeFragment_to_movieDetailsFragment,bundle)
    }

    private fun observer() {
        homeInfoViewModel.homeFeedList.observe(viewLifecycleOwner){ it

            when(it){
                is NetworkResults.Success-> binding.apply{
                    shimmerLoading.root.gone()
                    it.data?.let { homeFeedData: HomeFeedData ->
                       fragmentHomeHomeFeedLayout.visible()
                        bannerList = homeFeedData.bannerMovie
                        bannerAdapter.submitList(homeFeedData.bannerMovie)
                        homeAdapter.submitList(homeFeedData.homeFeedResponseList)
                    }
                }

                is NetworkResults.Error->binding.apply{
                    Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                    shimmerLoading.root.gone()
                    fragmentHomeHomeFeedLayout.gone()
                }

                is NetworkResults.Loading->binding.apply{
                    shimmerLoading.root.visible()
                    fragmentHomeHomeFeedLayout.gone()
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

    override fun onNetworkLost(network: Network?) {
        super.onNetworkLost(network)
        requireActivity().runOnUiThread {
            binding.fragmentHomeNetworkCheck.layoutNtwContainer.visible()
        }
    }

    override fun onNetworkAvailable(network: Network) {
        super.onNetworkAvailable(network)
        requireActivity().runOnUiThread {
            binding.fragmentHomeNetworkCheck.layoutNtwContainer.gone()
           homeInfoViewModel.getMovieInfoData()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding=null
    }
}