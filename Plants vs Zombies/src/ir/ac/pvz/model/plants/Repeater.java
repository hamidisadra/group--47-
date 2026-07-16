package com.pvz.model.plants;

import com.pvz.model.enums.PlantTag;
import com.pvz.model.enums.ProjectileType;

public class Repeater extends ShooterPlant {

    public Repeater(int id) {
        super(id, "Repeater", 200, 300, 5f, 1.5f, 20,
                ProjectileType.PEA, 2, PlantTag.PEA);
    }
}
