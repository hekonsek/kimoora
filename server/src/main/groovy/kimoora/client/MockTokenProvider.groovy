package kimoora.client

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm

import static com.auth0.jwt.algorithms.Algorithm.HMAC256

class MockTokenProvider implements TokenProvider {

    private final String secret

    private final String tenant

    private final String username

    private final String[] roles

    private final Algorithm algorithm

    MockTokenProvider(String secret, String tenant, String username, String... roles) {
        this.secret = secret
        this.tenant = tenant
        this.username = username
        this.roles = roles

        algorithm = HMAC256(secret)
    }

    @Override
    String token() {
        JWT.create().
                withClaim("username", username).
                withClaim("tenant", tenant).
                withArrayClaim("roles", roles).
                sign(algorithm)
    }

}