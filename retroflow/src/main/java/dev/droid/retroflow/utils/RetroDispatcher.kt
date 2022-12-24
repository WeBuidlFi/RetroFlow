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

package dev.droid.retroflow.utils

import dev.droid.retroflow.extensions.SINGLE
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import dev.droid.retroflow.annotations.Dispatcher

/**
 * An enum with values [ASYNC], [SYNC], and [NONE] that can be provided to the [Dispatcher] annotation
 * to specify which [CoroutineDispatcher] will be used for the particular service method returning
 * [Flow].
 *
 * [ASYNC] - [Dispatchers.IO] under the hood
 * [SYNC] - [Dispatchers.SINGLE] under the hood
 * [NONE] - No [CoroutineDispatcher] will be applied. Hence it's caller's responsibility to provide
 * a [CoroutineDispatcher] with [flowOn] while calling the service method.
 */
enum class RetroDispatcher {
    ASYNC, SYNC, NONE
}

internal val RetroDispatcher.dispatcher: CoroutineDispatcher?
    get() = when (this) {
        RetroDispatcher.ASYNC -> Dispatchers.IO
        RetroDispatcher.SYNC -> Dispatchers.SINGLE
        RetroDispatcher.NONE -> null
    }