package com.shalenmathew.movieflix.data.local_storage

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.shalenmathew.movieflix.data.local_storage.entity.CustomListEntity
import com.shalenmathew.movieflix.data.local_storage.entity.CustomListMovieEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomListDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: CustomListEntity): Long

    @Delete
    suspend fun deleteList(list: CustomListEntity)

    @Query("UPDATE custom_list_table SET name = :name, description = :description WHERE id = :listId")
    suspend fun updateListDetails(listId: Int, name: String, description: String?)

    @Query("UPDATE custom_list_table SET isPinnedToFav = :isPinned WHERE id = :listId")
    suspend fun updatePinnedToFavStatus(listId: Int, isPinned: Boolean)

    @Query("UPDATE custom_list_table SET isPinnedToWatchlist = :isPinned WHERE id = :listId")
    suspend fun updatePinnedToWatchlistStatus(listId: Int, isPinned: Boolean)

    @Query("SELECT * FROM custom_list_table ORDER BY createdAt DESC")
    fun getAllLists(): Flow<List<CustomListEntity>>

    @Query("SELECT * FROM custom_list_table WHERE isPinnedToFav = 1 ORDER BY createdAt DESC")
    fun getPinnedFavLists(): Flow<List<CustomListEntity>>

    @Query("SELECT * FROM custom_list_table WHERE isPinnedToWatchlist = 1 ORDER BY createdAt DESC")
    fun getPinnedWatchlistLists(): Flow<List<CustomListEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addMovieToList(movie: CustomListMovieEntity)

    @Query("DELETE FROM custom_list_movie_table WHERE listId = :listId AND mediaId = :mediaId")
    suspend fun removeMovieFromList(listId: Int, mediaId: Int)

    @Query("SELECT * FROM custom_list_movie_table WHERE listId = :listId ORDER BY addedAt DESC")
    fun getMoviesInList(listId: Int): Flow<List<CustomListMovieEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM custom_list_movie_table WHERE listId = :listId AND mediaId = :mediaId)")
    suspend fun isMovieInList(listId: Int, mediaId: Int): Boolean

    @Query("SELECT COUNT(*) FROM custom_list_movie_table WHERE listId = :listId")
    fun getMovieCountInList(listId: Int): Flow<Int>

    @Query("SELECT posterPath FROM custom_list_movie_table WHERE listId = :listId ORDER BY addedAt DESC LIMIT 4")
    fun getTopPostersForList(listId: Int): Flow<List<String>>

    @Query("UPDATE custom_list_movie_table SET posterPath = :posterPath WHERE mediaId = :mediaId")
    suspend fun updateMoviePosterAcrossLists(mediaId: Int, posterPath: String)

    @Query("UPDATE custom_list_movie_table SET backdropPath = :bannerPath WHERE mediaId = :mediaId")
    suspend fun updateMovieBannerAcrossLists(mediaId: Int, bannerPath: String)

    @Query("SELECT * FROM custom_list_table")
    suspend fun getAllListsSync(): List<CustomListEntity>

    @Query("SELECT * FROM custom_list_movie_table")
    suspend fun getAllCustomListMoviesSync(): List<CustomListMovieEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLists(lists: List<CustomListEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomListMovies(movies: List<CustomListMovieEntity>)

    @Query("DELETE FROM custom_list_table")
    suspend fun deleteAllLists()

    @Query("DELETE FROM custom_list_movie_table")
    suspend fun deleteAllCustomListMovies()
}
