package com.mvukosav.verage.data.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.mvukosav.verage.common.Constants.BASE_URL
import com.mvukosav.verage.data.RepositoriesApi
import com.mvukosav.verage.data.repository.RepositoryImpl
import com.mvukosav.verage.domain.Repository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindSquareRepository(
        repositoryImpl: RepositoryImpl
    ): Repository

    companion object {
        @Provides
        @Singleton
        fun provideOkHttpClient(): OkHttpClient {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            return OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build()
        }

        @Provides
        @Singleton
        fun provideGson(): Gson = GsonBuilder().create()

        @Provides
        @Singleton
        fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        @Provides
        @Singleton
        fun provideRepositoriesApi(retrofit: Retrofit): RepositoriesApi =
            retrofit.create(RepositoriesApi::class.java)
    }
}
