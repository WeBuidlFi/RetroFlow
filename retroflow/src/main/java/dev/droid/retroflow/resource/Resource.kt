/*
 *    Copyright © 2022 WeBuidl.com and Droid.dev.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package dev.droid.retroflow.resource

import dev.droid.retroflow.extensions.stringCopy
import dev.droid.retroflow.resource.Resource.Success
import dev.droid.retroflow.resource.Resource.Failure
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
     * @property data: The deserialized response body of a successful Retrofit call.
     */
    data class Success<S>(val response: Response<S>, val data: S): Resource<S, Nothing>() {
        val code: Int = response.code()
        val headers: Headers = response.headers()
        val raw: okhttp3.Response = response.raw()

        override fun toString(): String = "[Success](data = $data)"
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
get() = this is Success

/**
 * Returns 'true' if this instance represents a failure outcome(i.e [Resource.Failure]).
 */
inline val Resource<*, *>.isFailure: Boolean
get() = this is Failure

/**
 * Returns 'true' if this instance is a failure outcome of type [Resource.Failure.Error].
 */
inline val Resource<*, *>.isError: Boolean
get() = this is Failure.Error

/**
 * Returns 'true' if this instance is a failure outcome of type [Resource.Failure.Exception].
 */
inline val Resource<*, *>.isException: Boolean
get() = this is Failure.Exception

/**
 * Returns the [Resource.Success.data] of type [S] if the response is successful and has the
 * response body. Otherwise returns 'null'.
 *
 * @return [Resource.Success.data]
 */
fun <S> Resource<S, *>.getOrNull(): S? = when (this) {
    is Success -> data
    else -> null
}

/**
 * Returns the [Resource.Success.data] of type [S] if the response is successful and has the
 * response body. Otherwise returns the [defaultValue].
 *
 * @param defaultValue: The value of type [S] to be returned if [Resource.Success.data] is null.
 *
 * @return [Resource.Success.data] if not null, or else [defaultValue].
 */
fun <S> Resource<S, *>.getOrElse(defaultValue: S): S = getOrNull() ?: defaultValue

/**
 * Returns the [Resource.Success.data] of type [S] if the response is successful and has the
 * response body. Otherwise returns the result of [defaultValue] lambda invocation.
 *
 * @param defaultValue: A lambda that executes some code and then returns the value of type [S] on
 * invocation. The result of this lambda invocation will be returned if [Resource.Success.data]
 * is null.
 *
 * @return [Resource.Success.data] if not null, or result from [defaultValue] lambda invocation.
 */
fun <S> Resource<S, *>.getOrElse(defaultValue: () -> S): S = getOrNull() ?: defaultValue()

/**
 * Returns the [Resource.Success.data] of type [S] if the response is successful and has the
 * response body. Otherwise throws exception.
 *
 * @return [Resource.Success.data] if not null.
 */
fun <S> Resource<S, *>.getOrThrow(): S = getOrNull() ?: throw Throwable(toString())

/**
 * Returns the [Resource.Success.data] of type [S] if the response is successful and has the
 * response body. Otherwise throws exception provided by the caller.
 *
 * @param e: The [Throwable] that needs to be thrown if [Resource.Success.data] is null
 *
 * @return [Resource.Success.data] if not null.
 */
fun <S> Resource<S, *>.getOrThrow(e: Throwable): S = getOrNull() ?: throw e

/**
 * Returns the [Resource.Success.data] of type [S] if the response is successful and has the
 * response body. Otherwise throws exception provided by the caller.
 *
 * @param e: The [Exception] that needs to be thrown if [Resource.Success.data] is null
 *
 * @return [Resource.Success.data] if not null.
 */
fun <S> Resource<S, *>.getOrThrow(e: Exception): S = getOrNull() ?: throw e

/**
 * Returns the error [Resource.Failure.Error.data] of type [E] if the response is an API error
 * and has the error body. Otherwise returns 'null'.
 *
 * @return [Resource.Failure.Error.data]
 */
fun <E> Resource<*, E>.errorOrNull(): E? = when (this) {
    is Failure.Error -> data
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
    is Failure.Exception -> exception
    else -> null
}

/**
 * Returns the error message for [Resource.Failure]
 *
 * @return The error message or null.
 */
fun Failure<*>.messageOrNull(): String? = when (this) {
    is Failure.Error -> errorBody?.stringCopy() ?: response.message()
    is Failure.Exception -> message
}

/**
 * A scope function that takes an action lambda to handle the successful response if the request
 * succeeds.
 *
 * @param action: A lambda to be performed on the [Resource.Success] if the request succeeds.
 *
 * @return The original [Resource] unchanged.
 */
inline fun <S> Resource<S, *>.onSuccess(
    crossinline action: Success<S>.() -> Unit
): Resource<S, *> {
    if (this is Success) action(this)
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
    crossinline action: Failure<E>.() -> Unit
): Resource<*, E> {
    if (this is Failure) action(this)
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
    crossinline action: Failure.Error<E>.() -> Unit
): Resource<*, E> {
    if (this is Failure.Error) action(this)
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
    crossinline action: Failure.Exception.() -> Unit
): Resource<*, *> {
    if (this is Failure.Exception) action(this)
    return this
}

/**
 * A scope function that takes [onSuccess], [onError], and [onException] lambda functions to
 * handle [Resource.Success], [Resource.Failure.Error], or [Resource.Failure.Exception] outcome.
 *
 * @param onSuccess: Lambda to handle [Resource.Success]
 * @param onError: Lambda to handle [Resource.Failure.Error]
 * @param onException: Lambda to handle [Resource.Failure.Exception]
 *
 * @return The original [Resource]
 */
inline fun <S, E> Resource<S, E>.onResult(
    crossinline onSuccess: Success<S>.() -> Unit,
    crossinline onError: Failure.Error<E>.() -> Unit,
    crossinline onException: Failure.Exception.() -> Unit
): Resource<S, E> = apply {
    this.onSuccess(onSuccess)
    this.onError(onError)
    this.onException(onException)
}

/**
 * A scope function to operate on the [Resource] outcomes (i.e [Resource.Success],
 * [Resource.Failure.Error], and [Resource.Failure.Exception]) by providing a [ResourceOperator]
 *
 * @param operator: The [ResourceOperator] implementation to handle [Resource] outcomes.
 */
fun <S, E> Resource<S, E>.operator(operator: ResourceOperator<S, E>) {
    when (this) {
        is Success -> operator.onSuccess(this)
        is Failure.Error -> operator.onError(this)
        is Failure.Exception -> operator.onException(this)
    }
}

/**
 * A scope function to operate on the [Resource] outcomes (i.e [Resource.Success],
 * [Resource.Failure.Error], and [Resource.Failure.Exception]) by providing a list of
 * [ResourceOperator]s.
 *
 * @param operators: The list of [ResourceOperator]s to operate on the [Resource] outcomes.
 */
internal fun Resource<Any, Any>.operators(operators: List<ResourceOperator<Any, Any>>) {
    operators.forEach { operator(it) }
}

/**
 * Maps [Resource] of success type [S1] to [Resource] of result type [S2] if the [Resource]
 * is [Resource.Success].
 *
 * @param mapper: A mapper lambda that transforms the success type [S1] to [S2].
 *
 * @return [Resource] of type [S2].
 */
@Suppress("UNCHECKED_CAST")
inline fun <S1, S2> Resource<S1, *>.mapSuccess(
    crossinline mapper: S1.() -> S2
): Resource<S2, *> {
    if (this is Success) {
        return try {
            val mapped = mapper(data)
            Success(Response.success(mapped), mapped)
        } catch (e: Throwable) {
            Failure.Exception(e) as Resource<S2, *>
        }
    }
    return this as Resource<S2, *>
}

/**
 * Maps [Resource] of error type [E1] to [Resource] of error type [E2] if the [Resource]
 * is [Resource.Failure.Error].
 *
 * @param mapper: A mapper lambda that transforms the error type [E1] to [E2].
 *
 * @return [Resource] of error type [E2].
 */
@Suppress("UNCHECKED_CAST")
inline fun <E1, E2> Resource<*, E1>.mapError(
    crossinline mapper: E1.() -> E2
): Resource<*, E2> {
    if (this is Failure.Error) {
        return try {
            Failure.Error(response, mapper(data!!))
        } catch (e: Throwable) {
            Failure.Exception(e) as Resource<*, E2>
        }
    }
    return this as Resource<*, E2>
}