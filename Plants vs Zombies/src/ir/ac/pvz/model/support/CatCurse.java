package com.pvz.model.support;

import com.pvz.model.core.Plant;
import com.pvz.model.zombies.WizardZombie;

import java.util.ArrayList;
import java.util.List;

public class CatCurse {

    public WizardZombie caster;

    private final List<Plant> transformedPlants;

    public CatCurse(WizardZombie caster) {
        this.caster = caster;
        this.transformedPlants = new ArrayList<>();
    }

    public void transform(Plant plant) {
        if (plant != null && !transformedPlants.contains(plant)) {
            transformedPlants.add(plant);
            plant.addCatCurse();
        }
    }

    public void restoreWhenCasterDies() {
        if (caster == null || caster.isDead()) {
            for (Plant plant : transformedPlants) {
                plant.removeCatCurse();
            }
            transformedPlants.clear();
        }
    }
}
