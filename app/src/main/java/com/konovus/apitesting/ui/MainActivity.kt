package com.konovus.apitesting.ui

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.konovus.apitesting.R
import com.konovus.apitesting.data.redux.AppState
import com.konovus.apitesting.data.redux.Store
import com.konovus.apitesting.util.Constants.TAG
import com.konovus.apitesting.util.NetworkConnectionObserver
import com.konovus.apitesting.util.NetworkStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    @Inject
    lateinit var store: Store<AppState>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupNavigation()
        setupNetworkObserver()
    }

    private fun setupNavigation() {
        val navHost = supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        navController = navHost.navController

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        NavigationUI.setupWithNavController(bottomNav, navController)

//        store.stateFlow.map { it.bottomNavSelectedId }.distinctUntilChanged().asLiveData().observe(this){
//            bottomNav.selectedItemId = it
//        }
    }

    fun navigateToTab(id: Int) {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = id
    }

    private fun setupNetworkObserver() {
        val networkObserver =  NetworkConnectionObserver(this)
        networkObserver.observeConnection().onEach { networkStatus ->
            Log.i(TAG, "setupNetworkObserver: $networkStatus")
            store.update { it.copy(networkStatus = networkStatus) }
        }.launchIn(lifecycleScope)

        val networkStatusTV = findViewById<TextView>(R.id.no_internet_tv)
        store.stateFlow.map { it.networkStatus }.asLiveData().observe(this) {
            when(it) {
                NetworkStatus.BackOnline -> {
                    lifecycleScope.launch{
                        networkStatusTV.text = "Back Online"
                        delay(100)
                        networkStatusTV.isVisible = false
                    }
                }
                NetworkStatus.Unavailable -> {
                    networkStatusTV.text = "No internet connection"
                    networkStatusTV.isVisible = true
                }
                NetworkStatus.Available -> {}
                else -> { throw NoSuchFieldException()}
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}