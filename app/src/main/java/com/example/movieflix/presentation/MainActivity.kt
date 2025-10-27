package com.example.movieflix.presentation

import android.content.res.ColorStateList
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.movieflix.R
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
                R.id.homeFragment, R.id.searchFragment, R.id.watchListFragment, R.id.favFragment -> {
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
    }
}