package com.pvz.model.zombies;

import com.pvz.model.core.Zombie;

public class RaZombie extends Zombie {

    private static final int MAX_STOLEN_SUNS = 250;

    private int stolenSuns;

    public RaZombie() {
        super(0.2f, 190, 100, 100);
        this.stolenSuns = 0;
    }

    public void stealSun(int amount) {
        int capacity = MAX_STOLEN_SUNS - stolenSuns;
        stolenSuns += Math.min(Math.max(0, amount), capacity);
    }

    public int getRemainingSunCapacity() {
        return Math.max(0, MAX_STOLEN_SUNS - stolenSuns);
    }

    public int releaseStolenSuns() {
        int released = stolenSuns;
        stolenSuns = 0;
        return released;
    }
}
