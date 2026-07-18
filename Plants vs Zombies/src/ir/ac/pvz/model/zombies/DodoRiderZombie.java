package ir.ac.pvz.model.zombies;


import ir.ac.pvz.model.enums.PlantTag;
import ir.ac.pvz.model.plants.TallNut;
import ir.ac.pvz.model.plants.WallPlant;
import ir.ac.pvz.model.core.GameObject;
import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.core.Zombie;

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
