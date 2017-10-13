package kimoora

import kimoora.client.KimooraClient
import org.junit.Test

import static kimoora.util.Ids.uuid
import static org.assertj.core.api.Assertions.assertThat

class KimooraTest {

    static {
        new KimooraBuilder().build()
    }

    def kimoora = new KimooraClient('http://localhost:8080')

    def cacheName = uuid()

    def key = uuid()

    def value = [foo: uuid()]

    def function = uuid()

    @Test
    void shouldGetFromCache() {
        kimoora.cachePut(cacheName, key, value)
        def value = kimoora.cacheGet(cacheName, key)
        assertThat(value).isEqualTo(this.value)
    }

    @Test
    void shouldRemoveFromCache() {
        kimoora.cachePut(cacheName, key, value)
        kimoora.cacheRemove(cacheName, key)
        def value = kimoora.cacheGet(cacheName, key)
        assertThat(value).isNull()
    }

    @Test
    void shouldListCacheKeys() {
        kimoora.cachePut(cacheName, key, value)
        def keys = kimoora.cacheKeys(cacheName)
        assertThat(keys).contains(key)
    }

    // Invoke operations tests

    @Test
    void shouldExecuteDockerFunction() {
        // Given
        kimoora.registerFunctionDefinition(function, [artifact: 'hekonsek/echogo'])

        // When
        def response = kimoora.invoke(function, [hello: 'world'])

        // Then
        assertThat(response).containsEntry('hello', 'world')
    }

}
