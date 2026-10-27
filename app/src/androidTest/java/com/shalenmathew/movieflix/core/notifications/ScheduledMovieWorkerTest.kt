package com.shalenmathew.movieflix.core.notifications

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.google.gson.Gson
import com.shalenmathew.movieflix.core.utils.GsonParser
import com.shalenmathew.movieflix.data.local_storage.MovieDao
import com.shalenmathew.movieflix.data.local_storage.MovieDataTypeConverter
import com.shalenmathew.movieflix.data.local_storage.MovieDatabase
import com.shalenmathew.movieflix.data.local_storage.entity.ScheduledEntity
import com.shalenmathew.movieflix.domain.model.MovieResult
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScheduledMovieWorkerTest {

    private lateinit var context: Context
    private lateinit var database: MovieDatabase

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        val converter = MovieDataTypeConverter(GsonParser(Gson()))
        database = Room.inMemoryDatabaseBuilder(
            context,
            MovieDatabase::class.java
        ).addTypeConverter(converter)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun teardown() {
        database.close()
    }

    private fun createWorker(inputData: Data, dao: MovieDao = database.dao): ScheduledMovieWorker {
        val workerFactory = object : WorkerFactory() {
            override fun createWorker(
                appContext: Context,
                workerClassName: String,
                workerParameters: WorkerParameters
            ): ListenableWorker? {
                return if (workerClassName == ScheduledMovieWorker::class.java.name) {
                    ScheduledMovieWorker(appContext, workerParameters, dao)
                } else null
            }
        }
        return TestListenableWorkerBuilder<ScheduledMovieWorker>(context)
            .setWorkerFactory(workerFactory)
            .setInputData(inputData)
            .build()
    }

    @Test
    fun scheduledMovieWorker_shouldDeleteMovieFromDbAfterWork() = runBlocking {
        val movieId = 999
        
        // 1. Manually insert a movie into the in-memory database first
        val movie = MovieResult(id = movieId, title = "Test Movie")
        database.dao.insertScheduledMovie(ScheduledEntity(movieId, movie, System.currentTimeMillis()))
        
        // 2. Run the Worker
        val inputData = workDataOf(
            ScheduledMovieWorker.KEY_MOVIE_ID to movieId,
            ScheduledMovieWorker.KEY_MOVIE_TITLE to "Test Movie",
            ScheduledMovieWorker.KEY_MOVIE_POSTER to "",
            ScheduledMovieWorker.KEY_MOVIE_RESULT_JSON to "{}",
            ScheduledMovieWorker.KEY_SCHEDULED_DATE to System.currentTimeMillis()
        )
        val worker = createWorker(inputData)

        worker.doWork()

        // 3. Verify if it was deleted
        val result = database.dao.getScheduledMovieById(movieId)

        assertNull("The movie should have been deleted from the database", result)
    }

    @Test
    fun scheduledMovieWorker_shouldFailOnInvalidInput() = runBlocking {
        // 1. Provide invalid input (Missing ID)
        val inputData = workDataOf(
            ScheduledMovieWorker.KEY_MOVIE_TITLE to "Test"
            // KEY_MOVIE_ID is missing, so getInt returns -1
        )

        val worker = createWorker(inputData)

        // 2. Run the Worker
        val result = worker.doWork()

        // 3. Verify it returns failure
        assertEquals(ListenableWorker.Result.failure(), result)
    }

    @Test
    fun scheduledMovieWorker_shouldSucceedEvenIfMovieNotInDb() = runBlocking {
        // 1. Provide a valid ID but DON'T insert it into the DB
        val movieId = 888
        val inputData = workDataOf(
            ScheduledMovieWorker.KEY_MOVIE_ID to movieId,
            ScheduledMovieWorker.KEY_MOVIE_TITLE to "Ghost Movie",
            ScheduledMovieWorker.KEY_MOVIE_POSTER to "",
            ScheduledMovieWorker.KEY_MOVIE_RESULT_JSON to "{}",
            ScheduledMovieWorker.KEY_SCHEDULED_DATE to System.currentTimeMillis()
        )

        val worker = createWorker(inputData)

        // 2. Run the Worker
        val result = worker.doWork()

        // 3. Verify it still returns success (it should show notification then skip deletion)
        assertEquals(ListenableWorker.Result.success(), result)
    }
}
