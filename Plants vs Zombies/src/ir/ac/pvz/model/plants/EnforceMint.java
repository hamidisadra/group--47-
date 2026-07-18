package ir.ac.pvz.model.plants;

import ir.ac.pvz.model.enums.PlantCategory;
import ir.ac.pvz.model.support.BalanceDefaults;

public class EnforceMint extends MintPlant {
    public EnforceMint(int id) {
        super(id, "Enforce-mint", 85.0f, BalanceDefaults.ENFORCE_MINT_DURATION_SECONDS, PlantCategory.MELEE);
    }
}
