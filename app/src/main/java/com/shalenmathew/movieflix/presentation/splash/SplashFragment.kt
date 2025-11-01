package com.shalenmathew.movieflix.presentation.splash

import android.animation.Animator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.shalenmathew.movieflix.R
import com.shalenmathew.movieflix.core.utils.DataStoreReference
import com.shalenmathew.movieflix.databinding.FragmentSplashBinding
import kotlinx.coroutines.launch

class SplashFragment: Fragment() {

private  var _binding:FragmentSplashBinding?=null
    private val binding get()=_binding!!
    private var hasNavigated = false

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
        initView()
    }

    private fun initView(){
        viewLifecycleOwner.lifecycleScope.launch {
            // Take only the first emission to prevent multiple navigations
            DataStoreReference.isIntroCompleted(requireContext()).collect{completed->
                if(!hasNavigated){
                    if(completed){
                        navigateTo(R.id.action_splashFragment_to_homeFragment)
                    }else{
                        navigateTo(R.id.action_splashFragment_to_introFragment)
                    }
                }
            }
        }
    }

    private fun navigateTo(id: Int) {
        // Prevent multiple navigation attempts
        if (hasNavigated) return

        binding.lottieAnimation.addAnimatorListener(object: Animator.AnimatorListener{
            override fun onAnimationStart(p0: Animator) {
            }
            override fun onAnimationEnd(p0: Animator) {
                // Double check to prevent race conditions
                if (hasNavigated) return
                
                lifecycleScope.launch {
                    // Check if fragment is still added, lifecycle is active, and we're still on splash fragment
                    if (isAdded && 
                        viewLifecycleOwner.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED)) {
                        try {
                            // Only navigate if we're still on the splash fragment
                            val currentDestination = findNavController().currentDestination?.id
                            if (currentDestination == R.id.splashFragment && !hasNavigated) {
                                hasNavigated = true
                                findNavController().navigate(id)
                            }
                        } catch (e: Exception) {
                            // Silently fail if navigation controller is not available
                            e.printStackTrace()
                        }
                    }
                }
            }

            override fun onAnimationCancel(p0: Animator) {
            }

            override fun onAnimationRepeat(p0: Animator) {
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}