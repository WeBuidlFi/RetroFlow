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

/**
 * Wrapper class for the success & error mock responses.
 *
 * @property success: Representation of the 'success' node in the mock json file.
 * @property error: Representation of the 'error' node in the mock json file.
 *
 * See below examples for mock json format/specification:
 * [getUsers.json](https://bit.ly/getUsers_json)
 * [getUserById.json](https://bit.ly/getUserById_json)
 */
data class MockEnvelope<S, E>(
    val success: MockResponse<S>,
    val error: MockResponse<E>
)

/**
 * Representation of the success/error nodes in the mock json file.
 *
 * @property code: The status code to be set for the mock response
 * @property headers: The Headers to be set for the mock response
 * @property body: The body of type [T] to be set for the mock response
 *
 * See below examples for mock json format/specification:
 * [getUsers.json](https://bit.ly/getUsers_json)
 * [getUserById.json](https://bit.ly/getUserById_json)
 */
data class MockResponse<T>(
    val code: Int,
    val headers: List<MockHeader>,
    val body: T
)

/**
 * Representation of the 'headers' element in the 'success' and 'error' nodes in the mock json file.
 *
 * @property name: Header name/key
 * @property value: Header value
 *
 * See below examples for mock json format/specification:
 * [getUsers.json](https://bit.ly/getUsers_json)
 * [getUserById.json](https://bit.ly/getUserById_json)
 */
data class MockHeader(
    val name: String,
    val value: String
)
