package com.pvz.model.plants;

import com.pvz.model.enums.PlantTag;
import com.pvz.model.enums.ProjectileType;

public class PeaShooter extends ShooterPlant {

    public PeaShooter(int id) {
        super(id, "Peashooter", 100, 300, 5f, 1.5f, 20,
                ProjectileType.PEA, 1, PlantTag.PEA);
    }
}
