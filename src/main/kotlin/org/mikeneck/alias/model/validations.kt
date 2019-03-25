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

interface Validation<T : Any, R : Any> {

    fun examine(input: T): ValidationResult<T, R>

    fun and(validation: Validation<T, R>): Validation<T, R> = object : Validation<T, R> {
        override fun examine(input: T): ValidationResult<T, R> =
                this.examine(input).takeIf { it.hasError } ?: validation.examine(input)
    }

    interface Resulting<T: Any, R : Any> {
        fun ifTrueError(transform: (T) -> R): Validation<T, R>
        fun ifFalseError(transform: (T) -> R): Validation<T, R>
    }

    companion object {
        fun <T : Any, R : Any> on(predicate: (T) -> Boolean): Resulting<T, R> = object : Resulting<T, R> {
            override fun ifTrueError(transform: (T) -> R): Validation<T, R> =
                    object : Validation<T, R> {
                        override fun examine(input: T): ValidationResult<T, R> =
                                if (predicate.invoke(input)) ValidationFailure(input, transform.invoke(input))
                                else ValidationSuccess(input)
                    }

            override fun ifFalseError(transform: (T) -> R): Validation<T, R> =
                    object : Validation<T, R> {
                        override fun examine(input: T): ValidationResult<T, R> =
                                if (!predicate.invoke(input)) ValidationFailure(input, transform.invoke(input))
                                else ValidationSuccess(input)
                    }
        }

        fun <T : Any, R : Any> all(vararg validations: Validation<T, R>) =
                arrayOf(*validations).reduce { acc, validation -> acc.and(validation) }
    }
}

fun <T : Any, R : Any> validation(predicate: (T) -> Boolean): Validation.Resulting<T, R> = Validation.on(predicate)

interface ValidationResult<T : Any, R : Any> {

    val input: T

    val hasError: Boolean

    fun takeError(): R?

    fun <I: Any> mapInput(transform: (T) -> I): ValidationResult<I, R>

    fun <N : Any> map(transform: (R) -> N): ValidationResult<T, N>

    fun toEither(): Either<R, T>
}

private class ValidationSuccess<T : Any, R : Any>(override val input: T) : ValidationResult<T, R> {
    override val hasError: Boolean = false
    override fun takeError(): R? = null
    override fun <I : Any> mapInput(transform: (T) -> I): ValidationResult<I, R> =
            ValidationSuccess(transform.invoke(input))
    override fun <N : Any> map(transform: (R) -> N): ValidationResult<T, N> = ValidationSuccess(input)
    override fun toEither(): Either<R, T> = Either.right(input)
}

private class ValidationFailure<T : Any, R : Any>(override val input: T, private val result: R) : ValidationResult<T, R> {
    override val hasError: Boolean = true
    override fun takeError(): R? = result
    override fun <I : Any> mapInput(transform: (T) -> I): ValidationResult<I, R> =
            ValidationFailure(transform.invoke(input), result)
    override fun <N : Any> map(transform: (R) -> N): ValidationResult<T, N> =
            ValidationFailure(input, transform.invoke(result))
    override fun toEither(): Either<R, T> = Either.left(result)
}
