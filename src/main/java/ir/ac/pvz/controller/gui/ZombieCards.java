package ir.ac.pvz.controller.gui;

import ir.ac.pvz.model.minigame.IZombie;
import ir.ac.pvz.model.support.GridPosition;

import java.util.ArrayList;
import java.util.List;

final class ZombieCards {
    private ZombieCards() {
    }

    private static IZombie gameOf(MinigameController controller) {
        if (controller == null || !(controller.getGame() instanceof IZombie)) {
            return null;
        }

        return (IZombie) controller.getGame();
    }

    static List<String> types(MinigameController controller) {
        IZombie game = gameOf(controller);

        if (game == null) {
            return new ArrayList<>();
        }

        return game.getAvailableZombies();
    }

    static int cost(MinigameController controller, String type) {
        IZombie game = gameOf(controller);

        return game == null ? 0 : game.getCost(type);
    }

    static boolean place(SessionController controller, String type,
                         GridPosition position) {
        IZombie game = gameOf(controller.minigameController);

        if (game == null
            || position.x < SessionController.IZOMBIE_PLANT_COLUMNS) {
            return false;
        }

        if (!game.placeZombie(type, position.y + 1)) {
            return false;
        }

        controller.session.cheatSpawnZombie(ZombieCardTypes.modelTypeFor(type),
            position.x, position.y);

        return true;
    }
}
