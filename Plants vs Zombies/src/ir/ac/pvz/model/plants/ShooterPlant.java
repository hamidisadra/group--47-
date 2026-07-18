package ir.ac.pvz.model.plants;

import ir.ac.pvz.model.core.GameObject;
import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.enums.PlantCategory;
import ir.ac.pvz.model.enums.PlantTag;
import ir.ac.pvz.model.enums.ProjectileType;
import ir.ac.pvz.model.interfaces.IAttacker;

public class ShooterPlant extends Plant implements IAttacker {

    public int damage;
    public ProjectileType projectileType;
    public int multiShot;

    public ShooterPlant(int id, String name, int cost, int baseHp, float rechargeTime,
                        float actionInterval, int damage, ProjectileType projectileType,
                        int multiShot, PlantTag... tags) {
        super(id, name, cost, baseHp, rechargeTime, actionInterval, damage,
                PlantCategory.SHOOTER, tags);
        this.damage = damage;
        this.projectileType = projectileType;
        this.multiShot = multiShot;
    }

    @Override
    public void attack(GameObject target) {
        if (target == null || !target.isAlive) {
            return;
        }
        int shots = Math.max(1, multiShot);
        for (int i = 0; i < shots && target.isAlive; i++) {
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
