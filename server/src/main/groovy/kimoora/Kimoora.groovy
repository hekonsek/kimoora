package kimoora

interface Kimoora {

    // User management operations

    void addUser(String username, String password, List<String> roles)

    String login(String username, String password)

    // Functions definitions operations

    void registerFunctionDefinition(String function, Map<String, Object> functionDefinition)

    Map<String, Object> getFunctionDefinition(String function)

    // Cache operations

    void cachePut(String cacheName, String key, Map<String, Object> value)

    Map<String, Object> cacheGet(String cacheName, String key)

    void cacheRemove(String cacheName, String key)

    List<String> cacheKeys(String cacheName)

    // Document operations

    void documentPut(String collection, String key, Map<String, Object> value)

    Map<String, Object> documentGet(String collection, String key)

    void documentRemove(String collection, String key)

    List<String> documentsKeys(String collection)

    // Invoke operations

    Map<String, Object> invoke(String operation, Map<String, Object> event)

    // Streams operations

    void sendToStream(String stream, String eventId, Map<String, Object> event)

    int streamBacklogSize(String stream)

    void addPipe(String pipeId, Map<String, Object> pipeDefinition)

}