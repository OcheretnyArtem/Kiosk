@file:Suppress("unused")

package app.onem.domain.core.result

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@Suppress("ExpressionBodySyntax")
fun <T> Result<T>.isSuccess(): Boolean {
    return this is Result.Success
}

@Suppress("ExpressionBodySyntax")
fun <T> Result<T>.asSuccess(): Result.Success<T> {
    return this as Result.Success<T>
}

@OptIn(ExperimentalContracts::class)
fun <T> Result<T>.isFailure(): Boolean {
    contract {
        returns(true) implies (this@isFailure is Result.Failure<*>)
    }
    return this is Result.Failure<*>
}

@Suppress("ExpressionBodySyntax")
fun <T> Result<T>.asFailure(): Result.Failure<*> {
    return this as Result.Failure<*>
}

fun <T> Result<T>.toNullable(): T? {
    return when (this) {
        is Result.Success -> value
        is Result.Failure<*> -> null
    }
}

fun <T, R> Result<T>.map(transform: (value: T) -> R): Result<R> {
    return when (this) {
        is Result.Success -> Result.Success.Value(transform(value))
        is Result.Failure<*> -> this
    }
}

@Suppress("ExpressionBodySyntax")
fun <T, R> Result<T>.flatMap(transform: (T) -> Result<R>): Result<R> {
    return when (this) {
        is Result.Success -> transform(value)
        is Result.Failure<*> -> this
    }
}

fun <T> Result<List<T>>.checkIsEmpty(): Result<List<T>> =
    flatMap { list ->
        when {
            list.isEmpty() -> Result.Success.Empty
            else -> Result.Success.Value(list)
        }
    }

