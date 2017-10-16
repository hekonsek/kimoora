package kimoora.invoke

import com.fasterxml.jackson.databind.ObjectMapper
import kimoora.server.KimooraServer
import kimoora.util.docker.CommandLineDocker
import kimoora.util.docker.ContainerBuilder
import kimoora.util.process.Command
import kimoora.util.process.DefaultProcessManager
import kimoora.util.process.SudoResolver

class LocalDockerExecInvoker implements Invoker {

    private final json = new ObjectMapper()

    @Override
    Map<String, Object> invoke(KimooraServer kimoora, String operation, Map<String, Object> event) {
        def eventJson = json.writeValueAsString(event)
        def functionDefinition = kimoora.getFunctionDefinition(operation)
        if (functionDefinition == null) {
            throw new IllegalArgumentException('Unknown function.')
        }
        def artifact = functionDefinition.artifact as String

        def environment = [KIMOORA_FRONT_DOOR_ENDPOINT: 'http://localhost:8080']

        def imageExists = new DefaultProcessManager(new SudoResolver()).execute(Command.cmd("docker images ${artifact}")).size() > 1
        if (!imageExists) {
            new DefaultProcessManager(new SudoResolver()).execute(Command.cmd("docker pull ${artifact}")).size()
        }
        def commandResponse = new CommandLineDocker(new DefaultProcessManager(new SudoResolver())).execute(new ContainerBuilder(artifact).cleanUp(true).net('host').environment(environment).arguments(eventJson).build())
        def response = commandResponse.first()

        json.readValue(response, Map)
    }

}