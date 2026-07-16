package com.pvz.model.plants;

import com.pvz.model.enums.PlantCategory;
import com.pvz.model.support.BalanceDefaults;

public class AppeaseMint extends MintPlant {
    public AppeaseMint(int id) {
        super(id, "Appease-mint", 85.0f, BalanceDefaults.APPEASE_MINT_DURATION_SECONDS, PlantCategory.SHOOTER);
    }
}
