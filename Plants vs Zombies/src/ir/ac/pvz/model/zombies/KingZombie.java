package ir.ac.pvz.model.zombies;


import ir.ac.pvz.model.core.Zombie;

public class KingZombie extends Zombie {

    public KingZombie() {
        super(0f, 1000, 100, 750);
    }

    public KnightZombie promoteNearbyBasicZombie() {
        KnightZombie knight = new KnightZombie();
        knight.currentPosition.x = currentPosition.x;
        knight.currentPosition.y = lane;
        knight.positionX = currentPosition.x;
        knight.positionY = lane;
        knight.lane = lane;
        return knight;
    }
}
