package com.pvz.model.plants;

import com.pvz.model.enums.PlantCategory;
import com.pvz.model.support.BalanceDefaults;

public class BombardMint extends MintPlant {
    public BombardMint(int id) {
        super(id, "Bombard-mint", 85.0f, BalanceDefaults.BOMBARD_MINT_DURATION_SECONDS, PlantCategory.EXPLOSIVE);
    }
}
