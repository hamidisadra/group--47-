package ir.ac.pvz.model.plants;

import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.enums.PlantTag;
import ir.ac.pvz.model.enums.ProjectileType;
import ir.ac.pvz.model.interfaces.IFireEffect;

public class FirePea extends ShooterPlant implements IFireEffect {

    public FirePea(int id) {
        super(id, "Fire Peashooter", 175, 300, 5f, 1.5f, 40,
                ProjectileType.FIRE, 1, PlantTag.FIRE, PlantTag.PEA);
    }

    @Override
    public void burn(Zombie target) {
        if (target != null) {
            target.melt();
            target.takeDamage(getBurnDamage());
        }
    }

    @Override
    public int getBurnDamage() {
        return damage;
    }
}
