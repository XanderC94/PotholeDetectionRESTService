package json;

import java.util.Objects;

public class Upvote {

    private final int markerId;
    private final String token;

    public Upvote(int markerId, String token) {
        this.markerId = markerId;
        this.token = token;
    }

    public int getMarkerId() {
        return markerId;
    }
    public String getToken() { return token; }

    @Override
    public String toString() {
        return "Upvote{" +
                "markerId=" + markerId +
                ", token='" + token + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Upvote)) return false;
        Upvote upvote = (Upvote) o;
        return getMarkerId() == upvote.getMarkerId() &&
                Objects.equals(getToken(), upvote.getToken());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMarkerId(), getToken());
    }
}
