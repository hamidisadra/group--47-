package com.pvz.model.plants;

import com.pvz.model.enums.PlantCategory;
import com.pvz.model.support.BalanceDefaults;

public class EnforceMint extends MintPlant {
    public EnforceMint(int id) {
        super(id, "Enforce-mint", 85.0f, BalanceDefaults.ENFORCE_MINT_DURATION_SECONDS, PlantCategory.MELEE);
    }
}
