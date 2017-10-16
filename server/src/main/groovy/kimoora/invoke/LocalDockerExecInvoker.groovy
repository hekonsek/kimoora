package kimoora.invoke

import com.fasterxml.jackson.databind.ObjectMapper
import kimoora.server.KimooraServer
import kpipes.binding.util.docker.CommandLineDocker
import kpipes.binding.util.docker.ContainerBuilder
import kpipes.binding.util.process.Command
import kpipes.binding.util.process.DefaultProcessManager
import kpipes.binding.util.process.SudoResolver

class LocalDockerExecInvoker implements Invoker {

    private final json = new ObjectMapper()

    @Override
    Map<String, Object> invoke(KimooraServer kimoora, String operation, Map<String, Object> event) {
        def eventJson = json.writeValueAsString(event)
        def artifact = kimoora.getFunctionDefinition(operation).artifact as String

        def environment = [FRONT_DOOR_ENDPOINT: 'localhost']

        new DefaultProcessManager(new SudoResolver()).execute(Command.cmd("docker pull ${artifact}")).size()
        def commandResponse = new CommandLineDocker(new DefaultProcessManager(new SudoResolver())).execute(new ContainerBuilder(artifact).cleanUp(true).net('host').environment(environment).arguments(eventJson).build())
        def response = commandResponse.first()

        json.readValue(response, Map)
    }

}