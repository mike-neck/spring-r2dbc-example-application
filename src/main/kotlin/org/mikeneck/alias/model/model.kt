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

val Mono<Void>.asUnit: Mono<Unit> get() = this.thenReturn(Unit)

fun <A : Any, B : Any, C : Any> Pair<A, B>.map(function: (B) -> C): Pair<A, C> = this.first to function.invoke(this.second)

sealed class Either<A : Any, B : Any> {

    abstract fun <C : Any> map(function: (B) -> C): Either<A, C>

    abstract fun rescue(function: (A) -> B): B

    abstract fun <C : Any> flatMap(function: (B) -> Either<A, C>): Either<A, C>

    companion object {

        fun <A : Any, B : Any> right(value: B): Either<A, B> = Right(value)

        fun <A: Any, B: Any> left(failed: A): Either<A, B> = Left(failed)
    }
}

private data class Right<A : Any, B : Any>(val value: B) : Either<A, B>() {

    override fun <C : Any> map(function: (B) -> C): Either<A, C> = Right(function.invoke(value))

    override fun rescue(function: (A) -> B): B = value

    override fun <C : Any> flatMap(function: (B) -> Either<A, C>): Either<A, C> = function.invoke(value)
}

private data class Left<A : Any, B : Any>(val failed: A) : Either<A, B>() {

    override fun <C : Any> map(function: (B) -> C): Either<A, C> = Left(failed)

    override fun rescue(function: (A) -> B): B = function.invoke(failed)

    override fun <C : Any> flatMap(function: (B) -> Either<A, C>): Either<A, C> = Left(failed)
}
