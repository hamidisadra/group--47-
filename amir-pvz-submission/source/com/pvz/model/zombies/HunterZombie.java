package com.pvz.model.zombies;

import com.pvz.game.GameSession;
import com.pvz.model.core.Plant;
import com.pvz.model.core.Zombie;
import com.pvz.model.enums.DamageMode;
import com.pvz.model.enums.ProjectileTrajectory;
import com.pvz.model.enums.ProjectileType;
import com.pvz.model.support.ContinuousPosition;
import com.pvz.model.support.Projectile;

public class HunterZombie extends Zombie {
    public int iceHitCountRequired;
    public HunterZombie() {
        super("HunterZombie");
        this.iceHitCountRequired = 3;
    }
    public Projectile throwIceAtNearestPlant(GameSession session) {
        Plant target = null;
        if (session != null) {
            target = session.findNearestPlantAhead(this);
        }
        if (target == null) {
            return null;
        }
        return new Projectile(ProjectileType.HUNTER_ICE,
                ProjectileTrajectory.STRAIGHT,
                new ContinuousPosition(currentPosition.x, lane), 0f, 0,
                null, target, 0, 0f, false, DamageMode.ARMOR_FIRST);
    }

}
