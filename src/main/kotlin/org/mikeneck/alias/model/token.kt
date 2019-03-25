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

data class UserToken(val token: String)

fun userToken(token: String): UserToken = UserToken(token)

data class Token(val userId: UserId, val userToken: UserToken)

interface TokenGenerator {
  fun generate(userId: UserId): UserToken
}

interface TokenTransactionalRepository {
  fun create(token: Token): RepositoryAction
  fun deleteById(id: UserId): RepositoryAction
}
