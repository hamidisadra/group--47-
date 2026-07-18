package ir.ac.pvz.model.plants;

import ir.ac.pvz.model.enums.PlantTag;

public class DoomShroom extends ExplosivePlant {
    public DoomShroom(int id) {
        super(id, "Doom-shroom", 125, 0, 15.0f, 0.0f, 1800, 9.0f, true, PlantTag.SHROOM);
    }
}
