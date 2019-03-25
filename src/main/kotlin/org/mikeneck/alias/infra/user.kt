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
import org.springframework.data.annotation.Id
import org.springframework.data.r2dbc.function.DatabaseClient
import org.springframework.data.r2dbc.repository.query.Query
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import reactor.core.publisher.switchIfEmpty
import java.time.Instant

@Repository
interface UserRepository : ReactiveCrudRepository<UserDb, Long> {

  @Query(
      //language=sql
      """select id, name, created from users where name = $1""")
  fun findByName(name: String): Mono<UserDb>

  @Query(
      //language=sql
      """select u.id, u.name, u.created from users as u join tokens t on u.id = t.user_id where t.value = $1""")
  fun findByToken(userToken: String): Mono<UserDb>
}

@Table("users")
data class UserDb(
    @Id @Column("id") var id: Long?,
    @Column("name") var name: String?,
    @Column("created") var created: Instant?
) {
  fun toUser(): User = User(userId(id ?: 0L), userName(name ?: ""), created?.let { createdAt(it) } ?: createdAt())

  companion object {
    fun from(user: User): UserDb = UserDb(user.id.id, user.name.name, user.created.instant)
  }
}

@Component
class UserReaderImpl(private val userRepository: UserRepository) : UserReader {
  override fun findByName(userName: UserName): Mono<User> =
      userRepository.findByName(userName.name)
          .map { it.toUser() }

  override fun findByToken(token: UserToken): Mono<User> =
      userRepository.findByToken(token.token).map { it.toUser() }
}

@Component
class UserTransactionalRepositoryImpl : UserTransactionalRepository {
  override fun create(user: User): RepositoryAction =
      RepositoryAction.create { db ->
        db.insert()
            .into(UserDb::class.java)
            .using(UserDb.from(user))
            .fetch()
            .rowsUpdated()
      }

  override fun countUserByName(userName: UserName): RepositoryCondition<Long> =
      object : RepositoryCondition<Long> {
        override fun condition(predicate: (Long) -> Boolean): RepositoryAction =
            RepositoryAction.create { db ->
              db.execute()
                  //language=sql
                  .sql("""select count(id) as c from users where name = $1""")
                  .bind("$1", userName.name)
                  .map { row, _ -> row.get("c", Long::class.javaObjectType) }
                  .first()
                  .flatMap { if (it == null) Mono.empty() else Mono.just(it) }
                  .filter(predicate)
                  .switchIfEmpty(Mono.error(ConflictRequest("user (${userName.name}) already exists")))
            }
      }

  override fun delete(user: User): RepositoryAction =
      RepositoryAction.create { db ->
        db.execute()
            //language=sql
            .sql("""delete from users as u where u.id = $1""")
            .bind("$1", user.id.id)
            .fetch()
            .rowsUpdated()
      }
}

