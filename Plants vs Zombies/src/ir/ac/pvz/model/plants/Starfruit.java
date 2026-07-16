package com.pvz.model.plants;

import com.pvz.model.enums.PlantTag;
import com.pvz.model.enums.ProjectileType;

public class Starfruit extends ShooterPlant {
    public Starfruit(int id) {
        super(id, "Starfruit", 150, 300, 5.0f, 1.5f, 20, ProjectileType.PEA, 1);
    }
}
