package com.pvz.model.plants;

import com.pvz.model.enums.PlantTag;
import com.pvz.model.enums.ProjectileType;

public class SplitPea extends ShooterPlant {
    public SplitPea(int id) {
        super(id, "Split Pea", 125, 300, 5.0f, 1.5f, 20, ProjectileType.PEA, 1, PlantTag.PEA);
    }
}
