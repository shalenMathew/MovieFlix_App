package com.shalenmathew.movieflix.core.di

import android.app.Application
import android.content.Context
import com.shalenmathew.movieflix.BuildConfig
import com.shalenmathew.movieflix.core.utils.Constants
import com.shalenmathew.movieflix.core.utils.NetworkConnectivityObserver
import com.shalenmathew.movieflix.data.network.ApiClient
import com.shalenmathew.movieflix.data.remote.RemoteDataSource
import com.shalenmathew.movieflix.data.repository.SearchMovieMovieRepositoryImpl
import com.shalenmathew.movieflix.domain.repository.SearchMovieRepository
import com.shalenmathew.movieflix.domain.usecases.SearchMovie
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
object NetworkModule {

   @Singleton
   @Provides
   fun providesNetworkClient(@ApplicationContext context: Context):OkHttpClient
   {
       return OkHttpClient().newBuilder()
           .cache(Cache(context.cacheDir,(5 * 1024 * 1024).toLong())) // this will save the response from request in cache,
           // so the client dont have to send request everytime
           .addInterceptor(
               HttpLoggingInterceptor().apply {
                   level = if (BuildConfig.DEBUG) Level.BODY else Level.NONE // this is used to get the debug information
               // when request is sent
               // and the also provides the data of the response
               }
           )
           .addInterceptor{chain ->

               // adding api key before sending request to the server
               val request = chain.request().newBuilder()
               val  originalHttpUrl = chain.request().url

               val url = originalHttpUrl.newBuilder()
                   .addQueryParameter("api_key",BuildConfig.MOVIE_API_KEY)
                   .build()
               request.url(url)
               return@addInterceptor chain.proceed(request.build())
           }.addInterceptor{chain ->
               var request = chain.request()
               request = request.newBuilder().header("Cache-Control", "public, max-age=" + 60 * 5)
                   .build()
               chain.proceed(request)
           }
           .connectTimeout(10,TimeUnit.SECONDS)
           .retryOnConnectionFailure(true)
           .readTimeout(10,TimeUnit.SECONDS)
           .build()
}

    @Provides
    @Singleton
    fun providesGsonFactory():GsonConverterFactory{
        return GsonConverterFactory.create()
    }

@Provides
@Singleton
fun providesRetrofitInstance(okHttpClient: OkHttpClient,gsonConverterFactory: GsonConverterFactory):Retrofit{
    return Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(gsonConverterFactory)
        .build()
}

    @Provides
    @Singleton
    fun providesApiClient(retrofit: Retrofit):ApiClient{
        return retrofit.create(ApiClient::class.java)
    }

    @Provides
    @Singleton
    fun providesSearchMovieRepo(application: Application,remoteDataSource: RemoteDataSource):SearchMovieRepository{
        return SearchMovieMovieRepositoryImpl(application,remoteDataSource)
    }

    @Provides
    @Singleton
    fun providesSearchMovieUsecase(searchMovieRepository: SearchMovieRepository):SearchMovie{
        return SearchMovie(searchMovieRepository)
    }

    @Provides
    @Singleton
    fun provideNetworkConnectivityObserver(
        @ApplicationContext context: Context
    ): NetworkConnectivityObserver {
        return NetworkConnectivityObserver(context)
    }

}