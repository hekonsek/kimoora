package kimoora.util

import com.fasterxml.jackson.databind.ObjectMapper

final class Json {

    private static final mapper = new ObjectMapper()

    private Json() {
    }

    static byte[] jsonBytes(Object object) {
        mapper.writeValueAsBytes(object)
    }

    static String jsonString(Object object) {
        mapper.writeValueAsString(object)
    }

    static <T> T fromJson(byte[] json, Class<T> type) {
        mapper.readValue(json, type)
    }

    static <T> T fromJson(String json, Class<T> type) {
        mapper.readValue(json, type)
    }

    static Map<String, Object> fromJson(byte[] json) {
        fromJson(json, Map)
    }

    static Map<String, Object> fromJson(String json) {
        fromJson(json, Map)
    }

}