package ir.ac.pvz.model.zombies;

import ir.ac.pvz.model.enums.PlantTag;
import ir.ac.pvz.model.enums.ProjectileType;
import ir.ac.pvz.model.support.Projectile;
import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.core.Zombie;

public class ExplorerZombie extends Zombie {

    private boolean torchLit;

    public ExplorerZombie() {
        super(0.25f, 250, 100, 250);
        this.torchLit = true;
    }

    @Override
    public void onReachPlant(Plant plant) {
        if (plant != null && plant.plantTags.contains(PlantTag.ICE)) {
            torchLit = false;
        } else if (plant != null && plant.plantTags.contains(PlantTag.FIRE)) {
            torchLit = true;
        }
        if (torchLit && plant != null) {
            plant.die();
        } else {
            super.onReachPlant(plant);
        }
    }

    @Override
    public void receiveProjectile(Projectile projectile) {
        if (projectile != null && projectile.type == ProjectileType.ICE) {
            torchLit = false;
        } else if (projectile != null && projectile.type == ProjectileType.FIRE) {
            torchLit = true;
        }
        super.receiveProjectile(projectile);
    }
}
