package dev.droid.retroflow

import dev.droid.retroflow.extensions.stringCopy
import okhttp3.Headers
import okhttp3.ResponseBody
import retrofit2.Response

/**
 * @author rkpattanaik (Rajesh Pattanaik)
 *
 * [Resource] is a discriminated union that encapsulates the outcome of a network request, made
 * using Retrofit, with a [Success] of type [S] or a [Failure] of type [E].
 */
sealed class Resource<S, E> {
    /**
     * Represents successful response of a Retrofit call.
     * (i.e. status code in the range of 2XX)
     *
     * @param response: [Response] from the Retrofit request call.
     *
     * @property code: The status code of the Retrofit call.
     * @property headers: Headers from the Retrofit call [Response].
     * @property raw: The raw OkHttp Response object available in Retrofit [Response].
     * @property data: The deserialized response body of a successful Retrofit call. [data] will be
     * null in case the response is successful(i.e. status code 20X) but without a body.
     */
    data class Success<S>(val response: Response<S>): Resource<S, Nothing>() {
        val code: Int = response.code()
        val headers: Headers = response.headers()
        val raw: okhttp3.Response = response.raw()
        val data: S? = response.body()

        override fun toString(): String = "[Success](data = ${data ?: "Empty Response Body"})"
    }

    /**
     * Represents Retrofit request failure. The [Failure] can be either an [Error] or
     * an [Exception].
     *
     * [Failure.Error] Represents an API error response i.e for status codes in the range
     * of 4XX or 5XX.
     * [Failure.Exception] Represents an unexpected exception occurred at client side while
     * creating the request or processing the response. (e.g. network unavailable or
     * connection timeout or exception during serialization / deserialization of [ResponseBody])
     */
    sealed class Failure<E>: Resource<Nothing, E>() {
        /**
         * Represents API error response for status codes in the range of 40X or 50X.
         *
         * @param response: [Response] from the Retrofit call.
         * @param data: The deserialized error body of type [E] from the Retrofit call.
         * [data] will be null if there is no error body.
         *
         * @property code: The status code of the Retrofit call.
         * @property headers: The OkHttp Headers from the Retrofit call [Response].
         * @property raw: The raw OkHttp Response from the Retrofit call [Response].
         * @property errorBody: The error [ResponseBody] from Retrofit call [Response]. [errorBody]
         * can be null if there is no error body in the response.
         */
        data class Error<E>(val response: Response<*>, val data: E?): Failure<E>() {
            val code: Int = response.code()
            val headers: Headers = response.headers()
            val raw: okhttp3.Response = response.raw()
            val errorBody: ResponseBody? = response.errorBody()

            override fun toString(): String = "[Error](error = ${data ?: errorBody?.stringCopy()})"
        }

        /**
         * Represents an unexpected exception occurred at client side while creating the request
         * or processing the response. (e.g. network unavailable or connection timeout or
         * exception during serialization / deserialization of the [ResponseBody])
         *
         * @param exception: The exception thrown as [Throwable]
         *
         * @property message: The localized message from the [Throwable]
         */
        data class Exception(val exception: Throwable): Failure<Nothing>() {
            val message: String? = exception.localizedMessage

            override fun toString(): String = "[Exception](message = $message)"
        }
    }
}

/**
 * Returns 'true' if this instance represents a successful outcome(i.e [Resource.Success]).
 */
inline val Resource<*, *>.isSuccess: Boolean
get() = this is Resource.Success

/**
 * Returns 'true' if this instance represents a failure outcome(i.e [Resource.Failure]).
 */
inline val Resource<*, *>.isFailure: Boolean
get() = this is Resource.Failure

/**
 * Returns 'true' if this instance is a failure outcome of type [Resource.Failure.Error].
 */
inline val Resource<*, *>.isError: Boolean
get() = this is Resource.Failure.Error

/**
 * Returns 'true' if this instance is a failure outcome of type [Resource.Failure.Exception].
 */
inline val Resource<*, *>.isException: Boolean
get() = this is Resource.Failure.Exception

/**
 * A scope function that takes an action lambda to handle the successful response if the request succeeds.
 *
 * @param action: A lambda to be performed on the [Resource.Success] if the request succeeds.
 *
 * @return The original [Resource] unchanged.
 */
inline fun <S> Resource<S, *>.onSuccess(
    crossinline action: Resource.Success<S>.() -> Unit
): Resource<S, *> {
    if (this is Resource.Success) action(this)
    return this
}

/**
 * A scope function that takes an [action] lambda to handle the request failure.
 *
 * @param action: A lambda to be performed on the [Resource.Failure] if the request fails.
 *
 * @return The original [Resource] unchanged.
 */
inline fun <E> Resource<*, E>.onFailure(
    crossinline action: Resource.Failure<E>.() -> Unit
): Resource<*, E> {
    if (this is Resource.Failure) action(this)
    return this
}

/**
 * A scope function that takes an [action] lambda to handle the API Error response (i.e. status code
 * in 4XX to 5XX). Check [Resource.Failure.Error].
 *
 * @param action: A lambda to be performed on the [Resource.Failure.Error] if the request fails with
 * an error response (i.e. status code in 4XX to 5XX).
 *
 * @return The original [Resource] unchanged.
 */
inline fun <E> Resource<*, E>.onError(
    crossinline action: Resource.Failure.Error<E>.() -> Unit
): Resource<*, E> {
    if (this is Resource.Failure.Error) action(this)
    return this
}

/**
 * A scope function that takes an [action] lambda to handle the request failure due to an unexpected
 * exception occurred at client side. Check [Resource.Failure.Exception].
 *
 * @param action: A lambda to be performed on the [Resource.Failure.Exception] if the request fails
 * due to an unexpected exception occurred at client side.
 *
 * @return The original [Resource] unchanged.
 */
inline fun Resource<*, *>.onException(
    crossinline action: Resource.Failure.Exception.() -> Unit
): Resource<*, *> {
    if (this is Resource.Failure.Exception) action(this)
    return this
}

/**
 * A scope function that takes [onSuccess], [onError], and [onException] lambda functions all at once
 * and invokes them on request completion either successfully as [Resource.Success] or fails as
 * [Resource.Failure.Error] or [Resource.Failure.Exception].
 *
 * @param onSuccess: Lambda to invoke for [Resource.Success]
 * @param onError: Lambda to invoke for [Resource.Failure.Error]
 * @param onException: Lambda to invoke for [Resource.Failure.Exception]
 *
 * @return The original [Resource]
 */
inline fun <S, E> Resource<S, E>.onResult(
    crossinline onSuccess: Resource.Success<S>.() -> Unit,
    crossinline onError: Resource.Failure.Error<E>.() -> Unit,
    crossinline onException: Resource.Failure.Exception.() -> Unit
): Resource<S, E> = apply {
    this.onSuccess(onSuccess)
    this.onError(onError)
    this.onException(onException)
}

/**
 * Returns the [data] of type [S] if the response is successful and has the response body.
 * Otherwise returns 'null'.
 *
 * @return The [data] from [Resource.Success] or null.
 */
fun <S> Resource<S, *>.getOrNull(): S? = when (this) {
    is Resource.Success -> data
    else -> null
}

/**
 * Returns the [data] of type [S] if the response is successful and has the response body.
 * Otherwise returns the [defaultValue].
 *
 * @param defaultValue: The value of type [S] to be returned if [Resource.Success.data] is null.
 *
 * @return The [data] from [Resource.Success] or [defaultValue].
 */
fun <S> Resource<S, *>.getOrElse(defaultValue: S): S = getOrNull() ?: defaultValue

/**
 * Returns the [data] of type [S] if the response is successful and has the response body.
 * Otherwise returns the result of [defaultValue] lambda invocation.
 *
 * @param defaultValue: A lambda that executes some code and then returns the value of type [S] on
 * invocation. The result of this lambda invocation will be returned if [Resource.Success.data]
 * is null.
 *
 * @return The [data] from [Resource.Success] or result from [defaultValue] lambda invocation.
 */
fun <S> Resource<S, *>.getOrElse(defaultValue: () -> S): S = getOrNull() ?: defaultValue()

/**
 * Returns the [data] of type [S] if the response is successful and has the response body.
 * Otherwise throws exception.
 *
 * @return The [data] from [Resource.Success]
 */
fun <S> Resource<S, *>.getOrThrow(): S = getOrNull() ?: throw Throwable(toString())

/**
 * Returns the [data] of type [S] if the response is successful and has the response body.
 * Otherwise throws exception provided by the caller.
 *
 * @param e: The [Throwable] that needs to be thrown if [Resource.Success.data] is null
 *
 * @return The [data] from [Resource.Success]
 */
fun <S> Resource<S, *>.getOrThrow(e: Throwable): S = getOrNull() ?: throw e

/**
 * Returns the [data] of type [S] if the response is successful and has the response body.
 * Otherwise throws exception provided by the caller.
 *
 * @param e: The [Exception] that needs to be thrown if [Resource.Success.data] is null
 *
 * @return The [data] from [Resource.Success]
 */
fun <S> Resource<S, *>.getOrThrow(e: Exception): S = getOrNull() ?: throw e

/**
 * Returns the error [data] of type [E] if the response is an API error and has the error body.
 * Otherwise returns 'null'.
 *
 * @return The [data] from [Resource.Failure.Error] or null.
 */
fun <E> Resource<*, E>.errorOrNull(): E? = when (this) {
    is Resource.Failure.Error -> data
    else -> null
}

/**
 * Returns the [Throwable] if an unexpected exception occurred at client side while creating
 * the request or processing the response. Check [Resource.Failure.Exception]!
 * Otherwise returns 'null'.
 *
 * @return The [Throwable] from [Resource.Failure.Exception] or null.
 */
fun Resource<*, *>.exceptionOrNull(): Throwable? = when (this) {
    is Resource.Failure.Exception -> exception
    else -> null
}

/**
 * Returns the error message for [Resource.Failure]
 *
 * @return The error message or null.
 */
fun Resource.Failure<*>.messageOrNull(): String? = when (this) {
    is Resource.Failure.Error -> errorBody?.stringCopy() ?: response.message()
    is Resource.Failure.Exception -> message
}

/**
 * Maps [Resource] of success type [S] to [Resource] of result type [R] if the [Resource]
 * is [Resource.Success].
 *
 * @param mapper: A mapper lambda that transforms the success type [S] to [R].
 *
 * @return [Resource] of type [R].
 */
@Suppress("UNCHECKED_CAST")
inline fun <S, R> Resource<S, *>.map(
    crossinline mapper: S.() -> R
): Resource<R, *> {
    if (this is Resource.Success) {
        return try {
            Resource.Success(Response.success(mapper(data!!)))
        } catch (e: Throwable) {
            Resource.Failure.Exception(e) as Resource<R, *>
        }
    }
    return this as Resource<R, *>
}