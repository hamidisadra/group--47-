package com.pvz.model.plants;

import com.pvz.model.enums.PlantTag;

public class TwinSunflower extends SunProducerPlant {

    public TwinSunflower(int id) {
        super(id, "Twin Sunflower", 125, 300, 15f, 100, 24f, PlantTag.DAY);
    }

    @Override
    public void onPlantFood() {
        super.onPlantFood();
        queuePlantFoodSun(250);
    }
}
