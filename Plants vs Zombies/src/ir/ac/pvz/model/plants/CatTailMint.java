package com.pvz.model.plants;

import com.pvz.model.enums.PlantCategory;
import com.pvz.model.support.BalanceDefaults;

public class CatTailMint extends MintPlant {
    public CatTailMint(int id) {
        super(id, "catTail-mint", 85.0f, BalanceDefaults.CATTAIL_MINT_DURATION_SECONDS, PlantCategory.HOMING);
    }
}
