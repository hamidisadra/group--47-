package com.pvz.model.plants;

import com.pvz.model.enums.PlantTag;
import com.pvz.model.support.Board;
import com.pvz.model.support.GridPosition;

public class Jalapeno extends ExplosivePlant {

    private boolean explodedInLane;

    public Jalapeno(int id) {
        super(id, "Jalapeno", 125, 0, 35f, 0f, 1800, 9f, true,
                PlantTag.FIRE);
        this.explodedInLane = false;
    }

    @Override
    public void explode(Board board, GridPosition center) {
        if (explodedInLane || board == null || center == null) {
            return;
        }
        explodedInLane = true;
        board.getZombiesInLane(center.y)
                .forEach(zombie -> zombie.takeDamage(explosionDamage));
        die();
    }
}
