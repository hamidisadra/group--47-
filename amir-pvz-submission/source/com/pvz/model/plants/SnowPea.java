package com.pvz.model.plants;

import com.pvz.model.core.GameObject;
import com.pvz.model.core.Zombie;
import com.pvz.model.enums.PlantTag;
import com.pvz.model.enums.ProjectileType;
import com.pvz.model.interfaces.IIceEffect;

public class SnowPea extends ShooterPlant implements IIceEffect {
    private static final float SLOW_FACTOR = 0.5f;
    public SnowPea(int id) {
        super(id, "Snow Pea", 150, 300, 5f, 1.5f, 20,
                ProjectileType.ICE, 1, PlantTag.ICE, PlantTag.PEA);
    }
    @Override
    public void attack(GameObject target) {
        super.attack(target);
        if (target instanceof Zombie) {
            chill((Zombie) target);
        }
    }
    @Override
    public void chill(Zombie target) {
        float chillDuration = 3f;
        if (level >= 3) {
            chillDuration = 5f;
        }
        target.chill(SLOW_FACTOR, chillDuration);
    }
    @Override
    public float getSlowFactor() {
        return SLOW_FACTOR;
    }
}
