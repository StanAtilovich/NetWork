package ru.netology.network.api

import com.google.android.datatransport.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import ru.netology.network.auth.AppAuth
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object ApiServiceModule {
    @Provides
    @Singleton
    fun provideApiService(auth: AppAuth): ApiService {
        return retrofit(okhttp(loggingInterceptor(), authInterceptor(auth)))
            .create(ApiService::class.java)
    }
}

fun loggingInterceptor() = HttpLoggingInterceptor()
    .apply {
        if (BuildConfig.DEBUG) {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

fun authInterceptor(auth: AppAuth) = fun(chain: Interceptor.Chain): Response {
    auth.authStateFlow.value.token?.let { token ->
        val newRequest = chain.request().newBuilder()
            .addHeader("Authorization", token)
            .build()
        return chain.proceed(newRequest)
    }

    return chain.proceed(chain.request())
}