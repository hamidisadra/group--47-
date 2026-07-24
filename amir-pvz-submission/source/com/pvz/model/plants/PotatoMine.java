package com.pvz.model.plants;

import com.pvz.model.enums.PlantTag;
import com.pvz.model.support.Board;
import com.pvz.model.support.GridPosition;

public class PotatoMine extends ExplosivePlant {
    private boolean armed;
    private float elapsedSeconds;
    public PotatoMine(int id) {
        super(id, "Potato Mine", 25, 300, 25f, 0f, 1800, 0f, false,
                PlantTag.TRAP, PlantTag.CHARGE);
        armed = false;
        elapsedSeconds = 0f;
    }
    @Override
    public void onTick() {
        super.onTick();
        elapsedSeconds += 0.1f;
        armed = elapsedSeconds >= 15f;
    }
    @Override
    public void explode(Board board, GridPosition center) {
        if (armed) {
            super.explode(board, center);
        }
    }
    public boolean isArmed() {
        return armed;
    }

}
