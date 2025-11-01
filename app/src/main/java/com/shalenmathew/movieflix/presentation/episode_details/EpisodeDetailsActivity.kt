package com.shalenmathew.movieflix.presentation.episode_details

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.shalenmathew.movieflix.R
import com.shalenmathew.movieflix.core.utils.formatDate
import com.shalenmathew.movieflix.core.utils.loadImage
import com.shalenmathew.movieflix.databinding.FragmentEpisodeDetailsBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class EpisodeDetailsActivity : AppCompatActivity() {

    private lateinit var binding: FragmentEpisodeDetailsBinding

    private var currentIndex: Int = 0

    companion object {
        private const val EXTRA_EPISODE_INDEX = "episode_index"

        fun newIntent(
            context: Context,
            episodeIndex: Int
        ): Intent {
            return Intent(context, EpisodeDetailsActivity::class.java).apply {
                putExtra(EXTRA_EPISODE_INDEX, episodeIndex)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentEpisodeDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getIntentData()
        setupUI()
        setupClickListeners()
    }

    private fun getIntentData() {
        currentIndex = intent?.getIntExtra(EXTRA_EPISODE_INDEX, 0) ?: 0
    }

    private fun setupUI() {
        val episodes = EpisodeDataHolder.episodes
        val seasonNumber = EpisodeDataHolder.seasonNumber
        
        if (currentIndex !in episodes.indices) {
            finish()
            return
        }
        
        val episode = episodes[currentIndex]
        
        episode.let {
            binding.apply {
                // Episode title
                episodeTitle.text = episode.name ?: "Episode ${episode.episodeNumber}"

                // Episode number and season
                val episodeText = "Episode ${episode.episodeNumber} • Season $seasonNumber"
                episodeNumber.text = episodeText

                // Duration
                val runtime = episode.runtime
                episodeDuration.text = if (runtime != null && runtime > 0) {
                    "${runtime}m"
                } else {
                    ""
                }
                episodeDuration.visibility = if (runtime != null && runtime > 0) View.VISIBLE else View.GONE

                // Air date
                episodeAirDate.text = formatDate(episode.airDate)

                // Rating
                val rating = episode.voteAverage
                if (rating != null && rating > 0) {
                    ratingContainer.visibility = View.VISIBLE
                    String.format(Locale.getDefault(), "%.1f", rating).also { episodeRating.text = it }
                } else {
                    ratingContainer.visibility = View.GONE
                }

                // Overview
                episodeOverview.text = episode.overview?.takeIf { it.isNotBlank() }
                    ?: "No overview available for this episode."

                // Episode still/thumbnail - use W500 for detail view
                if (episode.stillPath != null) {
                    episodeStill.loadImage(
                        "https://image.tmdb.org/t/p/w500${episode.stillPath}",
                        placeholder = ContextCompat.getDrawable(this@EpisodeDetailsActivity, R.drawable.poster_bg)
                    )
                } else {
                    episodeStill.setImageDrawable(
                        ContextCompat.getDrawable(this@EpisodeDetailsActivity, R.drawable.poster_bg)
                    )
                }

                // Update navigation buttons state
                updateNavigationButtons()
            }
        }
    }

    private fun updateNavigationButtons() {
        val episodes = EpisodeDataHolder.episodes
        val seasonNumber = EpisodeDataHolder.seasonNumber
        val totalSeasons = EpisodeDataHolder.totalSeasons
        
        binding.apply {
            // Handle previous button
            when {
                currentIndex > 0 -> {
                    // Not at first episode - show normal Previous
                    "Previous".also { previousButton.text = it }
                    previousButton.isEnabled = true
                    previousButton.alpha = 1f
                }
                seasonNumber > 1 -> {
                    // First episode but not first season - show Previous Season
                    "Previous Season".also { previousButton.text = it }
                    previousButton.isEnabled = true
                    previousButton.alpha = 1f
                }
                else -> {
                    // First episode of first season - disable
                    "Previous".also { previousButton.text = it }
                    previousButton.isEnabled = false
                    previousButton.alpha = 0.5f
                }
            }
            
            // Handle next button
            when {
                currentIndex < episodes.size - 1 -> {
                    // Not at last episode - show normal Next
                    "Next".also { nextButton.text = it }
                    nextButton.isEnabled = true
                    nextButton.alpha = 1f
                }
                seasonNumber < totalSeasons -> {
                    // Last episode but not last season - show Next Season
                    "Next Season".also { nextButton.text = it }
                    nextButton.isEnabled = true
                    nextButton.alpha = 1f
                }
                else -> {
                    // Last episode of last season - disable
                    "Next".also { nextButton.text = it }
                    nextButton.isEnabled = false
                    nextButton.alpha = 0.5f
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            backButton.setOnClickListener {
                finish()
            }

            previousButton.setOnClickListener {
                when {
                    currentIndex > 0 -> {
                        // Navigate to previous episode
                        navigateToEpisode(currentIndex - 1)
                    }
                    EpisodeDataHolder.seasonNumber > 1 -> {
                        // Navigate to previous season
                        navigateToPreviousSeason()
                    }
                }
            }

            nextButton.setOnClickListener {
                val episodes = EpisodeDataHolder.episodes
                when {
                    currentIndex < episodes.size - 1 -> {
                        // Navigate to next episode
                        navigateToEpisode(currentIndex + 1)
                    }
                    EpisodeDataHolder.seasonNumber < EpisodeDataHolder.totalSeasons -> {
                        // Navigate to next season
                        navigateToNextSeason()
                    }
                }
            }
        }
    }

    private fun navigateToEpisode(index: Int) {
        val episodes = EpisodeDataHolder.episodes
        if (index in episodes.indices) {
            currentIndex = index
            setupUI()
        }
    }
    
    private fun navigateToPreviousSeason() {
        val currentSeason = EpisodeDataHolder.seasonNumber
        if (currentSeason > 1) {
            val targetSeason = currentSeason - 1
            EpisodeDataHolder.onSeasonChangeCallback?.invoke(targetSeason)
            finish()
        }
    }
    
    private fun navigateToNextSeason() {
        val currentSeason = EpisodeDataHolder.seasonNumber
        val totalSeasons = EpisodeDataHolder.totalSeasons
        if (currentSeason < totalSeasons) {
            val targetSeason = currentSeason + 1
            EpisodeDataHolder.onSeasonChangeCallback?.invoke(targetSeason)
            finish()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Don't clear data here as user might navigate back
    }
}
