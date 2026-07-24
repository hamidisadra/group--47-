package com.pvz.model.plants;

import com.pvz.model.core.Plant;

public class Imitater extends ModifierPlant {
    public String copiedPlantType;
    public void copyPlantType(String plantType) {
        this.copiedPlantType = plantType;
    }
    public Plant createCopiedPlant(int id) {
        if (copiedPlantType == null || copiedPlantType.trim().isEmpty()) {
            return null;
        }
        return PlantFactory.create(id, copiedPlantType);
    }
    public Imitater(int id) {
        super(id, "Imitater", 0, 0, 0f, 0f);
        this.copiedPlantType = null;
    }
}
