package com.pvz.model.plants;

import com.pvz.model.enums.PlantTag;
import com.pvz.model.enums.ProjectileType;

public class SeaShroom extends ShooterPlant {
    public SeaShroom(int id) {
        super(id, "Sea-shroom", 0, 300, 15.0f, 1.5f, 20, ProjectileType.PEA, 1, PlantTag.SHROOM, PlantTag.WATER);
    }
}
