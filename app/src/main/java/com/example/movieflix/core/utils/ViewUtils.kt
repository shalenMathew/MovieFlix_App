package com.example.movieflix.core.utils

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
import com.example.movieflix.domain.model.Genre
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


fun getGenreListById(id:List<Int>?):List<Genre>{
    if(id==null){
        return emptyList()
    }
    val results = mutableListOf<Genre>()

    id.forEach{
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

fun isNetworkAvailable(context:Context?):Boolean{

    val connectivityManger = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    return run {
        val activeNetworkInfo = connectivityManger.activeNetworkInfo
        activeNetworkInfo!=null&& activeNetworkInfo.isConnected
    }
}

fun View.gone(){
    visibility = View.GONE
}

fun View.visible(){
    visibility = View.VISIBLE
}

fun openNetworkSettings(context: Context){
    try {
        val i=Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY).also {
            it.flags=Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(i)
    }catch (e:Exception){
        val i=Intent(Settings.ACTION_SETTINGS).also {
            it.flags=Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(i)
    }
}

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

fun formatDate(year:String?):String{

    if(!year.isNullOrEmpty()){
        val sdf =SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val parsedString:Date = sdf.parse(year)

        val formattedString = SimpleDateFormat("yyyy",Locale.getDefault()).format(parsedString)

        return formattedString
    }

    return "Unknown"
}



fun shareMovie(context:Context,title:String,trailer:String){

    val movieTitle = " Movie \"$title\" Trailer..."
    val textExtra = "$movieTitle\n\n$trailer"
    val i = Intent()
    i.action = Intent.ACTION_SEND
    i.type="text/plain"
    i.putExtra(Intent.EXTRA_TEXT,textExtra)
    context.startActivity(Intent.createChooser(i,"Share:"))

}

fun getRandomChar():String{
    val alphabet = ('a'..'z')
    return alphabet.random().toString()
}

 val MIGRATION = object : Migration(3,4) {
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

val MIGRATION_3_5 =  object : Migration(3, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE watch_list_news_table RENAME TO watch_list_table")
        db.execSQL("ALTER TABLE favorites_table RENAME TO favorites_movies_table")
        db.execSQL("ALTER TABLE favorites_movies_table ADD COLUMN personalNote TEXT")
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

