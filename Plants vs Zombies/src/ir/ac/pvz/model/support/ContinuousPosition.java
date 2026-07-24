package ir.ac.pvz.model.support;

public class ContinuousPosition {
    public float x;
    public int y;
    public ContinuousPosition(float x, int y) {
        this.x = x;
        this.y = y;
    }
    public String toUserString() {
        return (x + 1f) + ", " + (y + 1);
    }
    @Override
    public String toString() {
        return x + ", " + y;
    }
}
