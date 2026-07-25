package com.shalenmathew.movieflix.presentation.splash

import android.animation.Animator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.shalenmathew.movieflix.R
import com.shalenmathew.movieflix.core.utils.DataStoreReference
import com.shalenmathew.movieflix.databinding.FragmentSplashBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment: Fragment() {

private  var _binding:FragmentSplashBinding?=null
    private val binding get()=_binding!!
    private var hasNavigated = false
    private var isAnimationFinished = false
    private var pendingNavigationId: Int? = null
    private var isFirstLaunch = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding=FragmentSplashBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Start animation only when Lottie is ready to prevent the "pop" glitch
        binding.lottieAnimation.addLottieOnCompositionLoadedListener {
            startFadeInAnimation()
        }
        
        checkDataStoreStatus()
    }

    private fun startFadeInAnimation() {
        binding.lottieAnimation.playAnimation()
        
        // Smooth fade-in for both elements
        val duration = 800L
        val interpolator = AccelerateDecelerateInterpolator()

        binding.lottieAnimation.animate()
            .alpha(1f)
            .setDuration(duration)
            .setInterpolator(interpolator)
            .start()

        binding.tvAppName.animate()
            .alpha(1f)
            .setDuration(duration)
            .setInterpolator(interpolator)
            .start()
    }

    private fun checkDataStoreStatus() {
        viewLifecycleOwner.lifecycleScope.launch {
            DataStoreReference.isIntroCompleted(requireContext()).collect { completed ->
                isFirstLaunch = !completed
                pendingNavigationId = if (completed) {
                    R.id.action_splashFragment_to_homeFragment
                } else {
                    R.id.action_splashFragment_to_introFragment
                }
                
                // Since Lottie is looping, we rely on time-based delays for navigation
                launch {
                    val splashTime = if (completed) 2500L else 4000L
                    delay(splashTime)
                    isAnimationFinished = true
                    tryNavigate()
                }
                
                tryNavigate()
            }
        }
    }

    private fun tryNavigate() {
        val navId = pendingNavigationId
        if (isAnimationFinished && navId != null && !hasNavigated) {
            hasNavigated = true
            lifecycleScope.launch {
                if (isAdded && viewLifecycleOwner.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED)) {
                    try {
                        if (findNavController().currentDestination?.id == R.id.splashFragment) {
                            findNavController().navigate(navId)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        hasNavigated = false // Reset on failure
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}