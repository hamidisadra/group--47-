package com.pvz.model.plants;

import com.pvz.model.enums.PlantCategory;
import com.pvz.model.support.BalanceDefaults;

public class ArmaMint extends MintPlant {
    public ArmaMint(int id) {
        super(id, "Arma-mint", 85.0f, BalanceDefaults.ARMA_MINT_DURATION_SECONDS, PlantCategory.LOBBER);
    }
}
