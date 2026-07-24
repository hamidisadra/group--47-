package ir.ac.pvz.model.support;

import ir.ac.pvz.model.core.GameObject;
import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.enums.DamageMode;
import ir.ac.pvz.model.enums.ProjectileTrajectory;
import ir.ac.pvz.model.enums.ProjectileType;

public class Projectile extends GameObject {

    private static int nextProjectileId = 1;

    public final int projectileId;

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
    public float height;
    public float flightProgress;
    public float arcHeight;

    private boolean expired;

    public Projectile(ProjectileType type, ProjectileTrajectory trajectory,
                      ContinuousPosition currentPosition, float movementSpeed,
                      int damageAmount, Plant sourcePlant, GameObject target,
                      int pierceCount, float splashRadius, boolean ignoresObstacles,
                      DamageMode damageMode) {
        super(currentPosition.x, currentPosition.y, 1);
        this.projectileId = nextProjectileId++;
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
        this.height = 0f;
        this.flightProgress = 0f;
        this.arcHeight = 0f;
        if (trajectory == ProjectileTrajectory.ARC) {
            this.arcHeight = 1f;
        }
        this.expired = false;
    }

    @Override
    public void update(int tickCount) {
        for (int i = 0; i < tickCount && !expired; i++) {
            move();
        }
    }

    public void move() {
        float direction = 1f;
        if (isReflected) {
            direction = -1f;
        }
        currentPosition.x += direction * movementSpeed / 10f;
        positionX = currentPosition.x;
        if (trajectory == ProjectileTrajectory.ARC) {
            updateArcHeight();
        }
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

    private void updateArcHeight() {
        if (!(target instanceof GameObject)) {
            return;
        }
        float targetX = target.positionX;
        float sourceX = positionX;
        if (sourcePlant != null) {
            sourceX = sourcePlant.positionX;
        }
        float distance = Math.max(0.0001f, Math.abs(targetX - sourceX));
        flightProgress = Math.min(1f,
                Math.abs(currentPosition.x - sourceX) / distance);
        height = 4f * arcHeight * flightProgress * (1f - flightProgress);
    }
}
