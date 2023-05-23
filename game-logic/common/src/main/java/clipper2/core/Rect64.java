package clipper2.core;

public final class Rect64 {

    public long left;
    public long top;
    public long right;
    public long bottom;

    public Rect64() {
    }

    public Rect64(long l, long t, long r, long b) {
        left = l;
        top = t;
        right = r;
        bottom = b;
    }

    public long getWidth() {
        return right - left;
    }

    public void setWidth(long value) {
        right = left + value;
    }

    public long getHeight() {
        return bottom - top;
    }

    public void setHeight(long value) {
        bottom = top + value;
    }

    public Path64 AsPath() {
        Path64 result = new Path64(4);
        result.add(new Point64(left, top));
        result.add(new Point64(right, top));
        result.add(new Point64(right, bottom));
        result.add(new Point64(left, bottom));
        return result;
    }

    @Override
    public Rect64 clone() {
        Rect64 varCopy = new Rect64();

        varCopy.left = this.left;
        varCopy.top = this.top;
        varCopy.right = this.right;
        varCopy.bottom = this.bottom;

        return varCopy;
    }
}