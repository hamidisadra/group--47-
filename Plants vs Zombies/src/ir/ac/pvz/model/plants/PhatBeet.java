package com.pvz.model.plants;

import com.pvz.model.enums.PlantTag;

public class PhatBeet extends MeleePlant {

    public PhatBeet(int id) {
        super(id, "Phat Beet", 150, 300, 5f, 2f, 15, true, PlantTag.AOE);
    }
}
