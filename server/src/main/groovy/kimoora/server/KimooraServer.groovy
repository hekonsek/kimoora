package kimoora.server

import kimoora.Kimoora
import kimoora.invoke.Invoker
import org.apache.ignite.Ignite
import org.apache.ignite.Ignition
import org.apache.ignite.configuration.IgniteConfiguration
import org.apache.ignite.configuration.PersistentStoreConfiguration

class KimooraServer implements Kimoora {

    private final File kimooraHome

    private final Invoker invoker

    private final Authentication authentication

    private Ignite ignite

    private RestEndpoint restEndpoint

    KimooraServer(File kimooraHome, Invoker invoker, Authentication authentication) {
        this.kimooraHome = kimooraHome
        this.invoker = invoker
        this.authentication = authentication
    }

    KimooraServer start() {
        def persistenceConfig = new PersistentStoreConfiguration().
                setPersistentStorePath("${kimooraHome.absolutePath}/store").
                setWalStorePath("${kimooraHome.absolutePath}/wal_store").
                setWalArchivePath("${kimooraHome.absolutePath}/wal_archive")

        def igniteConfig = new IgniteConfiguration().setPersistentStoreConfiguration(persistenceConfig)
        ignite = Ignition.start(igniteConfig)
        ignite.active(true)

        this.restEndpoint = new RestEndpoint(this, authentication).start()

        this
    }

    // Functions registry operations

    void registerFunctionDefinition(String function, Map<String, Object> functionDefinition) {
        ignite.getOrCreateCache('kimoora_functions').put(function, functionDefinition)
    }

    Map<String, Object> getFunctionDefinition(String function) {
        ignite.getOrCreateCache('kimoora_functions').get(function)
    }

    // Invoke operations

    Map<String, Object> invoke(String operation, Map<String, Object> event) {
        invoker.invoke(this, operation, event)
    }

}