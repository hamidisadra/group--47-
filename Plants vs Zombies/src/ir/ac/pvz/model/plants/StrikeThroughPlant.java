package ir.ac.pvz.model.plants;

import ir.ac.pvz.model.core.GameObject;
import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.enums.PlantCategory;
import ir.ac.pvz.model.enums.PlantTag;
import ir.ac.pvz.model.interfaces.IAttacker;

public class StrikeThroughPlant extends Plant implements IAttacker {
    public int pierceCount;
    public StrikeThroughPlant(int id, String name, int cost, int baseHp, float rechargeTime,
                              float actionInterval, int damage, int pierceCount,
                              PlantTag... tags) {
        super(id, name, cost, baseHp, rechargeTime, actionInterval, damage,
                PlantCategory.STRIKE_THROUGH, tags);
        this.pierceCount = pierceCount;
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
