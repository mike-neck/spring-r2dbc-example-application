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
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.nio.ByteBuffer
import java.security.SecureRandom
import java.util.*

@Component
class TokenGeneratorImpl : TokenGenerator {

  private val secureRandom: SecureRandom = SecureRandom()

  override fun generate(userId: UserId): UserToken =
      ByteBuffer.allocate(16)
          .apply { putLong(userId.id) }
          .apply { putLong(secureRandom.nextLong()) }
          .apply { flip() }
          .let { it.array() }
          .let { UUID.nameUUIDFromBytes(it) }
          .let { userToken(it.toString()) }
}

interface TokenRepository : ReactiveCrudRepository<TokenDb, Long>

@Table("tokens")
data class TokenDb(
    @Id @Column("user_id") var userId: Long?,
    @Column("value") var value: String?
) {
  companion object {
    fun from(token: Token): TokenDb = TokenDb(token.userId.id, token.userToken.token)
  }
}

@Component
class TokenTransactionalRepositoryImpl : TokenTransactionalRepository {
  override fun create(token: Token): RepositoryAction =
      RepositoryAction.create { db ->
        db.insert()
            .into(TokenDb::class.java)
            .table("tokens")
            .using(TokenDb.from(token))
            .fetch()
            .rowsUpdated()
      }

  override fun deleteById(id: UserId): RepositoryAction =
      RepositoryAction.create { db ->
        db.execute()
            //language=sql
            .sql("""delete from tokens where user_id = $1""")
            .bind("$1", id.id)
            .fetch()
            .rowsUpdated()
      }
}
