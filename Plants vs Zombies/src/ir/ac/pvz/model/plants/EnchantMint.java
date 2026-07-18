package ir.ac.pvz.model.plants;

import ir.ac.pvz.model.enums.PlantCategory;
import ir.ac.pvz.model.support.BalanceDefaults;

public class EnchantMint extends MintPlant {
    public EnchantMint(int id) {
        super(id, "Enchant-mint", 85.0f, BalanceDefaults.ENCHANT_MINT_DURATION_SECONDS, PlantCategory.MODIFIER);
    }
}
