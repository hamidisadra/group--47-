package com.pvz.model.armor;

public abstract class ArmorDecorator {

    public int armorHp;

    protected ArmorDecorator(int armorHp) {
        this.armorHp = armorHp;
    }

    public int absorbDamage(int amount) {
        if (armorHp <= 0) {
            return amount;
        }
        int absorbed = Math.min(armorHp, amount);
        armorHp -= absorbed;
        return amount - absorbed;
    }
}
