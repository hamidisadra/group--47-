package com.pvz.model.plants;

import com.pvz.model.enums.TargetingMode;

public class CatTail extends HomingPlant {

    public CatTail(int id) {
        super(id, "Cat-tail", 175, 300, 20f, 1.5f, 15, TargetingMode.NEAREST);
    }
}
