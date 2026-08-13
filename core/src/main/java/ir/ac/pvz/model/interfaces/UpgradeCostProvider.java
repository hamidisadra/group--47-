package ir.ac.pvz.model.interfaces;

import ir.ac.pvz.model.support.Upgrade;

public interface UpgradeCostProvider {

    void configureCost(String plantType, Upgrade upgrade);
}
