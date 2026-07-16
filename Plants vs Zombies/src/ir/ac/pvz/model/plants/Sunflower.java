package com.pvz.model.plants;

import com.pvz.model.enums.PlantTag;

public class Sunflower extends SunProducerPlant {

    public Sunflower(int id) {
        super(id, "Sunflower", 50, 300, 5f, 50, 24f, PlantTag.DAY);
    }

    @Override
    public void onPlantFood() {
        super.onPlantFood();
        queuePlantFoodSun(150);
    }
}
