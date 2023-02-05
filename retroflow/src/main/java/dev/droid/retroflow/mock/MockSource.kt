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

package dev.droid.retroflow.mock

import dev.droid.retroflow.annotations.RetroMock

/**
 * Enum class to defile the source of the mock json file, while annotating the method
 * with [RetroMock].
 *
 * [ASSET]: Assets folder as the source of the mock json file
 * [RESOURCE]: res/raw directory as the source of the mock json file
 * [TAG]: Service method parameter of type [MockEnvelope], annotated with [retrofit2.http.Tag], is
 * provided as the mock.
 */
enum class MockSource {
    ASSET, RESOURCE, TAG
}