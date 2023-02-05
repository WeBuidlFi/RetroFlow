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
import java.io.InputStream

/**
 * Helper function to read json file from the assets folder and return as [JsonObject].
 *
 * @param assetPath: Relative json file path in the assets folder
 *
 * @return [JsonObject]
 */
fun Context.readJson(assetPath: String): JsonObject {
    return assets.open(assetPath).use { it.asJsonObject() }
}

/**
 * Helper function to read json file from the res/raw directory and return as [JsonObject].
 *
 * @param resId: Resource id of the json file in res/raw directory
 *
 * @return [JsonObject]
 */
fun Context.readJson(resId: Int): JsonObject {
    return resources.openRawResource(resId).use { it.asJsonObject() }
}

/**
 * Helper function to convert the [InputStream] as a [JsonObject].
 */
fun InputStream.asJsonObject(): JsonObject {
    val jsonString = bufferedReader().use { it.readText() }
    return JsonParser.parseString(jsonString).asJsonObject
}