package org.kafkaless.datagrid

import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache
import org.apache.ignite.Ignition
import org.apache.ignite.cache.query.SqlFieldsQuery
import org.apache.ignite.configuration.CacheConfiguration
import org.apache.ignite.configuration.IgniteConfiguration
import org.apache.ignite.configuration.PersistentStoreConfiguration
import grider.spi.DataGridService

import javax.cache.expiry.Duration
import javax.cache.expiry.TouchedExpiryPolicy
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

import static java.util.concurrent.TimeUnit.DAYS

class DefaultDataGridService implements DataGridService {

    private final Ignite ignite

    DefaultDataGridService(File igniteDirectory) {
        def persistenceConfig = new PersistentStoreConfiguration().
                setPersistentStorePath("${igniteDirectory.absolutePath}/store").
                setWalStorePath("${igniteDirectory.absolutePath}/wal_store").
                setWalArchivePath("${igniteDirectory.absolutePath}/wal_archive")

        def igniteConfig = new IgniteConfiguration().setPersistentStoreConfiguration(persistenceConfig)
        ignite = Ignition.start(igniteConfig)
        ignite.active(true)
    }

    // SQL operations

    void assertSqlSchema(String table, Map<String, Object> schema) {
        def columnsSql = schema.entrySet().inject('') { result, column ->
            String columnType
            if (column.value == String) {
                columnType = 'varchar'
            } else {
                throw new IllegalArgumentException("Unsupported column type: ${column.value}")
            }
            result + ", ${column.key} ${columnType}"
        }

        Class.forName("org.apache.ignite.IgniteJdbcThinDriver");
        Connection connection = null
        try {
            connection = DriverManager.getConnection("jdbc:ignite:thin://127.0.0.1/")
            connection.createStatement().with { statement ->
                try {
                    statement.executeUpdate("create table ${table} (id varchar PRIMARY KEY${columnsSql}) with \"template=replicated\"");
                } catch (SQLException e) {
                    if (e.cause.message.contains('Table already exists')) {
                    } else {
                        throw e
                    }
                }
            }
        } finally {
            if(connection != null) {
                connection.close()
            }
        }
    }

    void sqlInsert(String table, String id, Map<String, Object> values) {
        def columnsSql = ", ${values.keySet().join(', ')}"
        def valuesSql = values.values().inject('') { result, value ->
            String valueString
            if (value instanceof String) {
                valueString = "'${value}'"
            } else {
                throw new IllegalArgumentException("Unsupported column type: ${value.class}")
            }
            result + ", ${valueString}"
        }
        ignite.getOrCreateCache('SQL_PUBLIC_' + table.toUpperCase()).query(new SqlFieldsQuery("INSERT INTO ${table} (id${columnsSql}) VALUES ('${id}'${valuesSql})"))
    }


    List<List<Object>> sqlQuery(String namespace, String query) {
        ignite.getOrCreateCache('SQL_PUBLIC_' + namespace.toUpperCase()).query(new SqlFieldsQuery(query)).toList()
    }

}
