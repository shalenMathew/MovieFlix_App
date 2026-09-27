package com.shalenmathew.movieflix.core.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.LayoutRes
import com.shalenmathew.movieflix.R

enum class ListShareTemplate(
    val id: String,
    val title: String,
    @param:LayoutRes val layoutResId: Int
) {
    CLASSIC_DARK("classic_dark", "Classic Dark", R.layout.layout_sharable_list),
    MINIMAL_LIGHT("minimal_light", "Minimal Light", R.layout.layout_sharable_list_minimal)
}

object ListShareImageGenerator {

    fun generateImage(
        context: Context,
        listName: String,
        listDesc: String?,
        posterBitmaps: List<Bitmap>,
        template: ListShareTemplate
    ): Bitmap {
        val shareView = LayoutInflater.from(context).inflate(template.layoutResId, null)

        shareView.findViewById<TextView>(R.id.sharable_title)?.text = listName
        val descView = shareView.findViewById<TextView>(R.id.sharable_desc)
        if (descView != null) {
            descView.text = listDesc ?: ""
            descView.visibility = if (listDesc.isNullOrEmpty()) View.GONE else View.VISIBLE
        }

        val posterIds = listOf(
            R.id.poster_1, R.id.poster_2, R.id.poster_3,
            R.id.poster_4, R.id.poster_5, R.id.poster_6,
            R.id.poster_7, R.id.poster_8, R.id.poster_9
        )

        posterIds.forEachIndexed { index, id ->
            val imageView = shareView.findViewById<ImageView>(id)
            if (imageView != null) {
                if (index < posterBitmaps.size) {
                    imageView.setImageBitmap(posterBitmaps[index])
                    imageView.visibility = View.VISIBLE
                } else {
                    imageView.visibility = View.GONE
                }
            }
        }

        // Center 1 or 2 items horizontally ONLY for Minimal Poster
        if (template == ListShareTemplate.MINIMAL_LIGHT) {
            val poster1 = shareView.findViewById<ImageView>(R.id.poster_1)
            if (poster1 != null && poster1.layoutParams is GridLayout.LayoutParams) {
                val params = poster1.layoutParams as GridLayout.LayoutParams
                val leftMargin = when (posterBitmaps.size) {
                    1 -> 327
                    2 -> 164
                    else -> 0
                }
                params.setMargins(leftMargin, params.topMargin, params.rightMargin, params.bottomMargin)
                poster1.layoutParams = params
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
}
