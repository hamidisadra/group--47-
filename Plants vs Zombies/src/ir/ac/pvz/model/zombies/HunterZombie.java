package ir.ac.pvz.model.zombies;

import ir.ac.pvz.model.others.GameSession;
import ir.ac.pvz.model.enums.DamageMode;
import ir.ac.pvz.model.enums.ProjectileTrajectory;
import ir.ac.pvz.model.enums.ProjectileType;
import ir.ac.pvz.model.support.ContinuousPosition;
import ir.ac.pvz.model.support.Projectile;
import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.core.Zombie;

public class HunterZombie extends Zombie {

    public int iceHitCountRequired;

    public HunterZombie() {
        super(0.12f, 700, 100, 500);
        this.iceHitCountRequired = 3;
    }

    public Projectile throwIceAtNearestPlant(GameSession session) {
        Plant target = session == null ? null
                : session.findNearestPlantAhead(this);
        if (target == null) {
            return null;
        }
        return new Projectile(ProjectileType.HUNTER_ICE,
                ProjectileTrajectory.STRAIGHT,
                new ContinuousPosition(currentPosition.x, lane), 0f, 0,
                null, target, 0, 0f, false, DamageMode.ARMOR_FIRST);
    }

    @Override
    public void freeze(int duration) {
    }
}
