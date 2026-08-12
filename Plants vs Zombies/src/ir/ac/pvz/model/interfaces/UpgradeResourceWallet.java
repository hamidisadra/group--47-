package ir.ac.pvz.model.interfaces;

public interface UpgradeResourceWallet {
    int getCoins();
    int getSeedPackets(String plantType);
    boolean spendCoins(int amount);
    boolean spendSeedPackets(String plantType, int amount);
    default boolean refundCoins(int amount) {
        return false;
    }
    default boolean refundSeedPackets(String plantType, int amount) {
        return false;
    }
}
