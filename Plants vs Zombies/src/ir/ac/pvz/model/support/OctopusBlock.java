package com.pvz.model.support;

import com.pvz.model.core.Plant;

public class OctopusBlock {

    public int health;

    private Plant blockedPlant;

    public OctopusBlock(int health) {
        this.health = Math.max(0, health);
    }

    public void blockPlant(Plant plant) {
        if (plant == null || plant.isOctopusBlocked) {
            return;
        }
        blockedPlant = plant;
        if (plant != null) {
            plant.isOctopusBlocked = true;
            plant.blockingOctopus = this;
            plant.freeze(Integer.MAX_VALUE);
        }
    }

    public void takeDamage(int amount) {
        if (amount <= 0 || health <= 0) {
            return;
        }
        health = Math.max(0, health - amount);
        if (health == 0) {
            destroy();
        }
    }

    public void destroy() {
        health = 0;
        if (blockedPlant != null) {
            if (blockedPlant.blockingOctopus == this) {
                blockedPlant.isOctopusBlocked = false;
                blockedPlant.blockingOctopus = null;
                if (!blockedPlant.isCatTransformed
                        && !blockedPlant.isPermanentlyFrozen()) {
                    blockedPlant.melt();
                }
            }
        }
        blockedPlant = null;
    }

    public Plant getBlockedPlant() {
        return blockedPlant;
    }
}
