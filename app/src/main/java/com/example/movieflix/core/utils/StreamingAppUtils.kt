package com.example.movieflix.core.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.example.movieflix.domain.model.Provider

/**
 * Utility object for handling streaming provider deep links
 * Maps provider IDs to their Android package names for app launching
 */
object StreamingAppUtils {
    
    // Map of TMDB provider IDs to Android package names
    private val providerPackages = mapOf(
        8 to "com.netflix.mediaclient",              // Netflix
        9 to "com.amazon.avod.thirdpartyclient",     // Amazon Prime Video
        10 to "com.google.android.videos",           // Google Play Movies
        337 to "com.disney.disneyplus",              // Disney Plus
        350 to "tv.apple.atve.androidtv.appletv",    // Apple TV
        384 to "com.hbo.hbonow",                     // HBO Max
        15 to "tv.hulu.plus",                        // Hulu
        531 to "com.Paramount.ParamountPlus",        // Paramount Plus
        207 to "com.crunchyroll.crunchyroid",        // Crunchyroll
        283 to "com.cbsinteractive.app",             // Paramount+
        386 to "com.nbc.android.peacock",            // Peacock
        1899 to "com.max.hbo",                       // Max (formerly HBO Max)
        175 to "com.nbc.live.nowapp",                // NBC
        309 to "com.apple.android.music",            // Apple Music
        2 to "com.google.android.youtube.tv",        // YouTube
        3 to "tv.twitch.android.app",                // Twitch
        175 to "com.amcn.amcfullepisodes",           // AMC
        1796 to "com.tidal.android",                 // Tidal
    )
    
    /**
     * Attempts to open a streaming app or fallback to web browser
     * @param context Application context
     * @param provider The streaming provider to open
     * @param fallbackUrl The URL to open if app is not installed
     */
    fun openStreamingApp(context: Context, provider: Provider, fallbackUrl: String?) {
        val packageName = providerPackages[provider.providerId]
        
        if (packageName != null && isAppInstalled(context, packageName)) {
            // Try to open the app
            try {
                val intent = context.packageManager.getLaunchIntentForPackage(packageName)
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    return
                }
            } catch (e: ActivityNotFoundException) {
                // App not found, fall through to web browser
            }
        }
        
        // Fallback to opening in browser
        openInBrowser(context, fallbackUrl ?: getProviderWebUrl(provider))
    }
    
    /**
     * Check if an app is installed
     */
    private fun isAppInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
    
    /**
     * Open URL in browser using Custom Tabs
     */
    private fun openInBrowser(context: Context, url: String) {
        try {
            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()
            customTabsIntent.launchUrl(context, Uri.parse(url))
        } catch (e: Exception) {
            // Fallback to regular browser intent
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }
    }
    
    /**
     * Get web URL for a provider (generic fallback)
     */
    private fun getProviderWebUrl(provider: Provider): String {
        // Return a generic search URL based on provider name
        val providerName = provider.providerName ?: "streaming"
        return when (provider.providerId) {
            8 -> "https://www.netflix.com"
            9 -> "https://www.primevideo.com"
            337 -> "https://www.disneyplus.com"
            384, 1899 -> "https://www.max.com"
            15 -> "https://www.hulu.com"
            else -> "https://www.google.com/search?q=$providerName"
        }
    }
    
    /**
     * Get a user-friendly message about app availability
     */
    fun getAppAvailabilityMessage(context: Context, provider: Provider): String {
        val packageName = providerPackages[provider.providerId]
        return if (packageName != null && isAppInstalled(context, packageName)) {
            "Opening ${provider.providerName} app"
        } else {
            "Opening ${provider.providerName} in browser"
        }
    }
}
