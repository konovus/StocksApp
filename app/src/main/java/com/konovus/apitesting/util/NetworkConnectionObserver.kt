package com.konovus.apitesting.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.util.Log
import com.konovus.apitesting.util.Constants.TAG
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class NetworkConnectionObserver(
    val context: Context
) {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private var switchBack = true

    fun observeConnection(): Flow<NetworkStatus> {
        return callbackFlow {
            val networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    launch {
                        if (switchBack) {
                            send(NetworkStatus.BackOnline)
                            switchBack = false
                        } else send(NetworkStatus.Available)
                    }
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    launch {
                        switchBack = true
                        send(NetworkStatus.Unavailable)
                    }
                }

                override fun onUnavailable() {
                    super.onUnavailable()
                    launch {
                        switchBack = true
                        send(NetworkStatus.Unavailable)
                    }
                }
            }
            send(NetworkStatus.Unavailable)
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
            awaitClose {
                connectivityManager.unregisterNetworkCallback(networkCallback)
            }
        }.distinctUntilChanged()
    }
}