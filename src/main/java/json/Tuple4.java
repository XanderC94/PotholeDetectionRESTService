package json;

import java.util.Objects;

public class Tuple4<X, Y, W, Z> {

    private final X x;
    private final Y y;
    private final W w;
    private final Z z;

    public Tuple4(final X x, final Y y, final W w, final Z z) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.z = z;
    }


    public Z getZ() {
        return z;
    }

    public Y getY() {
        return y;
    }

    public W getW() { return w; }

    public X getX() {
        return x;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tuple4)) return false;
        Tuple4<?, ?, ?, ?> tuple4 = (Tuple4<?, ?, ?, ?>) o;
        return Objects.equals(getX(), tuple4.getX()) &&
                Objects.equals(getY(), tuple4.getY()) &&
                Objects.equals(getW(), tuple4.getW()) &&
                Objects.equals(getZ(), tuple4.getZ());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getX(), getY(), getW(), getZ());
    }

    @Override
    public String toString() {
        return String.format("Q(%s, %s, %s, %s)", getX(), getY(), getX(), getZ());
    }


}
