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

import dev.droid.retroflow.extensions.asResource
import dev.droid.retroflow.extensions.dispatcher
import dev.droid.retroflow.extensions.flowOn
import dev.droid.retroflow.resource.Resource
import dev.droid.retroflow.resource.ResourceFlow
import kotlinx.coroutines.flow.flow
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Converter
import java.lang.reflect.Type

internal class ResourceFlowCallAdapter<S, E>(
    private val successType: Type,
    private val annotations: Array<out Annotation>,
    private val emptyBodyConverter: Converter<ResponseBody, S>,
    private val errorBodyConverter: Converter<ResponseBody, E>
) : CallAdapter<S, ResourceFlow<S, E>> {
    override fun responseType(): Type = successType

    @Suppress("UNCHECKED_CAST")
    override fun adapt(call: Call<S>): ResourceFlow<S, E> = flow {
        emit(
            try {
                val response = call.execute()
                response.asResource<S, E>(emptyBodyConverter, errorBodyConverter)
            } catch (e: Throwable) {
                Resource.Failure.Exception(e) as Resource<S, E>
            }
        )
    }.flowOn(annotations.dispatcher()?.retroDispatcher)
}