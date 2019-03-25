package org.mikeneck.alias.model

import reactor.core.publisher.Mono

data class UserId(val id: Long)

fun userId(id: Long): UserId = UserId(id)

data class UserName(val name: String) {
  fun validate(): ValidationResult<UserName, String> =
      validation<String, String> { it.matches(Regex(pattern)) }
          .ifFalseError { "invalid name" }
          .examine(name)
          .mapInput { this }

  companion object {
    //language=regexp
    private const val pattern = "[A-Za-z][a-zA-Z0-9-]{2,15}"

    fun extractUserName(prefix: String): (String) -> UserName? =
        Regex("$prefix/($pattern)").let { ptn ->
          { string ->
            ptn.find(string)?.groupValues?.find { it.matches(Regex(pattern)) }?.let { UserName(it) }
          }
        }
  }
}

fun userName(name: String): UserName = UserName(name)

data class User(val id: UserId, val name: UserName, val created: CreatedAt) {
  fun validate(): ValidationResult<User, String> = name.validate().mapInput { this }
}

interface UserReader {

  fun findByName(userName: UserName): Mono<User>

  fun findByToken(token: UserToken): Mono<User>
}

interface UserTransactionalRepository {
  fun create(user: User): RepositoryAction
  fun countUserByName(userName: UserName): RepositoryCondition<Long>
  fun delete(user: User): RepositoryAction
}
