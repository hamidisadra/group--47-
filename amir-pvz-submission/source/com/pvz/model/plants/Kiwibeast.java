package com.pvz.model.plants;

import com.pvz.model.enums.PlantTag;

public class Kiwibeast extends MeleePlant {
    private int growthStage;
    private int growthTicks;
    public Kiwibeast(int id) {
        super(id, "Kiwibeast", 175, 300, 5.0f, 2.00f, 15, true, PlantTag.AOE, PlantTag.RAMP_UP);
        this.growthStage = 1;
        this.growthTicks = 0;
    }
    @Override
    public void onTick() {
        super.onTick();
        growthTicks++;
        if (growthStage < 2 && growthTicks >= 240) {
            attackPower += 15;
            growthStage = 2;
        }
        if (growthStage < 3 && growthTicks >= 720) {
            attackPower += 15;
            growthStage = 3;
        }
    }
}
