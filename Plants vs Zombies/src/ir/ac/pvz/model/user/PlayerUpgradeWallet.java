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
        if (inventory.getSeedPacketCount(plantType) < amount) {
            return false;
        }

        for (int i = 0; i < amount; i++) {
            if (!inventory.useSeedPacket(plantType)) {
                inventory.addSeedPackets(plantType, i);
                return false;
            }
        }
        return true;
    }
}