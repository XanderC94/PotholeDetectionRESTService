package json;

import java.util.Objects;

public class Nuple<X, Y, Z> {
    private final X x;
    private final Y y;
    private final Z z;

    public Nuple(final X x, final Y y, final Z z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }


    public Z getZ() {
        return z;
    }

    public Y getY() {
        return y;
    }

    public X getX() {
        return x;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Nuple)) return false;
        Nuple<?, ?, ?> nuple = (Nuple<?, ?, ?>) o;
        return Objects.equals(getX(), nuple.getX()) &&
                Objects.equals(getY(), nuple.getY()) &&
                Objects.equals(getZ(), nuple.getZ());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getX(), getY(), getZ());
    }

    @Override
    public String toString() {
        return "(" + x + y + z + ')';
    }
}
