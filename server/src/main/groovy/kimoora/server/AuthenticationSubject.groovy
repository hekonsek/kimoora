package kimoora.server

class AuthenticationSubject {

    private final String username

    private final List<String> roles

    AuthenticationSubject(String username, List<String> roles) {
        this.username = username
        this.roles = roles
    }

    String username() {
        username
    }

    List<String> roles() {
        roles
    }

}