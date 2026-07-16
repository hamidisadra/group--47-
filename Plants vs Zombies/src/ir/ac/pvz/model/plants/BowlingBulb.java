package com.pvz.model.plants;

import com.pvz.model.enums.PlantTag;
import com.pvz.model.enums.ProjectileType;

public class BowlingBulb extends ShooterPlant {
    public BowlingBulb(int id) {
        super(id, "Bowling Bulb", 200, 300, 5.0f, 2.0f, 40, ProjectileType.PIERCING, 1, PlantTag.CHARGE);
    }
}
