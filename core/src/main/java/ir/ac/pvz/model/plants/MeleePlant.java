package ir.ac.pvz.model.plants;

import ir.ac.pvz.model.core.GameObject;
import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.enums.PlantCategory;
import ir.ac.pvz.model.enums.PlantTag;
import ir.ac.pvz.model.interfaces.IAttacker;
import ir.ac.pvz.model.interfaces.IChargeable;

public class MeleePlant extends Plant implements IAttacker, IChargeable {
    public float attackCooldown;
    public boolean areaEffect;
    protected boolean ready;
    private float attackElapsedSeconds;
    public MeleePlant(int id, String name, int cost, int baseHp, float rechargeTime,
                      float attackCooldown, int damage, boolean areaEffect, PlantTag... tags) {
        super(id, name, cost, baseHp, rechargeTime, attackCooldown, damage,
                PlantCategory.MELEE, tags);
        this.attackCooldown = attackCooldown;
        this.areaEffect = areaEffect;
        this.ready = true;
        this.attackElapsedSeconds = 0f;
    }
    @Override
    public void attack(GameObject target) {
        if (!ready || target == null || !target.isAlive) {
            return;
        }
        target.takeDamage(attackPower);
        ready = false;
        attackElapsedSeconds = 0f;
    }
    @Override
    public void onTick() {
        super.onTick();
        if (!ready) {
            attackElapsedSeconds += 0.1f;
            if (attackElapsedSeconds + 0.0001f >= attackCooldown) {
                charge();
            }
        }
    }
    @Override
    public int getDamage() {
        return attackPower;
    }
    @Override
    public int getRange() {
        return 1;
    }
    @Override
    public void charge() {
        ready = true;
        attackElapsedSeconds = 0f;
    }
    @Override
    public boolean isReady() {
        return ready;
    }
}
