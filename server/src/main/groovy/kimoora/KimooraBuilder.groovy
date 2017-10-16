package kimoora

import kimoora.invoke.Invoker
import kimoora.invoke.LocalDockerExecInvoker
import kimoora.server.JwtAuthenticationProvider
import kimoora.server.KimooraServer

import static com.google.common.io.Files.createTempDir

class KimooraBuilder {

    private File kimooraHome = createTempDir()

    private Invoker invoker = new LocalDockerExecInvoker()

    KimooraBuilder kimooraHome(File kimooraHome) {
        this.kimooraHome = kimooraHome
        this
    }

    KimooraBuilder invoker(Invoker invoker) {
        this.invoker = invoker
        this
    }

    Kimoora build() {
        def authorization = new JwtAuthenticationProvider()
        new KimooraServer(kimooraHome, invoker, authorization).start().with {
            authorization.setKimoora(it)
        }
    }

}
