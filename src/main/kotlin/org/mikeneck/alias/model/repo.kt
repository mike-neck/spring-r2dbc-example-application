/*
 * Copyright 2019 Shinya Mochida
 * 
 * Licensed under the Apache License,Version2.0(the"License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,software
 * Distributed under the License is distributed on an"AS IS"BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mikeneck.alias.model

import reactor.core.publisher.Mono
import kotlin.reflect.KClass

interface PreAction {
    operator fun <T: Any> get(klass: KClass<T>): Mono<T>

    companion object
}

interface RepositoryCondition<T> {
    fun condition(predicate: (T) -> Boolean): RepositoryAction
}

interface RepositoryAction {
    fun run(preAction: PreAction): Mono<PreAction>

    companion object
}

interface UnitOfWorkFactory {

    fun unitOfWork(vararg actions: RepositoryAction): Mono<Unit>
}
