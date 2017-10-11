package grider

import io.undertow.server.HttpServerExchange
import org.junit.Test
import org.kafkaless.datagrid.DataGridServiceBuilder
import org.kafkaless.datagrid.client.RestClientDataGridService
import org.kafkaless.datagrid.rest.Authentication
import org.kafkaless.datagrid.rest.AuthenticationSubject
import org.kafkaless.datagrid.rest.DataGridServiceRestEndpoint

import static java.util.UUID.randomUUID
import static org.assertj.core.api.Assertions.assertThat

class DataGridServiceTest {

    static rest = new DataGridServiceRestEndpoint(new Authentication() {
        @Override
        AuthenticationSubject authenticate(HttpServerExchange exchange) {
            return null
        }
    }, new DataGridServiceBuilder().build()).start()

    def dataGrid = new RestClientDataGridService('http://localhost:8080')

    def cacheName = randomUUID().toString()

    def key = randomUUID().toString()

    def value = [foo: randomUUID().toString()]

    // Cache tests

    @Test
    void shouldGetFromCache() {
        dataGrid.cachePut(cacheName, key, value)
        def value = dataGrid.cacheGet(cacheName, key)
        assertThat(value).isEqualTo(this.value)
    }

    @Test
    void shouldRemoveFromCache() {
        dataGrid.cachePut(cacheName, key, value)
        dataGrid.cacheRemove(cacheName, key)
        def value = dataGrid.cacheGet(cacheName, key)
        assertThat(value).isNull()
    }

    @Test
    void shouldListCacheKeys() {
        dataGrid.cachePut(cacheName, key, value)
        def keys = dataGrid.cacheKeys(cacheName)
        assertThat(keys).contains(key)
    }

}