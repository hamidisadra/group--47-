package ir.ac.pvz.model.zombies;

import ir.ac.pvz.model.support.ArmorDataRepository;
import ir.ac.pvz.model.support.ArmorPiece;
import ir.ac.pvz.model.core.Zombie;

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
