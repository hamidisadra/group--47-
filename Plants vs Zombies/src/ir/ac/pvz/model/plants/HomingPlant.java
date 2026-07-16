package com.pvz.model.plants;

import com.pvz.model.core.GameObject;
import com.pvz.model.core.Plant;
import com.pvz.model.enums.PlantCategory;
import com.pvz.model.enums.PlantTag;
import com.pvz.model.enums.TargetingMode;
import com.pvz.model.interfaces.IAttacker;

public class HomingPlant extends Plant implements IAttacker {

    public TargetingMode targetingMode;

    public HomingPlant(int id, String name, int cost, int baseHp, float rechargeTime,
                       float actionInterval, int damage, TargetingMode targetingMode,
                       PlantTag... tags) {
        super(id, name, cost, baseHp, rechargeTime, actionInterval, damage,
                PlantCategory.HOMING, tags);
        this.targetingMode = targetingMode;
    }

    @Override
    public void attack(GameObject target) {
        if (target != null && target.isAlive) {
            target.takeDamage(attackPower);
        }
    }

    @Override
    public int getDamage() {
        return attackPower;
    }

    @Override
    public int getRange() {
        return 9;
    }
}
