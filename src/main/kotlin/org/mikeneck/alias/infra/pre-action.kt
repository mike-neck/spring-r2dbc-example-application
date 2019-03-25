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
package org.mikeneck.alias.infra

import org.mikeneck.alias.model.PreAction
import org.mikeneck.alias.model.RepositoryAction
import org.springframework.data.r2dbc.function.DatabaseClient
import reactor.core.publisher.Mono

internal fun <T: Any> RepositoryAction.Companion.create(action: (DatabaseClient) -> Mono<T>): RepositoryAction =
    object : RepositoryAction {
      override fun run(preAction: PreAction): Mono<PreAction> =
          preAction[DatabaseClient::class].flatMap(action).thenReturn(preAction)
    }
