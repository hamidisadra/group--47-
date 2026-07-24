package com.pvz.model.plants;

import com.pvz.model.enums.PlantTag;
import com.pvz.model.enums.ProjectileType;

public class PeaPod extends ShooterPlant {
    public PeaPod(int id) {
        super(id, "Pea Pod", 125, 300, 5.0f, 1.5f, 20, ProjectileType.PEA, 1, PlantTag.PEA, PlantTag.STACK);
    }
}
