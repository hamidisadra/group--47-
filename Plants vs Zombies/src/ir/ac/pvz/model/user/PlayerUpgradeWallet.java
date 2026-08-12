package ir.ac.pvz.model.user;

import ir.ac.pvz.model.interfaces.UpgradeResourceWallet;

public class PlayerUpgradeWallet implements UpgradeResourceWallet {

    private final PlayerWallet wallet;
    private final Inventory inventory;

    public PlayerUpgradeWallet(PlayerWallet wallet, Inventory inventory) {
        this.wallet = wallet;
        this.inventory = inventory;
    }

    @Override
    public int getCoins() {
        return wallet.getCoins();
    }

    @Override
    public int getSeedPackets(String plantType) {
        return inventory.getSeedPacketCount(plantType);
    }

    @Override
    public boolean spendCoins(int amount) {
        return wallet.spendCoins(amount) == TransactionStatus.SUCCESS;
    }

    @Override
    public boolean spendSeedPackets(String plantType, int amount) {
        return inventory.useSeedPackets(plantType, amount);
    }
    @Override
    public boolean refundCoins(int amount) {
        return wallet.addCoins(amount) == TransactionStatus.SUCCESS;
    }

    @Override
    public boolean refundSeedPackets(String plantType, int amount) {
        try {
            inventory.addSeedPackets(plantType, amount);
            return true;
        }
        catch (IllegalArgumentException exception) {
            return false;
        }
    }

}