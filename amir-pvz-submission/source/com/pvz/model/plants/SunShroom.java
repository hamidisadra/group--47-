package com.pvz.model.plants;

import com.pvz.model.enums.PlantTag;
import com.pvz.model.interfaces.IGrowable;

public class SunShroom extends SunProducerPlant implements IGrowable {
    private int growthStage;
    private int growthTicks;
    public SunShroom(int id) {
        super(id, "Sun-shroom", 25, 300, 5f, 25, 24f,
                PlantTag.SHROOM, PlantTag.RAMP_UP, PlantTag.NIGHT);
        growthStage = 1;
        growthTicks = 0;
    }
    @Override
    public void onTick() {
        super.onTick();
        growthTicks++;
        grow();
    }
    @Override
    public void grow() {
        int stageTwoTicks = 240;
        int stageThreeTicks = 720;
        if (level >= 2) {
            stageTwoTicks = 190;
            stageThreeTicks = 670;
        }
        if (growthTicks >= stageThreeTicks) {
            growthStage = 3;
            sunAmount = 75;
        }
        else if (growthTicks >= stageTwoTicks) {
            growthStage = 2;
            sunAmount = 50;
        }
    }
    @Override
    public boolean isMature() {
        return growthStage == 3;
    }
    @Override
    public void onPlantFood() {
        super.onPlantFood();
        growthStage = 3;
        growthTicks = 720;
        sunAmount = 75;
        queuePlantFoodSun(225);
    }
}
