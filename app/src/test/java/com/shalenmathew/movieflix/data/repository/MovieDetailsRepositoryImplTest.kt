package com.shalenmathew.movieflix.data.repository

import android.app.Application
import com.shalenmathew.movieflix.core.utils.NetworkResults
import com.shalenmathew.movieflix.data.local_storage.LocalDataSource
import com.shalenmathew.movieflix.data.local_storage.entity.HomeFeedEntity
import com.shalenmathew.movieflix.data.model.MovieResponseList
import com.shalenmathew.movieflix.data.remote.RemoteDataSource
import com.shalenmathew.movieflix.domain.model.HomeFeedData
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import retrofit2.Response
import kotlin.test.assertTrue


@RunWith(MockitoJUnitRunner::class)
class MovieDetailsRepositoryImplTest {

    @Mock
    lateinit var remoteDataSource: RemoteDataSource

    @Mock
    lateinit var localDataSource: LocalDataSource

    @Mock
    lateinit var application: Application

    private lateinit var repository: MovieDetailsRepositoryImpl


    @Before
    fun setUp(){
        repository = MovieDetailsRepositoryImpl(remoteDataSource,
            localDataSource, application, networkChecker = {true})
    }

    @Test
    fun `getHomeFeedData emits loading then success`() = runTest {

        val fakeMovies = MovieResponseList(
            results = emptyList(),
            page = null,
            total_pages = null,
            total_results = null
        )


        val successResponse = Response.success(fakeMovies)

        whenever(remoteDataSource.getUpcomingMovies()).thenReturn(successResponse)
        whenever(remoteDataSource.getTrendingMovies()).thenReturn(successResponse)
        whenever(remoteDataSource.getPopularMovies()).thenReturn(successResponse)
        whenever(remoteDataSource.getTopRatedTV()).thenReturn(successResponse)
        whenever(remoteDataSource.getNetflixShows()).thenReturn(successResponse)
        whenever(remoteDataSource.getAmazonPrimeShows()).thenReturn(successResponse)
        whenever(remoteDataSource.getBollywoodMovies()).thenReturn(successResponse)
        whenever(remoteDataSource.getNowPlayingMovies()).thenReturn(successResponse)


        val mockEntity = mock(HomeFeedEntity::class.java)
        val homeFeedData = mock(HomeFeedData::class.java)

        whenever(localDataSource.readHomeFeedData()).thenReturn(mockEntity)
        whenever(mockEntity.toHomeFeedData()).thenReturn(homeFeedData)


        val emission =mutableListOf<NetworkResults<HomeFeedData>>()

        repository.getHomeFeedData().collect {
            emission.add(it)
        }

        assertTrue (emission[0] is NetworkResults.Loading)
        assertTrue(emission[1] is NetworkResults.Success)

        // Verify the Orchestration Spine
        verify(localDataSource).deleteAllHomeFeedData()
        verify(localDataSource).insertHomeFeedData(any())
        verify(localDataSource).readHomeFeedData()
    }


    @Test
    fun `getHomeFeedData skips network and reads from DB when offline`() = runTest {

        val offlineRepo = MovieDetailsRepositoryImpl(
            remoteDataSource, localDataSource, application,
            networkChecker = { false } // Force offline
        )

        val mockEntity = mock(HomeFeedEntity::class.java)
        val homeFeedData = mock(HomeFeedData::class.java)

        // STUB BEFORE ACTING
        whenever(localDataSource.readHomeFeedData()).thenReturn(mockEntity)
        whenever(mockEntity.toHomeFeedData()).thenReturn(homeFeedData)

        offlineRepo.getHomeFeedData().collect {  }

        // Verify that we NEVER touched the remoteDataSource because we were offline
        verifyNoInteractions(remoteDataSource)

        verify(localDataSource).readHomeFeedData()

    }



}
