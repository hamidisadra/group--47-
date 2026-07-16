package com.pvz.model.zombies;

import com.pvz.model.core.Zombie;
import com.pvz.model.support.ArmorDataRepository;
import com.pvz.model.support.ArmorPiece;

public class ConeheadZombie extends Zombie {

    public ConeheadZombie() {
        super(0.185f, 190, 100, 200);
        ArmorPiece cone = new ArmorPiece("cone", armorHealth("ConeDefault"), false);
        armorPieces.add(cone);
        armor = cone;
    }
    private int armorHealth(String alias) {
        return ArmorDataRepository.getInstance().getHealth(alias);
    }
}
