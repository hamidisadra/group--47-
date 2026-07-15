package ir.ac.pvz.view.menus;

import ir.ac.pvz.controller.managers.GameplayManager;
import ir.ac.pvz.model.shop.Shop;
import ir.ac.pvz.model.shop.ShopResult;
import ir.ac.pvz.model.user.User;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShopMenu extends Menu {

    public ShopMenu() {
        super("Shop Menu");
    }

    @Override
    public void executeCommand(String command) {
        command = command.trim();

        String buyRegex = "^shop\\s+buy\\s+-i\\s+(\\S+)\\s+-n\\s+(\\d+)(\\s+-t\\s+(\\S+))?$";
        Matcher buyMatcher = Pattern.compile(buyRegex).matcher(command);

        if (command.matches("^shop\\s+list$")) {
            GameplayManager.getInstance().getShop().showPermanentItems();
        }

        else if (command.matches("^shop\\s+daily$")) {
            showDaily();
        }

        else if (buyMatcher.matches()) {
            String itemId = buyMatcher.group(1);
            int count = Integer.parseInt(buyMatcher.group(2));
            String plantType = buyMatcher.group(4);
            buyItem(itemId, count, plantType);
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

    private void showDaily() {
        User user = menuManager.getActiveUser();
        if (user == null) {
            System.out.println("Error: No active user found!");
            return;
        }
        GameplayManager.getInstance().getShop().showDailyOffer(user.getCollection().getUnlockedPlants());
    }

    private void buyItem(String itemId, int count, String plantType) {
        User user = menuManager.getActiveUser();
        if (user == null) {
            System.out.println("Error: No active user found!");
            return;
        }

        Shop shop = GameplayManager.getInstance().getShop();
        ShopResult result = shop.buyItem(itemId, count, plantType, user.getWallet(), user.getGreenHouse(), user.getCollection(), user.getInventory());

        switch (result) {
            case SUCCESS:
                System.out.println("Purchase successful.");
                break;
            case INSUFFICIENT_FUNDS:
                System.out.println("Error: You don't have enough coins or gems!");
                break;
            case CAPACITY_FULL:
                System.out.println("Error: You have reached the capacity for this item!");
                break;
            case PLANT_TYPE_REQUIRED:
                System.out.println("Error: You must specify a plant type with -t!");
                break;
            case PLANT_NOT_UNLOCKED:
                System.out.println("Error: You have not unlocked this plant!");
                break;
            default:
                System.out.println("Error: Invalid item!");
                break;
        }
    }
}
