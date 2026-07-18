package ir.ac.pvz.model.plants;

import ir.ac.pvz.model.core.GameObject;
import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.enums.PlantTag;
import ir.ac.pvz.model.enums.ProjectileType;
import ir.ac.pvz.model.interfaces.IIceEffect;

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
        target.chill(SLOW_FACTOR, level >= 3 ? 5f : 3f);
    }

    @Override
    public float getSlowFactor() {
        return SLOW_FACTOR;
    }
}
