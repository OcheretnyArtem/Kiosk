package app.onem.domain.base

interface BaseUseCase<R, T> {

    suspend operator fun invoke(params: R? = null): T
}
