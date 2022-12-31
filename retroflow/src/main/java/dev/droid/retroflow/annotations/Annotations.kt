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

import dev.droid.retroflow.mock.MockMode
import dev.droid.retroflow.mock.MockSource
import dev.droid.retroflow.utils.RetroDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.CoroutineDispatcher

/**
 * The service methods can be annotated with [Dispatcher] to specify a [RetroDispatcher]
 * for the particular service call.
 *
 * @property retroDispatcher: The [RetroDispatcher] to be applied to the service method returning
 * [Flow]. The [CoroutineDispatcher] based on [RetroDispatcher] enum will be used as the context
 * of execution for the [Flow] using the [flowOn] operator.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val retroDispatcher: RetroDispatcher)

/**
 * Use [RetroMock] annotation with a service method to return mock response.
 *
 * @param mode: The [MockMode] to be used. [MockMode.SUCCESS] will return the success mock response,
 * [MockMode.ERROR] will return the error mock response, and [MockMode.NONE] will disable mock mode
 * for the service method.
 * @param source: The [MockSource] to be used to get the mock response file. [MockSource.ASSET] will
 * fetch the file from the 'assets' folder whereas [MockSource.RESOURCE] will fetch the file from the
 * 'res/raw' directory.
 * @param mockAssetPath: The mock file relative path from the 'assets' folder. This must not be
 * empty for [source] = [MockSource.ASSET].
 * @param mockResId: The resource id of the mock file which is present in the 'res/raw' directory.
 * This must not be 0 for [source] = [MockSource.RESOURCE].
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RetroMock(
    val mode: MockMode,
    val source: MockSource,
    val mockAssetPath: String = "",
    val mockResId: Int = 0
)