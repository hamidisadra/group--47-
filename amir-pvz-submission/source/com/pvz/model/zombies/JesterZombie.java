package com.pvz.model.zombies;

import com.pvz.model.core.Zombie;
import com.pvz.model.enums.ProjectileTrajectory;
import com.pvz.model.support.Projectile;
import com.pvz.model.support.ZombieDataRepository;

public class JesterZombie extends Zombie {

    public boolean isSpinning;
    private float normalSpeed;
    private final float spinSpeedMultiplier;
    private boolean incomingProjectileThisTick;
    public JesterZombie() {
        super("JesterZombie");
        this.isSpinning = false;
        this.normalSpeed = speed;
        this.spinSpeedMultiplier = (float) ZombieDataRepository.getInstance()
                .getNumber("JesterZombie",
                        "MoveSpeedMultiplierWhileJuggling", 1d);
        this.incomingProjectileThisTick = false;
    }
    @Override
    public void applyBaseData(float movementSpeed, int baseHealth,
                              int eatDamagePerSecond, int cost,
                              int weight, boolean plantFoodEligible) {
        float basicSpeed = (float) ZombieDataRepository.getInstance().getNumber(
                "BasicZombie", "Speed", 0.185d);
        float slowSpeed = Math.min(movementSpeed, basicSpeed);
        super.applyBaseData(slowSpeed, baseHealth, eatDamagePerSecond,
                cost, weight, plantFoodEligible);
        normalSpeed = slowSpeed;
        speed = normalSpeed;
        if (isSpinning) {
            speed = normalSpeed * spinSpeedMultiplier;
        }
    }
    public void spinUntilNoIncomingProjectile() {
        isSpinning = true;
        incomingProjectileThisTick = true;
        speed = normalSpeed * spinSpeedMultiplier;
    }
    public void stopSpinning() {
        isSpinning = false;
        speed = normalSpeed;
    }

    public Projectile reflectStraightProjectile(Projectile projectile) {
        if (projectile != null
                && projectile.trajectory == ProjectileTrajectory.STRAIGHT) {
            spinUntilNoIncomingProjectile();
            projectile.reflectToPlants();
            return projectile;
        }
        return null;
    }
    @Override
    public void receiveProjectile(Projectile projectile) {
        if (projectile != null
                && projectile.trajectory == ProjectileTrajectory.STRAIGHT) {
            reflectStraightProjectile(projectile);
            return;
        }
        super.receiveProjectile(projectile);
    }
    @Override
    public void update(int tickCount) {
        super.update(tickCount);
        if (!isSpinning) {
            return;
        }
        if (incomingProjectileThisTick) {
            incomingProjectileThisTick = false;
        } else {
            stopSpinning();
        }
    }
}
