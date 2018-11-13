package json;

import java.util.Objects;

public class Tuple3<X, Y, Z> {
    private final X x;
    private final Y y;
    private final Z z;

    public Tuple3(final X x, final Y y, final Z z) {
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
        if (!(o instanceof Tuple3)) return false;
        Tuple3<?, ?, ?> tuple3 = (Tuple3<?, ?, ?>) o;
        return Objects.equals(getX(), tuple3.getX()) &&
                Objects.equals(getY(), tuple3.getY()) &&
                Objects.equals(getZ(), tuple3.getZ());
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
