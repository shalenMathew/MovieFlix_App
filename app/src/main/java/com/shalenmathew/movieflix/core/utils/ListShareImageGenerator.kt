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

        val isMoreThanSix = posterBitmaps.size > 6
        val numColumns = if (isMoreThanSix) 4 else 3
        val gridLayout = shareView.findViewById<GridLayout>(R.id.sharable_grid)
        gridLayout?.columnCount = numColumns

        val posterIds = listOf(
            R.id.poster_1, R.id.poster_2, R.id.poster_3, R.id.poster_4,
            R.id.poster_5, R.id.poster_6, R.id.poster_7, R.id.poster_8,
            R.id.poster_9, R.id.poster_10, R.id.poster_11, R.id.poster_12
        )

        posterIds.forEachIndexed { index, id ->
            val imageView = shareView.findViewById<ImageView>(id)
            if (imageView != null) {
                if (index < posterBitmaps.size) {
                    imageView.setImageBitmap(posterBitmaps[index])
                    imageView.visibility = View.VISIBLE

                    val row = index / numColumns
                    val col = index % numColumns

                    val width = if (isMoreThanSix) {
                        if (template == ListShareTemplate.CLASSIC_DARK) 230 else 245
                    } else {
                        if (template == ListShareTemplate.CLASSIC_DARK) 310 else 326
                    }

                    val height = if (isMoreThanSix) {
                        if (template == ListShareTemplate.CLASSIC_DARK) 345 else 368
                    } else {
                        if (template == ListShareTemplate.CLASSIC_DARK) 465 else 489
                    }

                    val params = GridLayout.LayoutParams(
                        GridLayout.spec(row),
                        GridLayout.spec(col)
                    ).apply {
                        this.width = width
                        this.height = height
                        if (template == ListShareTemplate.CLASSIC_DARK) {
                            setMargins(5, 5, 5, 5)
                        } else {
                            setMargins(0, 0, 0, 0)
                        }
                    }

                    // Center 1 or 2 items horizontally ONLY for Minimal Poster when items <= 2
                    if (template == ListShareTemplate.MINIMAL_LIGHT && !isMoreThanSix) {
                        val leftMargin = when (posterBitmaps.size) {
                            1 -> 327
                            2 -> 164
                            else -> 0
                        }
                        if (index == 0) {
                            params.setMargins(leftMargin, 0, 0, 0)
                        }
                    }

                    imageView.layoutParams = params
                } else {
                    imageView.visibility = View.GONE
                }
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
