package ir.ac.pvz.model.zombies;

public class BeghouledZombie {
    private int lane;
    private int column;
    private int ticksWaited;

    public BeghouledZombie(int lane, int column) {
        this.lane = lane;
        this.column = column;
    }

    public int getLane() {
        return lane;
    }

    public int getColumn() {
        return column;
    }

    public int getTicksWaited() {
        return ticksWaited;
    }

    public void addTick() {
        ticksWaited++;
    }

    public void resetTicks() {
        ticksWaited = 0;
    }

    public void moveLeft() {
        column--;
    }
}