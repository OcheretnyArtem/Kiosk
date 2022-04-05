package app.onem.data.di

import app.onem.data.BuildConfig
import app.onem.domain.core.retrofit.ResultAdapterFactory
import com.google.gson.GsonBuilder
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.core.scope.Scope
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit

private const val CONNECT_TIMEOUT = 15L
private const val WRITE_TIMEOUT = 15L
private const val READ_TIMEOUT = 15L
private const val CACHE_MAX_SIZE = 10L * 1024 * 1024

val retrofitModule = module {
    single { Cache(androidApplication().cacheDir, CACHE_MAX_SIZE) }
    single { GsonBuilder().create() }
    single { retrofitHttpClient() }
    single { retrofitBuilder() }
//    single {
//        Interceptor { chain ->
//            chain.proceed(
//                chain.request().newBuilder().apply {
//                    header(
//                        "Accept", "application/json"
//                    )
//                }.build()
//            )
//        }
//    }
}

private fun Scope.retrofitBuilder(): Retrofit {
    return Retrofit.Builder()
        .baseUrl(BuildConfig.API_HOST)
        .addConverterFactory(GsonConverterFactory.create(get()))
        .addCallAdapterFactory(ResultAdapterFactory())
        .client(get())
        .build()
}

private fun Scope.retrofitHttpClient(): OkHttpClient {
    return OkHttpClient.Builder().apply {
        cache(get())
        connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
        writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
        readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
        retryOnConnectionFailure(true)
        addInterceptor(
            HttpLoggingInterceptor { message ->
                Timber.d(message)
            }.apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
        )
    }.build()
}
