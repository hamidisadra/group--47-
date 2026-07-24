package com.pvz.model.plants;

import com.pvz.model.enums.PlantTag;

public class Pumpkin extends WallPlant {
    public Pumpkin(int id) {
        super(id, "Pumpkin", 150, 4000, 20.0f, 0, PlantTag.STACK);
    }
}
