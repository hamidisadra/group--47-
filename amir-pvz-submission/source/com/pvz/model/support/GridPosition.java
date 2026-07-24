package com.pvz.model.support;

import java.util.Objects;

public final class GridPosition {
    public int x;
    public int y;
    public GridPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof GridPosition)) {
            return false;
        }
        GridPosition position = (GridPosition) other;
        return x == position.x && y == position.y;
    }
    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
    public String toUserString() {
        return "(" + (x + 1) + ", " + (y + 1) + ")";
    }
    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
