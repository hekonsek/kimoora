package kimoora.client

import kimoora.Kimoora
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

import static kimoora.util.Json.fromJson
import static kimoora.util.Json.jsonString

class KimooraClient implements Kimoora {

    private static final JSON = MediaType.parse("application/json; charset=utf-8")

    private final String endpointUrl

    private final client = new OkHttpClient()

    KimooraClient(String endpointUrl) {
        this.endpointUrl = endpointUrl
    }

    @Override
    void registerFunctionDefinition(String function, Map<String, Object> functionDefinition) {
        request("registerFunctionDefinition/${function}", functionDefinition)
    }

    @Override
    Map<String, Object> getFunctionDefinition(String function) {
        def request = new Request.Builder()
                .url("${endpointUrl}/getFunctionDefinition/${function}")
                .get()
                .build()
        def response = client.newCall(request).execute();
        fromJson(response.body().bytes())
    }

    @Override
    Map<String, Object> invoke(String operation, Map<String, Object> event) {
        request("invoke/${operation}", event)
    }

    // Helpers

    private Map<String, Object> request(String uri, Map<String, Object> event) {
        def body = RequestBody.create(JSON, jsonString(event))
        def request = new Request.Builder().url("${endpointUrl}/${uri}").post(body).build()
        def response = client.newCall(request).execute()
        fromJson(response.body().bytes())
    }

}