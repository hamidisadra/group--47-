package ir.ac.pvz.model.support;

import ir.ac.pvz.model.others.GameSession;
import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.enums.PlantTag;
import ir.ac.pvz.model.plants.WallPlant;

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
        return flyPast(zombie, session, plant.location.x);
    }

    public boolean escapeSlipperyTile(Zombie zombie, GameSession session,
                                      int slipperyTileX) {
        return flyPast(zombie, session, slipperyTileX);
    }

    private boolean flyPast(Zombie zombie, GameSession session,
                            int obstacleX) {
        if (zombie == null || session == null) {
            return false;
        }
        float destinationX = Math.max(0f,
                obstacleX - maximumGridSquares);
        if (destinationX >= obstacleX) {
            return false;
        }
        return session.getBoard().placeZombie(zombie,
                new ContinuousPosition(destinationX, zombie.lane));
    }
}
