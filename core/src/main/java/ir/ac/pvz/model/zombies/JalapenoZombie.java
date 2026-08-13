package ir.ac.pvz.model.zombies;

import ir.ac.pvz.model.core.Zombie;

public class JalapenoZombie extends Zombie {

    public float fuseSeconds;

    public float secondsInLawn;
    public boolean exploded;

    public JalapenoZombie() {
        super(0.1f, 190, 100, 150);
        this.fuseSeconds = 10f;
    }

    public void addTime(float seconds) {
        if (!exploded) {
            secondsInLawn += seconds;
        }
    }

    public boolean isReadyToExplode() {
        return !exploded && secondsInLawn >= fuseSeconds;
    }
}