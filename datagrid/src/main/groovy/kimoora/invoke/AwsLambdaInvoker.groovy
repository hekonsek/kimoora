package kimoora.invoke

import com.amazonaws.services.lambda.AWSLambdaClientBuilder
import com.amazonaws.services.lambda.model.InvokeRequest
import com.fasterxml.jackson.databind.ObjectMapper
import kimoora.server.KimooraServer

import static org.apache.commons.lang3.StringUtils.isNotBlank

class AwsLambdaInvoker implements Invoker {

    private final json = new ObjectMapper()

    private final lambda = AWSLambdaClientBuilder.defaultClient()

    @Override
    Map<String, Object> invoke(KimooraServer kimoora, String operation, Map<String, Object> event) {
        def eventJson = json.writeValueAsString(event)
        def response = lambda.invoke(new InvokeRequest().withFunctionName(operation).withPayload(eventJson))
        if(isNotBlank(response.functionError)) {
            throw new RuntimeException("Cannot execute lambda function. Reason: ${response.functionError}")
        }
        def responseBuffer = response.payload
        def responseBytes = new byte[responseBuffer.remaining()]
        responseBuffer.get(responseBytes)
        json.readValue(responseBytes, Map)
    }

}