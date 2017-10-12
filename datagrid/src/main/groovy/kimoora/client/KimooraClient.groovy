package kimoora.client

import com.fasterxml.jackson.databind.ObjectMapper
import kimoora.Kimoora
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

class KimooraClient implements Kimoora {

    private static final JSON = MediaType.parse("application/json; charset=utf-8")

    private final String endpointUrl

    private final client = new OkHttpClient()

    KimooraClient(String endpointUrl) {
        this.endpointUrl = endpointUrl
    }

    @Override
    void registerFunctionDefinition(String function, Map<String, Object> functionDefinition) {
        def body = RequestBody.create(JSON, new ObjectMapper().writeValueAsString(functionDefinition))
        def request = new Request.Builder()
                .url("${endpointUrl}/registerFunctionDefinition/${function}")
                .post(body)
                .build();
        def response = client.newCall(request).execute();
        new ObjectMapper().readValue(response.body().bytes(), Map)
    }

    @Override
    Map<String, Object> getFunctionDefinition(String function) {
        def request = new Request.Builder()
                .url("${endpointUrl}/getFunctionDefinition/${function}")
                .get()
                .build()
        def response = client.newCall(request).execute();
        new ObjectMapper().readValue(response.body().bytes(), Map)
    }

    @Override
    Map<String, Object> invoke(String operation, Map<String, Object> event) {
        def body = RequestBody.create(JSON, new ObjectMapper().writeValueAsString(event))
        def request = new Request.Builder()
                .url("${endpointUrl}/invoke/${operation}")
                .post(body)
                .build();
        def response = client.newCall(request).execute().body().bytes()
        new ObjectMapper().readValue(response, Map)
    }

}