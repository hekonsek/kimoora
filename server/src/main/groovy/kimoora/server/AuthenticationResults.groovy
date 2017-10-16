package kimoora.server

class AuthenticationResults {

    private final String token

    private final String username

    private final List<String> roles

    AuthenticationResults(String token, String username, List<String> roles) {
        this.token = token
        this.username = username
        this.roles = roles
    }

    String token() {
        return token
    }

    String username() {
        return username
    }

    List<String> roles() {
        return roles
    }

}