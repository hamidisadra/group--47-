package ir.ac.pvz.controller.gui;

import ir.ac.pvz.controller.managers.MenuManager;
import ir.ac.pvz.controller.managers.UserManager;
import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.support.Board;
import ir.ac.pvz.model.user.NewsType;
import ir.ac.pvz.model.user.User;

import java.util.Set;

final class SeenZombieLog {
    private SeenZombieLog() {
    }

    static void record(Board board, Set<String> observedZombies) {
        User user = MenuManager.getInstance().getActiveUser();

        if (user == null) {
            return;
        }

        boolean discovered = false;

        for (Zombie zombie : board.getAllAliveZombies()) {
            if (discover(user, zombie.getType(), observedZombies)) {
                discovered = true;
            }
        }

        if (discovered) {
            UserManager.getInstance().saveAll();
        }
    }

    private static boolean discover(User user, String type,
                                    Set<String> observedZombies) {
        if (type == null || !observedZombies.add(type)) {
            return false;
        }

        int before = user.getCollection().getSeenZombies().size();
        user.getCollection().addSeenZombies(type);

        if (user.getCollection().getSeenZombies().size() <= before) {
            return false;
        }

        user.addNews("A new zombie appeared on your lawn: " + type + ".",
            NewsType.ZOMBIE_UNLOCK);

        return true;
    }
}
