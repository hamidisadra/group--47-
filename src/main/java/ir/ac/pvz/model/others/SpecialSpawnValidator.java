package ir.ac.pvz.model.others;

import ir.ac.pvz.controller.game_core.ZombieSpawner;
import ir.ac.pvz.model.support.Board;
import ir.ac.pvz.model.support.Tile;

import java.util.List;

final class SpecialSpawnValidator {
    private SpecialSpawnValidator() {
    }

    static void validate(List<SpecialSpawnEvent> specialSpawnEvents,
                         Board board, ZombieSpawner spawner,
                         List<String> errors) {
        if (board == null || spawner == null) {
            return;
        }
        for (SpecialSpawnEvent event : specialSpawnEvents) {
            if (event.tick < 0 || !board.isInside(event.position)) {
                errors.add("Invalid special spawn position or tick.");
                continue;
            }

            Tile tile = board.getTile(event.position);

            if (tile == null || !tile.isLowTideSpawn) {
                errors.add("Special spawn must target LOW_TIDE or NECROMANCY.");
            }

            if (!spawner.isSupportedType(event.zombieType)) {
                errors.add("Unsupported special-spawn zombie: "
                        + event.zombieType);
            }
        }
    }
}
