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

    @Query("SELECT * FROM custom_list_table ORDER BY createdAt DESC")
    fun getAllLists(): Flow<List<CustomListEntity>>

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
}
