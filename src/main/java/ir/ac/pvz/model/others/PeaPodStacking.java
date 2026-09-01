package ir.ac.pvz.model.others;

import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.plants.ExplosivePlant;
import ir.ac.pvz.model.plants.ShooterPlant;
import ir.ac.pvz.model.support.Tile;

final class PeaPodStacking {
    static final int MAXIMUM_HEADS = 5;

    private PeaPodStacking() {
    }

    static boolean isAtMaximum(Tile tile, Plant plant) {
        if (tile == null || !plant.getNormalizedType().equals("peapod")) {
            return false;
        }

        boolean hasPeaPod = false;

        for (Plant existing : tile.getPlants()) {
            if (!isPeaPod(existing)) {
                continue;
            }

            hasPeaPod = true;

            if (((ShooterPlant) existing).multiShot < MAXIMUM_HEADS) {
                return false;
            }
        }

        return hasPeaPod;
    }

    static boolean merge(Tile tile, Plant plant) {
        if (!plant.getNormalizedType().equals("peapod")) {
            return false;
        }

        for (Plant existing : tile.getPlants()) {
            if (!isPeaPod(existing)) {
                continue;
            }

            ShooterPlant peaPod = (ShooterPlant) existing;

            if (peaPod.multiShot < MAXIMUM_HEADS) {
                peaPod.multiShot++;
                return true;
            }
        }

        return false;
    }

    static boolean isInstantPlant(Plant plant) {
        String type = plant.getNormalizedType();

        return type.endsWith("mint") || type.equals("goldbloom")
            || type.equals("iceshroom")
            || type.equals("hotpotato")
            || type.equals("doomshroom") || type.equals("jalapeno")
            || plant instanceof ExplosivePlant
            && ((ExplosivePlant) plant).instantUse;
    }

    private static boolean isPeaPod(Plant plant) {
        return plant.getNormalizedType().equals("peapod")
            && plant instanceof ShooterPlant;
    }
}
