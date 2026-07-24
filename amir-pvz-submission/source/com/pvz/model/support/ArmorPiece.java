package com.pvz.model.support;

import com.pvz.model.armor.ArmorDecorator;

public class ArmorPiece extends ArmorDecorator {
    public String name;
    public int health;
    public boolean magnetic;
    public boolean metallic;
    public int damageOrder;
    public ArmorPiece(String name, int health, boolean magnetic) {
        super(health);
        this.name = name;
        this.health = health;
        this.magnetic = magnetic;
        this.metallic = magnetic;
        this.damageOrder = 0;
    }
    public ArmorPiece(String name, int health, boolean metallic,
                      boolean magnetizable, int damageOrder) {
        super(health);
        this.name = name;
        this.health = health;
        this.metallic = metallic;
        this.magnetic = magnetizable;
        this.damageOrder = damageOrder;
    }
    @Override
    public int absorbDamage(int amount) {
        int remaining = super.absorbDamage(amount);
        health = armorHp;
        return remaining;
    }
    public boolean isBroken() {
        return health <= 0;
    }
}
