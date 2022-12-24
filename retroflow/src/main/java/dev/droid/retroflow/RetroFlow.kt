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

package dev.droid.retroflow

import dev.droid.retroflow.utils.RetroDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

/**
 * @author rkpattanaik (Rajesh Pattanaik)
 *
 * [RetroFlow] is the global configuration file to configure the
 * [dev.droid.retroflow.factory.RetroFlowCallAdapterFactory] for each service call.
 */
object RetroFlow {
    internal var dispatcher: CoroutineDispatcher? = IO
        private set

    /**
     * Set your own [CoroutineDispatcher] to be used for each service call. By default RetroFlow
     * uses [RetroDispatcher.ASYNC] dispatcher which is nothing but the [IO] dispatcher under
     * the hood.
     * If you don't wish RetroFlow to launch the [Flow] on any dispatcher, then pass 'null' to
     * [RetroFlow.dispatcher] function. Remember to apply [flowOn] when you are calling the service
     * method returning the [Flow].
     *
     * @param dispatcher: Your [CoroutineDispatcher] implementation
     * @return [RetroFlow]
     */
    fun dispatcher(dispatcher: CoroutineDispatcher?): RetroFlow {
        this.dispatcher = dispatcher
        return this
    }
}