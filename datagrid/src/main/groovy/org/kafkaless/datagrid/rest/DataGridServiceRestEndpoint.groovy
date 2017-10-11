package org.kafkaless.datagrid.rest

import com.fasterxml.jackson.databind.ObjectMapper
import io.undertow.Undertow
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import io.undertow.server.handlers.BlockingHandler
import org.kafkaless.datagrid.DefaultDataGridService

class DataGridServiceRestEndpoint {

    private final Authentication authentication

    private final DefaultDataGridService dataGridService

    DataGridServiceRestEndpoint(Authentication authentication, DefaultDataGridService dataGridService) {
        this.authentication = authentication
        this.dataGridService = dataGridService
    }

    void start() {
        Undertow.builder()
                .addHttpListener(8080, '0.0.0.0')
                .setHandler(new BlockingHandler(new HttpHandler() {
            @Override
            void handleRequest(HttpServerExchange exchange) {
                def subject = authentication.authenticate(exchange)
                def uri = exchange.requestURI.substring(1)
                def path = uri.split(/\//)

                Object response
                if(path.first() == 'cachePut') {
                    def payload = new ObjectMapper().readValue(exchange.inputStream, Map)
                    dataGridService.cachePut(path[1], path[2], payload)
                    response = [status: 'OK']
                } else if(path.first() == 'cacheGet') {
                    response = dataGridService.cacheGet(path[1], path[2])
                } else if(path.first() == 'cacheRemove') {
                    dataGridService.cacheRemove(path[1], path[2])
                    response = [status: 'OK']
                } else if(path.first() == 'cacheKeys') {
                    response = [keys: dataGridService.cacheKeys(path[1])]
                }

                exchange.getResponseSender().send(new ObjectMapper().writeValueAsString(response))
            }
        })).build().start()
    }

}