package ir.ac.pvz.model.plants;

import ir.ac.pvz.model.enums.PlantTag;

public class SweetPotato extends WallPlant {
    public SweetPotato(int id) {
        super(id, "Sweet Potato", 150, 3000, 20.0f, 0, PlantTag.MOVE_ZOMBIES);
    }
}
