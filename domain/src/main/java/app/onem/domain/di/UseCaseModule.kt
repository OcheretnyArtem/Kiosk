package app.onem.domain.di

import app.onem.domain.usecase.api.CapturePaymentIntentUseCase
import app.onem.domain.usecase.api.CreateLocationUseCase
import app.onem.domain.usecase.api.CreatePaymentIntentUseCase
import app.onem.domain.usecase.api.GetTokenUseCase
import app.onem.domain.usecase.api.SearchShopsUseCase
import app.onem.domain.usecase.reader.GetSavedReaderSerialNumberUseCase
import app.onem.domain.usecase.reader.SaveSelectedReaderSerialNumberUseCase
import app.onem.domain.usecase.sensors.GetSavedLocationIdUseCase
import app.onem.domain.usecase.sensors.SaveLocationIdUseCase
import app.onem.domain.usecase.shop.GetSelectedShopIdUseCase
import app.onem.domain.usecase.shop.GetSelectedShopUseCase
import app.onem.domain.usecase.shop.SaveSelectedShopUseCase
import org.koin.core.component.KoinApiExtension
import org.koin.dsl.module

@OptIn(KoinApiExtension::class)
val useCasesModule = module {
    factory { GetSelectedShopUseCase(get(), get()) }
    single { SaveSelectedShopUseCase(get(), get()) }
    single { GetTokenUseCase(get()) }
    single { SaveSelectedReaderSerialNumberUseCase(get()) }
    single { GetSavedReaderSerialNumberUseCase(get()) }
    single { CreatePaymentIntentUseCase(get()) }
    single { CapturePaymentIntentUseCase(get()) }
    single { CreateLocationUseCase(get()) }
    single { SaveLocationIdUseCase(get()) }
    single { GetSavedLocationIdUseCase(get()) }
    single { SearchShopsUseCase(get()) }
    single { GetSelectedShopIdUseCase(get()) }
}
