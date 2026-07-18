package ir.ac.pvz.model.plants;

import ir.ac.pvz.model.enums.PlantTag;

public class FumeShroom extends StrikeThroughPlant {

    public FumeShroom(int id) {
        super(id, "Fume-shroom", 125, 300, 5f, 1.5f, 20, Integer.MAX_VALUE,
                PlantTag.SHROOM);
    }
}
