package com.shalenmathew.movieflix.presentation.widget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.bumptech.glide.Glide
import com.shalenmathew.movieflix.R
import com.shalenmathew.movieflix.core.utils.Constants
import com.shalenmathew.movieflix.data.local_storage.SeriesTrackingDao
import com.shalenmathew.movieflix.presentation.MainActivity
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

@EntryPoint
@InstallIn(SingletonComponent::class)
interface UpNextWidgetEntryPoint {
    fun seriesTrackingDao(): SeriesTrackingDao
}

class RefreshAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val now = System.currentTimeMillis()
        if (now - lastRefreshTime >= COOLDOWN_MS) {
            lastRefreshTime = now
            updateUpNextWidget(context)
        }
    }

    companion object {
        @Volatile
        private var lastRefreshTime = 0L
        private const val COOLDOWN_MS = 3000L // 3-second cooldown to avoid spamming
    }
}

data class UpNextItem(
    val seriesId: Int,
    val seriesName: String,
    val posterPath: String?,
    val nextEpisodeText: String,
    val progressPercent: Int,
    val posterBitmap: Bitmap? = null
)

class UpNextWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val upNextItems = loadUpNextItems(context)

        provideContent {
            UpNextWidgetContent(context, upNextItems)
        }
    }

    private suspend fun loadUpNextItems(context: Context): List<UpNextItem> = withContext(Dispatchers.IO) {
        try {
            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                UpNextWidgetEntryPoint::class.java
            )
            val dao = entryPoint.seriesTrackingDao()
            val trackedList = dao.getTrackedSeriesListSync()

            trackedList.map { series ->
                val allEpisodes = dao.getAllEpisodesForSeriesSync(series.id)
                val totalEpisodes = allEpisodes.size
                val watchedEpisodes = allEpisodes.count { it.isWatched }
                val progressPercent = if (totalEpisodes > 0) (watchedEpisodes * 100) / totalEpisodes else 0

                val nextEpisode = allEpisodes.firstOrNull { !it.isWatched }
                val nextEpisodeText = when {
                    nextEpisode != null -> "S${nextEpisode.seasonNumber.toString().padStart(2, '0')} | E${nextEpisode.episodeNumber}"
                    totalEpisodes > 0 && watchedEpisodes == totalEpisodes -> "Completed"
                    else -> "S01 | E1"
                }

                // Per-item isolated try-catch with a 2.5s timeout per image to prevent blocking provideGlance
                val posterBitmap = try {
                    val path = series.posterPath
                    if (path != null) {
                        val isLocal = path.startsWith("content://") || path.count { it == '/' } > 1
                        val fullUrl = if (isLocal) path else Constants.TMDB_POSTER_IMAGE_BASE_URL_W342.plus(path)
                        withTimeoutOrNull(2500L) {
                            Glide.with(context.applicationContext)
                                .asBitmap()
                                .load(fullUrl)
                                .submit(120, 180)
                                .get()
                        }
                    } else null
                } catch (e: Exception) {
                    null
                }

                UpNextItem(
                    seriesId = series.id,
                    seriesName = series.name,
                    posterPath = series.posterPath,
                    nextEpisodeText = nextEpisodeText,
                    progressPercent = progressPercent,
                    posterBitmap = posterBitmap
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}

@Composable
private fun UpNextWidgetContent(context: Context, items: List<UpNextItem>) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(day = Color.Black, night = Color.Black))
            .cornerRadius(16.dp)
            .padding(14.dp)
    ) {
        // Header Row
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "UP Next",
                style = TextStyle(
                    color = ColorProvider(day = Color.White, night = Color.White),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = GlanceModifier.defaultWeight())

            Box(
                modifier = GlanceModifier
                    .size(36.dp)
                    .clickable(actionRunCallback<RefreshAction>()),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_refresh),
                    contentDescription = "Refresh Widget",
                    modifier = GlanceModifier.size(20.dp)
                )
            }
        }

        if (items.isEmpty()) {
            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No tracked series yet.\nTrack shows in MovieFlix!",
                    style = TextStyle(
                        color = ColorProvider(day = Color(0xFFAAAAAA), night = Color(0xFFAAAAAA)),
                        fontSize = 14.sp
                    )
                )
            }
        } else {
            LazyColumn(
                modifier = GlanceModifier.fillMaxSize()
            ) {
                items(items) { item ->
                    UpNextItemRow(context, item)
                }
            }
        }
    }
}

@Composable
private fun UpNextItemRow(context: Context, item: UpNextItem) {
    val intent = Intent(context, MainActivity::class.java).apply {
        putExtra("series_id", item.seriesId)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(actionStartActivity(intent)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Poster Thumbnail
        if (item.posterBitmap != null) {
            Image(
                provider = ImageProvider(item.posterBitmap),
                contentDescription = item.seriesName,
                modifier = GlanceModifier
                    .width(48.dp)
                    .height(68.dp)
            )
        } else {
            Box(
                modifier = GlanceModifier
                    .width(48.dp)
                    .height(68.dp)
                    .background(ColorProvider(day = Color(0xFF222222), night = Color(0xFF222222))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.seriesName.take(1).uppercase(),
                    style = TextStyle(color = ColorProvider(day = Color.White, night = Color.White), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                )
            }
        }

        Spacer(modifier = GlanceModifier.width(10.dp))

        // Details (Title + Season/Ep + Progress Bar)
        Column(
            modifier = GlanceModifier.defaultWeight()
        ) {
            Text(
                text = item.seriesName,
                style = TextStyle(
                    color = ColorProvider(day = Color.White, night = Color.White),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )

            Text(
                text = item.nextEpisodeText,
                style = TextStyle(
                    color = ColorProvider(day = Color(0xFFDDDDDD), night = Color(0xFFDDDDDD)),
                    fontSize = 13.sp
                ),
                maxLines = 1
            )

            Spacer(modifier = GlanceModifier.height(6.dp))

            // Progress Bar (Rounded with Soft Muted Purple Accent #7352C7)
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .cornerRadius(3.dp)
                    .background(ColorProvider(day = Color(0xFF333333), night = Color(0xFF333333)))
            ) {
                val purpleFill = item.progressPercent.coerceAtLeast(1)
                val remainingFill = (100 - purpleFill).coerceAtLeast(0)

                Box(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .fillMaxHeight()
                        .cornerRadius(3.dp)
                        .background(ColorProvider(day = Color(0xFF6E41EC), night = Color(0xFF5E53E0)))
                ) {}

                if (remainingFill > 0) {
                    Spacer(
                        modifier = GlanceModifier
                            .width(remainingFill.dp)
                            .fillMaxHeight()
                            .background(ColorProvider(day = Color(0xFF333333), night = Color(0xFF333333)))
                    )
                }
            }
        }

        Spacer(modifier = GlanceModifier.width(12.dp))

        // Percentage Text
        Text(
            text = "${item.progressPercent}%",
            style = TextStyle(
                color = ColorProvider(day = Color.White, night = Color.White),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

private var widgetUpdateJob: kotlinx.coroutines.Job? = null
private val widgetUpdateScope = kotlinx.coroutines.CoroutineScope(Dispatchers.Default)

suspend fun updateUpNextWidget(context: Context) {
    try {
        widgetUpdateJob?.cancel()
        widgetUpdateJob = widgetUpdateScope.launch {
            kotlinx.coroutines.delay(200L)
            UpNextWidget().updateAll(context)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
