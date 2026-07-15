package ir.ac.pvz.model.shop;

import ir.ac.pvz.model.user.Collection;
import ir.ac.pvz.model.user.GreenHouse;
import ir.ac.pvz.model.user.Inventory;
import ir.ac.pvz.model.user.PlayerWallet;
import ir.ac.pvz.model.user.TransactionStatus;

import java.util.ArrayList;
import java.util.List;

public class Shop {
    private List<ShopItem> permanentItems;
    private DailyOffer dailyOffer;

    public Shop() {
        this.permanentItems = new ArrayList<>();
        this.dailyOffer = new DailyOffer();

        permanentItems.add(new ShopItem("pot", "Pot", 2000, 0, 1));
        permanentItems.add(new ShopItem("plant-food", "Plant Food", 0, 3, 1));
        permanentItems.add(new ShopItem("random-seed", "Random Seed Packet", 1000, 0, 5));
        permanentItems.add(new ShopItem("selective-seed", "Selective Seed Packet", 0, 5, 10));
        permanentItems.add(new ShopItem("currency-exchange", "Currency Exchange", 0, 5, 500));
    }

    public List<ShopItem> getPermanentItems() {
        return permanentItems;
    }

    public DailyOffer getDailyOffer() {
        return dailyOffer;
    }

    public void showPermanentItems() {
        System.out.println("========== Shop ==========");
        for (ShopItem item : permanentItems) {
            String price = item.getPriceCoins() > 0 ? item.getPriceCoins() + " coins" : item.getPriceGems() + " gems";
            System.out.println(item.getId() + " - " + item.getName() + " (" + price + " for " + item.getBuyUnit() + ")");
        }
    }

    public void showDailyOffer(List<String> unlockedPlants) {
        dailyOffer.refreshIfNeeded(unlockedPlants);
        if (dailyOffer.getPlantType() == null) {
            System.out.println("No daily offer is available today.");
            return;
        }
        System.out.println("Daily offer: 10 seed packets of " + dailyOffer.getPlantType() + " for " + dailyOffer.getPriceCoins() + " coins.");
    }

    public ShopResult buyItem(String itemId, int count, String plantType, PlayerWallet wallet, GreenHouse greenHouse, Collection collection, Inventory inventory) {
        if (count <= 0) {
            return ShopResult.INVALID_ITEM;
        }

        ShopItem item = findItem(itemId);
        if (item == null) {
            return ShopResult.INVALID_ITEM;
        }

        switch (itemId) {
            case "pot":
                return buyPot(count, wallet, greenHouse, item);
            case "plant-food":
                return buyPlantFood(count, wallet, inventory, item);
            case "random-seed":
                return buyRandomSeed(count, wallet, collection, inventory, item);
            case "selective-seed":
                return buySelectiveSeed(count, plantType, wallet, collection, inventory, item);
            case "currency-exchange":
                return buyCurrencyExchange(count, wallet, item);
            default:
                return ShopResult.INVALID_ITEM;
        }
    }

    private ShopItem findItem(String itemId) {
        for (ShopItem item : permanentItems) {
            if (item.getId().equals(itemId)) {
                return item;
            }
        }
        return null;
    }

    private ShopResult buyPot(int count, PlayerWallet wallet, GreenHouse greenHouse, ShopItem item) {
        for (int i = 0; i < count; i++) {
            if (wallet.spendCoins(item.getPriceCoins()) == TransactionStatus.INSUFFICIENT_FUND) {
                return ShopResult.INSUFFICIENT_FUNDS;
            }
            if (!greenHouse.unlockNextPot()) {
                wallet.addCoins(item.getPriceCoins());
                return ShopResult.CAPACITY_FULL;
            }
        }
        return ShopResult.SUCCESS;
    }

    private ShopResult buyPlantFood(int count, PlayerWallet wallet, Inventory inventory, ShopItem item) {
        for (int i = 0; i < count; i++) {
            if (wallet.spendGems(item.getPriceGems()) == TransactionStatus.INSUFFICIENT_FUND) {
                return ShopResult.INSUFFICIENT_FUNDS;
            }
            if (!inventory.addPlantFood()) {
                wallet.addGems(item.getPriceGems());
                return ShopResult.CAPACITY_FULL;
            }
        }
        return ShopResult.SUCCESS;
    }

    private ShopResult buyRandomSeed(int count, PlayerWallet wallet, Collection collection, Inventory inventory, ShopItem item) {
        List<String> unlockedPlants = collection.getUnlockedPlants();
        if (unlockedPlants.isEmpty()) {
            return ShopResult.PLANT_NOT_UNLOCKED;
        }
        int totalPrice = item.getPriceCoins() * count;
        if (wallet.spendCoins(totalPrice) == TransactionStatus.INSUFFICIENT_FUND) {
            return ShopResult.INSUFFICIENT_FUNDS;
        }
        for (int i = 0; i < count; i++) {
            String plant = unlockedPlants.get((int) (Math.random() * unlockedPlants.size()));
            inventory.addSeedPackets(plant, item.getBuyUnit());
        }
        return ShopResult.SUCCESS;
    }

    private ShopResult buySelectiveSeed(int count, String plantType, PlayerWallet wallet, Collection collection, Inventory inventory, ShopItem item) {
        if (plantType == null) {
            return ShopResult.PLANT_TYPE_REQUIRED;
        }
        if (!collection.getUnlockedPlants().contains(plantType)) {
            return ShopResult.PLANT_NOT_UNLOCKED;
        }
        int totalPrice = item.getPriceGems() * count;
        if (wallet.spendGems(totalPrice) == TransactionStatus.INSUFFICIENT_FUND) {
            return ShopResult.INSUFFICIENT_FUNDS;
        }
        inventory.addSeedPackets(plantType, item.getBuyUnit() * count);
        return ShopResult.SUCCESS;
    }

    private ShopResult buyCurrencyExchange(int count, PlayerWallet wallet, ShopItem item) {
        int totalPrice = item.getPriceGems() * count;
        if (wallet.spendGems(totalPrice) == TransactionStatus.INSUFFICIENT_FUND) {
            return ShopResult.INSUFFICIENT_FUNDS;
        }
        wallet.addCoins(item.getBuyUnit() * count);
        return ShopResult.SUCCESS;
    }

    public ShopResult buyDailyOffer(PlayerWallet wallet, Inventory inventory) {
        if (!dailyOffer.isAvailableToday()) {
            return ShopResult.ALREADY_PURCHASED_TODAY;
        }
        if (wallet.spendCoins(dailyOffer.getPriceCoins()) == TransactionStatus.INSUFFICIENT_FUND) {
            return ShopResult.INSUFFICIENT_FUNDS;
        }
        inventory.addSeedPackets(dailyOffer.getPlantType(), 10);
        dailyOffer.purchase();
        return ShopResult.SUCCESS;
    }
}
