package com.shalenmathew.movieflix.core.notifications

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.google.gson.Gson
import com.shalenmathew.movieflix.core.utils.Constants
import com.shalenmathew.movieflix.core.utils.GsonParser
import com.shalenmathew.movieflix.core.utils.MIGRATION_3_4
import com.shalenmathew.movieflix.core.utils.MIGRATION_4_5
import com.shalenmathew.movieflix.core.utils.MIGRATION_5_6
import com.shalenmathew.movieflix.core.utils.MIGRATION_6_7
import com.shalenmathew.movieflix.data.local_storage.MovieDataTypeConverter
import com.shalenmathew.movieflix.data.local_storage.MovieDatabase
import com.shalenmathew.movieflix.data.local_storage.entity.ScheduledEntity
import com.shalenmathew.movieflix.domain.model.MovieResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScheduledMovieWorkerTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun scheduledMovieWorker_shouldDeleteMovieFromDbAfterWork() = runBlocking {
        val movieId = 999
        
        // 1. Manually insert a movie into the REAL database first
        val converter = MovieDataTypeConverter(GsonParser(Gson()))
        val db = Room.databaseBuilder(context, MovieDatabase::class.java, Constants.DATABASE_NAME)
            .addTypeConverter(converter)
            .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
            .build()
        
        val movie = MovieResult(id = movieId, title = "Test Movie")
        db.dao.insertScheduledMovie(ScheduledEntity(movieId, movie, System.currentTimeMillis()))
        
        // 2. Run the Worker
        val inputData = workDataOf(
            ScheduledMovieWorker.KEY_MOVIE_ID to movieId,
            ScheduledMovieWorker.KEY_MOVIE_TITLE to "Test Movie",
            ScheduledMovieWorker.KEY_MOVIE_POSTER to "",
            ScheduledMovieWorker.KEY_MOVIE_RESULT_JSON to "{}",
            ScheduledMovieWorker.KEY_SCHEDULED_DATE to System.currentTimeMillis()
        )
        val worker = TestListenableWorkerBuilder<ScheduledMovieWorker>(context)
            .setInputData(inputData)
            .build()

        worker.doWork() // This takes 10 seconds
        // the movie will be deleted after 10 sec

        // 3. Verify if it was deleted
        val result = db.dao.getScheduledMovieById(movieId)
        db.close()

        // This assertion will FAIL if the worker's internal DB logic crashed silently
        assertNull("The movie should have been deleted from the database", result)
    }


    @Test
    fun scheduledMovieWorker_shouldFailOnInvalidInput() = runBlocking {
        // 1. Provide invalid input (Missing ID)
        val inputData = workDataOf(
            ScheduledMovieWorker.KEY_MOVIE_TITLE to "Test"
            // KEY_MOVIE_ID is missing, so getInt returns -1
        )

        val worker = TestListenableWorkerBuilder<ScheduledMovieWorker>(context)
            .setInputData(inputData)
            .build()

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

        val worker = TestListenableWorkerBuilder<ScheduledMovieWorker>(context)
            .setInputData(inputData)
            .build()

        // 2. Run the Worker
        val result = worker.doWork()

        // 3. Verify it still returns success (it should show notification then skip deletion)
        assertEquals(ListenableWorker.Result.success(), result)
    }

}
