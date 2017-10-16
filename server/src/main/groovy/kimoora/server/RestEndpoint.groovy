package kimoora.server

import com.fasterxml.jackson.databind.ObjectMapper
import io.undertow.Undertow
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import io.undertow.server.handlers.BlockingHandler

import static kimoora.util.Json.jsonString
import static org.apache.commons.lang3.StringUtils.isNotBlank

class RestEndpoint {

    private final KimooraServer kimooraServer

    private final Authentication authentication

    RestEndpoint(KimooraServer kimooraServer, Authentication authentication) {
        this.kimooraServer = kimooraServer
        this.authentication = authentication
    }

    RestEndpoint start() {
        Undertow.builder()
                .addHttpListener(8080, '0.0.0.0')
                .setHandler(new BlockingHandler(new HttpHandler() {
            @Override
            void handleRequest(HttpServerExchange exchange) {
                try {
                    def uri = exchange.requestURI.substring(1)
                    def path = uri.split(/\//)

                    if(path.first() == 'login') {
                        def payload = new ObjectMapper().readValue(exchange.inputStream, Map)
                        def response = [token: kimooraServer.login(payload.username as String, payload.password as String)]
                        exchange.getResponseSender().send(jsonString(response))
                        return
                    }

                    authentication.authenticate(exchange)

                    Object response
                    if (path.first() == 'registerFunctionDefinition') {
                        def payload = new ObjectMapper().readValue(exchange.inputStream, Map)
                        kimooraServer.registerFunctionDefinition(path[1], payload)
                        response = [status: 'OK']
                    } else if (path.first() == 'getFunctionDefinition') {
                        response = kimooraServer.getFunctionDefinition(path[1])
                    } else if (path.first() == 'invoke') {
                        def payload = new ObjectMapper().readValue(exchange.inputStream, Map)
                        response = kimooraServer.invoke(path[1], payload)
                    } else if (path.first() == 'cacheGet') {
                        response = kimooraServer.cacheGet(path[1], path[2])
                    } else if (path.first() == 'cachePut') {
                        def payload = new ObjectMapper().readValue(exchange.inputStream, Map)
                        kimooraServer.cachePut(path[1], path[2], payload)
                        response = [status: 'OK']
                    } else if (path.first() == 'cacheRemove') {
                        kimooraServer.cacheRemove(path[1], path[2])
                        response = [status: 'OK']
                    } else if (path.first() == 'cacheKeys') {
                        response = [keys: kimooraServer.cacheKeys(path[1])]
                    } else if (path.first() == 'addUser') {
                        def payload = new ObjectMapper().readValue(exchange.inputStream, Map)
                        kimooraServer.addUser(path[1], payload.password as String, payload.roles as List<String>)
                        response = [status: 'OK']
                    } else {
                        throw new UnsupportedOperationException(path.first())
                    }

                    exchange.getResponseSender().send(new ObjectMapper().writeValueAsString(response))
                } catch (NullPointerException e) {
                    if(isNotBlank(e.message)) {
                        exchange.getResponseSender().send(jsonString([status: 'ERROR', message: e.message]))
                    } else {
                        exchange.getResponseSender().send(jsonString([status: 'ERROR', message: 'Internal error.']))
                    }
                } catch (Exception e) {
                    exchange.getResponseSender().send(jsonString([status: 'ERROR', message: "${e.class.simpleName}: ${e.message}".toString()]))
                }
            }
        })).build().start()
        this
    }

}