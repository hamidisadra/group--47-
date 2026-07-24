package com.pvz.model.support;

import com.pvz.game.GameSession;
import com.pvz.model.core.Plant;
import com.pvz.model.core.Zombie;
import com.pvz.model.enums.PlantTag;
import com.pvz.model.plants.WallPlant;

public final class DodoFlightAbility extends ZombieAbility {
    private final int maximumGridSquares;
    public DodoFlightAbility() {
        this(2);
    }
    public DodoFlightAbility(int maximumGridSquares) {
        super("dodo-flight", "Flies over selected dangerous obstacles.", 0f);
        this.maximumGridSquares = Math.max(1, maximumGridSquares);
    }
    @Override
    public boolean onPlantContact(Zombie zombie, Plant plant,
                                  GameSession session) {
        if (plant == null || session == null
                || plant.getNormalizedType().equals("tallnut")) {
            return false;
        }
        boolean obstacle = plant instanceof WallPlant
                || plant.plantTags.contains(PlantTag.TRAP)
                || plant.plantTags.contains(PlantTag.MOVE_ZOMBIES);
        if (!obstacle) {
            return false;
        }
        float destinationX = Math.max(0f,
                plant.location.x - maximumGridSquares);
        if (destinationX >= plant.location.x) {
            return false;
        }
        return session.getBoard().placeZombie(zombie,
                new ContinuousPosition(destinationX, zombie.lane));
    }
}
