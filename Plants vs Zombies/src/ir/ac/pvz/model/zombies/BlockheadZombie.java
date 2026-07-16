package com.pvz.model.zombies;

import com.pvz.model.core.Zombie;
import com.pvz.model.support.ArmorDataRepository;
import com.pvz.model.support.ArmorPiece;

public class BlockheadZombie extends Zombie {

    public BlockheadZombie() {
        super(0.185f, 190, 100, 700);
        ArmorPiece block = new ArmorPiece("block", armorHealth("BrickDefault"), false);
        armorPieces.add(block);
        armor = block;
    }
    private int armorHealth(String alias) {
        return ArmorDataRepository.getInstance().getHealth(alias);
    }
}
