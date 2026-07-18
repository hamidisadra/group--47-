package ir.ac.pvz.model.plants;

import ir.ac.pvz.model.enums.PlantCategory;
import ir.ac.pvz.model.support.BalanceDefaults;

public class PierceMint extends MintPlant {
    public PierceMint(int id) {
        super(id, "Pierce-mint", 85.0f, BalanceDefaults.PIERCE_MINT_DURATION_SECONDS, PlantCategory.STRIKE_THROUGH);
    }
}
