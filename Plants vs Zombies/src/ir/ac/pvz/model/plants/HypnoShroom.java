package ir.ac.pvz.model.plants;

import ir.ac.pvz.model.enums.PlantTag;

public class HypnoShroom extends ModifierPlant {
    private boolean gargantuarPlantFoodArmed;
    public HypnoShroom(int id) {
        super(id, "Hypno-shroom", 125, 300, 20.0f, 0.0f,
                PlantTag.SHROOM, PlantTag.MAGIC);
        gargantuarPlantFoodArmed = false;
    }
    @Override
    public void onPlantFood() {
        super.onPlantFood();
        gargantuarPlantFoodArmed = true;
    }
    public boolean consumeGargantuarPlantFood() {
        boolean armed = gargantuarPlantFoodArmed;
        gargantuarPlantFoodArmed = false;
        return armed;
    }
}
