package ir.ac.pvz.model.plants;

import ir.ac.pvz.model.enums.PlantTag;
import ir.ac.pvz.model.enums.ProjectileType;

public class Rotobaga extends ShooterPlant {
    public Rotobaga(int id) {
        super(id, "Rotobaga", 150, 300, 5.0f, 1.5f, 10, ProjectileType.PEA, 3);
    }
}
