package ir.ac.pvz.model.plants;

import ir.ac.pvz.model.enums.PlantTag;
import ir.ac.pvz.model.enums.ProjectileType;

public class MegaGatlingPea extends ShooterPlant {
    public MegaGatlingPea(int id) {
        super(id, "Mega Gatling Pea", 400, 300, 5.0f, 1.5f, 20, ProjectileType.PEA, 4, PlantTag.PEA);
    }
}
