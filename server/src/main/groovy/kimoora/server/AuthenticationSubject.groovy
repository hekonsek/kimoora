package kimoora.server

class AuthenticationSubject {

    private final String tenant

    private final String username

    private final List<String> roles

    AuthenticationSubject(String tenant, String username, List<String> roles) {
        this.tenant = tenant
        this.username = username
        this.roles = roles
    }

    String tenant() {
        tenant
    }

    String username() {
        username
    }

    List<String> roles() {
        roles
    }

}