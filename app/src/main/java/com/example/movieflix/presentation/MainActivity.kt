package com.example.movieflix.presentation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.navigation.fragment.NavHostFragment
import com.example.movieflix.BuildConfig
import com.example.movieflix.R
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val navController by lazy{
        (supportFragmentManager.findFragmentById(R.id.fragmentContainerView)
                as NavHostFragment).navController
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var firebaseAnalytics:FirebaseAnalytics = Firebase.analytics

        if (BuildConfig.DEBUG) {
            Log.d("TAG","DEBUGGING MODE")
            firebaseAnalytics.setAnalyticsCollectionEnabled(false)
        } else {
            Log.d("TAG","RELEASE MODE")
            firebaseAnalytics.setAnalyticsCollectionEnabled(true)
        }

    }

    override fun onBackPressed() {
if (navController.currentDestination?.id == R.id.homeFragment){
    finish()
}
        super.onBackPressed()
    }

}