package com.bianca.moneymind.domain.model

/**
 * Wrapper for AI operation results
 */
sealed class AiResult<out T> {
    data class Success<T>(val data: T) : AiResult<T>()
    data class Error(val exception: AiException) : AiResult<Nothing>()

    fun <R> map(transform: (T) -> R): AiResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
    }

    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }

    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception
    }

    inline fun onSuccess(action: (T) -> Unit): AiResult<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onError(action: (AiException) -> Unit): AiResult<T> {
        if (this is Error) action(exception)
        return this
    }
}
