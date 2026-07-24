package ir.ac.pvz.model.plants;

import ir.ac.pvz.model.core.GameObject;
import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.enums.PlantCategory;
import ir.ac.pvz.model.enums.PlantTag;
import ir.ac.pvz.model.interfaces.IAttacker;

public class LobberPlant extends Plant implements IAttacker {
    public int damage;
    public float splashRadius;
    public LobberPlant(int id, String name, int cost, int baseHp, float rechargeTime,
                       float actionInterval, int damage, float splashRadius,
                       PlantTag... tags) {
        super(id, name, cost, baseHp, rechargeTime, actionInterval, damage,
                PlantCategory.LOBBER, tags);
        this.damage = damage;
        this.splashRadius = splashRadius;
    }
    @Override
    public void attack(GameObject target) {
        if (target != null && target.isAlive) {
            target.takeDamage(damage);
        }
    }
    @Override
    public int getDamage() {
        return damage;
    }
    @Override
    public int getRange() {
        return 9;
    }
}
