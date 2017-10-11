package org.kafkaless.datagrid.client

import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import grider.spi.DataGridService

class RestClientDataGridService implements DataGridService {

    private static final JSON = MediaType.parse("application/json; charset=utf-8")

    private final String endpointUrl

    private final client = new OkHttpClient()

    RestClientDataGridService(String endpointUrl) {
        this.endpointUrl = endpointUrl
    }

    void cachePut(String cacheName, String key, Map<String, Object> value) {
        def body = RequestBody.create(JSON, new ObjectMapper().writeValueAsString(value))
        Request request = new Request.Builder()
                .url("${endpointUrl}/cachePut/${cacheName}/${key}")
                .post(body)
                .build();
        def response = client.newCall(request).execute();
        new ObjectMapper().readValue(response.body().bytes(), Map)
    }

    Map<String, Object> cacheGet(String cacheName, String key) {
        def request = new Request.Builder()
                .url("${endpointUrl}/cacheGet/${cacheName}/${key}")
                .get()
                .build();
        def response = client.newCall(request).execute();
        new ObjectMapper().readValue(response.body().bytes(), Map)
    }

    @Override
    void cacheRemove(String cacheName, String key) {
        Request request = new Request.Builder()
                .url("${endpointUrl}/cacheRemove/${cacheName}/${key}")
                .get()
                .build();
        def response = client.newCall(request).execute();
        new ObjectMapper().readValue(response.body().bytes(), Map)
    }

    @Override
    List<String> cacheKeys(String cacheName) {
        def request = new Request.Builder()
                .url("${endpointUrl}/cacheKeys/${cacheName}")
                .get()
                .build();
        def response = client.newCall(request).execute()
        new ObjectMapper().readValue(response.body().bytes(), Map).keys
    }

    @Override
    void documentPut(String collection, String key, Map<String, Object> value) {

    }

    @Override
    Map<String, Object> documentGet(String collection, String key) {
        return null
    }

    @Override
    void documentRemove(String collection, String key) {

    }

    @Override
    List<String> documentsKeys(String collection) {
        return null
    }

    @Override
    void assertSqlSchema(String table, Map<String, Object> schema) {

    }

    @Override
    void sqlInsert(String table, String id, Map<String, Object> values) {

    }

    @Override
    List<List<Object>> sqlQuery(String namespace, String query) {
        return null
    }
}
