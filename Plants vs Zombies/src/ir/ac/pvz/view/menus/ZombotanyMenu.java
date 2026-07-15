package ir.ac.pvz.view.menus;

import ir.ac.pvz.model.minigame.PlantZombieType;
import ir.ac.pvz.model.minigame.Zombotany;
import ir.ac.pvz.model.user.User;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ZombotanyMenu extends Menu {
    private Zombotany zombotany;

    public ZombotanyMenu() {
        super("Zombotany Menu");
        this.zombotany = new Zombotany(1);
        zombotany.startGame();
    }

    @Override
    public void executeCommand(String command) {
        command = command.trim();

        String spawnRegex = "^spawn\\s+plant\\s+zombie\\s+-t\\s+(\\S+)\\s+-r\\s+(\\d+)$";
        Matcher spawnMatcher = Pattern.compile(spawnRegex).matcher(command);

        if (spawnMatcher.matches()) {
            String type = spawnMatcher.group(1).toLowerCase();
            int row = Integer.parseInt(spawnMatcher.group(2));
            spawn(type, row);
        }

        else if (command.matches("^menu\\s+show\\s+current$")) {
            showMenu();
        }

        else if (command.matches("^menu\\s+exit$")) {
            menuManager.popMenu();
        }

        else {
            System.out.println("Invalid command.");
        }
    }

    private void spawn(String type, int row) {
        User user = menuManager.getActiveUser();
        if (user == null) {
            System.out.println("Error: No active user found!");
            return;
        }

        PlantZombieType plantZombieType;
        switch (type) {
            case "peashooter":
                plantZombieType = PlantZombieType.PEASHOOTER_ZOMBIE;
                break;
            case "wall-nut":
                plantZombieType = PlantZombieType.WALLNUT_ZOMBIE;
                break;
            case "jalapeno":
                plantZombieType = PlantZombieType.JALAPENO_ZOMBIE;
                break;
            case "squash":
                plantZombieType = PlantZombieType.SQUASH_ZOMBIE;
                break;
            default:
                System.out.println("Error: Invalid plant zombie type!");
                return;
        }

        zombotany.spawnPlantZombie(plantZombieType, row);
    }
}
