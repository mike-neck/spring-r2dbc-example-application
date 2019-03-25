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
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.flux
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.runBlocking
import org.mikeneck.alias.model.IdGenerator
import reactor.test.StepVerifier
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class IdGeneratorImplTest {

    lateinit var idGenerator: IdGeneratorImpl

    @BeforeTest
    fun initiate() {
        idGenerator = IdGeneratorImpl(2)
    }

    @AfterTest
    fun dispose() {
        idGenerator.close()
    }

    // 938676724722302976
    // 938700295502823424
    // 938702288367648768
    // 1352913390288240640
    // 1153019156908539904
    @Test
    fun value() {
        val newId = idGenerator.newId()
        StepVerifier.create(newId)
                .assertNext { id -> println("id: $id") }
                .verifyComplete()
    }

    @Test
    fun idGeneratedLaterHasLarge() {
        val result = GlobalScope.mono {
            val first = idGenerator.newId().awaitSingle()
            val second = idGenerator.newId().awaitSingle()
            first to second
        }
        runBlocking {
            val tpl = result.awaitSingle()
            assertTrue { tpl.first < tpl.second }
            println("1st: ${tpl.first}, 2nd: ${tpl.second}")
        }
    }

    @Test
    fun generateIds() {
        val newIds = idGenerator.newIds(10)
        StepVerifier.create(newIds)
                .assertNext { println(it) }
                .assertNext { println(it) }
                .expectNextCount(8)
                .verifyComplete()
    }
}
