package com.pvz.model.zombies;

import com.pvz.model.core.Plant;
import com.pvz.model.core.Zombie;

public class PianistZombie extends Zombie {
    public PianistZombie() {
        super("PianistZombie");
    }
    @Override
    public void onReachPlant(Plant plant) {
        if (plant != null) {
            plant.receiveInstantKill();
        }
    }
    public void moveZombieToAdjacentLane(Zombie zombie, int targetLane) {
        if (zombie != null && Math.abs(zombie.lane - targetLane) == 1) {
            zombie.lane = targetLane;
            zombie.positionY = targetLane;
            zombie.currentPosition.y = targetLane;
        }
    }
}
