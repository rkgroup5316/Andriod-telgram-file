package com.rkgroup.app.di

import com.rkgroup.app.data.network.TelegramApiConfig
import com.rkgroup.app.data.network.TelegramApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(TelegramApiConfig.API_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(TelegramApiConfig.API_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(TelegramApiConfig.API_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideTelegramApiService(okHttpClient: OkHttpClient): TelegramApiService {
        return Retrofit.Builder()
            .baseUrl(TelegramApiConfig.BASE_URL + TelegramApiConfig.BOT_TOKEN + "/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TelegramApiService::class.java)
    }
}