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

package dev.droid.retroflow.annotations

import dev.droid.retroflow.utils.RetroDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.CoroutineDispatcher

/**
 * The service methods can be annotated with [Dispatcher] to specify a [RetroDispatcher]
 * for the particular service call.
 *
 * @property retroDispatcher: The [RetroDispatcher] to be applied to the service method returning
 * [Flow]. The [CoroutineDispatcher] based on [RetroDispatcher] enum will be applied to the [Flow]
 * with [flowOn].
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val retroDispatcher: RetroDispatcher)