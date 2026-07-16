package com.pvz.model.plants;

import com.pvz.model.enums.PlantTag;

public class Garlic extends WallPlant {
    public Garlic(int id) {
        super(id, "Garlic", 50, 300, 20.0f, 0, PlantTag.MOVE_ZOMBIES);
    }
}
