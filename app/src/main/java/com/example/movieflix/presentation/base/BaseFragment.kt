package com.example.movieflix.presentation.base

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.movieflix.R
import com.example.movieflix.core.utils.isNetworkAvailable

open class BaseFragment : Fragment() {


    protected open fun onNetworkLost(network: Network?) {}
    protected open fun onNetworkUnavailable() {}
    protected open fun onNetworkLosing(network: Network, maxMsToLive: Int) {}
    protected open fun onNetworkAvailable(network: Network) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_base, container, false)
    }

    private fun observeNetwork() {

        isNetworkLost(null)

        // Check internet connection
        val connectivityManager =
            requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }


    private fun isNetworkLost(network: Network?) {
        if (!isNetworkAvailable(requireActivity())) {
            onNetworkLost(network)
        }
    }

    private var networkCallback = object : ConnectivityManager.NetworkCallback() {

        override fun onLost(network: Network) {
            isNetworkLost(network)
        }

        override fun onUnavailable() {
            onNetworkUnavailable()
        }

        override fun onLosing(network: Network, maxMsToLive: Int) {
            onNetworkLosing(network, maxMsToLive)
        }

        override fun onAvailable(network: Network) {
            onNetworkAvailable(network)
        }

    }

    override fun onPause() {
        super.onPause()
        val connectivityManager =
            requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    override fun onResume() {
        super.onResume()
        observeNetwork()
    }

}