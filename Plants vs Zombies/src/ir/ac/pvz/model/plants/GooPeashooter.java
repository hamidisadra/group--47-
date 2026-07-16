package com.pvz.model.plants;

import com.pvz.model.enums.PlantTag;
import com.pvz.model.enums.ProjectileType;

public class GooPeashooter extends ShooterPlant {
    public GooPeashooter(int id) {
        super(id, "Goo Peashooter", 125, 300, 5.0f, 1.5f, 20, ProjectileType.POISON, 1, PlantTag.POISON);
    }
}
