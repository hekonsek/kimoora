package kimoora

interface Kimoora {

    void registerFunctionDefinition(String function, Map<String, Object> functionDefinition)

    Map<String, Object> getFunctionDefinition(String function)

    // Cache operations

    void cachePut(String cacheName, String key, Map<String, Object> value)

    Map<String, Object> cacheGet(String cacheName, String key)

    void cacheRemove(String cacheName, String key)

    List<String> cacheKeys(String cacheName)

    // Invoke operations

    Map<String, Object> invoke(String operation, Map<String, Object> event)

}