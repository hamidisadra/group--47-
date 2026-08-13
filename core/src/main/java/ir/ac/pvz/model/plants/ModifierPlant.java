package ir.ac.pvz.model.plants;

import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.enums.PlantCategory;
import ir.ac.pvz.model.enums.PlantTag;
import ir.ac.pvz.model.interfaces.IModifier;

public class ModifierPlant extends Plant implements IModifier {
    public ModifierPlant(int id, String name, int cost, int baseHp, float rechargeTime,
                         float actionInterval, PlantTag... tags) {
        super(id, name, cost, baseHp, rechargeTime, actionInterval, 0,
                PlantCategory.MODIFIER, tags);
    }
    @Override
    public void modify(Plant target) {
        if (target != null) {
            target.applyPlantFoodEffect();
        }
    }
}
