package ir.ac.pvz.model.plants;

import ir.ac.pvz.model.enums.PlantTag;

public class TangleKelp extends ExplosivePlant {
    public TangleKelp(int id) {
        super(id, "Tangle Kelp", 25, 300, 15.0f, 0.0f, 0, 0.0f, false, PlantTag.TRAP, PlantTag.WATER);
    }
}
