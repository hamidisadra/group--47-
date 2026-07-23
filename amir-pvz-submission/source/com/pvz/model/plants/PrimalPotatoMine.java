package com.pvz.model.plants;

import com.pvz.model.enums.PlantTag;

public class PrimalPotatoMine extends ExplosivePlant {
    public PrimalPotatoMine(int id) {
        super(id, "Primal Potato Mine", 50, 300, 5.0f, 0.0f, 2400, 1.0f, false, PlantTag.TRAP, PlantTag.CHARGE);
    }
}
