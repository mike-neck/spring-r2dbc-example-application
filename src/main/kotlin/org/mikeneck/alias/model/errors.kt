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

import java.lang.RuntimeException

sealed class ApplicationException(private val msg: String): RuntimeException(msg) {
    override val message: String get() = msg
}

class ActionException(msg: String): ApplicationException(msg)

sealed class ApiException(val status: Int, msg: String): ApplicationException(msg)

class BadRequest(msg: String): ApiException(400, msg)

class ForbiddenRequest(msg: String): ApiException(403, msg)

class ConflictRequest(msg: String): ApiException(409, msg)

class NotFound(msg: String): ApiException(404, msg)

class UnAuthorize(msg: String): ApiException(401, msg)
