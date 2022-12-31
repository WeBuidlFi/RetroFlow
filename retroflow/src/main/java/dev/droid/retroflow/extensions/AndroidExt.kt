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

import android.content.Context
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dev.droid.retroflow.annotations.RetroMock
import dev.droid.retroflow.mock.MockSource

internal fun Context.readMockJson(retroMock: RetroMock): JsonObject {
    val inputStream = if (retroMock.source == MockSource.ASSET) {
        assets.open(retroMock.mockAssetPath)
    } else {
        resources.openRawResource(retroMock.mockResId)
    }
    val mockJsonString = inputStream.bufferedReader().use { it.readText() }
    val mockJsonObject = JsonParser.parseString(mockJsonString).asJsonObject
    inputStream.close()
    return mockJsonObject
}