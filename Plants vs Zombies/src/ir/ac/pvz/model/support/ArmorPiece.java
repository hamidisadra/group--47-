package com.pvz.model.support;

import com.pvz.model.armor.ArmorDecorator;

public class ArmorPiece extends ArmorDecorator {

    public String name;
    public int health;
    public boolean magnetic;

    public ArmorPiece(String name, int health, boolean magnetic) {
        super(health);
        this.name = name;
        this.health = health;
        this.magnetic = magnetic;
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
