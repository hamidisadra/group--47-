package ir.ac.pvz.model.support;

import ir.ac.pvz.model.core.Plant;

public class PlantUpgradePath {

    public String fromPlantType;
    public String toPlantType;
    public int sunCost;

    public PlantUpgradePath(String fromPlantType, String toPlantType, int sunCost) {
        this.fromPlantType = fromPlantType;
        this.toPlantType = toPlantType;
        this.sunCost = sunCost;
    }

    public boolean canApply(Plant plant) {
        return plant != null && plant.type.equals(fromPlantType);
    }

    public Plant apply(Plant plant) {
        return canApply(plant) ? plant : null;
    }
}
