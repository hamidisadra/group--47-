package ir.ac.pvz.model.zombies;

import ir.ac.pvz.model.others.GameSession;
import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.enums.DamageMode;
import ir.ac.pvz.model.enums.ProjectileTrajectory;
import ir.ac.pvz.model.enums.ProjectileType;
import ir.ac.pvz.model.support.ContinuousPosition;
import ir.ac.pvz.model.support.Projectile;

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
