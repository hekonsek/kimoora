package kimoora.server

import com.auth0.jwt.JWT
import kimoora.Kimoora
import kimoora.invoke.Invoker
import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache
import org.apache.ignite.Ignition
import org.apache.ignite.configuration.CacheConfiguration
import org.apache.ignite.configuration.IgniteConfiguration
import org.apache.ignite.configuration.PersistentStoreConfiguration

import javax.cache.expiry.Duration
import javax.cache.expiry.TouchedExpiryPolicy

import static com.auth0.jwt.algorithms.Algorithm.HMAC256
import static java.util.concurrent.TimeUnit.DAYS
import static kimoora.util.Ids.randomStringId

class KimooraServer implements Kimoora {

    private final File kimooraHome

    private final Invoker invoker

    private final Authentication authentication

    private Ignite ignite

    private RestEndpoint restEndpoint

    KimooraServer(File kimooraHome, Invoker invoker, Authentication authentication) {
        this.kimooraHome = kimooraHome
        this.invoker = invoker
        this.authentication = authentication
    }

    KimooraServer start() {
        def persistenceConfig = new PersistentStoreConfiguration().
                setPersistentStorePath("${kimooraHome.absolutePath}/store").
                setWalStorePath("${kimooraHome.absolutePath}/wal_store").
                setWalArchivePath("${kimooraHome.absolutePath}/wal_archive")

        def igniteConfig = new IgniteConfiguration().setPersistentStoreConfiguration(persistenceConfig)
        ignite = Ignition.start(igniteConfig)
        ignite.active(true)

        this.restEndpoint = new RestEndpoint(this, authentication).start()

        def tokenCache = ignite.getOrCreateCache('kimoora_jwt_secret')
        if(!tokenCache.containsKey('secret')) {
            tokenCache.put('secret', randomStringId())
        }

        addUser('admin', 'admin', ['admin'])

        this
    }

    // User management operations

    @Override
    void addUser(String username, String password, List<String> roles) {
        if(ignite.getOrCreateCache('kimoora_users').containsKey(username)) {
            throw new IllegalStateException('User already exists.')
        }
        ignite.getOrCreateCache('kimoora_users').put(username, [password: password, roles: roles])
    }

    @Override
    String login(String username, String password) {
        def user = ignite.getOrCreateCache('kimoora_users').get(username) as Map
        if(user.password != password) {
            throw new IllegalArgumentException('Invalid login attempt.')
        }

        def tokenSecret = ignite.getOrCreateCache('kimoora_jwt_secret').get('secret') as String
        def algorithm = HMAC256(tokenSecret)
        String[] roles = (user.roles as List).toArray()
        JWT.create().
                withSubject(username).withArrayClaim("roles", roles).
                sign(algorithm)
    }

    // Functions definitions operations

    void registerFunctionDefinition(String function, Map<String, Object> functionDefinition) {
        ignite.getOrCreateCache('kimoora_functions').put(function, functionDefinition)
    }

    Map<String, Object> getFunctionDefinition(String function) {
        ignite.getOrCreateCache('kimoora_functions').get(function)
    }

    // Cache operations

    private IgniteCache<String, Map<String, Object>> cache(String cacheName) {
        ignite.getOrCreateCache(new CacheConfiguration<String, Map<String, Object>>().setName("cache_${cacheName}").
                setExpiryPolicyFactory(TouchedExpiryPolicy.factoryOf(new Duration(DAYS, 2))))
    }

    void cachePut(String cacheName, String key, Map<String, Object> value) {
        cache(cacheName).put(key, value)
    }

    Map<String, Object> cacheGet(String cacheName, String key) {
        cache(cacheName).get(key)
    }

    void cacheRemove(String cacheName, String key) {
        cache(cacheName).remove(key)
    }

    List<String> cacheKeys(String cacheName) {
        def entries = cache(cacheName).iterator()
        entries.inject([]) { keys, entry -> keys << entry.key; keys }
    }

    // Invoke operations

    Map<String, Object> invoke(String operation, Map<String, Object> event) {
        invoker.invoke(this, operation, event)
    }

}