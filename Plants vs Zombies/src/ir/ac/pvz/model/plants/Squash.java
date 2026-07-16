package com.pvz.model.plants;

import com.pvz.model.enums.PlantTag;

public class Squash extends ExplosivePlant {
    public Squash(int id) {
        super(id, "Squash", 50, 300, 20.0f, 0.0f, 1800, 0.0f, false, PlantTag.TRAP);
    }
}
