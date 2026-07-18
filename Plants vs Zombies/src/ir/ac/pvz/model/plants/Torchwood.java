package ir.ac.pvz.model.plants;

import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.enums.PlantTag;

public class Torchwood extends ModifierPlant {

    public Torchwood(int id) {
        super(id, "Torchwood", 175, 300, 5f, 0f, PlantTag.FIRE);
    }

    @Override
    public void modify(Plant target) {
        if (target instanceof ShooterPlant) {
            ShooterPlant shooter = (ShooterPlant) target;
            shooter.damage *= 2;
            shooter.attackPower = shooter.damage;
        }
    }
}
