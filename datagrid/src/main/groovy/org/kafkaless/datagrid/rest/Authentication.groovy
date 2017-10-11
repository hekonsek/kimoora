package org.kafkaless.datagrid.rest;

import io.undertow.server.HttpServerExchange

interface Authentication {

    AuthenticationSubject authenticate(HttpServerExchange exchange)

}