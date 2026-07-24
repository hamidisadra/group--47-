package ir.ac.pvz.model.support;

public class TickClock {
    public int ticksPerSecond;
    public int currentTick;
    public TickClock(int ticksPerSecond) {
        this.ticksPerSecond = ticksPerSecond;
        this.currentTick = 0;
    }
    public void advance(int count) {
        currentTick += count;
    }
    public float toSeconds(int ticks) {
        return (float) ticks / ticksPerSecond;
    }
    public float getElapsedSeconds() {
        return toSeconds(currentTick);
    }
    public float getTickDurationSeconds() {
        return 1f / ticksPerSecond;
    }
    public int getCurrentTick() {
        return currentTick;
    }
}
