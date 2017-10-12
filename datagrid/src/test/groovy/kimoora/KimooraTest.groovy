package kimoora

import kimoora.client.KimooraClient
import org.junit.Test

import static java.util.UUID.randomUUID
import static org.assertj.core.api.Assertions.assertThat

class KimooraTest {

    static {
        new KimooraBuilder().build()
    }

    def kimoora = new KimooraClient('http://localhost:8080')

    def function = randomUUID().toString()

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
