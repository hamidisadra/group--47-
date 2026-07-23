package com.pvz.model.plants;

import com.pvz.model.enums.PlantTag;

public class TangleKelp extends ExplosivePlant {
    public TangleKelp(int id) {
        super(id, "Tangle Kelp", 25, 300, 15.0f, 0.0f, 0, 0.0f, false, PlantTag.TRAP, PlantTag.WATER);
    }
}
