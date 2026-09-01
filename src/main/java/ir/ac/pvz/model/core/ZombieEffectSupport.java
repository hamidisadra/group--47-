package ir.ac.pvz.model.core;

import ir.ac.pvz.model.enums.ZombieEffectType;
import ir.ac.pvz.model.support.ZombieEffect;

import java.util.Iterator;

final class ZombieEffectSupport {
    private ZombieEffectSupport() {
    }

    static ZombieEffect find(Zombie zombie, ZombieEffectType type) {
        for (ZombieEffect effect : zombie.effects) {
            if (effect.type == type) {
                return effect;
            }
        }

        return null;
    }

    static void refresh(Zombie zombie, ZombieEffectType type, float seconds) {
        ZombieEffect existing = find(zombie, type);

        if (existing == null) {
            zombie.effects.add(new ZombieEffect(type, seconds));
            return;
        }

        existing.remainingSeconds = Math.max(existing.remainingSeconds, seconds);
    }

    static void expire(Zombie zombie, float elapsedSeconds) {
        Iterator<ZombieEffect> iterator = zombie.effects.iterator();

        while (iterator.hasNext()) {
            ZombieEffect effect = iterator.next();
            effect.remainingSeconds -= elapsedSeconds;

            if (effect.remainingSeconds <= 0f) {
                effect.expire();
                iterator.remove();
            }
        }
    }

    static void clearExpiredState(Zombie zombie) {
        if (find(zombie, ZombieEffectType.CHILLED) == null) {
            zombie.chillSlowFactor = 1f;
        }

        if (find(zombie, ZombieEffectType.POISONED) == null) {
            zombie.poisonDamagePerSecond = 0;
            zombie.poisonDamageAccumulator = 0f;
        }
    }

    static int poisonDamageThisStep(Zombie zombie, float elapsedSeconds) {
        if (zombie.poisonDamagePerSecond <= 0 || !zombie.isAlive
                || find(zombie, ZombieEffectType.POISONED) == null) {
            return 0;
        }

        zombie.poisonDamageAccumulator +=
                zombie.poisonDamagePerSecond * elapsedSeconds;

        int damage = (int) zombie.poisonDamageAccumulator;

        if (damage <= 0) {
            return 0;
        }

        zombie.poisonDamageAccumulator -= damage;

        return damage;
    }
}
