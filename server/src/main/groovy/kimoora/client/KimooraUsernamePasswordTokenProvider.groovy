package kimoora.client

import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

import static kimoora.util.Json.fromJson
import static kimoora.util.Json.jsonString

class KimooraUsernamePasswordTokenProvider implements TokenProvider {

    private static final JSON = MediaType.parse("application/json; charset=utf-8")

    private final String endpointUrl

    private final String username

    private final String password

    private final http = new OkHttpClient()

    private String tokenCache

    KimooraUsernamePasswordTokenProvider(String endpointUrl, String username, String password) {
        this.endpointUrl = endpointUrl
        this.username = username
        this.password = password
    }

    @Override
    String token() {
        if(tokenCache != null) {
            return tokenCache
        }
        def body = RequestBody.create(JSON, jsonString([username: username, password: password]))
        def request = new Request.Builder().url("${endpointUrl}/login").post(body).build()
        def response = http.newCall(request).execute()
        tokenCache = fromJson(response.body().bytes()).token
    }

}