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

package dev.droid.retroflow.mock

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import dev.droid.retroflow.RetroFlow
import dev.droid.retroflow.annotations.RetroMock
import dev.droid.retroflow.extensions.readJson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response
import java.lang.reflect.Type

/**
 * Helper function to return mock response.
 *
 * @param responseType: The successful response data type
 *
 * @return Retrofit [Response] with mock data.
 */
internal fun <T> RetroMock.mock(
    responseType: Type
): Response<T> {
    val context = RetroFlow.context
    checkNotNull(context) {
        "Context must be provided to enable mock!"
    }

    val mockJson = when (source) {
        MockSource.ASSET -> context.readJson(mockAssetPath)
        else -> context.readJson(mockResId)
    }
    val gson = Gson()

    return if (mode == MockMode.SUCCESS) {
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