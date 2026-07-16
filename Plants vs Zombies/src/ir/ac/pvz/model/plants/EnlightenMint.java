package com.pvz.model.plants;

import com.pvz.model.enums.PlantCategory;
import com.pvz.model.support.BalanceDefaults;

public class EnlightenMint extends MintPlant {
    public EnlightenMint(int id) {
        super(id, "Enlighten-mint", 85.0f, BalanceDefaults.ENLIGHTEN_MINT_DURATION_SECONDS, PlantCategory.SUN_PRODUCER);
    }
}
