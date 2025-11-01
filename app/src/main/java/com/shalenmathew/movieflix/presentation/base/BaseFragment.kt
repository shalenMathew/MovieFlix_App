package com.shalenmathew.movieflix.presentation.base

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.shalenmathew.movieflix.core.utils.NetworkConnectivityObserver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
open class BaseFragment : Fragment() {

    @Inject
    lateinit var networkConnectivityObserver: NetworkConnectivityObserver

    protected open fun onNetworkAvailable() {}
    protected open fun onNetworkLost() {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeConnectivity()
    }

    private fun observeConnectivity() {
        viewLifecycleOwner.lifecycleScope.launch {
            networkConnectivityObserver.observe().collectLatest { isConnected ->
                if (isConnected) {
                    onNetworkAvailable()
                } else {
                    onNetworkLost()
                }
            }
        }
    }
}