package kimoora

import kimoora.client.KimooraClient
import kimoora.client.KimooraUsernamePasswordTokenProvider
import org.junit.Test

import static java.util.UUID.randomUUID
import static kimoora.util.Ids.randomStringId
import static kimoora.util.Ids.uuid
import static org.assertj.core.api.Assertions.assertThat
import static org.awaitility.Awaitility.await

class KimooraTest {

    static {
        new KimooraBuilder().build()
    }

    def endpointUrl = 'http://localhost:8080'

    def kimoora = new KimooraClient(new KimooraUsernamePasswordTokenProvider(endpointUrl, 'admin', 'admin'), endpointUrl)

    def cacheName = uuid()

    def key = uuid()

    def value = [foo: uuid()]

    def event = [key: uuid(), payload: value, metadata: [:]]

    def collection = randomUUID().toString()

    def functionId = uuid()

    def function = [artifact: 'hekonsek/echogo']

    def pipeId = "pipeId-${randomStringId()}"

    def pipe = [from: randomStringId(), function: functionId, cache: randomStringId()]

    // Functions definitions operations tests

    @Test
    void shouldListFunctions() {
        // Given
        kimoora.registerFunctionDefinition(functionId, function)

        // When
        def functions = kimoora.listFunctionsDefinitions()

        // Then
        def registeredFunction = functions.find { it.key == functionId }
        assertThat(registeredFunction).isNotNull()
    }

    // Cache operations tests

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

    // Document operations tests

    @Test
    void shouldGetDocument() {
        kimoora.documentPut(collection, key, value)
        def value = kimoora.documentGet(collection, key)
        assertThat(value).isEqualTo(this.value)
    }

    @Test
    void shouldRemoveFromDocuments() {
        kimoora.documentPut(collection, key, value)
        kimoora.documentRemove(collection, key)
        def value = kimoora.documentGet(collection, key)
        assertThat(value).isNull()
    }

    @Test
    void shouldListDocumentKeys() {
        kimoora.documentPut(collection, key, value)
        def keys = kimoora.documentsKeys(collection)
        assertThat(keys).contains(key)
    }

    // Invoke operations tests

    @Test
    void shouldExecuteDockerFunction() {
        // Given
        kimoora.registerFunctionDefinition(functionId, [artifact: 'hekonsek/echogo'])

        // When
        def response = kimoora.invoke(functionId, [hello: 'world'])

        // Then
        assertThat(response.payload as Map).containsEntry('hello', 'world')
    }

    @Test
    void shouldInjectTokenIntoInvokingEventMetadata() {
        // Given
        kimoora.registerFunctionDefinition(functionId, [artifact: 'hekonsek/echogo'])

        // When
        def response = kimoora.invoke(functionId, [hello: 'world'])

        // Then
        assertThat(response.metadata as Map).containsKey('token')
    }

    // Streams tests

    @Test
    void shouldCacheProcessedStream() {
        // Given
        kimoora.registerFunctionDefinition(functionId, [artifact: 'hekonsek/echogo'])
        kimoora.addPipe(pipeId, pipe)

        // When
        kimoora.streamSendTo(pipe.from, event)

        // Then
        await().untilAsserted {
            def cachedResult = kimoora.cacheGet(pipe.cache, event.key)
            assertThat(cachedResult).isNotNull()
        }
    }

    @Test
    void shouldMulticastStream() {
        // Given
        kimoora.registerFunctionDefinition(functionId, [artifact: 'hekonsek/echogo'])
        def multicast = [randomStringId(), randomStringId()]
        pipe.multicast = multicast
        pipe.to = null
        kimoora.addPipe(pipeId, pipe)

        // When
        kimoora.streamSendTo(pipe.from, event)

        // Then
        await().untilAsserted {
            def firstMulticastResult = kimoora.streamBacklogSize(multicast[0])
            assertThat(firstMulticastResult).isEqualTo(1)
            def secondMulticastResult = kimoora.streamBacklogSize(multicast[1])
            assertThat(secondMulticastResult).isEqualTo(1)
        }
    }


}
