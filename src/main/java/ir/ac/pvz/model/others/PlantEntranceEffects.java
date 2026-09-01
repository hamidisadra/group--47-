package ir.ac.pvz.model.others;

import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.plants.ExplosivePlant;

final class PlantEntranceEffects {
    private PlantEntranceEffects() {
    }

    static void resolve(GameSession session, Plant plant) {
        boolean goldbloom = plant.getNormalizedType().equals("goldbloom");

        if (goldbloom) {
            session.getSunManager().producePlantSun(plant);
            plant.die();
        }

        if (!PeaPodStacking.isInstantPlant(plant)) {
            return;
        }

        if (plant.getNormalizedType().endsWith("mint")) {
            session.getPlantFoodInventory().applyMint(plant, session,
                session.getProjectileResolver());
        }

        else if (!goldbloom) {
            ExplosivePlant.resolveInstantPlant(plant, session,
                session.getProjectileResolver());
        }
    }
}
