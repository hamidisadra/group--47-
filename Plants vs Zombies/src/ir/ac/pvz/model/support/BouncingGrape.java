package com.pvz.model.support;

import com.pvz.model.core.Plant;
import com.pvz.model.core.Zombie;
import com.pvz.model.enums.ProjectileType;

import java.util.List;
import java.util.Random;

public class BouncingGrape {

    private final Plant source;
    private final int damage;
    private final Random random;
    private float remainingSeconds;
    private float untilNextBounce;
    private int bouncesRemaining;

    public BouncingGrape(Plant source, int damage, int bounces) {
        this.source = source;
        this.damage = damage;
        this.remainingSeconds = 5f;
        this.untilNextBounce = 0f;
        this.bouncesRemaining = Math.max(0, bounces);
        this.random = new Random();
    }

    public boolean update(float elapsedSeconds, Board board,
                          ProjectileResolver resolver) {
        remainingSeconds -= elapsedSeconds;
        untilNextBounce -= elapsedSeconds;
        if (remainingSeconds <= 0f || bouncesRemaining <= 0) {
            return false;
        }
        if (untilNextBounce > 0f) {
            return true;
        }
        List<Zombie> zombies = board.getAllAliveZombies();
        if (!zombies.isEmpty()) {
            Zombie target = zombies.get(random.nextInt(zombies.size()));
            resolver.hitZombie(source, target, damage, ProjectileType.PIERCING, board);
            bouncesRemaining--;
        }
        untilNextBounce = 0.5f;
        return remainingSeconds > 0f && bouncesRemaining > 0;
    }

    public float getRemainingSeconds() {
        return remainingSeconds;
    }

    public int getBouncesRemaining() {
        return bouncesRemaining;
    }
}
