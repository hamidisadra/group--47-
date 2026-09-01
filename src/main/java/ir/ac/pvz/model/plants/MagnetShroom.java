package ir.ac.pvz.model.plants;

import ir.ac.pvz.model.enums.PlantCategory;
import ir.ac.pvz.model.enums.PlantTag;

public class MagnetShroom extends ModifierPlant {
    public MagnetShroom(int id) {
        super(id, "Magnet-shroom", 100, 300, 15f, 10f,
                PlantTag.SHROOM, PlantTag.MAGIC);
        category = PlantCategory.HOMING;
    }
}
