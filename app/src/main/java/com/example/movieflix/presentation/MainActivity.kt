package com.example.movieflix.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup bottom navigation with nav controller
        binding.bottomNavigationView.setupWithNavController(navController)
        
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
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
if (navController.currentDestination?.id == R.id.homeFragment){
    finish()
}
        super.onBackPressed()
    }

}