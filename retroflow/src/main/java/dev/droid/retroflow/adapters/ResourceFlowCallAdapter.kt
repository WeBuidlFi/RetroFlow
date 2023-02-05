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

package dev.droid.retroflow.adapters

import dev.droid.retroflow.RetroFlow
import dev.droid.retroflow.extensions.*
import dev.droid.retroflow.mock.MockMode
import dev.droid.retroflow.mock.mock
import dev.droid.retroflow.resource.Resource
import dev.droid.retroflow.resource.ResourceFlow
import dev.droid.retroflow.resource.operators
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Converter
import java.lang.reflect.Type

internal class ResourceFlowCallAdapter<S, E>(
    private val successType: Type,
    private val annotations: Array<out Annotation>,
    private val successBodyConverter: Converter<ResponseBody, S>,
    private val errorBodyConverter: Converter<ResponseBody, E>
) : CallAdapter<S, ResourceFlow<S, E>> {
    override fun responseType(): Type = successType

    @Suppress("UNCHECKED_CAST")
    override fun adapt(call: Call<S>): ResourceFlow<S, E> = flow {
        emit(
            try {
                val retroMock = annotations.mock()
                val response = if (retroMock != null
                                    && retroMock.mode != MockMode.NONE
                                    && RetroFlow.isMockEnabled) {
                    retroMock.mock<S, E>(successType, call.request())
                } else {
                    call.execute()
                }
                response.asResource(successBodyConverter, errorBodyConverter)
            } catch (e: Throwable) {
                Resource.Failure.Exception(e) as Resource<S, E>
            }
        )
    }.flowOn(annotations.dispatcher()?.retroDispatcher)
        .map {
            (it as Resource<Any, Any>).operators(RetroFlow.operators)
            it
        }
}