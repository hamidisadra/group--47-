package com.pvz.model.support;

import com.pvz.model.core.GameObject;
import com.pvz.model.enums.ZombieEffectType;

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
