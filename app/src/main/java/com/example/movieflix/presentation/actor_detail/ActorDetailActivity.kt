package com.example.movieflix.presentation.actor_detail

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movieflix.R
import com.example.movieflix.core.adapters.ActorMoviesAdapter
import com.example.movieflix.core.utils.Constants
import com.example.movieflix.core.utils.NetworkResults
import com.example.movieflix.core.utils.loadImage
import com.example.movieflix.databinding.ActivityActorDetailBinding
import com.example.movieflix.domain.model.ActorDetail
import com.example.movieflix.domain.model.MovieResult
import com.example.movieflix.presentation.viewmodels.ActorDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ActorDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityActorDetailBinding
    private val viewModel: ActorDetailViewModel by viewModels()
    private lateinit var moviesAdapter: ActorMoviesAdapter
    private var personId: Int = 0

    companion object {
        const val EXTRA_PERSON_ID = "person_id"
        const val EXTRA_PERSON_NAME = "person_name"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Make status bar transparent
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        binding = ActivityActorDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        personId = intent.getIntExtra(EXTRA_PERSON_ID, 0)
        val personName = intent.getStringExtra(EXTRA_PERSON_NAME)

        setupToolbar()
        setupRecyclerView()
        observeViewModel()

        if (personId != 0) {
            viewModel.loadActorDetail(personId)
            viewModel.loadActorMoviesAndShows(personId)
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        moviesAdapter = ActorMoviesAdapter { movie ->
            // Handle movie click - can navigate to movie details
            // For now, just a placeholder
        }

        binding.actorMoviesRecycler.apply {
            adapter = moviesAdapter
            layoutManager = LinearLayoutManager(
                this@ActorDetailActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
        }
    }

    private fun observeViewModel() {
        viewModel.actorDetail.observe(this) { result ->
            when (result) {
                is NetworkResults.Loading -> {
                    showLoading(true)
                }
                is NetworkResults.Success -> {
                    showLoading(false)
                    result.data?.let { actorDetail ->
                        bindActorDetail(actorDetail)
                    }
                }
                is NetworkResults.Error -> {
                    showLoading(false)
                    showError(result.message ?: "Failed to load actor details")
                }
            }
        }

        viewModel.actorMoviesAndShows.observe(this) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    result.data?.let { content ->
                        moviesAdapter.submitList(content.take(20)) // Show top 20 items
                    }
                }
                is NetworkResults.Error -> {
                    // Handle error if needed
                }
                is NetworkResults.Loading -> {
                    // Loading handled by actor detail
                }
            }
        }
    }

    private fun bindActorDetail(actor: ActorDetail) {
        binding.apply {
            // Set actor name
            actorName.text = actor.name

            // Set profile image
            if (actor.profilePath != null) {
                actorProfileImage.loadImage(
                    Constants.TMDB_IMAGE_BASE_URL_W500.plus(actor.profilePath),
                    placeholder = ContextCompat.getDrawable(this@ActorDetailActivity, R.drawable.poster_bg)
                )
            }

            // Set backdrop image (use different image if available)
            val backdropPath = actor.backdropImagePath ?: actor.profilePath
            if (backdropPath != null) {
                actorBackdrop.loadImage(
                    Constants.TMDB_IMAGE_BASE_URL_W500.plus(backdropPath),
                    placeholder = ContextCompat.getDrawable(this@ActorDetailActivity, R.drawable.poster_bg)
                )
            }

            // Set known for department
            actorKnownFor.text = actor.knownForDepartment?.let { "Known for $it" } ?: ""

            // Set biography
            if (!actor.biography.isNullOrEmpty()) {
                actorBiography.text = actor.biography
            } else {
                actorBiography.text = "No biography available."
            }

            // Set birthday
            if (!actor.birthday.isNullOrEmpty()) {
                birthdayContainer.visibility = View.VISIBLE
                actorBirthday.text = actor.birthday
            } else {
                birthdayContainer.visibility = View.GONE
            }

            // Set birthplace
            if (!actor.placeOfBirth.isNullOrEmpty()) {
                birthplaceContainer.visibility = View.VISIBLE
                actorBirthplace.text = actor.placeOfBirth
            } else {
                birthplaceContainer.visibility = View.GONE
            }

            // Setup social media
            setupSocialMedia(actor)
        }
    }

    private fun setupSocialMedia(actor: ActorDetail) {
        binding.apply {
            var hasSocials = false

            // Instagram
            if (!actor.instagramId.isNullOrEmpty()) {
                actorInstagram.visibility = View.VISIBLE
                actorInstagram.setOnClickListener {
                    openUrl("https://www.instagram.com/${actor.instagramId}")
                }
                hasSocials = true
            } else {
                actorInstagram.visibility = View.GONE
            }

            // Twitter
            if (!actor.twitterId.isNullOrEmpty()) {
                actorTwitter.visibility = View.VISIBLE
                actorTwitter.setOnClickListener {
                    openUrl("https://twitter.com/${actor.twitterId}")
                }
                hasSocials = true
            } else {
                actorTwitter.visibility = View.GONE
            }

            // Facebook
            if (!actor.facebookId.isNullOrEmpty()) {
                actorFacebook.visibility = View.VISIBLE
                actorFacebook.setOnClickListener {
                    openUrl("https://www.facebook.com/${actor.facebookId}")
                }
                hasSocials = true
            } else {
                actorFacebook.visibility = View.GONE
            }

            actorSocialsContainer.visibility = if (hasSocials) View.VISIBLE else View.GONE
        }
    }

    private fun openUrl(url: String) {
        try {
            val customTabsIntent = androidx.browser.customtabs.CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()
            customTabsIntent.launchUrl(this, android.net.Uri.parse(url))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.loadingProgress.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        binding.errorMessage.visibility = View.VISIBLE
        binding.errorMessage.text = message
    }
}
