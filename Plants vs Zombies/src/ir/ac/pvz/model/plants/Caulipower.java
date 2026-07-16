package com.pvz.model.plants;

import com.pvz.model.core.GameObject;
import com.pvz.model.core.Zombie;
import com.pvz.model.enums.PlantTag;
import com.pvz.model.enums.TargetingMode;

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
