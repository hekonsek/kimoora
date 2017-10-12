package kimoora

import kimoora.invoke.AwsLambdaInvoker
import kimoora.invoke.Invoker
import kimoora.invoke.LocalDockerExecInvoker

import static com.google.common.io.Files.createTempDir

class KimooraBuilder {

    private Invoker invoker = new LocalDockerExecInvoker()

    KimooraBuilder invoker(Invoker invoker) {
        this.invoker = invoker
        this
    }

    Kimoora build() {
        new Kimoora(createTempDir(), invoker).start()
    }

}
