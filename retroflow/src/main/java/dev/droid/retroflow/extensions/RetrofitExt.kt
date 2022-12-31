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

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import dev.droid.retroflow.RetroFlow
import dev.droid.retroflow.annotations.RetroMock
import dev.droid.retroflow.mock.MockHeader
import dev.droid.retroflow.mock.MockMode
import dev.droid.retroflow.resource.Resource
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Converter
import retrofit2.Response
import java.io.IOException
import java.lang.reflect.Type

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
fun <S, E> Response<S>.errorBody(converter: Converter<ResponseBody, E>): E? {
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

/**
 * Helper function to return mock response.
 *
 * @param retroMock: The [RetroMock] annotation instance
 * @param responseType: The successful response data type
 *
 * @return Retrofit [Response] with mock data.
 */
internal fun <T> executeMock(
    retroMock: RetroMock,
    responseType: Type
): Response<T> {
    val context = RetroFlow.context
    checkNotNull(context) {
        "Context must be provided to enable mock!"
    }

    val mockJson = context.readMockJson(retroMock)
    val gson = Gson()

    return if (retroMock.mode == MockMode.SUCCESS) {
        val success = mockJson.get("success").asJsonObject
        Response.success(
            gson.fromJson<T>(success.get("body"), responseType),
            success.asOkHttpResponse(gson)
        )
    } else {
        val error = mockJson.get("error").asJsonObject
        Response.error(
            error.get("body").asResponseBody(),
            error.asOkHttpResponse(gson)
        )
    }
}

/**
 * Helper function to convert the success and error json elements from the mock json file to an
 * instance of [okhttp3.Response].
 */
private fun JsonObject.asOkHttpResponse(gson: Gson): okhttp3.Response {
    return okhttp3.Response.Builder()
        .code(get("code").asInt)
        .apply {
            gson.fromJson(get("headers"), listOf<MockHeader>().javaClass).forEach {
                addHeader(it.name, it.value)
            }
        }
        .body(get("body").asResponseBody())
        .build()
}

/**
 * Helper function to convert the 'success.body' or 'error.body' elements from the mock json
 * file to [ResponseBody].
 */
private fun JsonElement.asResponseBody(): ResponseBody {
    return asString.toResponseBody("application/json".toMediaType())
}