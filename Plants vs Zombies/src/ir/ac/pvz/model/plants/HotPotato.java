package ir.ac.pvz.model.plants;

import ir.ac.pvz.model.enums.PlantTag;

public class HotPotato extends ExplosivePlant {
    public HotPotato(int id) {
        super(id, "Hot Potato", 0, 0, 5.0f, 0.0f, 0, 0.0f, true, PlantTag.FIRE);
    }
}
