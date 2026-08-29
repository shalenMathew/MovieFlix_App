package com.shalenmathew.movieflix.core.utils

/**
 * Global utility to prevent multiple rapid clicks on UI elements that trigger
 * actions like navigation or sheet opening.
 */
object ClickHandler {
    private var lastClick = 0L

    /**
     * Checks if a click is allowed based on a debounce time.
     * Default is 1000ms as per existing implementation in HomeFragment.
     */
    fun isClickAllowed(debounceTime: Long = 1000L): Boolean {
        val currentTime = System.currentTimeMillis()
        val allowUser = currentTime - lastClick > debounceTime
        if (allowUser) {
            lastClick = currentTime
        }
        return allowUser
    }
}
