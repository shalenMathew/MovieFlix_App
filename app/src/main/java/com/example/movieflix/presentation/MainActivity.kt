package com.example.movieflix.presentation

import android.content.res.ColorStateList
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
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
        binding.bottomNavigationView.setItemActiveIndicatorColor(csl)
        
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
        // Make status bar and navigation bar transparent
        setupTransparentSystemBars()

        // Setup bottom navigation
        setupBottomNavigation()
    }

    private fun setupTransparentSystemBars() {
        // Make system bars (status bar and navigation bar) transparent
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigationView.setupWithNavController(navController)
        navController.addOnDestinationChangedListener(this)
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        // Hide bottom navigation for splash and intro screens
        when (destination.id) {
            R.id.splashFragment, R.id.introFragment, R.id.movieDetailsFragment -> {
                binding.bottomNavigationView.visibility = View.GONE
            }
            else -> {
                binding.bottomNavigationView.visibility = View.VISIBLE
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (navController.currentDestination?.id == R.id.homeFragment) {
            finish()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        navController.removeOnDestinationChangedListener(this)
        super.onDestroy()
    }
}