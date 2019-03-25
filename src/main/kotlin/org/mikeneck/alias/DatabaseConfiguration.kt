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
package org.mikeneck.alias

import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryOptions
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.data.r2dbc.dialect.Dialect
import org.springframework.data.r2dbc.dialect.PostgresDialect
import org.springframework.data.r2dbc.function.TransactionalDatabaseClient
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@Configuration
@EnableR2dbcRepositories
class DatabaseConfiguration : AbstractR2dbcConfiguration() {

  @Bean
  override fun connectionFactory(): ConnectionFactory =
      ConnectionFactoryOptions.builder()
          .option(ConnectionFactoryOptions.DRIVER, "postgresql")
          .option(ConnectionFactoryOptions.HOST, "localhost")
          .option(ConnectionFactoryOptions.PORT, 5432)
          .option(ConnectionFactoryOptions.USER, "postgres-user")
          .option(ConnectionFactoryOptions.PASSWORD, "postgres-pass")
          .option(ConnectionFactoryOptions.DATABASE, "postgres")
          .build()
          .let { ConnectionFactories.get(it) }

  override fun getDialect(connectionFactory: ConnectionFactory): Dialect = PostgresDialect()

  @Bean
  fun transactionalDatabaseClient(connectionFactory: ConnectionFactory): TransactionalDatabaseClient =
      TransactionalDatabaseClient.create(connectionFactory)
}
