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

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.AssetManager
import android.content.res.Resources
import dev.droid.retroflow.factory.RetroFlowCallAdapterFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

/**
 * @author rkpattanaik (Rajesh Pattanaik)
 *
 * [RetroFlow] is the global configuration file to configure the [RetroFlowCallAdapterFactory].
 */
@SuppressLint("StaticFieldLeak")
object RetroFlow {
    internal var dispatcher: CoroutineDispatcher? = Dispatchers.IO
        private set
    internal var isMockEnabled = false
        private set
    internal var context: Context? = null
        private set

    /**
     * Provide the context of [Flow] execution for all service calls. By default RetroFlow will use
     * the [Dispatchers.IO] dispatcher.
     * Provide 'null' if RetroFlow does not have to switch the context of execution. It's the
     * responsibility of the caller to handle the context of [Flow] execution using the [flowOn]
     * operator while making the service call. If the context of execution is not switched then
     * the calls will be executed on the current thread which may lead to
     * [android.os.NetworkOnMainThreadException] and crash the application.
     *
     * @param dispatcher: Your [CoroutineDispatcher] implementation
     *
     * @return [RetroFlow]
     */
    fun dispatcher(dispatcher: CoroutineDispatcher?): RetroFlow {
        this.dispatcher = dispatcher
        return this
    }

    /**
     * Use this function to enable or disable mock mode.
     *
     * @param enable: [Boolean] value to enable/disable mock mode
     * @param context: Android [Context] object. This is required to obtain the [AssetManager] or
     * [Resources] for accessing 'assets' or 'res/raw' folders where the mock json files are stored.
     *
     * @return [RetroFlow]
     */
    fun mock(enable: Boolean, context: Context): RetroFlow {
        isMockEnabled = enable
        this.context = context
        return this
    }
}