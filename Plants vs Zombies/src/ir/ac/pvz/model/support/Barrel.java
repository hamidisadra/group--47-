package ir.ac.pvz.model.support;

import ir.ac.pvz.model.zombies.ImpZombie;

import java.util.ArrayList;
import java.util.List;

public class Barrel {

    public int health;
    public ContinuousPosition position;
    public boolean blocksProjectiles;
    public boolean blocksZombies;

    public Barrel(int health, ContinuousPosition position) {
        this.health = health;
        this.position = position;
        this.blocksProjectiles = true;
        this.blocksZombies = false;
    }

    public List<ImpZombie> breakAndSpawnImps() {
        health = 0;
        List<ImpZombie> imps = new ArrayList<>();
        ImpZombie first = createConfiguredImp();
        ImpZombie second = createConfiguredImp();
        imps.add(first);
        imps.add(second);
        for (ImpZombie imp : imps) {
            imp.currentPosition = new ContinuousPosition(position.x, position.y);
            imp.positionX = position.x;
            imp.positionY = position.y;
            imp.lane = position.y;
        }
        return imps;
    }

    private ImpZombie createConfiguredImp() {
        ImpZombie imp = new ImpZombie();
        ZombieDataRepository.getInstance().applyTo(imp, "ImpZombie");
        imp.setIdentity("ImpZombie", "ImpZombie");
        imp.canSpawnPlantFood = true;
        return imp;
    }
}
