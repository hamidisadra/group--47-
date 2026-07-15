package ir.ac.pvz.view.menus;

import ir.ac.pvz.model.minigame.BowlingNut;
import ir.ac.pvz.model.minigame.BowlingNutType;
import ir.ac.pvz.model.minigame.WallnutBowling;
import ir.ac.pvz.model.user.User;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WallnutBowlingMenu extends Menu {
    private WallnutBowling wallnutBowling;

    public WallnutBowlingMenu() {
        super("Wallnut Bowling Menu");
        this.wallnutBowling = new WallnutBowling(1, 5);
        wallnutBowling.startGame();
    }

    @Override
    public void executeCommand(String command) {
        command = command.trim();

        String launchRegex = "^launch\\s+nut\\s+-r\\s+(\\d+)\\s+-t\\s+(\\S+)$";
        Matcher launchMatcher = Pattern.compile(launchRegex).matcher(command);

        if (launchMatcher.matches()) {
            int row = Integer.parseInt(launchMatcher.group(1));
            String type = launchMatcher.group(2).toLowerCase();
            launchNut(row, type);
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

    private void launchNut(int row, String type) {
        User user = menuManager.getActiveUser();
        if (user == null) {
            System.out.println("Error: No active user found!");
            return;
        }

        BowlingNutType nutType;
        switch (type) {
            case "explode-o-nut":
                nutType = BowlingNutType.EXPLODE_O_NUT;
                break;
            case "giant":
                nutType = BowlingNutType.GIANT;
                break;
            default:
                nutType = BowlingNutType.NORMAL;
                break;
        }

        BowlingNut nut = wallnutBowling.launchNut(row, nutType);
        nut.move();
        System.out.println("A " + type + " is launched in row " + row + ".");
    }
}
