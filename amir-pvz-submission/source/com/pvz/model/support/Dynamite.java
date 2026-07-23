package com.pvz.model.support;

import com.pvz.model.zombies.ProspectorZombie;

public class Dynamite {
    public float remainingSeconds;
    public boolean isExtinguished;
    public Dynamite() {
        this(10f);
    }
    public Dynamite(float launchCountdownSeconds) {
        remainingSeconds = Math.max(0f, launchCountdownSeconds);
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
