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
import com.shalenmathew.movieflix.domain.model.Genre
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


fun getGenreListById(id: List<Int>?): List<Genre> {
    if (id == null) {
        return emptyList()
    }
    val results = mutableListOf<Genre>()

    id.forEach {
        moviesGenresMap.containsKey(it) && results.add(Genre(it, moviesGenresMap.get(it)!!))
    }
    return results
}

private val moviesGenresMap: HashMap<Int, String> = hashMapOf(
    28 to "Action",
    12 to "Adventure",
    16 to "Animation",
    35 to "Comedy",
    80 to "Crime",
    99 to "Documentary",
    18 to "Drama",
    10751 to "Family",
    14 to "Fantasy",
    36 to "History",
    27 to "Horror",
    10402 to "Music",
    9648 to "Mystery",
    10749 to "Romance",
    878 to "Science Fiction",
    10770 to "TV Movie",
    53 to "Thriller",
    10752 to "War",
    37 to "Western",
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

val MIGRATION_5_7 = object : Migration(5, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        complex5To6Migration(db)
        complex6To7Migration(db)
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

val MIGRATION_3_6 = object : Migration(3, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // From migration 3->4
        db.execSQL("ALTER TABLE watch_list_news_table RENAME TO watch_list_table")
        db.execSQL("ALTER TABLE favorites_table RENAME TO favorites_movies_table")

        // From migration 4->5
        db.execSQL("ALTER TABLE favorites_movies_table ADD COLUMN personalNote TEXT")
        // From migration 5->6
        complex5To6Migration(db)
    }
}

val MIGRATION_4_6 = object : Migration(4, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // From migration 4->5
        db.execSQL("ALTER TABLE favorites_movies_table ADD COLUMN personalNote TEXT")

        // From migration 5->6
        complex5To6Migration(db)
    }
}

val MIGRATION_3_7 = object : Migration(3, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // From migration 3->4
        db.execSQL("ALTER TABLE watch_list_news_table RENAME TO watch_list_table")
        db.execSQL("ALTER TABLE favorites_table RENAME TO favorites_movies_table")

        // From migration 4->5
        db.execSQL("ALTER TABLE favorites_movies_table ADD COLUMN personalNote TEXT")

        // From migration 5->6
        complex5To6Migration(db)

        // From migration 6->7
        complex6To7Migration(db)
    }
}

val MIGRATION_4_7 = object : Migration(4, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // From migration 4->5
        db.execSQL("ALTER TABLE favorites_movies_table ADD COLUMN personalNote TEXT")

        // From migration 5->6
        complex5To6Migration(db)

        // From migration 6->7
        complex6To7Migration(db)
    }
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

