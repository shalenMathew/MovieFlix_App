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
import com.shalenmathew.movieflix.domain.model.MovieResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

enum class MediaShareTemplate(
    val id: String,
    val title: String,
    @param:LayoutRes val layoutResId: Int
) {
    EDITORIAL_POSTER("editorial_poster", "Editorial Poster", R.layout.layout_sharable_media_editorial)
}

object MediaShareImageGenerator {

    fun generateImage(
        context: Context,
        movie: MovieResult,
        heroBitmap: Bitmap?,
        template: MediaShareTemplate
    ): Bitmap {
        val shareView = LayoutInflater.from(context).inflate(template.layoutResId, null)

        val title = movie.title ?: movie.name ?: ""
        val isTv = movie.mediaType.equals("tv", ignoreCase = true)

        // Title
        shareView.findViewById<TextView>(R.id.media_title)?.text = title

        // Category
        val categoryText = if (isTv) "TV-SERIES" else "FEATURE FILM"
        shareView.findViewById<TextView>(R.id.media_category)?.text = categoryText
        shareView.findViewById<TextView>(R.id.media_subtitle)?.text = if (isTv) "TV SERIES" else "FEATURE FILM"

        // Release Date
        val rawDate = movie.releaseDate ?: ""
        val formattedDate = formatDate(rawDate)
        shareView.findViewById<TextView>(R.id.media_release_date)?.text = formattedDate

        // Client & App Branding
        shareView.findViewById<TextView>(R.id.media_client)?.text = "HBO"
        shareView.findViewById<TextView>(R.id.media_app_branding)?.text = "MOVIEFLIX"

        // Director
        shareView.findViewById<TextView>(R.id.media_director_label)?.text = "Director"

        // Hero Image
        val heroImageView = shareView.findViewById<ImageView>(R.id.media_hero_image)
        if (heroImageView != null && heroBitmap != null) {
            heroImageView.setImageBitmap(heroBitmap)
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
}

fun shareMediaCard(fragment: Fragment, movie: MovieResult) {
    val ctx = fragment.context ?: return

    showToast(ctx, "Generating sharable image...")

    fragment.lifecycleScope.launch(Dispatchers.IO) {
        try {
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

            withContext(Dispatchers.Main) {
                showMediaSharePreviewBottomSheet(fragment, movie, heroBitmap)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                showToast(ctx, "Failed to generate image")
            }
        }
    }
}

private fun showMediaSharePreviewBottomSheet(fragment: Fragment, movie: MovieResult, heroBitmap: Bitmap?) {
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

    chipGroup?.visibility = View.GONE

    val template = MediaShareTemplate.EDITORIAL_POSTER
    val currentBitmap = MediaShareImageGenerator.generateImage(
        ctx,
        movie,
        heroBitmap,
        template
    )

    previewImg.setImageBitmap(currentBitmap)

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
