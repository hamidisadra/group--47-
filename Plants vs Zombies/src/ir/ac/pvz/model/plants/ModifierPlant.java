package com.pvz.model.plants;

import com.pvz.model.core.Plant;
import com.pvz.model.enums.PlantCategory;
import com.pvz.model.enums.PlantTag;
import com.pvz.model.interfaces.IModifier;

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
