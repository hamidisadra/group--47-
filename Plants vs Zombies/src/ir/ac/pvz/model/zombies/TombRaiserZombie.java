package com.pvz.model.zombies;

import com.pvz.model.core.Zombie;
import com.pvz.model.support.Board;
import com.pvz.model.support.GridPosition;
import com.pvz.model.support.Tile;
import com.pvz.model.support.Tombstone;

public class TombRaiserZombie extends Zombie {

    public TombRaiserZombie() {
        super(0.185f, 380, 100, 300);
    }

    public void createTombstone(Board board, GridPosition position) {
        if (board == null || !board.isInside(position)) {
            return;
        }
        Tile tile = board.getTile(position);
        tile.obstacle = new Tombstone();
        tile.canPlant = false;
    }
}
