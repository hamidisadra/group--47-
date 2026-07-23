package com.pvz.model.zombies;

import com.pvz.model.core.Zombie;
import com.pvz.model.support.Barrel;
import com.pvz.model.support.ContinuousPosition;
import com.pvz.model.support.ZombieBaseStats;

public class BarrelRollerZombie extends Zombie {
    public Barrel barrel;
    public BarrelRollerZombie(ZombieBaseStats stats) {
        super(stats.speed, stats.health, stats.eatDamagePerSecond,
                stats.waveCost);
        this.barrel = new Barrel(stats.accessoryHealth,
                new ContinuousPosition(currentPosition.x, currentPosition.y));
    }
    @Override
    public void move(float deltaX) {
        super.move(deltaX);
        if (barrel != null && barrel.health > 0) {
            barrel.position.x = currentPosition.x - 0.2f;
            barrel.position.y = lane;
        }
    }

}
