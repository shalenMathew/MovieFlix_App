package com.shalenmathew.movieflix.core.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.core.content.FileProvider
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

enum class MediaShareTemplate(
    val id: String,
    val title: String,
    @param:LayoutRes val layoutResId: Int
) {
    EDITORIAL_POSTER("editorial_poster", "Editorial Poster", R.layout.layout_sharable_media_editorial),
    CHARACTER_POSTER("editorial_poster_2", "Editorial Poster 2", R.layout.layout_sharable_media_character)
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
        characterName: String? = null
    ): Bitmap {
        val shareView = LayoutInflater.from(context).inflate(template.layoutResId, null)

        val title = movie.title ?: movie.name ?: ""
        val isTv = movie.mediaType.equals("tv", ignoreCase = true)

        if (template == MediaShareTemplate.CHARACTER_POSTER) {
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

        val width = 1080
        shareView.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        val measuredHeight = shareView.measuredHeight
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

fun shareMediaCard(fragment: Fragment, movie: MovieResult) {
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

            // 2. Fetch Client Provider & Director Name
            var clientProvider: String? = null
            var directorName: String? = null
            var castName: String? = null
            var characterName: String? = null

            if (movieId != -1) {
                try {
                    val entryPoint = EntryPointAccessors.fromApplication(
                        ctx.applicationContext,
                        MediaShareEntryPoint::class.java
                    )
                    val apiClient = entryPoint.getApiClient()

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

            withContext(Dispatchers.Main) {
                showMediaSharePreviewBottomSheet(
                    fragment,
                    movie,
                    heroBitmap,
                    clientProvider,
                    directorName,
                    castName,
                    characterName
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
    characterName: String?
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

    view.findViewById<View>(R.id.chip_template_classic).visibility = View.GONE
    view.findViewById<View>(R.id.chip_template_minimal).visibility = View.GONE
    view.findViewById<View>(R.id.chip_media_editorial).visibility = View.VISIBLE
    view.findViewById<View>(R.id.chip_media_character).visibility = View.VISIBLE
    chipGroup?.check(R.id.chip_media_editorial)

    var selectedTemplate = MediaShareTemplate.EDITORIAL_POSTER
    var currentBitmap = MediaShareImageGenerator.generateImage(
        ctx, movie, heroBitmap, selectedTemplate, clientProvider, directorName, castName, characterName
    )
    previewImg.setImageBitmap(currentBitmap)

    chipGroup?.setOnCheckedStateChangeListener { _, checkedIds ->
        selectedTemplate = if (R.id.chip_media_character in checkedIds) {
            MediaShareTemplate.CHARACTER_POSTER
        } else {
            MediaShareTemplate.EDITORIAL_POSTER
        }
        currentBitmap = MediaShareImageGenerator.generateImage(
            ctx, movie, heroBitmap, selectedTemplate, clientProvider, directorName, castName, characterName
        )
        previewImg.setImageBitmap(currentBitmap)
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
