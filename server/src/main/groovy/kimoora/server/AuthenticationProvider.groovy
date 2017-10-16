package kimoora.server;

import io.undertow.server.HttpServerExchange

interface AuthenticationProvider {

    AuthenticationResults authenticate(HttpServerExchange exchange)

}