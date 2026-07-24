package ir.ac.pvz.model.support;

import ir.ac.pvz.model.interfaces.UpgradeCostProvider;

public class LevelBasedUpgradeCost implements UpgradeCostProvider {

    public static final int BASE_COIN_COST = 1000;
    public static final int BASE_SEED_PACKET_COST = 5;

    @Override
    public void configureCost(String plantType, Upgrade upgrade) {
        if (upgrade == null) {
            return;
        }
        int step = upgrade.level - 1;
        upgrade.setCost(BASE_COIN_COST * step, BASE_SEED_PACKET_COST * step);
    }

    public static int coinCostForLevel(int nextLevel) {
        return BASE_COIN_COST * (nextLevel - 1);
    }

    public static int seedPacketCostForLevel(int nextLevel) {
        return BASE_SEED_PACKET_COST * (nextLevel - 1);
    }
}