package ir.ac.pvz.model.zombies;

import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.core.Zombie;

public class FishermanZombie extends Zombie {

    public boolean staysInRightmostColumn;
    public float hookCooldownSeconds;

    public FishermanZombie() {
        super(0f, 1000, 100, 700);
        this.staysInRightmostColumn = true;
        this.hookCooldownSeconds = 2.5f;
    }

    public void hookPlantOneTileRight(Plant plant) {
        if (plant != null && plant.location != null) {
            plant.location.x++;
            plant.positionX = plant.location.x;
        }
    }

    public void throwAdjacentHookedPlant(Plant plant) {
        if (plant != null) {
            plant.die();
        }
    }
}
