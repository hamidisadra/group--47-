package ir.ac.pvz.model.support;


import ir.ac.pvz.model.zombies.ProspectorZombie;

public class Dynamite {

    public float remainingSeconds;
    public boolean isExtinguished;

    public Dynamite() {
        remainingSeconds = 10f;
        isExtinguished = false;
    }

    public void explodeAndReverseDirection(ProspectorZombie owner) {
        if (!isExtinguished && owner != null) {
            remainingSeconds = 0f;
        }
    }

    public void extinguishByIce() {
        isExtinguished = true;
    }
}
