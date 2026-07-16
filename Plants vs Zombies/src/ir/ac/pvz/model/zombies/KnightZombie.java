package com.pvz.model.zombies;

import com.pvz.model.core.Zombie;
import com.pvz.model.support.ArmorDataRepository;
import com.pvz.model.support.ArmorPiece;

public class KnightZombie extends Zombie {

    public KnightZombie() {
        super(0.185f, 190, 100, 550);
        ArmorPiece helmet = new ArmorPiece("helmet", armorHealth("CrownDefault"), true);
        ArmorPiece shoulder = new ArmorPiece("shoulder armor", armorHealth("ShoulderArmorDefault"), false);
        armorPieces.add(helmet);
        armorPieces.add(shoulder);
        armor = helmet;
    }
    private int armorHealth(String alias) {
        return ArmorDataRepository.getInstance().getHealth(alias);
    }
}
