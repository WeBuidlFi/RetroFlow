package dev.droid.retroflow

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

        override fun toString(): String = "[$resource:Success](data = $data)"
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

            override fun toString(): String = "[$resource:Error](error = ${data ?: errorBody})"
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
        data class Exception(val exception: Throwable): Resource<Nothing, Nothing>() {
            val message: String? = exception.localizedMessage

            override fun toString(): String = "[$resource:Exception](message = $message)"
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
 * Returns the 'data' of type [S] if the response is successful and has the response body.
 * Otherwise returns 'null'.
 */
fun <S> Resource<S, *>.getOrNull(): S? = when {
    isSuccess -> (this as Resource.Success<S>).data
    else -> null
}

/**
 * Returns the 'error data' of type [E] if the response is an API error and has the error body.
 * Otherwise returns 'null'.
 */
fun <E> Resource<*, E>.errorOrNull(): E? = when {
    isError -> (this as Resource.Failure.Error<E>).data
    else -> null
}

/**
 * Returns the [Throwable] if an unexpected exception occurred at client side while creating
 * the request or processing the response. Check [Resource.Failure.Exception]!
 * Otherwise returns 'null'.
 */
fun Resource<*, *>.exceptionOrNull(): Throwable? = when {
    isException -> (this as Resource.Failure.Exception).exception
    else -> null
}

private const val resource = "RetroFlow:Resource"