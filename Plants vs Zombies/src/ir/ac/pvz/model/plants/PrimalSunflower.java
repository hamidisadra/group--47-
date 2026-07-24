package ir.ac.pvz.model.plants;

public class PrimalSunflower extends SunProducerPlant {
    public PrimalSunflower(int id) {
        super(id, "Primal Sunflower", 75, 300, 5f, 75, 24f);
    }
    @Override
    public void onPlantFood() {
        super.onPlantFood();
        queuePlantFoodSun(225);
    }
}
