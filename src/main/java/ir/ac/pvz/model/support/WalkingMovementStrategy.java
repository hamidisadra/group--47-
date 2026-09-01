package ir.ac.pvz.model.support;

import ir.ac.pvz.model.core.Zombie;

public final class WalkingMovementStrategy implements ZombieMovementStrategy {
    @Override
    public void move(Zombie zombie, float distance) {
        if (zombie == null) {
            return;
        }
        float direction = -1f;
        if (zombie.isHypnotized) {
            direction = 1f;
        }
        zombie.currentPosition.x += direction * distance * zombie.speed
                * zombie.getChillSlowFactor();
        zombie.positionX = zombie.currentPosition.x;
    }
}
