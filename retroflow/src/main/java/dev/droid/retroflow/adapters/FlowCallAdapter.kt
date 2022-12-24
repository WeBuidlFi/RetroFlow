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

import dev.droid.retroflow.extensions.dispatcher
import dev.droid.retroflow.extensions.flowOn
import dev.droid.retroflow.extensions.successBody
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.HttpException
import java.lang.reflect.Type

internal class FlowCallAdapter<T>(
    private val responseType: Type,
    private val annotations: Array<out Annotation>,
    private val emptyBodyConverter: Converter<ResponseBody, T>
): CallAdapter<T, Flow<T>> {
    override fun responseType(): Type = responseType

    override fun adapt(call: Call<T>): Flow<T> = flow {
        emit(
            try {
                val response = call.execute()
                if (response.isSuccessful) {
                    response.successBody(emptyBodyConverter)
                } else {
                    throw HttpException(response)
                }
            } catch (e: Exception) {
                throw e
            }
        )
    }.flowOn(annotations.dispatcher()?.retroDispatcher)
}