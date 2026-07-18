package ir.ac.pvz.model.plants;

public class WallNut extends WallPlant {

    public WallNut(int id) {
        super(id, "Wall-nut", 50, 4000, 20f, 0);
    }

    @Override
    public void onPlantFood() {
        super.onPlantFood();
        baseHp += 4000;
        health += 4000;
        currentHp += 4000;
    }
}
