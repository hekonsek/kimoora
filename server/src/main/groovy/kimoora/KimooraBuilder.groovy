package kimoora

import io.undertow.server.HttpServerExchange
import kimoora.invoke.Invoker
import kimoora.invoke.LocalDockerExecInvoker
import kimoora.server.Authentication
import kimoora.server.AuthenticationSubject
import kimoora.server.KimooraServer

import static com.google.common.io.Files.createTempDir

class KimooraBuilder {

    private Invoker invoker = new LocalDockerExecInvoker()

    KimooraBuilder invoker(Invoker invoker) {
        this.invoker = invoker
        this
    }

    Kimoora build() {
        new KimooraServer(createTempDir(), invoker, new Authentication() {
            @Override
            AuthenticationSubject authenticate(HttpServerExchange exchange) {
                return null
            }
        }).start()
    }

}
