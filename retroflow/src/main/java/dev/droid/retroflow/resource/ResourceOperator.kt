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

package dev.droid.retroflow.resource

/**
 * @author rkpattanaik (Rajesh Pattanaik)
 *
 * [ResourceOperator] is an operator to handle [Resource.Success], [Resource.Failure.Error], and
 * [Resource.Failure.Exception] outcomes of [Resource] in a single implementation, instead of
 * using scope functions like [Resource.onSuccess], [Resource.onError], [Resource.onException],
 * or [Resource.onResult].
 */
interface ResourceOperator<S, E> {
    /**
     * Implementation to handle [Resource.Success].
     *
     * @param success: The [Resource.Success] outcome.
     */
    fun onSuccess(success: Resource.Success<S>)

    /**
     * Implementation to handle [Resource.Failure.Error].
     *
     * @param error: The [Resource.Failure.Error] outcome.
     */
    fun onError(error: Resource.Failure.Error<E>)

    /**
     * Implementation to handle [Resource.Failure.Exception].
     *
     * @param exception: The [Resource.Failure.Exception] outcome.
     */
    fun onException(exception: Resource.Failure.Exception)
}