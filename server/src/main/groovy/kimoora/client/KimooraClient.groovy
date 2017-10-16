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

    private final TokenProvider tokenProvider

    private final String endpointUrl

    private final client = new OkHttpClient()

    KimooraClient(TokenProvider tokenProvider, String endpointUrl) {
        this.tokenProvider = tokenProvider
        this.endpointUrl = endpointUrl
    }

    // User management operations

    @Override
    void addUser(String username, String password, List<String> roles) {
        validateOkResponse(request("addUser/${username}", [password: password, roles: roles]))
    }

    @Override
    String login(String username, String password) {
        request('login', [username: username, password: password])
    }

    // Functions definitions operations

    @Override
    void registerFunctionDefinition(String function, Map<String, Object> functionDefinition) {
        validateOkResponse(request("registerFunctionDefinition/${function}", functionDefinition))
    }

    @Override
    Map<String, Object> getFunctionDefinition(String function) {
        request("getFunctionDefinition/${function}")
    }

    // Cache operations

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

    // Streams operations

    @Override
    void sendToStream(String stream, String eventId, Map<String, Object> event) {
        validateOkResponse(request("sendToStream/${stream}/${eventId}", event))
    }

    @Override
    void addPipe(String pipeId, Map<String, Object> pipeDefinition) {
        validateOkResponse(request("addPipe/${pipeId}", pipeDefinition))
    }

    // Helpers

    private Map<String, Object> request(String uri, Map<String, Object> event) {
        def body = RequestBody.create(JSON, jsonString(event))
        def request = new Request.Builder().url("${endpointUrl}/${uri}").post(body).
                header('Authentication',"Bearer ${tokenProvider.token()}").build()
        def response = client.newCall(request).execute()
        fromJson(response.body().bytes())
    }

    private Map<String, Object> request(String uri) {
        def request = new Request.Builder().url("${endpointUrl}/${uri}").get().
                header('Authentication',"Bearer ${tokenProvider.token()}").build()
        def response = client.newCall(request).execute()
        fromJson(response.body().bytes())
    }

    private void validateOkResponse(Map<String, Object> response) {
        Validate.isTrue(response.status == 'OK', "Expected status to be OK. Found: ${response}")
    }

}