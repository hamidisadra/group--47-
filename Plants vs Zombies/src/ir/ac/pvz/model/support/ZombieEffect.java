package ir.ac.pvz.model.support;

import ir.ac.pvz.model.enums.ZombieEffectType;
import ir.ac.pvz.model.core.GameObject;

public class ZombieEffect {

    public ZombieEffectType type;
    public float remainingSeconds;

    public ZombieEffect(ZombieEffectType type, float remainingSeconds) {
        this.type = type;
        this.remainingSeconds = remainingSeconds;
    }

    public void apply(GameObject target) {
        if (type == ZombieEffectType.FROZEN || type == ZombieEffectType.CHILLED) {
            target.freeze(Math.round(remainingSeconds * 10));
        }
    }

    public void expire() {
        remainingSeconds = 0f;
    }
}
