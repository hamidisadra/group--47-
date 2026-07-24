package com.pvz.model.plants;

import com.pvz.model.core.Zombie;
import com.pvz.model.enums.PlantTag;

public class ExplodeONut extends WallPlant {
    public int metalArmorHealth;
    public int metalArmorMaxHealth;
    private boolean armorExplosionPending;
    public ExplodeONut(int id) {
        super(id, "Explode-o-nut", 50, 4000, 20f, 0,
                PlantTag.EXPLOSIVE);
        attackPower = 1800;
        metalArmorHealth = 0;
        metalArmorMaxHealth = 0;
        armorExplosionPending = false;
    }
    public void equipMetalArmor(int health) {
        metalArmorMaxHealth = Math.max(1, health);
        metalArmorHealth = metalArmorMaxHealth;
        armorExplosionPending = false;
    }
    @Override
    public void takeDamage(int amount) {
        int remaining = Math.max(0, amount);
        if (metalArmorHealth > 0 && remaining > 0) {
            int absorbed = Math.min(metalArmorHealth, remaining);
            metalArmorHealth -= absorbed;
            remaining -= absorbed;
            if (metalArmorHealth == 0) {
                armorExplosionPending = true;
            }
        }
        if (remaining > 0) {
            super.takeDamage(remaining);
        }
    }
    public boolean consumeArmorExplosionPending() {
        boolean pending = armorExplosionPending;
        armorExplosionPending = false;
        return pending;
    }
    @Override
    public void block(Zombie zombie) {
        if (!isAlive && zombie != null) {
            zombie.takeDamage(attackPower);
        }
    }
}
