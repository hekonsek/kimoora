package grider.spi

interface DataGridService {

    // Cache operations

    void cacheRemove(String cacheName, String key)

    List<String> cacheKeys(String cacheName)

    // Document operations

    void documentPut(String collection, String key, Map<String, Object> value)

    Map<String, Object> documentGet(String collection, String key)

    void documentRemove(String collection, String key)

    List<String> documentsKeys(String collection)

    // SQL operations

    void assertSqlSchema(String table, Map<String, Object> schema)

    void sqlInsert(String table, String id, Map<String, Object> values)

    List<List<Object>> sqlQuery(String namespace, String query)

}