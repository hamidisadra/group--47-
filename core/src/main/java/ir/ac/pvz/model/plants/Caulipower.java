package ir.ac.pvz.model.plants;

import ir.ac.pvz.model.core.GameObject;
import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.enums.PlantTag;
import ir.ac.pvz.model.enums.TargetingMode;

public class Caulipower extends HomingPlant {
    public Caulipower(int id) {
        super(id, "Caulipower", 250, 300, 15f, 12f, 0,
                TargetingMode.RANDOM, PlantTag.MAGIC, PlantTag.CHARGE);
    }
    @Override
    public void attack(GameObject target) {
        if (target instanceof Zombie) {
            ((Zombie) target).setHypnotized(true);
        }
    }
}
