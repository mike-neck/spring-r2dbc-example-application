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

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class My {

  @Test
  fun test(): Unit =
      "foo".let { item ->
        assertAll(
            { assertEquals("foo", item) },
            { assertFalse { item.isBlank() } },
            { assertTrue { item.isEmpty() } }
        )
      }

  @TestFactory
  fun run(): Iterable<DynamicTest> =
      "foo".let { item ->
        listOf(
            DynamicTest.dynamicTest("this is foo") { assertEquals("foo", item) },
            DynamicTest.dynamicTest("this is not blank") { assertFalse { item.isBlank() } },
            DynamicTest.dynamicTest("this is not empty") { assertFalse { item.isEmpty() } } 
        )
      }
}

