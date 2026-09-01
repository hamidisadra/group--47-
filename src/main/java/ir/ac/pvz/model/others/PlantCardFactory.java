package ir.ac.pvz.model.others;

import ir.ac.pvz.model.core.Plant;

final class PlantCardFactory {
    private PlantCardFactory() {
    }

    static String normalize(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("-", "").replace("_", "")
            .replace(" ", "").toLowerCase();
    }

    static Plant createPlant(StageConfig stageConfig, String type, int id) {
        if (type == null) {
            return null;
        }

        Plant plant = Plant.createSpreadsheetPlant(id, type);

        if (plant == null) {
            return null;
        }

        applyConfiguredLevel(stageConfig, type, plant);

        return plant;
    }

    static void applyConfiguredLevel(StageConfig stageConfig, String type,
                                     Plant plant) {
        int targetLevel = stageConfig.getPlantLevel(type);

        for (int nextLevel = plant.level + 1;
             nextLevel <= targetLevel; nextLevel++) {
            int previousLevel = plant.level;

            plant.upgrade();

            if (plant.level == previousLevel) {
                throw new IllegalStateException("Missing level " + nextLevel
                    + " upgrade for " + plant.type + ".");
            }
        }
    }

    static Plant createPlantForCard(StageConfig stageConfig, String type,
                                    int id) {
        if (!normalize(type).equals("imitater")) {
            return createPlant(stageConfig, type, id);
        }

        if (stageConfig.imitaterTargetType == null) {
            return null;
        }

        Plant copiedPlant = createPlant(stageConfig,
            stageConfig.imitaterTargetType, id);

        applyImitaterCardUpgrades(stageConfig, copiedPlant);

        return copiedPlant;
    }

    static void applyImitaterCardUpgrades(StageConfig stageConfig,
                                          Plant copiedPlant) {
        if (copiedPlant == null) {
            return;
        }

        int imitaterLevel = stageConfig.getPlantLevel("Imitater");

        if (imitaterLevel >= 2) {
            copiedPlant.rechargeTime = Math.max(0f,
                copiedPlant.rechargeTime - 2f);
        }

        if (imitaterLevel >= 3) {
            copiedPlant.sunCost = Math.max(0, copiedPlant.sunCost - 25);
            copiedPlant.cost = copiedPlant.sunCost;
        }
    }
}
