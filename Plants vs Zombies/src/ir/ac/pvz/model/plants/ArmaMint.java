package ir.ac.pvz.model.plants;

import ir.ac.pvz.model.enums.PlantCategory;
import ir.ac.pvz.model.support.BalanceDefaults;

public class ArmaMint extends MintPlant {
    public ArmaMint(int id) {
        super(id, "Arma-mint", 85.0f, BalanceDefaults.ARMA_MINT_DURATION_SECONDS, PlantCategory.LOBBER);
    }
}
