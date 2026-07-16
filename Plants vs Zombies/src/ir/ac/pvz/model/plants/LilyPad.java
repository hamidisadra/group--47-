package com.pvz.model.plants;

import com.pvz.model.enums.PlantTag;

public class LilyPad extends ModifierPlant {
    public LilyPad(int id) {
        super(id, "Lily Pad", 25, 300, 5.0f, 0.0f, PlantTag.WATER, PlantTag.STACK);
    }
}
