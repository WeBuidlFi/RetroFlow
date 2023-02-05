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

package dev.droid.retroflow.extensions

import dev.droid.retroflow.resource.Resource
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Converter
import retrofit2.Response
import java.io.IOException

/**
 * Helper function to preserve the response body string value. The drawback of using
 * [ResponseBody.string] function is that the string is replaced with null once you call
 * the string() function. By using [ResponseBody.stringCopy] function, you'll get a copy of
 * the response body string and the original will be preserved.
 */
@Throws(IOException::class)
fun ResponseBody.stringCopy(): String {
    val peekSource = source().peek()
    val charset = contentType()?.charset(Charsets.UTF_8) ?: Charsets.UTF_8
    return peekSource.readString(charset)
}

/**
 * Helper function to return 'success' body of type [T]. This function helps to resolve
 * empty response body for status code 204 (NoContent) and 205 (ResetContent).
 *
 * @param converter: [Converter] to convert empty [ResponseBody] (for status code 204/205) to type [T]
 * @return [T]: Converted response body
 */
@Throws(IOException::class)
fun <T> Response<T>.successBody(converter: Converter<ResponseBody, T>): T {
    val emptyBody = "{}".toResponseBody("application/json".toMediaType())
    return body() ?: converter.convert(emptyBody)!!
}

/**
 * Helper function to extract the error [ResponseBody] as type [E] if the response is not successful
 * and [Response.errorBody] is not null.
 *
 * @param converter: [Converter] to convert [Response.errorBody] to type [E]
 * @return [E]?: Converted error body.
 */
fun <E> Response<*>.errorBody(converter: Converter<ResponseBody, E>): E? {
    val errorBody = errorBody()
    return when {
        errorBody == null || errorBody.contentLength() == 0L -> null
        else -> try {
            converter.convert(errorBody)
        } catch (ex: Exception) {
            null
        }
    }
}

/**
 * Helper function to convert [Response] as [Resource].
 *
 * @param successBodyConverter: [Converter] to resolve empty response body for status code 204 & 205.
 * @param errorBodyConverter: [Converter] to convert [Response.errorBody] to type [E].
 */
@Suppress("UNCHECKED_CAST")
fun <S, E> Response<S>.asResource(
    successBodyConverter: Converter<ResponseBody, S>,
    errorBodyConverter: Converter<ResponseBody, E>
): Resource<S, E> = try {
    if (isSuccessful) {
        Resource.Success(this, successBody(successBodyConverter))
    } else {
        Resource.Failure.Error(this, errorBody(errorBodyConverter))
    }
} catch (e: Throwable) {
    Resource.Failure.Exception(e)
} as Resource<S, E>