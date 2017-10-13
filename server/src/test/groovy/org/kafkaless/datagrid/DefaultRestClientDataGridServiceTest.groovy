package org.kafkaless.datagrid

import org.apache.commons.lang3.RandomStringUtils
import org.junit.Ignore
import org.junit.Test

import static java.util.UUID.randomUUID
import static org.assertj.core.api.Assertions.assertThat

@Ignore
class DefaultRestClientDataGridServiceTest {

    def collection = randomUUID().toString()

    def key = randomUUID().toString()

    def value = [foo: randomUUID().toString()]

    def table = RandomStringUtils.randomAlphabetic(20)

    def schema = [foo: String]

    // Document operations tests

    @Test
    void shouldGetDocument() {
        dataGrid.documentPut(collection, key, value)
        def value = dataGrid.documentGet(collection, key)
        assertThat(value).isEqualTo(this.value)
    }

    @Test
    void shouldRemoveFromDocuments() {
        dataGrid.documentPut(collection, key, value)
        dataGrid.documentRemove(collection, key)
        def value = dataGrid.documentGet(collection, key)
        assertThat(value).isNull()
    }

    @Test
    void shouldListDocumentKeys() {
        dataGrid.documentPut(collection, key, value)
        def keys = dataGrid.documentsKeys(collection)
        assertThat(keys).contains(key)
    }

    // SQL operations tests

    @Test
    void shouldExecuteSqlCount() {
        dataGrid.assertSqlSchema(table, schema)
        dataGrid.sqlInsert(table, key, value)
        def countResult = dataGrid.sqlQuery(table, "SELECT COUNT(*) FROM ${table}")
        assertThat(countResult[0][0]).isEqualTo(1)
    }

}