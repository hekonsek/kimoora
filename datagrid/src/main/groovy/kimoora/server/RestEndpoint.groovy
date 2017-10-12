package kimoora.server

import com.fasterxml.jackson.databind.ObjectMapper
import io.undertow.Undertow
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import io.undertow.server.handlers.BlockingHandler
import kimoora.Kimoora
import org.kafkaless.datagrid.DefaultDataGridService

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
                def subject = authentication.authenticate(exchange)
                def uri = exchange.requestURI.substring(1)
                def path = uri.split(/\//)

                Object response
                if(path.first() == 'registerFunctionDefinition') {
                    def payload = new ObjectMapper().readValue(exchange.inputStream, Map)
                    kimooraServer.registerFunctionDefinition(path[1], payload)
                    response = [status: 'OK']
                } else   if(path.first() == 'getFunctionDefinition') {
                    response = kimooraServer.getFunctionDefinition(path[1])
                } else if(path.first() == 'invoke') {
                    def payload = new ObjectMapper().readValue(exchange.inputStream, Map)
                    response = kimooraServer.invoke(path[1], payload)
                }

                exchange.getResponseSender().send(new ObjectMapper().writeValueAsString(response))
            }
        })).build().start()
        this
    }

}