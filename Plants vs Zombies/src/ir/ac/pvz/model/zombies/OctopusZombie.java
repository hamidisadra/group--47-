package com.pvz.model.zombies;

import com.pvz.model.core.Plant;
import com.pvz.model.core.Zombie;
import com.pvz.model.support.OctopusBlock;

import java.util.Random;

public class OctopusZombie extends Zombie {

    public float throwOctopusCooldownSeconds;
    public OctopusBlock octopusBlock;

    private final Random random;

    public OctopusZombie() {
        this(new Random());
    }

    public OctopusZombie(Random random) {
        super(0.12f, 910, 100, 900);
        this.random = random == null ? new Random() : random;
        this.throwOctopusCooldownSeconds = randomBetween(7f, 9f);
        this.octopusBlock = null;
    }

    public void scheduleNextThrow() {
        throwOctopusCooldownSeconds = randomBetween(11f, 14f);
    }

    public OctopusBlock throwOctopusAtPlant(Plant plant) {
        return throwOctopusAtPlant(plant, 0);
    }

    public OctopusBlock throwOctopusAtPlant(Plant plant, int health) {
        if (plant == null || plant.isOctopusBlocked || health <= 0) {
            return null;
        }
        OctopusBlock block = new OctopusBlock(health);
        block.blockPlant(plant);
        octopusBlock = block;
        return block;
    }

    private float randomBetween(float minimum, float maximum) {
        return minimum + random.nextFloat() * (maximum - minimum);
    }
}
