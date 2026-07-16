package com.pvz.model.plants;

import com.pvz.model.enums.PlantCategory;
import com.pvz.model.support.BalanceDefaults;

public class EnchantMint extends MintPlant {
    public EnchantMint(int id) {
        super(id, "Enchant-mint", 85.0f, BalanceDefaults.ENCHANT_MINT_DURATION_SECONDS, PlantCategory.MODIFIER);
    }
}
