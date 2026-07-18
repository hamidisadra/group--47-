package ir.ac.pvz.model.support;

import ir.ac.pvz.model.core.GameObject;
import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.enums.DamageMode;
import ir.ac.pvz.model.enums.ProjectileTrajectory;
import ir.ac.pvz.model.enums.ProjectileType;

public class Projectile extends GameObject {

    public ProjectileType type;
    public ProjectileTrajectory trajectory;
    public ContinuousPosition currentPosition;
    public float movementSpeed;
    public int damageAmount;
    public Plant sourcePlant;
    public GameObject target;
    public int pierceCount;
    public float splashRadius;
    public boolean ignoresObstacles;
    public DamageMode damageMode;
    public boolean isReflected;

    private boolean expired;

    public Projectile(ProjectileType type, ProjectileTrajectory trajectory,
                      ContinuousPosition currentPosition, float movementSpeed,
                      int damageAmount, Plant sourcePlant, GameObject target,
                      int pierceCount, float splashRadius, boolean ignoresObstacles,
                      DamageMode damageMode) {
        super(currentPosition.x, currentPosition.y, 1);
        this.type = type;
        this.trajectory = trajectory;
        this.currentPosition = currentPosition;
        this.movementSpeed = movementSpeed;
        this.damageAmount = damageAmount;
        this.sourcePlant = sourcePlant;
        this.target = target;
        this.pierceCount = pierceCount;
        this.splashRadius = splashRadius;
        this.ignoresObstacles = ignoresObstacles;
        this.damageMode = damageMode;
        this.isReflected = false;
        this.expired = false;
    }

    @Override
    public void update(int tickCount) {
        for (int i = 0; i < tickCount && !expired; i++) {
            move();
        }
    }

    public void move() {
        float direction = isReflected ? -1f : 1f;
        currentPosition.x += direction * movementSpeed / 10f;
        positionX = currentPosition.x;
    }

    public boolean canHit(GameObject target) {
        return target != null && target.isAlive && !expired;
    }

    public void hit(GameObject target) {
        if (!canHit(target)) {
            return;
        }
        if (type == ProjectileType.HUNTER_ICE && target instanceof Plant) {
            ((Plant) target).receiveHunterIceHit(3);
        } else if (target instanceof Zombie) {
            Zombie zombie = (Zombie) target;
            if (damageMode == DamageMode.INSTANT_KILL) {
                zombie.receiveInstantKill(trajectory);
            } else {
                zombie.receiveProjectile(this);
            }
        } else if (damageMode == DamageMode.INSTANT_KILL) {
            target.die();
        } else {
            target.takeDamage(damageAmount);
        }
        if (pierceCount > 0) {
            pierceCount--;
        } else {
            expired = true;
        }
    }

    public void hitObstacle(TileObstacle obstacle) {
        if (obstacle != null && !ignoresObstacles) {
            obstacle.takeDamage(damageAmount);
            expired = true;
        }
    }

    public void reflectToPlants() {
        isReflected = true;
        type = ProjectileType.REFLECTED;
    }

    public boolean isExpired() {
        return expired || !isAlive;
    }
}
