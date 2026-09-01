package ir.ac.pvz.model.travel;

import ir.ac.pvz.model.user.Collection;
import ir.ac.pvz.model.user.Inventory;
import ir.ac.pvz.model.user.PlayerWallet;

public class Reward {
    private RewardType type;
    private int amount;
    private String plantType;

    public Reward(RewardType type, int amount, String plantType) {
        this.type = type;
        this.amount = amount;
        this.plantType = plantType;
    }

    public RewardType getType() {
        return type;
    }

    public int getAmount() {
        return amount;
    }

    public String getPlantType() {
        return plantType;
    }

    public void apply(PlayerWallet wallet, Collection collection, Inventory inventory) {
        switch (type) {
            case CURRENCY_COINS:
                wallet.addCoins(amount);
                break;
            case CURRENCY_GEMS:
                wallet.addGems(amount);
                break;
            case UNLOCKABLE:
                collection.unlockPlant(plantType);
                break;
            case INVENTORY:
                inventory.addSeedPackets(plantType, amount);
                break;
            default:
                break;
        }
    }
}
