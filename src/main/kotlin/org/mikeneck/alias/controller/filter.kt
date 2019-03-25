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
import org.springframework.http.HttpHeaders
import org.springframework.http.server.RequestPath
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import reactor.core.publisher.switchIfEmpty

@Component
class TokenFilter(private val userReader: UserReader) : WebFilter {

  companion object {
    private val excludingPatterns: List<Regex> =
        listOf(Regex("/users$"))

    private fun requireToken(path: String): Boolean =
        excludingPatterns
            .map { ptn: Regex -> { path: String -> !ptn.matches(path) } }
            .reduce { checker, function ->
              { path: String -> if (checker.invoke(path)) true else function.invoke(path) }
            }.invoke(path)
  }

  private fun extractUserName(requestPath: RequestPath): UserName? =
      requestPath.value().let { UserName.extractUserName("/users").invoke(it) }

  override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> =
      if (requireToken(exchange.request.path.value()))
        Mono.defer<String> { exchange.request.headers.findHeader("X-USER-TOKEN") }
            .flatMap { userReader.findByToken(userToken(it)) }
            .flatMap { user ->
              if (extractUserName(exchange.request.path) == user.name) Mono.just(user)
              else Mono.empty() }
            .switchIfEmpty { Mono.error(UnAuthorize("unauthorized")) }
            .flatMap { chain.filter(exchange) }
      else
        chain.filter(exchange)

  fun HttpHeaders.findHeader(name: String): Mono<String> =
      this.keys.find { it.toUpperCase() == name.toUpperCase() }
          .let {
            return@let if (it == null) Mono.empty()
            else Mono.justOrEmpty(this[it])
                .flatMap { if (it.isEmpty()) Mono.empty() else Mono.just(it.first()) }
          }
}

