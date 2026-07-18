package ir.ac.pvz.model.zombies;

import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.core.Zombie;

public class PianistZombie extends Zombie {

    public PianistZombie() {
        super(0.12f, 840, 4000, 450);
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
