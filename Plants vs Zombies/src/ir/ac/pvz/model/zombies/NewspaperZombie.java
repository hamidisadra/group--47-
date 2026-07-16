package com.pvz.model.zombies;

import com.pvz.model.core.Zombie;
import com.pvz.model.support.ArmorPiece;

public class NewspaperZombie extends Zombie {

    private boolean enraged;

    public NewspaperZombie() {
        super(0.22f, 460, 200, 700);
        ArmorPiece newspaper = new ArmorPiece("newspaper", 190, false);
        armorPieces.add(newspaper);
        armor = newspaper;
        this.enraged = false;
    }

    @Override
    public void takeDamage(int amount) {
        boolean hadNewspaper = !armorPieces.isEmpty();
        super.takeDamage(amount);
        if (hadNewspaper && armorPieces.isEmpty()) {
            enraged = true;
            speed *= 4f;
            attackDamage *= 4;
            damageToPlant *= 4;
        }
    }

    public boolean isEnraged() {
        return enraged;
    }
}
