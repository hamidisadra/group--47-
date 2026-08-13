package ir.ac.pvz.model.zombies;

import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.support.Board;
import ir.ac.pvz.model.support.GridPosition;
import ir.ac.pvz.model.support.Tile;
import ir.ac.pvz.model.support.Tombstone;

public class TombRaiserZombie extends Zombie {
    public TombRaiserZombie() {
        super("TombRaiserZombie");
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
