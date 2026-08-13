package ir.ac.pvz.model.support;

import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.core.Zombie;
import java.util.ArrayList;
import java.util.List;

public class CatCurse {
    public Zombie caster;
    private final List<Plant> transformedPlants;
    public CatCurse(Zombie caster) {
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
