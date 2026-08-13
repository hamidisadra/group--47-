package ir.ac.pvz.model.plants;

import ir.ac.pvz.model.enums.PlantTag;

public class PepperPult extends LobberPlant {
    public PepperPult(int id) {
        super(id, "Pepper-pult", 200, 300, 5.0f, 2.9f, 50, 1.0f, PlantTag.FIRE, PlantTag.AOE);
    }
}
