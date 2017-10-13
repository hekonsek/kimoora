package kimoora.client

import kimoora.Kimoora
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.apache.commons.lang3.Validate

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
        validateOkResponse(request("registerFunctionDefinition/${function}", functionDefinition))
    }

    @Override
    Map<String, Object> getFunctionDefinition(String function) {
        request("getFunctionDefinition/${function}")
    }

    @Override
    void cachePut(String cacheName, String key, Map<String, Object> value) {
        validateOkResponse(request("cachePut/${cacheName}/${key}", value))
    }

    @Override
    Map<String, Object> cacheGet(String cacheName, String key) {
        request("cacheGet/${cacheName}/${key}")
    }

    @Override
    void cacheRemove(String cacheName, String key) {
        validateOkResponse(request("cacheRemove/${cacheName}/${key}"))
    }

    @Override
    List<String> cacheKeys(String cacheName) {
        request("cacheKeys/${cacheName}").keys as List
    }

    // Invoke operations

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

    private Map<String, Object> request(String uri) {
        def request = new Request.Builder().url("${endpointUrl}/${uri}").get().build()
        def response = client.newCall(request).execute()
        fromJson(response.body().bytes())
    }

    private void validateOkResponse(Map<String, Object> response) {
        Validate.isTrue(response.status == 'OK')
    }

}