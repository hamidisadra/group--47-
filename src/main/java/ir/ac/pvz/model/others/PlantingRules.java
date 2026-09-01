package ir.ac.pvz.model.others;

import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.support.GridPosition;
import ir.ac.pvz.model.support.Tile;

final class PlantingRules {
    private PlantingRules() {
    }

    static String describe(GameSession session, String type,
                           GridPosition position, Plant plant) {
        if (plant == null) {
            return "Unknown plant type.";
        }

        if (!session.getStageConfig().isPlantSelected(type)) {
            return "Plant is not selected.";
        }

        if (position == null || !session.getBoard().isInside(position)) {
            return "Invalid location.";
        }

        if (isOnCooldown(session, type)) {
            return "Plant is on cooldown.";
        }

        Tile tile = session.getBoard().getTile(position);

        if (session.isPeaPodAtMaximum(tile, plant)) {
            return "Pea Pod already has maximum heads.";
        }

        if (!plant.canPlantOn(tile)) {
            return "Plant cannot be planted on this tile.";
        }

        if (session.getCurrentSunAmount() < plant.sunCost) {
            return "Not enough sun.";
        }

        return null;
    }

    private static boolean isOnCooldown(GameSession session, String type) {
        if (session.isCooldownDisabled()) {
            return false;
        }

        return session.getCooldown(PlantCardFactory.normalize(type)) > 0f;
    }
}
