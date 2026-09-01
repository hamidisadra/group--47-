package ir.ac.pvz.model.zombies;

import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.enums.DamageMode;
import ir.ac.pvz.model.enums.ProjectileTrajectory;
import ir.ac.pvz.model.enums.ProjectileType;
import ir.ac.pvz.model.others.GameSession;
import ir.ac.pvz.model.support.ContinuousPosition;
import ir.ac.pvz.model.support.Projectile;

public class PeashooterZombie extends Zombie {

    public int peaDamage;
    public float shootCooldownSeconds;

    public PeashooterZombie() {
        super(0.1f, 190, 100, 100);
        this.peaDamage = 20;
        this.shootCooldownSeconds = 1.5f;
    }

    public Projectile shootAtNearestPlant(GameSession session) {
        Plant target = session == null ? null : session.findNearestPlantAhead(this);

        if (target == null) {
            return null;
        }

        return (new Projectile(ProjectileType.PEA,
                ProjectileTrajectory.STRAIGHT,
                new ContinuousPosition(currentPosition.x, lane), 0f, peaDamage,
                null, target, 0, 0f, false, DamageMode.ARMOR_FIRST));
    }
}