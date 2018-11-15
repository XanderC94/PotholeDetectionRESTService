package json;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Registration {

    private final String token;
    private Set<Integer> ids = new HashSet<>();

    public Registration(String token) {
        this.token = token;
    }

    public Registration(String token, Set<Integer> ids) {
        this.token = token;
        this.ids = ids;
    }

    public String getToken() {
        return token;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Registration)) return false;
        Registration that = (Registration) o;
        return Objects.equals(getToken(), that.getToken());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getToken());
    }

    @Override
    public String toString() {
        return "Registration{" +
                "token='" + token + '\'' +
                '}';
    }

    public Set<Integer> getIds() {
        return ids;
    }
}
