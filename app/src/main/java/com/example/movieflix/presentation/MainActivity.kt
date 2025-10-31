package com.example.movieflix.presentation

import android.content.res.ColorStateList
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.movieflix.R
import com.example.movieflix.core.utils.Constants
import com.example.movieflix.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val navController by lazy{
        (supportFragmentManager.findFragmentById(R.id.fragmentContainerView)
                as NavHostFragment).navController
    }
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                scrim = android.graphics.Color.BLACK
            ),
            navigationBarStyle = SystemBarStyle.dark(
                scrim = android.graphics.Color.BLACK
            )
        )
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        // Setup bottom navigation with nav controller
        binding.bottomNavigationView.setupWithNavController(navController)

        binding.bottomNavigationView.isItemActiveIndicatorEnabled = true

        val csl = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
            intArrayOf(
                ContextCompat.getColor(this, R.color.black),
                ContextCompat.getColor(this, R.color.bottom_nav_grey),
            )
        )
        binding.bottomNavigationView.itemActiveIndicatorColor = csl

        // Handle bottom navigation visibility
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment, R.id.searchFragment, R.id.libraryFragment -> {
                    binding.bottomNavigationView.visibility = android.view.View.VISIBLE
                }
                else -> {
                    binding.bottomNavigationView.visibility = android.view.View.GONE
                }
            }
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Check if the current destination is the home fragment
                if (navController.currentDestination?.id == R.id.homeFragment) {
                    // If it is, finish the activity
                    finish()
                } else {
                    // Otherwise, let the NavController handle the back press
                    navController.navigateUp()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
        
        // Handle notification intent - open movie details if launched from notification
        handleNotificationIntent()
    }
    
    private fun handleNotificationIntent() {
        if (intent?.getBooleanExtra("OPEN_MOVIE_DETAILS", false) == true) {
            val movieData = intent.getStringExtra("MOVIE_DATA")
            if (!movieData.isNullOrEmpty()) {
                // Navigate to movie details with a delay to ensure nav controller is ready
                binding.root.postDelayed({
                    try {
                        val currentDestination = navController.currentDestination?.id
                        val bundle = bundleOf(Constants.MEDIA_SEND_REQUEST_KEY to movieData)
                        
                        // Navigate from a valid destination that has an action to movieDetailsFragment
                        when (currentDestination) {
                            R.id.homeFragment, R.id.searchFragment, R.id.watchListFragment -> {
                                // We're on a valid destination, navigate directly
                                navController.navigate(R.id.movieDetailsFragment, bundle)
                            }
                            R.id.splashFragment, R.id.introFragment -> {
                                // Wait for navigation to complete to homeFragment, then navigate to details
                                navController.addOnDestinationChangedListener(object : androidx.navigation.NavController.OnDestinationChangedListener {
                                    override fun onDestinationChanged(
                                        controller: androidx.navigation.NavController,
                                        destination: androidx.navigation.NavDestination,
                                        arguments: Bundle?
                                    ) {
                                        if (destination.id == R.id.homeFragment) {
                                            // Now we can navigate to movie details
                                            controller.navigate(R.id.movieDetailsFragment, bundle)
                                            controller.removeOnDestinationChangedListener(this)
                                        }
                                    }
                                })
                            }
                            else -> {
                                // Unknown destination, navigate to home first then to details
                                navController.navigate(R.id.homeFragment)
                                binding.root.postDelayed({
                                    try {
                                        navController.navigate(R.id.movieDetailsFragment, bundle)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }, 200)
                            }
                        }
                        
                        // Clear the intent flag to prevent re-navigation on config changes
                        intent?.removeExtra("OPEN_MOVIE_DETAILS")
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, 500) // Increased delay to ensure nav graph is fully initialized
            }
        }
    }
    
    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent()
    }
}