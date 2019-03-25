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
package org.mikeneck.alias.controller

import org.mikeneck.alias.model.*
import org.mikeneck.alias.service.UserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import reactor.core.publisher.switchIfEmpty
import java.time.Instant
import kotlin.math.log

@RestController
@RequestMapping("users")
class UserController(val userService: UserService, val idGenerator: IdGenerator) {

  companion object {
    val logger: Logger = LoggerFactory.getLogger(UserController::class.java)
  }

  @PostMapping
  fun createUser(@RequestBody createUser: CreateUser): Mono<CreateUserResult> =
      Mono.just(createUser).map { it.asNonnull }
          .filter { it.agreeTermsOfService }
          .switchIfEmpty(Mono.error(ForbiddenRequest("you should agreeTermsOfService")))
          .flatMap { it.toUser(idGenerator.newId()) }
          .map { it.validate() }
          .flatMap { validation -> validation.toEither().map { Mono.just(it) }.rescue { Mono.error(BadRequest(it)) } }
          .flatMap { userService.createNewUser(it) }
          .doOnSuccess { logger.info("""item: "create-user", result: success, user:"{}"""", it) }
          .doOnError {
            if (it !is ApiException)
              logger.info("""item: "create-user", result: failure, request:"{}", error:"{}" , message:"{}""",
                  createUser,
                  it.javaClass,
                  it.message)
            else
              logger.info("""item: "create-user": result: failure, request:"{}", error:"{}", message:"{}""",
                  createUser,
                  it.javaClass,
                  it.message)
          }
          .map { CreateUserResult(it.first, it.second, "succeed to create user") }

  @GetMapping(path = ["{userName}"])
  fun findUser(@PathVariable("userName") userName: String): Mono<UserView> =
      findUserInternal(userName)
          .map { UserView(it) }
          .doOnError {
            logger.info(
                """item: "find-user", result: failure, request: "{}", error: "{}", message: "{}"""",
                userName,
                it.javaClass,
                it.message)
          }

  private fun findUserInternal(userName: String): Mono<User> {
    return Mono.just(userName)
        .map { userName(it) }
        .map { it.validate().toEither() }
        .flatMap { validation ->
          validation
              .map { userService.findUser(it) }
              .rescue { Mono.error(NotFound("not found")) }
        }
        .switchIfEmpty { Mono.error(NotFound("not found")) }
  }

  @DeleteMapping(path = ["{userName}"])
  fun deleteUser(@PathVariable("userName") userName: String): Mono<AppResponse> =
      findUserInternal(userName)
          .flatMap { userService.deleteUser(it) }
          .switchIfEmpty { Mono.error(NotFound("not found")) }
          .map { AppResponse(true, "") }
          .doOnError { 
            logger.info(
                """item: "delete-user", result: failure, request: "{}", error: "{}", message: "{}"""",
                userName,
                it.javaClass,
                it.message)
          }
}

data class CreateUser(var name: String?, var agreeTermsOfService: Boolean?) {
  data class Nonnull(val name: String, val agreeTermsOfService: Boolean) {
    fun toUser(id: Mono<Long>): Mono<User> =
        id.map { userId(it) }
            .map { User(it, userName(name), createdAt()) }

  }

  val asNonnull: CreateUser.Nonnull = Nonnull(name ?: "", agreeTermsOfService ?: false)
}

data class CreateUserResult(val name: String, val token: String, val success: Boolean, val message: String) {
  constructor(user: User, userToken: UserToken, message: String) : this(user.name.name, userToken.token, true, message)
}

data class UserView(val name: String, val createdAt: Instant) {
  constructor(user: User) : this(user.name.name, user.created.instant)
}
