package ir.ac.pvz.model.others;

import ir.ac.pvz.controller.game_core.*;

import ir.ac.pvz.model.support.GridPosition;

public final class SpecialSpawnEvent {
    public final int tick;
    public final String zombieType;
    public final GridPosition position;
    public SpecialSpawnEvent(int tick, String zombieType,
                             GridPosition position) {
        this.tick = tick;
        this.zombieType = zombieType;
        this.position = position;
    }
}
