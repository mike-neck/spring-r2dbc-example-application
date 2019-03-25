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

import org.mikeneck.alias.model.ActionException
import org.mikeneck.alias.model.ApiException
import org.mikeneck.alias.model.ApplicationException
import org.mikeneck.alias.model.ConflictRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.ResolvableType
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.util.MimeType
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebExceptionHandler
import reactor.core.publisher.Mono

@Component
@Order(-1)
class ExceptionHandler(val jackson2Encoder: Jackson2JsonEncoder) : WebExceptionHandler {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(ExceptionHandler::class.java)
    }

    override fun handle(exchange: ServerWebExchange, ex: Throwable): Mono<Void> =
            Unit.apply {
                logger.warn("""item: "error handling", request: "{}", exception: "{}", app-message: "{}"""",
                        exchange.request.path,
                        ex,
                        ex.message)
            }
                    .let {
                        when (ex) {
                            is ActionException ->
                                handleResponse(HttpStatus.INTERNAL_SERVER_ERROR, exchange.response, ex.message)
                            is ApiException ->
                                handleResponse(HttpStatus.valueOf(ex.status), exchange.response, ex.message)
                            is ApplicationException ->
                                handleResponse(HttpStatus.INTERNAL_SERVER_ERROR, exchange.response, ex.message)
                            else ->
                                handleResponse(HttpStatus.INTERNAL_SERVER_ERROR, exchange.response, "${ex.message}")

                        }
                    }

    private fun handleResponse(status: HttpStatus, response: ServerHttpResponse, msg: String): Mono<Void> {
        response.statusCode = status
        return response.writeWith(jackson2Encoder.encode(Mono.just(AppResponse(false, msg)),
                response.bufferFactory(),
                ResolvableType.forClass(AppResponse::class.java),
                MimeType.valueOf("application/json"),
                null))
    }
} 
