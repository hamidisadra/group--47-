package ir.ac.pvz.model.support;

import ir.ac.pvz.model.core.Zombie;

public final class ZombieDeathEvent {

    public final Zombie zombie;
    public final String type;
    public final ContinuousPosition position;

    public ZombieDeathEvent(Zombie zombie) {
        this.zombie = zombie;
        if (zombie == null) {
            this.type = "";
            this.position = new ContinuousPosition(0f, 0);
            return;
        }
        this.type = zombie.getType();
        if (zombie.currentPosition == null) {
            this.position = new ContinuousPosition(0f, 0);
        } else {
            this.position = new ContinuousPosition(
                    zombie.currentPosition.x, zombie.lane);
        }
    }
}
