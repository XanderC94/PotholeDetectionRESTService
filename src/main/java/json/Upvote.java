package json;

import java.util.Objects;

public class Upvote {

    private final int markerId;

    public Upvote(int markerId) {
        this.markerId = markerId;
    }

    public int getMarkerId() {
        return markerId;
    }

    @Override
    public String toString() {
        return "Upvote{" +
                "markerId=" + markerId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Upvote)) return false;
        Upvote upvote = (Upvote) o;
        return getMarkerId() == upvote.getMarkerId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMarkerId());
    }
}
