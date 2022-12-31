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

package dev.droid.retroflow.factory

import dev.droid.retroflow.resource.Resource
import dev.droid.retroflow.adapters.FlowCallAdapter
import dev.droid.retroflow.adapters.ResourceFlowCallAdapter
import kotlinx.coroutines.flow.Flow
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * @author rkpattanaik (Rajesh Pattanaik)
 *
 * [RetroFlowCallAdapterFactory] is a Retrofit [CallAdapter.Factory] to support [Flow] return type
 * in Retrofit service methods.
 *
 * Adding [RetroFlowCallAdapterFactory] to the Retrofit instance, allows you to use
 * [Flow]<[Resource]> or [Flow]<T> as return types in service methods.
 * It's recommended to use Flow<[Resource]> instead of just Flow<T>.
 *
 * Examples:
 *
 * @GET("api/users")
 * fun getUsers(): Flow<Resource<GetUsersResponse, GetUsersErrorResponse>
 *
 * @GET("api/movies")
 * fun getMovies(): Flow<List<Movie>>
 */
class RetroFlowCallAdapterFactory private constructor(): CallAdapter.Factory() {
    override fun get(
        returnType: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        if (getRawType(returnType) != Flow::class.java) return null

        val flowType = getParameterUpperBound(0, returnType as ParameterizedType)
        return if (getRawType(flowType) == Resource::class.java) {
            val (successType, errorType) = (flowType as ParameterizedType).bodyTypes()
            val successBodyConverter = retrofit.responseBodyConverter<Any>(successType, annotations)
            val errorBodyConverter = retrofit.responseBodyConverter<Any>(errorType, annotations)
            ResourceFlowCallAdapter<Any, Any>(successType, annotations,
                successBodyConverter, errorBodyConverter)
        } else {
            val successBodyConverter = retrofit.responseBodyConverter<Any>(flowType, annotations)
            FlowCallAdapter<Any>(flowType, annotations, successBodyConverter)
        }
    }

    private fun ParameterizedType.bodyTypes(): Pair<Type, Type> {
        val successType = getParameterUpperBound(0, this)
        val errorType = getParameterUpperBound(1, this)
        return successType to errorType
    }

    companion object {
        /**
         * Factory method to create an instance of [RetroFlowCallAdapterFactory].
         */
        fun create(): RetroFlowCallAdapterFactory = RetroFlowCallAdapterFactory()
    }
}