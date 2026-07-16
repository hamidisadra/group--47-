package ir.ac.pvz.view.menus;

import ir.ac.pvz.model.user.GreenHouse;
import ir.ac.pvz.model.user.HarvestResult;
import ir.ac.pvz.model.user.Pot;
import ir.ac.pvz.model.user.TransactionStatus;
import ir.ac.pvz.model.user.User;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GreenhouseMenu extends Menu {

    public GreenhouseMenu() {
        super("Greenhouse Menu");
    }

    @Override
    public void executeCommand(String command) {
        command = command.trim();

        String plantPotRegex = "^plant\\s+pot\\s+at\\s+\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)$";
        String collectRegex = "^collect\\s+\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)$";
        String growRegex = "^grow\\s+\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)$";

        Matcher plantPotMatcher = Pattern.compile(plantPotRegex).matcher(command);
        Matcher collectMatcher = Pattern.compile(collectRegex).matcher(command);
        Matcher growMatcher = Pattern.compile(growRegex).matcher(command);

        if (command.matches("^show\\s+greenhouse$")) {
            showGreenhouse();
        }

        else if (plantPotMatcher.matches()) {
            int x = Integer.parseInt(plantPotMatcher.group(1));
            int y = Integer.parseInt(plantPotMatcher.group(2));
            plantPot(x, y);
        }

        else if (collectMatcher.matches()) {
            int x = Integer.parseInt(collectMatcher.group(1));
            int y = Integer.parseInt(collectMatcher.group(2));
            collect(x, y);
        }

        else if (growMatcher.matches()) {
            int x = Integer.parseInt(growMatcher.group(1));
            int y = Integer.parseInt(growMatcher.group(2));
            grow(x, y);
        }

        else if (command.matches("^enter\\s+shop$")) {
            menuManager.pushMenu(new ShopMenu());
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

    private void showGreenhouse() {
        User user = menuManager.getActiveUser();
        if (user == null) {
            System.out.println("Error: No active user found!");
            return;
        }
        user.getGreenHouse().showGreenhouse();
    }

    private void plantPot(int x, int y) {
        User user = menuManager.getActiveUser();
        if (user == null) {
            System.out.println("Error: No active user found!");
            return;
        }

        GreenHouse greenHouse = user.getGreenHouse();
        Pot pot = greenHouse.getPot(x, y);

        if (pot == null) {
            System.out.println("Error: Invalid pot position!");
            return;
        }
        if (pot.isLocked()) {
            System.out.println("Error: This pot is locked!");
            return;
        }
        if (!pot.isEmpty()) {
            System.out.println("Error: This pot is already occupied!");
            return;
        }

        greenHouse.plantRandom(pot, user.getCollection().getUnlockedPlants());
        System.out.println("A " + pot.getPlantType() + " seed was planted at (" + x + ", " + y + ").");
    }

    private void collect(int x, int y) {
        User user = menuManager.getActiveUser();
        if (user == null) {
            System.out.println("Error: No active user found!");
            return;
        }

        Pot pot = user.getGreenHouse().getPot(x, y);

        if (pot == null) {
            System.out.println("Error: Invalid pot position!");
            return;
        }
        if (pot.isLocked() || pot.isEmpty()) {
            System.out.println("Error: There is nothing to collect here!");
            return;
        }
        if (!pot.isReady()) {
            System.out.println("Error: This plant is not fully grown yet!");
            return;
        }

        HarvestResult result = pot.harvest();

        if (result.getCoins() > 0) {
            user.getWallet().addCoins(result.getCoins());
            System.out.println("Collected " + result.getCoins() + " coins.");
        } else {
            user.getCollection().boostPlant(result.getBoostedPlant());
            System.out.println("Collected a boost for " + result.getBoostedPlant() + ".");
        }
    }

    private void grow(int x, int y) {
        User user = menuManager.getActiveUser();
        if (user == null) {
            System.out.println("Error: No active user found!");
            return;
        }

        Pot pot = user.getGreenHouse().getPot(x, y);

        if (pot == null) {
            System.out.println("Error: Invalid pot position!");
            return;
        }
        if (pot.isLocked() || pot.isEmpty()) {
            System.out.println("Error: There is no growing plant here!");
            return;
        }
        if (pot.isReady()) {
            System.out.println("Error: This plant is already ready to harvest!");
            return;
        }

        int cost = (int) Math.ceil(pot.getRemainingHours());
        TransactionStatus status = user.getWallet().spendGems(cost);

        if (status == TransactionStatus.INSUFFICIENT_FUND) {
            System.out.println("Error: You don't have enough gems!");
            return;
        }

        pot.growInstantly();
        System.out.println("The plant at (" + x + ", " + y + ") is now fully grown.");
    }
}
