package ir.ac.pvz.model.plants;

import ir.ac.pvz.model.enums.PlantTag;

public class Kiwibeast extends MeleePlant {
    public Kiwibeast(int id) {
        super(id, "Kiwibeast", 175, 300, 5.0f, 2.00f, 15, true, PlantTag.AOE, PlantTag.RAMP_UP);
    }
}
