package grider.spi

interface DataGridService {

    // SQL operations

    void assertSqlSchema(String table, Map<String, Object> schema)

    void sqlInsert(String table, String id, Map<String, Object> values)

    List<List<Object>> sqlQuery(String namespace, String query)

}