package ir.ac.pvz.model.plants;

import ir.ac.pvz.model.enums.PlantTag;
import ir.ac.pvz.model.enums.ProjectileType;

public class Threepeater extends ShooterPlant {

    public Threepeater(int id) {
        super(id, "Threepeater", 300, 300, 5f, 1.5f, 20,
                ProjectileType.PEA, 3, PlantTag.PEA);
    }
}
