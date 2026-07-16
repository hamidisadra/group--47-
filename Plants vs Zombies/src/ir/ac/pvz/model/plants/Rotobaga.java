package com.pvz.model.plants;

import com.pvz.model.enums.PlantTag;
import com.pvz.model.enums.ProjectileType;

public class Rotobaga extends ShooterPlant {
    public Rotobaga(int id) {
        super(id, "Rotobaga", 150, 300, 5.0f, 1.5f, 10, ProjectileType.PEA, 3);
    }
}
