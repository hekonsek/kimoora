package kimoora

import com.fasterxml.jackson.databind.ObjectMapper
import kimoora.invoke.Invoker
import org.apache.ignite.Ignite
import org.apache.ignite.Ignition
import org.apache.ignite.configuration.IgniteConfiguration
import org.apache.ignite.configuration.PersistentStoreConfiguration

class Kimoora {

    private final File kimooraHome

    private final Invoker invoker

    private Ignite ignite

    Kimoora(File kimooraHome, Invoker invoker) {
        this.kimooraHome = kimooraHome
        this.invoker = invoker
    }

    Kimoora start() {
        def persistenceConfig = new PersistentStoreConfiguration().
                setPersistentStorePath("${kimooraHome.absolutePath}/store").
                setWalStorePath("${kimooraHome.absolutePath}/wal_store").
                setWalArchivePath("${kimooraHome.absolutePath}/wal_archive")

        def igniteConfig = new IgniteConfiguration().setPersistentStoreConfiguration(persistenceConfig)
        ignite = Ignition.start(igniteConfig)
        ignite.active(true)
        this
    }

    // Functions registry

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

    public static void main(String[] args) {
        def xxx = new KimooraBuilder().build()
        xxx.registerFunctionDefinition('func', [artifact: 'hekonsek/echogo'])
        println xxx.invoke('func', ["hello":"world"])
        println xxx.invoke('func', ["hello":"world"])
        println xxx.invoke('func', ["hello":"world"])
        println xxx.invoke('func', ["hello":"world"])
        println xxx.invoke('func', ["hello":"world"])
        println xxx.invoke('func', ["hello":"world"])
        println xxx.invoke('func', ["hello":"world"])
        println xxx.invoke('func', ["hello":"world"])
    }

}