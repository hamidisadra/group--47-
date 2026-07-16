package com.pvz.model.zombies;

import com.pvz.model.core.Plant;
import com.pvz.model.core.Zombie;

public class FootballZombie extends Zombie {

    private boolean running;

    public FootballZombie() {
        super(0.16f, 1100, 100, 1000);
        this.running = true;
    }

    @Override
    public void onReachPlant(Plant plant) {
        if (running && plant != null) {
            plant.receiveInstantKill();
            running = false;
            speed *= 0.5f;
            return;
        }
        super.onReachPlant(plant);
    }

    public boolean collideWithHypnotizedZombie(Zombie zombie) {
        if (!running || zombie == null || !zombie.isHypnotized) {
            return false;
        }
        zombie.forceDie();
        return true;
    }

    public boolean isRunning() {
        return running;
    }
}
