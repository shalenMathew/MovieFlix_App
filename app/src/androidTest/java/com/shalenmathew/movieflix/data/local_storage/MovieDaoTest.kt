package com.shalenmathew.movieflix.data.local_storage

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.Gson
import com.shalenmathew.movieflix.core.utils.GsonParser
import com.shalenmathew.movieflix.data.local_storage.entity.FavouritesEntity
import com.shalenmathew.movieflix.data.local_storage.entity.HomeFeedEntity
import com.shalenmathew.movieflix.data.local_storage.entity.ScheduledEntity
import com.shalenmathew.movieflix.data.local_storage.entity.WatchListEntity
import com.shalenmathew.movieflix.data.model.HomeFeedResponse
import com.shalenmathew.movieflix.data.model.MovieResponseResult
import com.shalenmathew.movieflix.domain.model.MovieResult
import com.shalenmathew.movieflix.getOrAwaitValue
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MovieDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: MovieDatabase
    private lateinit var dao: MovieDao

    @Before
    fun setup() {
        // We need the converter because your DB uses @ProvidedTypeConverter
        val converter = MovieDataTypeConverter(GsonParser(Gson()))
        
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MovieDatabase::class.java
        ).addTypeConverter(converter)
            .allowMainThreadQueries()
            .build()
        
        dao = database.dao
    }

    @After
    fun teardown() {
        database.close()
    }
    
    
    

    @Test
    fun insertAndReadHomeFeed() = runBlocking {
        val movie = MovieResponseResult(
            id = 1, title = "Trending Movie",
            adult = false, backdrop_path = "", genre_ids = emptyList(),
            original_language = "en", original_title = "Trending Movie",
            name = "", overview = "", popularity = 0.0, poster_path = "",
            release_date = "", video = false, vote_average = 0.0,
            vote_count = 0, media_type = ""
        )
        val homeFeed = HomeFeedEntity(
            id = 1,
            bannerMovies = listOf(movie),
            homeFeedResponseList = listOf(HomeFeedResponse("Trending", listOf(movie)))
        )

        dao.insertHomeFeedData(homeFeed)

        val retrieved = dao.readHomeFeedData()
        assertEquals(1, retrieved.bannerMovies.size)
        assertEquals("Trending Movie", retrieved.bannerMovies[0].title)
        assertEquals("Trending", retrieved.homeFeedResponseList[0].title)
    }

    @Test
    fun insertAndReadWatchList() = runBlocking {
        val movie = MovieResult(id = 1, title = "Inception")
        val entity = WatchListEntity(id = 1, movieResult = movie, insertedAt = "2023-10-01")
        
        dao.insertWatchListData(entity)
        
        val allMovies = dao.getAllWatchListData().getOrAwaitValue()
        assertEquals(1, allMovies.size)
        assertEquals("Inception", allMovies[0].movieResult.title)
    }

    @Test
    fun insertAndReadScheduledMovieById() = runBlocking {
        val movie = MovieResult(id = 101, title = "Dark Knight")
        val scheduled = ScheduledEntity(id = 101, movieResult = movie, scheduledDate = 1700000000000L)
        
        dao.insertScheduledMovie(scheduled)
        
        val retrieved = dao.getScheduledMovieById(101)
        assertEquals("Dark Knight", retrieved?.movieResult?.title)
    }

    @Test
    fun deleteWatchListMovie() = runBlocking {
        val movie = MovieResult(id = 1, title = "Inception")
        val entity = WatchListEntity(id = 1, movieResult = movie, insertedAt = null)
        
        dao.insertWatchListData(entity)
        dao.deleteWatchListData(entity)
        
        val allMovies = dao.getAllWatchListData().getOrAwaitValue()
        assertEquals(0, allMovies.size)
    }

    @Test
    fun addPersonalNoteToFavorite() = runBlocking {
        val movie = MovieResult(id = 5, title = "Interstellar")
        val favorite = FavouritesEntity(id = 5, movieResult = movie, personalNote = null, insertedAt = null)
        
        dao.insertFavMovie(favorite)
        dao.addPersonalNote(5, "Best movie ever!")
        
        val allFavs = dao.getAllFavMovies().getOrAwaitValue()
        assertEquals("Best movie ever!", allFavs[0].personalNote)
    }

    @Test
    fun conflictStrategy_replaceOnSameId() = runBlocking {
        val movie1 = MovieResult(id = 1, title = "Original")
        val movie2 = MovieResult(id = 1, title = "Updated")
        
        dao.insertWatchListData(WatchListEntity(1, movie1, null))
        dao.insertWatchListData(WatchListEntity(1, movie2, null))
        
        val allMovies = dao.getAllWatchListData().getOrAwaitValue()
        assertEquals(1, allMovies.size)
        assertEquals("Updated", allMovies[0].movieResult.title)
    }
}
