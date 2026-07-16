package com.pvz.game;

import com.pvz.model.core.Plant;
import com.pvz.model.enums.UpgradeResult;
import com.pvz.model.interfaces.UpgradeCostProvider;
import com.pvz.model.interfaces.UpgradeResourceWallet;
import com.pvz.model.support.Upgrade;

public class PlantUpgradeService {

    public UpgradeResult upgrade(Plant plant, UpgradeResourceWallet wallet,
                                 UpgradeCostProvider costProvider) {
        if (plant == null || wallet == null) {
            return UpgradeResult.COST_NOT_CONFIGURED;
        }
        Upgrade upgrade = findNextUpgrade(plant);
        if (upgrade == null) {
            return UpgradeResult.MAX_LEVEL;
        }
        if (costProvider != null) {
            costProvider.configureCost(plant.type, upgrade);
        }
        if (!upgrade.hasConfiguredCost()) {
            return UpgradeResult.COST_NOT_CONFIGURED;
        }
        if (wallet.getCoins() < upgrade.coinCost) {
            return UpgradeResult.NOT_ENOUGH_COINS;
        }
        if (wallet.getSeedPackets(plant.type) < upgrade.seedPacketCost) {
            return UpgradeResult.NOT_ENOUGH_SEED_PACKETS;
        }
        if (!wallet.spendCoins(upgrade.coinCost)) {
            return UpgradeResult.NOT_ENOUGH_COINS;
        }
        if (!wallet.spendSeedPackets(plant.type, upgrade.seedPacketCost)) {
            return UpgradeResult.NOT_ENOUGH_SEED_PACKETS;
        }
        upgrade.applyTo(plant);
        return UpgradeResult.SUCCESS;
    }

    public UpgradeResult upgrade(Plant plant, UpgradeResourceWallet wallet) {
        return upgrade(plant, wallet, null);
    }

    private Upgrade findNextUpgrade(Plant plant) {
        int nextLevel = plant.level + 1;
        for (Upgrade upgrade : plant.levelUpgrades) {
            if (upgrade.level == nextLevel) {
                return upgrade;
            }
        }
        return null;
    }
}
