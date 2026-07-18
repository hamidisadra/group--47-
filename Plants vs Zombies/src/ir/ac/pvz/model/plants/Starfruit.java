package ir.ac.pvz.model.plants;

import ir.ac.pvz.model.enums.PlantTag;
import ir.ac.pvz.model.enums.ProjectileType;

public class Starfruit extends ShooterPlant {
    public Starfruit(int id) {
        super(id, "Starfruit", 150, 300, 5.0f, 1.5f, 20, ProjectileType.PEA, 1);
    }
}
