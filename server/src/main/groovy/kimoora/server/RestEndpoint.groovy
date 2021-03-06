package kimoora.server

import com.fasterxml.jackson.databind.ObjectMapper
import io.undertow.Undertow
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import io.undertow.server.handlers.BlockingHandler

import static kimoora.util.Json.fromJson
import static kimoora.util.Json.jsonString
import static org.apache.commons.lang3.StringUtils.isNotBlank

class RestEndpoint {

    private final KimooraServer kimooraServer

    private final AuthenticationProvider authentication

    RestEndpoint(KimooraServer kimooraServer, AuthenticationProvider authentication) {
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
                        def payload = fromJson(exchange.inputStream)
                        def response = [token: kimooraServer.login(payload.username as String, payload.password as String)]
                        exchange.getResponseSender().send(jsonString(response))
                        return
                    }

                    def authenticationResults = authentication.authenticate(exchange)

                    Object response
                    if (path.first() == 'registerFunctionDefinition') {
                        def payload = new ObjectMapper().readValue(exchange.inputStream, Map)
                        kimooraServer.registerFunctionDefinition(path[1], payload)
                        response = [status: 'OK']
                    } else if (path.first() == 'getFunctionDefinition') {
                        response = kimooraServer.getFunctionDefinition(path[1])
                    } else if (path.first() == 'listFunctionsDefinitions') {
                        response = kimooraServer.listFunctionsDefinitions()
                    } else if (path.first() == 'invoke') {
                        def payload = fromJson(exchange.inputStream)
                        def event = [payload: payload, metadata: [token: authenticationResults.token()]]
                        response = kimooraServer.invoke(path[1], event)
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
                    } else if (path.first() == 'documentGet') {
                        response = kimooraServer.documentGet(path[1], path[2])
                    } else if (path.first() == 'documentPut') {
                        def payload = new ObjectMapper().readValue(exchange.inputStream, Map)
                        kimooraServer.documentPut(path[1], path[2], payload)
                        response = [status: 'OK']
                    } else if (path.first() == 'documentRemove') {
                        kimooraServer.documentRemove(path[1], path[2])
                        response = [status: 'OK']
                    } else if (path.first() == 'documentsKeys') {
                        response = [keys: kimooraServer.documentsKeys(path[1])]
                    } else if (path.first() == 'addUser') {
                        def payload = new ObjectMapper().readValue(exchange.inputStream, Map)
                        kimooraServer.addUser(path[1], payload.password as String, payload.roles as List<String>)
                        response = [status: 'OK']
                    } else if (path.first() == 'streamSendTo') {
                        def payload = new ObjectMapper().readValue(exchange.inputStream, Map)
                        kimooraServer.streamSendTo(path[1], payload)
                        response = [status: 'OK']
                    } else if (path.first() == 'streamBacklogSize') {
                        response = [backlogSize: kimooraServer.streamBacklogSize(path[1])]
                    } else if (path.first() == 'addPipe') {
                        def payload = new ObjectMapper().readValue(exchange.inputStream, Map)
                        kimooraServer.addPipe(path[1], payload)
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