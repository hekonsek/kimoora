package grider

import io.undertow.server.HttpServerExchange
import org.apache.commons.lang3.RandomStringUtils
import org.apache.ignite.cache.query.ScanQuery
import org.apache.ignite.lang.IgniteBiPredicate
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Test
import org.kafkaless.datagrid.DataGridServiceBuilder
import org.kafkaless.datagrid.client.RestClientDataGridService
import org.kafkaless.datagrid.rest.Authentication
import org.kafkaless.datagrid.rest.AuthenticationSubject
import org.kafkaless.datagrid.rest.DataGridServiceRestEndpoint

import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future

import static java.util.UUID.randomUUID
import static org.assertj.core.api.Assertions.assertThat

@Ignore
class CacheStressTest {

    static rest = new DataGridServiceRestEndpoint(new Authentication() {
        @Override
        AuthenticationSubject authenticate(HttpServerExchange exchange) {
            return null
        }
    }, new DataGridServiceBuilder().build()).start()

    static def dataGrid = new RestClientDataGridService('http://localhost:8080')

    static def cacheName = randomUUID().toString()

    def key = randomUUID().toString()

    def value = [name: randomUUID().toString()]


    @BeforeClass
    static void beforeClass() {
        def executor = Executors.newCachedThreadPool()
        List<Future> tasks = []
        (1..1000).each {
            tasks << executor.submit(new Callable() {
                @Override
                Object call() throws Exception {
                    (1..10000).each {
                        def person = [name: RandomStringUtils.randomAlphabetic(10), surname: RandomStringUtils.randomAlphabetic(10)]
                        dataGrid.cachePut(cacheName, randomUUID().toString(), person)
                    }
                    return null
                }
            })
        }
        tasks.forEach{
            it.get()
        }
    }

    // Cache tests

    @Test
    void shouldGetFromCache() {
        def started = System.currentTimeMillis()
        dataGrid.cachePut(cacheName, key, value)
        def value = dataGrid.cacheGet(cacheName, key)
        assertThat(value).isEqualTo(this.value)
        println "Put/get time: ${System.currentTimeMillis() - started}ms"

        started = System.currentTimeMillis()
        def xxx = (rest as DataGridServiceRestEndpoint).@dataGridService.@ignite.getOrCreateCache("cache_" + cacheName).query(new ScanQuery(new IgniteBiPredicate(){
            @Override
            boolean apply(Object o, Object o2) {
                return ((o2 as Map).name as String).contains('a')
            }
        }))
        println "Scan query time: ${System.currentTimeMillis() - started}ms"

        started = System.currentTimeMillis()
        println "Size: ${xxx.size()}"
        println "Size query time: ${System.currentTimeMillis() - started}ms"
    }

}