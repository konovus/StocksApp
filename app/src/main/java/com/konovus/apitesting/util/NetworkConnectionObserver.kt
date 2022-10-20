package com.konovus.apitesting.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.MutableStateFlow

class NetworkConnectionObserver(
    val context: Context
) {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val networkRequest = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .build()
    private var switchBack = false
    private val _connection = MutableStateFlow(NetworkStatus.Unavailable)
    val connection: LiveData<NetworkStatus> = _connection.asLiveData()

    init {
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                if (switchBack) {
                    _connection.value = (NetworkStatus.BackOnline)
                    switchBack = false
                } else _connection.value = (NetworkStatus.Available)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                    switchBack = true
                    _connection.value = (NetworkStatus.Unavailable)
            }

            override fun onUnavailable() {
                super.onUnavailable()
                    switchBack = true
                    _connection.value = (NetworkStatus.Unavailable)
            }
        }
        connectivityManager.requestNetwork(networkRequest, networkCallback)
    }
}