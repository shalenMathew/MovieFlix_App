package com.shalenmathew.movieflix.core.utils

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.provider.Settings
import android.util.AttributeSet
import android.view.View
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.shalenmathew.movieflix.R
import com.shalenmathew.movieflix.domain.model.Genre
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


fun getGenreListById(context: Context, id: List<Int>?): List<Genre> {
    if (id == null) {
        return emptyList()
    }
    val results = mutableListOf<Genre>()

    id.forEach {
        moviesGenresMap.containsKey(it) && results.add(Genre(it, context.getString(moviesGenresMap.get(it)!!)))
    }
    return results
}

private val moviesGenresMap: HashMap<Int, Int> = hashMapOf(
    28 to R.string.genre_action,
    12 to R.string.genre_adventure,
    16 to R.string.genre_animation,
    35 to R.string.genre_comedy,
    80 to R.string.genre_crime,
    99 to R.string.genre_documentary,
    18 to R.string.genre_drama,
    10751 to R.string.genre_family,
    14 to R.string.genre_fantasy,
    36 to R.string.genre_history,
    27 to R.string.genre_horror,
    10402 to R.string.genre_music,
    9648 to R.string.genre_mystery,
    10749 to R.string.genre_romance,
    878 to R.string.genre_scifi,
    10770 to R.string.genre_tv_movie,
    53 to R.string.genre_thriller,
    10752 to R.string.genre_war,
    37 to R.string.genre_western,
)

fun isNetworkAvailable(context: Context?): Boolean {

    val connectivityManger =
        context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    return run {
        val activeNetworkInfo = connectivityManger.activeNetworkInfo
        activeNetworkInfo != null && activeNetworkInfo.isConnected
    }
}

fun View.gone() {
    visibility = View.GONE
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun openNetworkSettings(context: Context) {
    try {
        val i = Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY).also {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(i)
    } catch (_: Exception) {
        val i = Intent(Settings.ACTION_SETTINGS).also {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(i)
    }
}

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

fun formatDate(year: String?): String {

    if (!year.isNullOrEmpty()) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val parsedString: Date = sdf.parse(year) ?: Date()

        val formattedString = SimpleDateFormat("yyyy", Locale.getDefault()).format(parsedString)

        return formattedString
    }

    return "Unknown"
}


fun shareMovie(context: Context, title: String, trailer: String) {

    val movieTitle = " Movie \"$title\" Trailer..."
    val textExtra = "$movieTitle\n\n$trailer"
    val i = Intent()
    i.action = Intent.ACTION_SEND
    i.type = "text/plain"
    i.putExtra(Intent.EXTRA_TEXT, textExtra)
    context.startActivity(Intent.createChooser(i, "Share:"))

}

fun getRandomChar(): String {
    val alphabet = ('a'..'z')
    return alphabet.random().toString()
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL("ALTER TABLE watch_list_news_table RENAME TO watch_list_table")
        db.execSQL("ALTER TABLE favorites_table RENAME TO favorites_movies_table")

    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE favorites_movies_table ADD COLUMN personalNote TEXT")
    }
}

val MIGRATION_3_5 = object : Migration(3, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // From migration 3->4
        db.execSQL("ALTER TABLE watch_list_news_table RENAME TO watch_list_table")
        db.execSQL("ALTER TABLE favorites_table RENAME TO favorites_movies_table")
        // From migration 4->5
        db.execSQL("ALTER TABLE favorites_movies_table ADD COLUMN personalNote TEXT")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        complex5To6Migration(db)
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        complex5To6Migration(db)
        complex6To7Migration(db)
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `series_tracking_table` (`id` INTEGER NOT NULL, `name` TEXT NOT NULL, `posterPath` TEXT, `backdropPath` TEXT, `overview` TEXT, `lastWatchedEpisodeId` INTEGER, `lastWatchedSeasonNumber` INTEGER, `lastWatchedEpisodeNumber` INTEGER, `lastUpdated` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        db.execSQL("CREATE TABLE IF NOT EXISTS `seasons_tracking_table` (`id` INTEGER NOT NULL, `seriesId` INTEGER NOT NULL, `seasonNumber` INTEGER NOT NULL, `name` TEXT, `episodeCount` INTEGER, `posterPath` TEXT, PRIMARY KEY(`id`), FOREIGN KEY(`seriesId`) REFERENCES `series_tracking_table`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        db.execSQL("CREATE TABLE IF NOT EXISTS `episodes_tracking_table` (`id` INTEGER NOT NULL, `seriesId` INTEGER NOT NULL, `seasonId` INTEGER NOT NULL, `episodeNumber` INTEGER NOT NULL, `seasonNumber` INTEGER NOT NULL, `name` TEXT, `overview` TEXT, `stillPath` TEXT, `runtime` INTEGER, `isWatched` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`seriesId`) REFERENCES `series_tracking_table`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`seasonId`) REFERENCES `seasons_tracking_table`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_seasons_tracking_table_seriesId` ON `seasons_tracking_table` (`seriesId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_episodes_tracking_table_seriesId` ON `episodes_tracking_table` (`seriesId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_episodes_tracking_table_seasonId` ON `episodes_tracking_table` (`seasonId`)")
    }
}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE series_tracking_table ADD COLUMN syncStatus TEXT NOT NULL DEFAULT 'PENDING'")
    }
}

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE series_tracking_table ADD COLUMN isTracked INTEGER NOT NULL DEFAULT 1")
    }
}

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Create the new progress table
        db.execSQL("CREATE TABLE IF NOT EXISTS `series_progress_table` (`seriesId` INTEGER NOT NULL, `lastWatchedSeasonNumber` INTEGER, `lastWatchedEpisodeNumber` INTEGER, `lastWatchedEpisodeId` INTEGER, `lastUpdated` INTEGER NOT NULL, PRIMARY KEY(`seriesId`))")
        
        // Transfer existing progress from tracking table to progress table
        db.execSQL("""
            INSERT OR IGNORE INTO series_progress_table (seriesId, lastWatchedSeasonNumber, lastWatchedEpisodeNumber, lastWatchedEpisodeId, lastUpdated)
            SELECT id, lastWatchedSeasonNumber, lastWatchedEpisodeNumber, lastWatchedEpisodeId, lastUpdated FROM series_tracking_table
        """)
        
        // Clean up tracking table (remove columns no longer needed)
        // Room doesn't support DROP COLUMN easily on older SQLite, so we'll just ignore them or recreate.
        // For simplicity in migration script, we'll just leave them for now or recreate the table if needed.
        // Let's do the standard recreate pattern to match the new Entity class exactly.
        
        db.execSQL("CREATE TABLE `series_tracking_table_new` (`id` INTEGER NOT NULL, `name` TEXT NOT NULL, `posterPath` TEXT, `backdropPath` TEXT, `overview` TEXT, `lastUpdated` INTEGER NOT NULL, `syncStatus` TEXT NOT NULL, PRIMARY KEY(`id`))")
        db.execSQL("INSERT INTO series_tracking_table_new (id, name, posterPath, backdropPath, overview, lastUpdated, syncStatus) SELECT id, name, posterPath, backdropPath, overview, lastUpdated, syncStatus FROM series_tracking_table")
        db.execSQL("DROP TABLE series_tracking_table")
        db.execSQL("ALTER TABLE series_tracking_table_new RENAME TO series_tracking_table")
    }
}

val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE episodes_tracking_table ADD COLUMN airDate TEXT")
        db.execSQL("ALTER TABLE episodes_tracking_table ADD COLUMN voteAverage REAL")
    }
}

val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `custom_list_table` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `description` TEXT, `createdAt` INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `custom_list_movie_table` (`listId` INTEGER NOT NULL, `mediaId` INTEGER NOT NULL, `mediaType` TEXT, `name` TEXT, `posterPath` TEXT, `backdropPath` TEXT, `overview` TEXT, `addedAt` INTEGER NOT NULL, PRIMARY KEY(`listId`, `mediaId`), FOREIGN KEY(`listId`) REFERENCES `custom_list_table`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_custom_list_movie_table_listId` ON `custom_list_movie_table` (`listId`)")
    }
}

val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE custom_list_movie_table ADD COLUMN voteAverage REAL")
        db.execSQL("ALTER TABLE custom_list_movie_table ADD COLUMN releaseDate TEXT")
        db.execSQL("ALTER TABLE custom_list_movie_table ADD COLUMN originalLanguage TEXT")
    }
}

val MIGRATION_14_15 = object : Migration(14, 15) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `personal_gallery_table` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                `mediaId` INTEGER NOT NULL, 
                `imagePath` TEXT NOT NULL, 
                `addedAt` INTEGER NOT NULL, 
                FOREIGN KEY(`mediaId`) REFERENCES `favorites_movies_table`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE 
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_personal_gallery_table_mediaId` ON `personal_gallery_table` (`mediaId`)")
    }
}

private fun complex5To6Migration(db: SupportSQLiteDatabase) {
    db.execSQL("""
            CREATE TABLE IF NOT EXISTS scheduled_movies_table (
                id INTEGER PRIMARY KEY NOT NULL,
                movieResult TEXT NOT NULL,
                scheduledDate INTEGER NOT NULL
            )
        """.trimIndent())
}

private fun complex6To7Migration(db: SupportSQLiteDatabase) {
    db.execSQL("""
            CREATE TABLE watch_list_table_new (
                id INTEGER PRIMARY KEY NOT NULL,
                movieResult TEXT NOT NULL,
                insertedAt TEXT DEFAULT CURRENT_TIMESTAMP
            )
        """.trimIndent())

    db.execSQL("""
            INSERT INTO watch_list_table_new (id, movieResult)
            SELECT id, movieResult FROM watch_list_table
        """.trimIndent())

    db.execSQL("DROP TABLE watch_list_table")

    db.execSQL("ALTER TABLE watch_list_table_new RENAME TO watch_list_table")

    db.execSQL("""
            CREATE TABLE favorites_movies_table_new (
                id INTEGER PRIMARY KEY NOT NULL,
                movieResult TEXT NOT NULL,
                personalNote TEXT,
                insertedAt TEXT DEFAULT CURRENT_TIMESTAMP
            )
        """.trimIndent())

    db.execSQL("""
            INSERT INTO favorites_movies_table_new (id, movieResult, personalNote)
            SELECT id, movieResult, personalNote FROM favorites_movies_table
        """.trimIndent())

    db.execSQL("DROP TABLE favorites_movies_table")

    db.execSQL("ALTER TABLE favorites_movies_table_new RENAME TO favorites_movies_table")
}


class CustomNestedScrollView @JvmOverloads constructor(
    ctx: Context, attrs: AttributeSet? = null
) : NestedScrollView(ctx, attrs) {

    override fun onNestedPreScroll(
        target: View, dx: Int, dy: Int, consumed: IntArray
    ) {
        if (dy < 0 && !canScrollVertically(-1)) {
            // At top & swiping down
            val behavior = (layoutParams as? CoordinatorLayout.LayoutParams)
                ?.behavior as? BottomSheetBehavior<*>
            behavior?.let {
                it.state = BottomSheetBehavior.STATE_HIDDEN
                consumed[1] = dy
                return
            }
        }
        super.onNestedPreScroll(target, dx, dy, consumed)
    }
}

