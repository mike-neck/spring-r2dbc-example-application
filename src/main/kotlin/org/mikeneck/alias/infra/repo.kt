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

import org.mikeneck.alias.model.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.r2dbc.function.DatabaseClient
import org.springframework.data.r2dbc.function.TransactionalDatabaseClient
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.publisher.switchIfEmpty
import kotlin.reflect.KClass

@Component
class UnitOfWorkImpl(private val transactionalDatabaseClient: TransactionalDatabaseClient): UnitOfWorkFactory {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(UnitOfWorkImpl::class.java)
    }

    override fun unitOfWork(vararg actions: RepositoryAction): Mono<Unit> {
        if (actions.isEmpty()) {
            return Mono.just(Unit)
        }
        val flux = transactionalDatabaseClient.inTransaction { db ->
            val preAction: PreAction = PreActionImpl(db)
            val repositoryAction = actions.reduce { acc, action ->
                object : RepositoryAction {
                    override fun run(preAction: PreAction): Mono<PreAction> = acc.run(preAction).flatMap { action.run(it) }
                }
            }
            return@inTransaction repositoryAction.run(preAction)
                    .switchIfEmpty { Mono.error(ActionException("failed to save data")) }
                    .thenReturn(Unit)
        }
        return flux.last()
                .doOnError { logger.warn("""item: "repository action", result: fail, error: {}""", it.message, it) }
                .onErrorMap { when (it) {
                    is ApiException -> it
                    else -> ActionException("failed by unknown cause")
                } }
    }
}

class PreActionImpl(private val databaseClient: DatabaseClient): PreAction {

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> get(klass: KClass<T>): Mono<T> =
            when (klass) {
                DatabaseClient::class -> Mono.just(databaseClient) as Mono<T>
                else -> Mono.empty()
            }
}
