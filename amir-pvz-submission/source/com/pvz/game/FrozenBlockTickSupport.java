package com.pvz.game;

import com.pvz.model.support.Board;
import com.pvz.model.support.FrozenBlock;
import com.pvz.model.support.GridPosition;
import com.pvz.model.support.TickClock;
import com.pvz.model.support.Tile;

final class FrozenBlockTickSupport {
    private FrozenBlockTickSupport() {
    }
    static void update(Board board, TickClock clock) {
        for (int row = 0; row < board.rows; row++) {
            for (int column = 0; column < board.columns; column++) {
                Tile tile = board.getTile(new GridPosition(column, row));
                meltIfHeated(board, clock, tile);
            }
        }
    }
    private static void meltIfHeated(
            Board board, TickClock clock, Tile tile) {
        if (!(tile.obstacle instanceof FrozenBlock)
                || !tile.hasAdjacentFirePlant(board)) {
            return;
        }
        FrozenBlock block = (FrozenBlock) tile.obstacle;
        int damage = Math.round(block.adjacentFireMeltRatePerSecond
                * clock.getTickDurationSeconds());
        block.takeDamage(Math.max(1, damage));
        if (!block.isAlive) {
            board.clearDestroyedObstacle(tile);
        }
    }
}
