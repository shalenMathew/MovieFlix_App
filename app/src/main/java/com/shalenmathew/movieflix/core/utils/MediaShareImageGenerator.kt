package com.shalenmathew.movieflix.core.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.ChipGroup
import com.shalenmathew.movieflix.R
import com.shalenmathew.movieflix.data.network.ApiClient
import com.shalenmathew.movieflix.domain.model.MovieResult
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

@EntryPoint
@InstallIn(SingletonComponent::class)
interface MediaShareEntryPoint {
    fun getApiClient(): ApiClient
}

class MediaSharePosterPicker(fragment: Fragment) {
    private var pendingResult: ((Uri?) -> Unit)? = null
    private val launcher = fragment.registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val result = pendingResult
        pendingResult = null
        result?.invoke(uri)
    }

    fun launch(onResult: (Uri?) -> Unit) {
        pendingResult = onResult
        launcher.launch("image/*")
    }
}

enum class MediaShareTemplate(
    val id: String,
    val title: String,
    @param:LayoutRes val layoutResId: Int
) {
    EDITORIAL_POSTER("editorial_poster", "Editorial Poster", R.layout.layout_sharable_media_editorial),
    CHARACTER_POSTER("editorial_poster_2", "Editorial Poster 2", R.layout.layout_sharable_media_character),
    SERIES_HEATMAP_POSTER("series_heatmap", "Series Heatmap", R.layout.layout_sharable_media_series_heatmap)
}

object MediaShareImageGenerator {

    fun generateImage(
        context: Context,
        movie: MovieResult,
        heroBitmap: Bitmap?,
        template: MediaShareTemplate,
        clientProvider: String? = null,
        directorName: String? = null,
        castName: String? = null,
        characterName: String? = null,
        seasonRatingsMap: Map<Int, List<Double?>>? = null
    ): Bitmap {
        val shareView = LayoutInflater.from(context).inflate(template.layoutResId, null)

        val title = movie.title ?: movie.name ?: ""
        val isTv = movie.mediaType.equals("tv", ignoreCase = true)

        if (template == MediaShareTemplate.SERIES_HEATMAP_POSTER) {
            shareView.findViewById<TextView>(R.id.heatmap_show_title)?.text = title
            shareView.findViewById<TextView>(R.id.heatmap_brand_title)?.text =
                "| " + context.getString(R.string.app_name).uppercase(Locale.ENGLISH)

            val imageView = shareView.findViewById<ImageView>(R.id.heatmap_hero_image)
            if (imageView != null && heroBitmap != null) {
                imageView.setImageBitmap(heroBitmap)
            }

            val rowsContainer = shareView.findViewById<LinearLayout>(R.id.heatmap_episodes_rows_container)
            val headersRow = shareView.findViewById<LinearLayout>(R.id.heatmap_season_headers_row)
            val avgRowContainer = shareView.findViewById<LinearLayout>(R.id.heatmap_avg_row_container)

            if (rowsContainer != null) {
                rowsContainer.removeAllViews()

                // Only show the actual seasons the show has (no fake dash columns for non-existent seasons)
                val availableSeasons = seasonRatingsMap?.keys?.sorted()?.take(10)?.takeIf { it.isNotEmpty() } ?: listOf(1)

                if (headersRow != null) {
                    headersRow.removeAllViews()
                    headersRow.gravity = Gravity.CENTER_HORIZONTAL
                    headersRow.addView(TextView(context).apply {
                        layoutParams = LinearLayout.LayoutParams(60, LinearLayout.LayoutParams.WRAP_CONTENT)
                    })
                    for (sNum in availableSeasons) {
                        headersRow.addView(TextView(context).apply {
                            layoutParams = LinearLayout.LayoutParams(100, LinearLayout.LayoutParams.WRAP_CONTENT)
                            text = "S$sNum"
                            setTextColor(Color.parseColor("#AAAAAA"))
                            textSize = 20f
                            gravity = Gravity.CENTER
                            typeface = ResourcesCompat.getFont(context, R.font.montserrat_bold)
                        })
                    }
                }

                val maxEpCount = availableSeasons.maxOfOrNull { sNum ->
                    seasonRatingsMap?.get(sNum)?.size ?: 30
                }?.coerceAtMost(30) ?: 30

                for (epNum in 1..maxEpCount) {
                    val rowLayout = LinearLayout(context).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            bottomMargin = 8
                        }
                        orientation = LinearLayout.HORIZONTAL
                        gravity = Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL
                    }

                    val labelTv = TextView(context).apply {
                        layoutParams = LinearLayout.LayoutParams(60, LinearLayout.LayoutParams.WRAP_CONTENT)
                        text = "E$epNum"
                        setTextColor(Color.parseColor("#AAAAAA"))
                        textSize = 14f
                        typeface = ResourcesCompat.getFont(context, R.font.montserrat_bold)
                    }
                    rowLayout.addView(labelTv)

                    for (sNum in availableSeasons) {
                        val ratingsList = seasonRatingsMap?.get(sNum)
                        val rating = if (ratingsList != null && epNum <= ratingsList.size) ratingsList[epNum - 1] else null

                        val scoreText = if (rating != null && rating > 0.0) {
                            String.format(Locale.ENGLISH, "%.1f", rating)
                        } else {
                            "-"
                        }

                        val scoreVal = scoreText.toDoubleOrNull()
                        val (textColor, bgDrawable) = when {
                            scoreText == "-" || scoreVal == null || scoreVal <= 0.0 ->
                                Color.WHITE to ContextCompat.getDrawable(context, R.drawable.bg_heatmap_gray)
                            scoreVal >= 9.0 ->
                                Color.WHITE to ContextCompat.getDrawable(context, R.drawable.bg_heatmap_dark_green)
                            scoreVal >= 8.0 ->
                                Color.BLACK to ContextCompat.getDrawable(context, R.drawable.bg_heatmap_green)
                            scoreVal >= 7.6 ->
                                Color.BLACK to ContextCompat.getDrawable(context, R.drawable.bg_heatmap_mint)
                            scoreVal >= 5.0 ->
                                Color.BLACK to ContextCompat.getDrawable(context, R.drawable.bg_heatmap_yellow)
                            else ->
                                Color.WHITE to ContextCompat.getDrawable(context, R.drawable.bg_heatmap_red)
                        }

                        val cellTv = TextView(context).apply {
                            layoutParams = LinearLayout.LayoutParams(100, 40).apply {
                                setMargins(3, 0, 3, 0)
                            }
                            gravity = Gravity.CENTER
                            text = scoreText
                            setTextColor(textColor)
                            textSize = 14f
                            typeface = ResourcesCompat.getFont(context, R.font.montserrat_bold)
                            background = bgDrawable
                        }
                        rowLayout.addView(cellTv)
                    }
                    rowsContainer.addView(rowLayout)
                }

                if (avgRowContainer != null) {
                    val labelView = avgRowContainer.getChildAt(0)
                    avgRowContainer.removeAllViews()
                    avgRowContainer.gravity = Gravity.CENTER_HORIZONTAL
                    if (labelView != null) {
                        labelView.layoutParams = LinearLayout.LayoutParams(60, LinearLayout.LayoutParams.WRAP_CONTENT)
                        avgRowContainer.addView(labelView)
                    }

                    for (sNum in availableSeasons) {
                        val ratingsList = seasonRatingsMap?.get(sNum) ?: emptyList()
                        val validRatings = ratingsList.filter { it != null && it > 0.0 }.map { it!! }
                        val rawAvg = if (validRatings.isNotEmpty()) validRatings.average() else 0.0
                        val avgScore = if (rawAvg > 0.0) String.format(Locale.ENGLISH, "%.1f", rawAvg).toDouble() else 0.0
                        val avgText = if (avgScore > 0.0) String.format(Locale.ENGLISH, "%.1f", avgScore) else "-"

                        val scoreVal = avgScore.takeIf { it > 0.0 }
                        val (textColor, bgDrawable) = when {
                            scoreVal == null ->
                                Color.WHITE to ContextCompat.getDrawable(context, R.drawable.bg_heatmap_gray)
                            scoreVal >= 9.0 ->
                                Color.WHITE to ContextCompat.getDrawable(context, R.drawable.bg_heatmap_dark_green)
                            scoreVal >= 8.0 ->
                                Color.BLACK to ContextCompat.getDrawable(context, R.drawable.bg_heatmap_green)
                            scoreVal >= 7.6 ->
                                Color.BLACK to ContextCompat.getDrawable(context, R.drawable.bg_heatmap_mint)
                            scoreVal >= 5.0 ->
                                Color.BLACK to ContextCompat.getDrawable(context, R.drawable.bg_heatmap_yellow)
                            else ->
                                Color.WHITE to ContextCompat.getDrawable(context, R.drawable.bg_heatmap_red)
                        }

                        val avgCellTv = TextView(context).apply {
                            layoutParams = LinearLayout.LayoutParams(100, 50).apply {
                                setMargins(3, 0, 3, 0)
                            }
                            gravity = Gravity.CENTER
                            text = avgText
                            setTextColor(textColor)
                            textSize = 16f
                            typeface = ResourcesCompat.getFont(context, R.font.montserrat_bold)
                            includeFontPadding = false
                            setPadding(0, 4, 0, 4)
                            background = bgDrawable
                        }
                        avgRowContainer.addView(avgCellTv)
                    }
                }
            }
        } else if (template == MediaShareTemplate.CHARACTER_POSTER) {
            shareView.findViewById<TextView>(R.id.character_client)?.text =
                clientProvider?.takeIf(String::isNotBlank)?.uppercase(Locale.ENGLISH) ?: "PARAMOUNT"
            shareView.findViewById<TextView>(R.id.character_category)?.text =
                if (isTv) "TV SERIES" else "FEATURE FILM"
            shareView.findViewById<TextView>(R.id.character_name)?.text =
                characterName?.takeIf(String::isNotBlank) ?: "LEAD CHARACTER"
            shareView.findViewById<TextView>(R.id.character_artist)?.text =
                castName?.takeIf(String::isNotBlank) ?: "FEATURED ARTIST"
            shareView.findViewById<TextView>(R.id.character_release_date)?.text =
                formatCompactDate(movie.releaseDate)
            shareView.findViewById<TextView>(R.id.character_title)?.text = title
            shareView.findViewById<TextView>(R.id.character_service_credit)?.text =
                directorName?.takeIf(String::isNotBlank) ?: "DIRECTOR NAME"
            shareView.findViewById<TextView>(R.id.character_overview)?.text =
                movie.overview?.takeIf(String::isNotBlank)
                    ?: "A story worth discovering. Add a description to learn more about this title."
            val imageView = shareView.findViewById<ImageView>(R.id.character_hero_image)
            if (imageView != null && heroBitmap != null) {
                imageView.setImageBitmap(heroBitmap)
            }
        } else {
            shareView.findViewById<TextView>(R.id.media_title)?.text = title

            val categoryText = if (isTv) "TV-SERIES" else "FEATURE FILM"
            shareView.findViewById<TextView>(R.id.media_category)?.text = categoryText
            shareView.findViewById<TextView>(R.id.media_subtitle)?.text =
                if (isTv) "TV SERIES" else "FEATURE FILM"

            val rawDate = movie.releaseDate ?: ""
            val formattedDate = formatDate(rawDate)
            shareView.findViewById<TextView>(R.id.media_release_date)?.text = formattedDate

            val clientText = clientProvider?.uppercase(Locale.ENGLISH) ?: "HBO"
            shareView.findViewById<TextView>(R.id.media_client)?.text = clientText
            shareView.findViewById<TextView>(R.id.media_app_branding)?.text = "MOVIEFLIX"

            val directorText = directorName ?: "Director"
            shareView.findViewById<TextView>(R.id.media_director_label)?.text = directorText

            val heroImageView = shareView.findViewById<ImageView>(R.id.media_hero_image)
            if (heroImageView != null && heroBitmap != null) {
                heroImageView.setImageBitmap(heroBitmap)
            }
        }

        val seasonCount = (seasonRatingsMap?.keys?.size ?: 4).coerceAtMost(10)
        val width = if (template == MediaShareTemplate.SERIES_HEATMAP_POSTER) {
            val gridWidth = 60 + (seasonCount * 106)
            val widthNeededForGrid = gridWidth * 3 / 2 + 140
            val existingWidth = 1080 + ((seasonCount - 5).coerceAtLeast(0) * 110)
            maxOf(existingWidth, widthNeededForGrid)
        } else {
            1080
        }
        shareView.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        if (template == MediaShareTemplate.SERIES_HEATMAP_POSTER) {
            val gridHeight = shareView.findViewById<View>(R.id.heatmap_grid_container)?.measuredHeight
            val posterCard = shareView.findViewById<View>(R.id.heatmap_poster_card)
            val showTitle = shareView.findViewById<TextView>(R.id.heatmap_show_title)
            if (gridHeight != null && posterCard != null && showTitle != null) {
                val titleLayoutParams = showTitle.layoutParams as LinearLayout.LayoutParams
                val titleHeight = showTitle.measuredHeight + titleLayoutParams.topMargin
                val episodeCount = shareView.findViewById<LinearLayout>(
                    R.id.heatmap_episodes_rows_container
                )?.childCount ?: 0
                val maxCardHeight = if (episodeCount >= 10) 560 else 460
                val cardHeight = (gridHeight - titleHeight).coerceAtLeast(0).coerceAtMost(maxCardHeight)
                posterCard.layoutParams = posterCard.layoutParams.apply {
                    height = cardHeight
                }
                shareView.measure(
                    View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                )
            }
        }
        val measuredHeight = shareView.measuredHeight.coerceAtLeast(100)
        shareView.layout(0, 0, width, measuredHeight)

        val bitmap = Bitmap.createBitmap(width, measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        shareView.draw(canvas)

        return bitmap
    }

    private fun formatDate(rawDate: String): String {
        if (rawDate.isBlank()) return "RELEASE DATE"
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val date = inputFormat.parse(rawDate)
            if (date != null) {
                val outputFormat = SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH)
                outputFormat.format(date).uppercase(Locale.ENGLISH)
            } else {
                rawDate.uppercase(Locale.ENGLISH)
            }
        } catch (e: Exception) {
            rawDate.uppercase(Locale.ENGLISH)
        }
    }

    private fun formatCompactDate(rawDate: String?): String {
        if (rawDate.isNullOrBlank()) return "DEC, 2024"
        return try {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(rawDate)
            if (date != null) {
                SimpleDateFormat("MMM, yyyy", Locale.ENGLISH)
                    .format(date)
                    .uppercase(Locale.ENGLISH)
            } else {
                rawDate.uppercase(Locale.ENGLISH)
            }
        } catch (e: Exception) {
            rawDate.uppercase(Locale.ENGLISH)
        }
    }
}

fun shareMediaCard(fragment: Fragment, movie: MovieResult, posterPicker: MediaSharePosterPicker) {
    val ctx = fragment.context ?: return

    showToast(ctx, "Generating sharable image...")

    fragment.lifecycleScope.launch(Dispatchers.IO) {
        try {
            val movieId = movie.id ?: -1
            val isTv = movie.mediaType.equals("tv", ignoreCase = true)

            // 1. Fetch Hero Image
            val imagePath = movie.posterPath ?: movie.backdropPath
            val isLocal = imagePath != null && (imagePath.startsWith("content://") || imagePath.count { it == '/' } > 1)
            val fullPath = if (isLocal) imagePath else Constants.TMDB_IMAGE_BASE_URL_W780.plus(imagePath)

            val heroBitmap = try {
                Glide.with(fragment)
                    .asBitmap()
                    .load(fullPath)
                    .submit()
                    .get()
            } catch (e: Exception) {
                null
            }

            // 2. Fetch Client Provider, Director & Season Ratings
            var clientProvider: String? = null
            var directorName: String? = null
            var castName: String? = null
            var characterName: String? = null
            var numberOfSeasons = 0
            var maxEpisodes = 0
            val seasonRatingsMap = mutableMapOf<Int, List<Double?>>()

            if (movieId != -1) {
                try {
                    val entryPoint = EntryPointAccessors.fromApplication(
                        ctx.applicationContext,
                        MediaShareEntryPoint::class.java
                    )
                    val apiClient = entryPoint.getApiClient()

                    if (isTv) {
                        val tvDetailResponse = apiClient.fetchTVDetailApiCall(movieId)
                        if (tvDetailResponse.isSuccessful) {
                            val detail = tvDetailResponse.body()
                            val seasonsList = detail?.seasons?.filter { (it.season_number ?: 0) > 0 } ?: emptyList()
                            numberOfSeasons = seasonsList.size
                            maxEpisodes = seasonsList.maxOfOrNull { it.episode_count ?: 0 } ?: 0

                            if (numberOfSeasons <= 10 && maxEpisodes <= 30) {
                                val results = coroutineScope {
                                    seasonsList.take(10).map { season ->
                                        val seasonNum = season.season_number ?: 1
                                        async(Dispatchers.IO) {
                                            try {
                                                val seasonResp = apiClient.fetchTVSeasonApiCall(movieId, seasonNum)
                                                if (seasonResp.isSuccessful) {
                                                    val episodes = seasonResp.body()?.episodes ?: emptyList()
                                                    seasonNum to episodes.map { it.vote_average }
                                                } else {
                                                    seasonNum to emptyList()
                                                }
                                            } catch (e: Exception) {
                                                seasonNum to emptyList()
                                            }
                                        }
                                    }.awaitAll()
                                }
                                for ((sNum, ratings) in results) {
                                    seasonRatingsMap[sNum] = ratings
                                }
                            }
                        }
                    }

                    // Watch Provider
                    val providerResponse = if (isTv) {
                        apiClient.getTVWatchProvidersApiCall(movieId)
                    } else {
                        apiClient.getMovieWatchProvidersApiCall(movieId)
                    }
                    if (providerResponse.isSuccessful) {
                        val results = providerResponse.body()?.results
                        val provider = results?.IN?.flatrate?.firstOrNull()
                            ?: results?.IN?.buy?.firstOrNull()
                            ?: results?.IN?.rent?.firstOrNull()
                        clientProvider = provider?.providerName
                    }

                    // Credits / Director
                    val castResponse = if (isTv) {
                        apiClient.fetchTVCastApiCall(movieId)
                    } else {
                        apiClient.fetchMovieCastApiCall(movieId)
                    }
                    if (castResponse.isSuccessful) {
                        val castBody = castResponse.body()
                        val crewList = castBody?.crew ?: emptyList()
                        val director = crewList.firstOrNull { it.job.equals("Director", ignoreCase = true) }
                            ?: crewList.firstOrNull { it.department.equals("Directing", ignoreCase = true) }
                            ?: crewList.firstOrNull { it.job.equals("Executive Producer", ignoreCase = true) }
                        directorName = director?.name
                        val leadCast = castBody?.cast?.firstOrNull()
                        castName = leadCast?.name
                        characterName = leadCast?.character
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val isHeatmapAllowed = !isTv || (numberOfSeasons <= 10 && maxEpisodes <= 30)

            withContext(Dispatchers.Main) {
                showMediaSharePreviewBottomSheet(
                    fragment,
                    movie,
                    heroBitmap,
                    clientProvider,
                    directorName,
                    castName,
                    characterName,
                    isHeatmapAllowed,
                    seasonRatingsMap,
                    posterPicker
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                showToast(ctx, "Failed to generate image")
            }
        }
    }
}

private fun showMediaSharePreviewBottomSheet(
    fragment: Fragment,
    movie: MovieResult,
    heroBitmap: Bitmap?,
    clientProvider: String?,
    directorName: String?,
    castName: String?,
    characterName: String?,
    isHeatmapAllowed: Boolean,
    seasonRatingsMap: Map<Int, List<Double?>>,
    posterPicker: MediaSharePosterPicker
) {
    val ctx = fragment.context ?: return
    val dialog = BottomSheetDialog(ctx, R.style.SheetDialog)
    dialog.setOnShowListener { dialogInterface ->
        val bottomSheetDialog = dialogInterface as BottomSheetDialog
        val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        if (bottomSheet != null) {
            val behavior = BottomSheetBehavior.from(bottomSheet)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
        }
    }

    val view = fragment.layoutInflater.inflate(R.layout.bottom_sheet_share_preview, null)
    val previewImg = view.findViewById<ImageView>(R.id.share_preview_image)
    val chipGroup = view.findViewById<ChipGroup>(R.id.share_template_chip_group)
    val downloadBtn = view.findViewById<View>(R.id.share_download_btn)
    val shareBtn = view.findViewById<View>(R.id.share_now_btn)
    val cancelBtn = view.findViewById<View>(R.id.share_cancel_btn)
    val customPosterBtn = view.findViewById<View>(R.id.share_custom_poster_btn)

    val isTv = movie.mediaType.equals("tv", ignoreCase = true)
    view.findViewById<View>(R.id.chip_template_classic).visibility = View.GONE
    view.findViewById<View>(R.id.chip_template_minimal).visibility = View.GONE
    view.findViewById<View>(R.id.chip_media_editorial).visibility = View.VISIBLE
    view.findViewById<View>(R.id.chip_media_character).visibility = View.VISIBLE
    view.findViewById<View>(R.id.chip_media_heatmap).visibility = if (isTv) View.VISIBLE else View.GONE
    customPosterBtn.visibility = View.VISIBLE
    chipGroup?.check(R.id.chip_media_editorial)

    var selectedTemplate = MediaShareTemplate.EDITORIAL_POSTER
    var selectedHeroBitmap = heroBitmap
    var currentBitmap = MediaShareImageGenerator.generateImage(
        ctx, movie, selectedHeroBitmap, selectedTemplate, clientProvider, directorName, castName, characterName, seasonRatingsMap
    )

    fun updatePreview() {
        currentBitmap = MediaShareImageGenerator.generateImage(
            ctx, movie, selectedHeroBitmap, selectedTemplate, clientProvider, directorName, castName, characterName, seasonRatingsMap
        )
        previewImg.setImageBitmap(currentBitmap)
    }

    previewImg.setImageBitmap(currentBitmap)

    chipGroup?.setOnCheckedStateChangeListener { _, checkedIds ->
        if (R.id.chip_media_heatmap in checkedIds) {
            if (!isHeatmapAllowed) {
                showToast(ctx, "Series Heatmap poster supports up to 10 seasons and 30 episodes per season")
                chipGroup.check(R.id.chip_media_editorial)
                return@setOnCheckedStateChangeListener
            }
        }
        selectedTemplate = when {
            R.id.chip_media_character in checkedIds -> MediaShareTemplate.CHARACTER_POSTER
            R.id.chip_media_heatmap in checkedIds -> MediaShareTemplate.SERIES_HEATMAP_POSTER
            else -> MediaShareTemplate.EDITORIAL_POSTER
        }
        updatePreview()
    }

    customPosterBtn.setOnClickListener {
        posterPicker.launch { uri ->
            if (uri != null) {
                fragment.lifecycleScope.launch(Dispatchers.IO) {
                    val customPoster = try {
                        Glide.with(fragment)
                            .asBitmap()
                            .load(uri)
                            .fitCenter()
                            .submit(1080, 1600)
                            .get()
                    } catch (e: Exception) {
                        null
                    }

                    withContext(Dispatchers.Main) {
                        if (!dialog.isShowing) return@withContext
                        if (customPoster == null) {
                            showToast(ctx, "Unable to load selected poster")
                        } else {
                            selectedHeroBitmap = customPoster
                            updatePreview()
                        }
                    }
                }
            }
        }
    }

    downloadBtn.setOnClickListener {
        saveMediaBitmapToGallery(ctx, currentBitmap)
        dialog.dismiss()
    }

    shareBtn.setOnClickListener {
        saveAndShareMediaBitmap(fragment, currentBitmap)
        dialog.dismiss()
    }

    cancelBtn.setOnClickListener {
        dialog.dismiss()
    }

    dialog.setContentView(view)
    dialog.show()
}

private fun saveMediaBitmapToGallery(context: Context, bitmap: Bitmap) {
    val filename = "MovieFlix_Share_${System.currentTimeMillis()}.png"
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MovieFlix")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
    }

    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

    try {
        uri?.let {
            val stream = resolver.openOutputStream(it)
            if (stream != null) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                stream.close()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(it, values, null, null)
                }
                showToast(context, "Saved to Gallery!")
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        showToast(context, "Failed to save image")
    }
}

private fun saveAndShareMediaBitmap(fragment: Fragment, bitmap: Bitmap) {
    val context = fragment.context ?: return
    try {
        val cachePath = File(context.cacheDir, "shared_images")
        cachePath.mkdirs()
        val file = File(cachePath, "movie_share.png")
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        fragment.startActivity(Intent.createChooser(intent, "Share Movie"))
    } catch (e: Exception) {
        e.printStackTrace()
        showToast(context, "Failed to share image")
    }
}
