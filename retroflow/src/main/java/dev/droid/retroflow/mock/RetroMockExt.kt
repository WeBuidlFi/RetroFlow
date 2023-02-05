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
import com.google.gson.reflect.TypeToken
import dev.droid.retroflow.RetroFlow
import dev.droid.retroflow.annotations.RetroMock
import dev.droid.retroflow.extensions.readJson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
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
internal fun <S, E> RetroMock.mock(
    responseType: Type,
    request: Request
): Response<S> {
    val gson = Gson()
    val context = RetroFlow.context

    if (source == MockSource.ASSET || source == MockSource.RESOURCE) {
        checkNotNull(context) {
            "Context must be provided to fetch mock json file from assets/resources folders."
        }
    }

    val mockJson = when (source) {
        MockSource.ASSET -> context!!.readJson(mockAssetPath)
        MockSource.RESOURCE -> context!!.readJson(mockResId)
        MockSource.TAG -> {
            val mockEnvelope = request.tag<MockEnvelope<S, E>>()
            checkNotNull(mockEnvelope) {
                "Service method ${request.url.encodedPath} is annotated with RetroMock and " +
                        "MockSource value is MockSource.TAG, but no @Tag parameter of type " +
                        "MockEnvelope is available in the method parameters."
            }
            gson.toJsonTree(mockEnvelope).asJsonObject
        }
    }

    return if (mode == MockMode.SUCCESS) {
        val success = mockJson.get(MockJsonProps.SUCCESS).asJsonObject
        Response.success(
            gson.fromJson<S>(success.get(MockJsonProps.BODY), responseType),
            success.asOkHttpResponse(gson)
        )
    } else {
        val error = mockJson.get(MockJsonProps.ERROR).asJsonObject
        Response.error(
            error.get(MockJsonProps.BODY).asResponseBody(),
            error.asOkHttpResponse(gson)
        )
    }
}

/**
 * Helper function to convert the success and error json elements from the mock json file to an
 * instance of [okhttp3.Response].
 */
internal fun JsonObject.asOkHttpResponse(gson: Gson): okhttp3.Response {
    return okhttp3.Response.Builder()
        .code(get(MockJsonProps.CODE).asInt)
        .apply {
            val mockHeaderListType = object : TypeToken<List<MockHeader>>() {}.type
            gson.fromJson<List<MockHeader>>(get(MockJsonProps.HEADERS), mockHeaderListType).forEach {
                addHeader(it.name, it.value)
            }
        }
        .body(get(MockJsonProps.BODY).asResponseBody())
        .build()
}

/**
 * Helper function to convert the 'success.body' or 'error.body' elements from the mock json
 * file to [ResponseBody].
 */
internal fun JsonElement.asResponseBody(): ResponseBody {
    return this.toString().toResponseBody("application/json".toMediaType())
}

private object MockJsonProps {
    const val SUCCESS = "success"
    const val ERROR = "error"
    const val BODY = "body"
    const val CODE = "code"
    const val HEADERS = "headers"
}