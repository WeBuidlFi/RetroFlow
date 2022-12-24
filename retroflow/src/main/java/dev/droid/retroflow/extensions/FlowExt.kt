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

import dev.droid.retroflow.RetroFlow
import dev.droid.retroflow.utils.RetroDispatcher
import dev.droid.retroflow.utils.dispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import java.util.concurrent.Executors

/**
 * Single-threaded [CoroutineDispatcher] to perform tasks on a single thread (i.e. synchronously).
 */
val Dispatchers.SINGLE: CoroutineDispatcher
    get() = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

/**
 * [flowOn] is an overloaded extension function to apply the proper dispatcher extracted from
 * [RetroDispatcher] or [RetroFlow.dispatcher].
 */
internal fun <T> Flow<T>.flowOn(retroDispatcher: RetroDispatcher?): Flow<T> = apply {
    retroDispatcher?.dispatcher?.let { flowOn(it) }
        ?: RetroFlow.dispatcher?.let { flowOn(it) }
}