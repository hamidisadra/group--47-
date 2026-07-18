package ir.ac.pvz.model.plants;

import ir.ac.pvz.model.enums.PlantTag;
import ir.ac.pvz.model.enums.TargetingMode;

public class ElectricBlueberry extends HomingPlant {
    public ElectricBlueberry(int id) {
        super(id, "Electric Blueberry", 150, 300, 15.0f, 12.0f, 5000, TargetingMode.RANDOM, PlantTag.CHARGE);
    }
}
