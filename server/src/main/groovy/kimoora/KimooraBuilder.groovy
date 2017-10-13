package kimoora

import kimoora.invoke.Invoker
import kimoora.invoke.LocalDockerExecInvoker
import kimoora.server.JwtAuthentication
import kimoora.server.KimooraServer

import static com.google.common.io.Files.createTempDir

class KimooraBuilder {

    private final String tokenSecret

    private File kimooraHome = createTempDir()

    private Invoker invoker = new LocalDockerExecInvoker()

    KimooraBuilder(String tokenSecret) {
        this.tokenSecret = tokenSecret
    }

    KimooraBuilder kimooraHome(File kimooraHome) {
        this.kimooraHome = kimooraHome
        this
    }

    KimooraBuilder invoker(Invoker invoker) {
        this.invoker = invoker
        this
    }

    Kimoora build() {
        new KimooraServer(kimooraHome, invoker, new JwtAuthentication(tokenSecret)).start()
    }

}
