package kimoora.invoke

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.base.MoreObjects
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
        (MoreObjects.firstNonNull(event.metadata.environment as Map, [:])).entrySet().each {
            environment[it.key] = it.value
        }

        def imageExists = new DefaultProcessManager(new SudoResolver()).execute(Command.cmd("docker images ${artifact}")).size() > 1
        if (!imageExists) {
            new DefaultProcessManager(new SudoResolver()).execute(Command.cmd("docker pull ${artifact}")).size()
        }
        def commandResponse = new CommandLineDocker(new DefaultProcessManager(new SudoResolver())).execute(new ContainerBuilder(artifact).cleanUp(true).net('host').environment(environment).arguments(eventJson).build())
        def response = commandResponse.first()

        try {
            json.readValue(response, Map)
        } catch (JsonParseException e) {
            throw new RuntimeException("Cannot parse JSON: ${commandResponse}. Operation: ${operation}. Event: ${event}")
        }
    }

}