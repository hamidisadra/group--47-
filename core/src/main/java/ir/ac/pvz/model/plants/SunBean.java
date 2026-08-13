package ir.ac.pvz.model.plants;

import ir.ac.pvz.model.enums.PlantTag;

public class SunBean extends WallPlant {
    public SunBean(int id) {
        super(id, "Sun Bean", 50, 1000, 20.0f, 0, PlantTag.SUN);
    }
}
