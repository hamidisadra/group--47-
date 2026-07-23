package com.pvz.model.plants;

import com.pvz.model.enums.PlantTag;
import com.pvz.model.enums.ProjectileType;

public class PuffShroom extends ShooterPlant {
    public PuffShroom(int id) {
        super(id, "Puff-shroom", 0, 300, 5.0f, 1.5f, 20, ProjectileType.PEA, 1, PlantTag.SHROOM);
    }
}
