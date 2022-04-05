package app.onem.data.di

import app.onem.data.network.api.ApiService
import app.onem.data.repositories.DataRepositoryImpl
import app.onem.data.repositories.KioskRepositoryImpl
import app.onem.domain.repositories.DataRepository
import app.onem.domain.repositories.KioskRepository
import app.onem.domain.repositories.TokenProvider
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module
import retrofit2.Retrofit
import timber.log.Timber

val repositoriesModule = module {

    single {
        val errorHandler = CoroutineExceptionHandler { _, throwable ->
            Timber.e(throwable)
        }
        CoroutineScope(SupervisorJob() + errorHandler)
    }
    single<KioskRepository> { KioskRepositoryImpl(get()) }
    single<DataRepository> { DataRepositoryImpl(get()) }
    single { TokenProvider(get()) }
    single { get<Retrofit>().create(ApiService::class.java) }
}
