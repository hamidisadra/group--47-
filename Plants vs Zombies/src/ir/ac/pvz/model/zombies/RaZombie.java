package ir.ac.pvz.model.zombies;

import ir.ac.pvz.model.core.Zombie;

public class RaZombie extends Zombie {
    private final int maxStolenSuns;
    private int stolenSuns;
    public RaZombie() {
        super("RaZombie");
        this.maxStolenSuns = Math.max(1, (int) Math.round(
                ir.ac.pvz.model.support.ZombieDataRepository.getInstance()
                        .getNumber("RaZombie", "MaxClaimedSunCurrency", 250d)));
        this.stolenSuns = 0;
    }
    public void stealSun(int amount) {
        int capacity = maxStolenSuns - stolenSuns;
        stolenSuns += Math.min(Math.max(0, amount), capacity);
    }
    public int getRemainingSunCapacity() {
        return Math.max(0, maxStolenSuns - stolenSuns);
    }
    public int releaseStolenSuns() {
        int released = stolenSuns;
        stolenSuns = 0;
        return released;
    }
}
