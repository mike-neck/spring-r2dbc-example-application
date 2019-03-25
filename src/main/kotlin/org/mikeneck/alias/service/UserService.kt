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
package org.mikeneck.alias.service

import org.mikeneck.alias.model.*
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class UserService(
        private val tokenGenerator: TokenGenerator,
        private val userTransactionalRepository: UserTransactionalRepository,
        private val tokenTransactionalRepository: TokenTransactionalRepository,
        private val userReader: UserReader,
        private val unitOfWorkFactory: UnitOfWorkFactory) {

    fun createNewUser(user: User): Mono<Pair<User, UserToken>> {
        val userToken = tokenGenerator.generate(userId = user.id)
        val token = Token(user.id, userToken)
        return unitOfWorkFactory.unitOfWork(
                userTransactionalRepository.countUserByName(user.name).condition { it == 0L },
                userTransactionalRepository.create(user),
                tokenTransactionalRepository.create(token))
                .thenReturn(user to userToken)
    }

    fun findUser(userName: UserName): Mono<User> =
        userReader.findByName(userName)

  fun deleteUser(user: User): Mono<User> =
      unitOfWorkFactory.unitOfWork(
          tokenTransactionalRepository.deleteById(user.id),
          userTransactionalRepository.delete(user))
          .thenReturn(user)
}
