package com.pvz.model.plants;

import com.pvz.model.enums.PlantTag;
import com.pvz.model.enums.ProjectileType;

public class Citron extends ShooterPlant {
    public Citron(int id) {
        super(id, "Citron", 350, 300, 5.0f, 9.0f, 800, ProjectileType.LASER, 1, PlantTag.CHARGE);
    }
}
