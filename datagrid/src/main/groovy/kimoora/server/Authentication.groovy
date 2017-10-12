package kimoora.server;

import io.undertow.server.HttpServerExchange

interface Authentication {

    AuthenticationSubject authenticate(HttpServerExchange exchange)

}