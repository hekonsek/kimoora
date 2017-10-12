package grider

import org.junit.Ignore
import org.junit.Test

import static java.util.UUID.randomUUID
import static org.assertj.core.api.Assertions.assertThat

@Ignore
class DataGridServiceTest {


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