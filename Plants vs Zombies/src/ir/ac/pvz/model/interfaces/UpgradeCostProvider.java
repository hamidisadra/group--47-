package com.pvz.model.interfaces;

import com.pvz.model.support.Upgrade;

public interface UpgradeCostProvider {

    void configureCost(String plantType, Upgrade upgrade);
}
