package com.pvz.model.zombies;

import com.pvz.model.core.GameObject;
import com.pvz.model.core.Plant;
import com.pvz.model.core.Zombie;
import com.pvz.model.enums.PlantTag;
import com.pvz.model.plants.TallNut;
import com.pvz.model.plants.WallPlant;

public class DodoRiderZombie extends Zombie {

    public boolean cannotFlyOverTallNut;
    public int maximumFlyTiles;

    public DodoRiderZombie() {
        super(0.3f, 490, 100, 600);
        this.cannotFlyOverTallNut = true;
        this.maximumFlyTiles = 2;
    }

    public boolean canFlyOver(GameObject obstacle) {
        if (obstacle instanceof TallNut) {
            return false;
        }
        if (!(obstacle instanceof Plant)) {
            return false;
        }
        Plant plant = (Plant) obstacle;
        return plant instanceof WallPlant
                || plant.plantTags.contains(PlantTag.TRAP)
                || plant.plantTags.contains(PlantTag.MOVE_ZOMBIES);
    }

    @Override
    public void freeze(int duration) {
    }
}
