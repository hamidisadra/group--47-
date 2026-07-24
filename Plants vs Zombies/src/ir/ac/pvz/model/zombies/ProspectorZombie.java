package ir.ac.pvz.model.zombies;

import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.enums.ProjectileType;
import ir.ac.pvz.model.support.Dynamite;
import ir.ac.pvz.model.support.Projectile;
import ir.ac.pvz.model.support.ZombieDataRepository;

public class ProspectorZombie extends Zombie {
    public Dynamite dynamite;
    public boolean reversedByDynamite;
    public ProspectorZombie() {
        super("ProspectorZombie");
        this.dynamite = new Dynamite((float) ZombieDataRepository.getInstance()
                .getNumber("ProspectorZombie", "LaunchCountdown", 10d));
        this.reversedByDynamite = false;
    }
    @Override
    public void update(int tickCount) {
        super.update(tickCount);
        if (!dynamite.isExtinguished && dynamite.remainingSeconds > 0f) {
            dynamite.remainingSeconds -= tickCount / 10f;
            if (dynamite.remainingSeconds <= 0f) {
                dynamite.explodeAndReverseDirection(this);
                reversedByDynamite = true;
            }
        }
    }
    @Override
    public void move(float deltaX) {
        if (!reversedByDynamite) {
            super.move(deltaX);
            return;
        }
        if (!isAlive) {
            return;
        }
        currentPosition.x += deltaX * speed;
        positionX = currentPosition.x;
    }
    @Override
    public void receiveProjectile(Projectile projectile) {
        if (projectile != null && projectile.type == ProjectileType.ICE) {
            dynamite.extinguishByIce();
        }
        super.receiveProjectile(projectile);
    }
}
