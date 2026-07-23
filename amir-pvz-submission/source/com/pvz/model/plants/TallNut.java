package com.pvz.model.plants;

public class TallNut extends WallPlant {

    public TallNut(int id) {
        super(id, "Tall-nut", 125, 8000, 20f, 0);
    }

    @Override
    public void onPlantFood() {
        super.onPlantFood();
        baseHp += 8000;
        health += 8000;
        currentHp += 8000;
    }
}
