package ir.ac.pvz.model.zombies;

import ir.ac.pvz.model.support.ArmorDataRepository;
import ir.ac.pvz.model.support.ArmorPiece;
import ir.ac.pvz.model.core.Zombie;

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
