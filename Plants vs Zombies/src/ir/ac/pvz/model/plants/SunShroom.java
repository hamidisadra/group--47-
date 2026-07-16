package com.pvz.model.plants;

import com.pvz.model.enums.PlantTag;
import com.pvz.model.interfaces.IGrowable;

public class SunShroom extends SunProducerPlant implements IGrowable {

    private int growthStage;
    private float growthElapsedSeconds;

    public SunShroom(int id) {
        super(id, "Sun-shroom", 25, 300, 5f, 25, 24f,
                PlantTag.SHROOM, PlantTag.RAMP_UP, PlantTag.NIGHT);
        growthStage = 1;
        growthElapsedSeconds = 0f;
    }

    @Override
    public void onTick() {
        super.onTick();
        growthElapsedSeconds += 0.1f;
        grow();
    }

    @Override
    public void grow() {
        float stageTwoTime = level >= 2 ? 19f : 24f;
        float stageThreeTime = level >= 2 ? 67f : 72f;
        if (growthElapsedSeconds >= stageThreeTime) {
            growthStage = 3;
            sunAmount = 75;
        } else if (growthElapsedSeconds >= stageTwoTime) {
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
        growthElapsedSeconds = 72f;
        sunAmount = 75;
        queuePlantFoodSun(225);
    }
}
