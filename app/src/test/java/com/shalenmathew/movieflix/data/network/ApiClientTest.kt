package com.shalenmathew.movieflix.data.network




import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApiClientTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiClient: ApiClient

    @Before
    fun setUp(){

        mockWebServer = MockWebServer()

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val originalUrl = originalRequest.url

                val newUrl = originalUrl.newBuilder()
                    .addQueryParameter("api_key", "test_api_key")
                    .build()

                val newRequest = originalRequest.newBuilder()
                    .url(newUrl)
                    .build()

                chain.proceed(newRequest)
            }
            .build()

        apiClient = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(okHttpClient) // Use the client with our interceptor
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiClient::class.java)

    }

    @After
    fun tearDown(){
       mockWebServer.shutdown()
    }

    // instead of testing whole api we will just test diff category that covers all api

    // Category A: Verifies interceptors, default parameters, and standard list parsing.
    @Test
    fun `getPopularMoviesApiCall returns success `() = runTest {

        val mockResponse = MockResponse()
        mockResponse.apply {
            setResponseCode(200)
            setBody(
                """
                {
                  "page": 1,
                  "results": [
                    {
                      "id": 123,
                      "title": "Movie1",
                      "poster_path": "/path.jpg"
                    }
                  ]
                }
            """.trimIndent()
            )
        }


        mockWebServer.enqueue(mockResponse)

        val response = apiClient.getPopularMoviesApiCall(lang = "en-US", page =1)

        assertTrue(response.isSuccessful) // checking is the response true ?

        val body = response.body()
        assertEquals(1,body?.results?.size)

        assertEquals("Movie1", body?.results?.get(0)?.title)


        //checking url path
        val request = mockWebServer.takeRequest()
        val path = request.requestUrl.toString()

        println("DEBUG: The Actual Path was: $path")

        assertTrue(path.contains("3/movie/popular"),"Endpoint path is wrong")
        assertTrue(path.contains("api_key=test_api_key"),"API Key missing from Interceptor")
        assertTrue(path.contains("language=en-US"), "Default language missing")
        assertTrue(path.contains("page=1"), "Default page missing")

    }


    // Category B: Verifies that dynamic path variables like movie_id are correctly injected into the URL.
    @Test
    fun `fetchMovieTrailerApiCall sends correct movie_id in path`() = runTest {

        val movieId = 500

        val mockResponse = MockResponse()
        mockResponse.setResponseCode(200)
        mockResponse.setBody(
            """
        {
          "id": $movieId,
          "results": [
            {
              "id": "vid_123",
              "key": "dQw4w9WgXcQ",
              "name": "Official Trailer",
              "site": "YouTube",
              "type": "Trailer"
            }
          ]
        }
    """.trimIndent()
        )

        mockWebServer.enqueue(mockResponse)

        val result = apiClient.fetchMovieTrailerApiCall(movieId = movieId)


        val body = result.body()

        assertTrue(result.isSuccessful)
        assertNotNull(body)
        assertEquals(500,body?.id)


        val request = mockWebServer.takeRequest()
        val path = request.requestUrl.toString()


        assertTrue(path.contains("3/movie/$movieId/videos"), "Path does not contain correct movie_id")

    }

    // Category C: Verifies that hardcoded complex query parameters and business filters are correct.
    @Test
    fun `getBollywoodMoviesApiCall sends all complex query parameters`() = runTest {
        // 1. Prepare
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        // 2. Act (Calling with defaults)
        apiClient.getBollywoodMoviesApiCall()

        // 3. Assert (The Spine Check)
        val request = mockWebServer.takeRequest()
        val url = request.requestUrl.toString()

        // Verify the "Magic Strings" that define your Bollywood section
        assertTrue(url.contains("region=IN"), "Region should be IN")
        assertTrue(url.contains("with_original_language=hi"), "Original language should be hi")
        assertTrue(url.contains("language=en-IN"), "Language should be hi-IN")
        assertTrue(url.contains("primary_release_date.gte=2012-08-01"), "Release date filter missing")
    }


    // Category D: Verifies correct parsing for single-object response models like actor details.
    @Test
    fun `fetchActorDetailApiCall parses actor details correctly`() = runTest {
        // 1. Prepare
        val actorId = 123
        val mockResponse = MockResponse()
        mockResponse.setResponseCode(200)
        mockResponse.setBody("""
        {
          "id": $actorId,
          "name": "Tom Holland",
          "biography": "English actor...",
          "birthday": "1996-06-01"
        }
    """.trimIndent())
        mockWebServer.enqueue(mockResponse)

        // 2. Act
        val response = apiClient.fetchActorDetailApiCall(personId = actorId)

        // 3. Assert
        assertTrue(response.isSuccessful)
        val body = response.body()
        assertEquals("Tom Holland", body?.name)
        assertEquals(actorId, body?.id)
    }

}