/**
 * Licensed to the Kimoora under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kimoora.server

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import io.undertow.server.HttpServerExchange
import org.apache.commons.lang3.Validate
import org.apache.ignite.IgniteCache

class JwtAuthenticationProvider implements AuthenticationProvider {

    private IgniteCache<String, String> tokenCache

    @Override
    AuthenticationResults authenticate(HttpServerExchange exchange) {
        def authHeader = exchange.requestHeaders.getFirst('Authentication')
        Validate.notBlank(authHeader, 'Authentication header cannot be blank.')
        def token = authHeader.replaceFirst(/Bearer /, '')

        Algorithm algorithm = Algorithm.HMAC256(tokenCache.get('secret'))
        JWTVerifier verifier = JWT.require(algorithm).build()
        DecodedJWT jwt = verifier.verify(token);
        def username = jwt.getSubject()
        def roles = jwt.getClaim("roles").asArray(String)

        new AuthenticationResults(token, username, roles.toList())
    }

    void setKimoora(KimooraServer kimoora) {
        tokenCache = kimoora.@ignite.getOrCreateCache('kimoora_jwt_secret')
    }

}