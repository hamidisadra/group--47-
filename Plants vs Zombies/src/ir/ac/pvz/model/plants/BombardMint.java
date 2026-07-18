package ir.ac.pvz.model.plants;

import ir.ac.pvz.model.enums.PlantCategory;
import ir.ac.pvz.model.support.BalanceDefaults;

public class BombardMint extends MintPlant {
    public BombardMint(int id) {
        super(id, "Bombard-mint", 85.0f, BalanceDefaults.BOMBARD_MINT_DURATION_SECONDS, PlantCategory.EXPLOSIVE);
    }
}
