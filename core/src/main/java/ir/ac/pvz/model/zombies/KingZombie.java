package ir.ac.pvz.model.zombies;

import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.support.ArmorDataRepository;
import ir.ac.pvz.model.support.ArmorPiece;

public class KingZombie extends Zombie {

    public KingZombie() {
        super("KingZombie");
    }
    public boolean promoteNearbyBasicZombie(Zombie target) {
        if (target == null || target.isDead()) {
            return false;
        }
        ArmorDataRepository repository = ArmorDataRepository.getInstance();
        target.addArmorPiece(new ArmorPiece("helmet",
                repository.getHealth("CrownDefault"),
                repository.isMetallic("CrownDefault")));
        target.addArmorPiece(new ArmorPiece("shoulder armor",
                repository.getHealth("ShoulderArmorDefault"),
                repository.isMetallic("ShoulderArmorDefault")));
        target.setIdentity("KnightZombie", "KnightZombie");
        return true;
    }
    public Zombie promoteNearbyBasicZombie() {
        BasicGroup knight = new BasicGroup(0.185f, 190, 100, 550);
        knight.setIdentity("KnightZombie", "KnightZombie");
        ArmorDataRepository repository = ArmorDataRepository.getInstance();
        knight.addArmorPiece(new ArmorPiece("helmet",
                repository.getHealth("CrownDefault"),
                repository.isMetallic("CrownDefault")));
        knight.addArmorPiece(new ArmorPiece("shoulder armor",
                repository.getHealth("ShoulderArmorDefault"),
                repository.isMetallic("ShoulderArmorDefault")));
        knight.currentPosition.x = currentPosition.x;
        knight.currentPosition.y = lane;
        knight.positionX = currentPosition.x;
        knight.positionY = lane;
        knight.lane = lane;
        return knight;
    }
}
