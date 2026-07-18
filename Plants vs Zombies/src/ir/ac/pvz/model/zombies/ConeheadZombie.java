package ir.ac.pvz.model.zombies;

import ir.ac.pvz.model.support.ArmorDataRepository;
import ir.ac.pvz.model.support.ArmorPiece;
import ir.ac.pvz.model.core.Zombie;

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
