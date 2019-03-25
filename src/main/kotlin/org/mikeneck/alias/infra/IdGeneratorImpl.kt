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

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.reactor.mono
import org.mikeneck.alias.model.IdGenerator
import reactor.core.Disposable
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.*
import java.util.concurrent.atomic.AtomicInteger

class IdGeneratorImpl(id: Int) : IdGenerator, AutoCloseable {

    companion object {
        private val from: Instant = LocalDateTime.of(1972, 1, 1, 0, 0, 0).toInstant(ZoneOffset.UTC)
        private val clock: Clock = Clock.systemUTC()
    }

    private val count: AtomicInteger = AtomicInteger(0)

    private val task: Disposable = Flux.interval(Duration.ofSeconds(1L))
            .subscribe { count.set(0) }

    override fun close() = task.dispose()

    private val mid: Long = (id and 0xf).toLong() shl 59

    override fun newId(): Mono<Long> =
            GlobalScope.mono {
                val now = Instant.now(clock)
                val duration = now.epochSecond - from.epochSecond
                val top = duration shl 16
                val low = count.getAndIncrement()
                return@mono (top + mid + low) and Long.MAX_VALUE
            }

    override fun newIds(upTo: Int): Flux<Long> = (1..upTo).map { newId() }.let { Flux.merge(it) }
}
