package ir.ac.pvz.model.plants;

import ir.ac.pvz.model.enums.PlantCategory;
import ir.ac.pvz.model.support.BalanceDefaults;

public class ReinforceMint extends MintPlant {
    public ReinforceMint(int id) {
        super(id, "Reinforce-mint", 85.0f, BalanceDefaults.REINFORCE_MINT_DURATION_SECONDS, PlantCategory.WALL);
    }
}
