package com.pvz.model.zombies;

import com.pvz.model.core.Zombie;
import com.pvz.model.support.ArmorDataRepository;
import com.pvz.model.support.ArmorPiece;

public class BucketheadZombie extends Zombie {

    public BucketheadZombie() {
        super(0.185f, 190, 100, 400);
        ArmorPiece bucket = new ArmorPiece("bucket", armorHealth("BucketDefault"), true);
        armorPieces.add(bucket);
        armor = bucket;
    }
    private int armorHealth(String alias) {
        return ArmorDataRepository.getInstance().getHealth(alias);
    }
}
