package com.pvz.model.interfaces;

public interface UpgradeResourceWallet {

    int getCoins();

    int getSeedPackets(String plantType);

    boolean spendCoins(int amount);

    boolean spendSeedPackets(String plantType, int amount);
}
