package org.kafkaless.datagrid

import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache
import org.apache.ignite.Ignition
import org.apache.ignite.cache.query.SqlFieldsQuery
import org.apache.ignite.configuration.CacheConfiguration
import org.apache.ignite.configuration.IgniteConfiguration
import org.apache.ignite.configuration.PersistentStoreConfiguration

import javax.cache.expiry.Duration
import javax.cache.expiry.TouchedExpiryPolicy
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

import static java.util.concurrent.TimeUnit.DAYS

class DataGridService {

    private final Ignite ignite

    DataGridService(File igniteDirectory) {
        def persistenceConfig = new PersistentStoreConfiguration().
                setPersistentStorePath("${igniteDirectory.absolutePath}/store").
                setWalStorePath("${igniteDirectory.absolutePath}/wal_store").
                setWalArchivePath("${igniteDirectory.absolutePath}/wal_archive")

        def igniteConfig = new IgniteConfiguration().setPersistentStoreConfiguration(persistenceConfig)
        ignite = Ignition.start(igniteConfig)
        ignite.active(true)
    }

    // Cache operations

    private CacheConfiguration<String, Map<String, Object>> cacheConfiguration(String cacheName) {
        new CacheConfiguration<String, Map<String, Object>>().setName("cache_${cacheName}").
                setExpiryPolicyFactory(TouchedExpiryPolicy.factoryOf(new Duration(DAYS, 2)))
    }

    void cachePut(String cacheName, String key, Map<String, Object> value) {
        ignite.getOrCreateCache(cacheConfiguration(cacheName)).put(key, value)
    }

    Map<String, Object> cacheGet(String cacheName, String key) {
        ignite.getOrCreateCache(cacheConfiguration(cacheName)).get(key)
    }

    void cacheRemove(String cacheName, String key) {
        ignite.getOrCreateCache(cacheConfiguration(cacheName)).remove(key)
    }

    List<String> cacheKeys(String cacheName) {
        def entries = ignite.getOrCreateCache(cacheConfiguration(cacheName)).iterator()
        entries.inject([]) { keys, entry -> keys << entry.key; keys }
    }

    // Document operations

    private IgniteCache<String, Map<String, Object>> documentConfiguration(String collection) {
        ignite.getOrCreateCache(new CacheConfiguration<String, Map<String, Object>>().setName("document_${collection}"))
    }

    void documentPut(String collection, String key, Map<String, Object> value) {
        documentConfiguration(collection).put(key, value)
    }

    Map<String, Object> documentGet(String collection, String key) {
        documentConfiguration(collection).get(key)
    }

    void documentRemove(String collection, String key) {
        documentConfiguration(collection).remove(key)
    }

    List<String> documentsKeys(String collection) {
        def entries = documentConfiguration(collection).iterator()
        entries.inject([]) { keys, entry -> keys << entry.key; keys }
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
