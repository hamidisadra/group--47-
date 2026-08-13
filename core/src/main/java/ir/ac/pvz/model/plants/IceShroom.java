package ir.ac.pvz.model.plants;

import ir.ac.pvz.model.enums.PlantTag;

public class IceShroom extends ExplosivePlant {
    public IceShroom(int id) {
        super(id, "Ice-shroom", 75, 0, 50.0f, 0.0f, 0, 0.0f, true, PlantTag.SHROOM, PlantTag.ICE);
    }
}
