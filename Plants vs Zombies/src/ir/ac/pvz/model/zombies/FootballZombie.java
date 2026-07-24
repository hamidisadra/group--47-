package ir.ac.pvz.model.zombies;

import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.core.Zombie;

public class FootballZombie extends Zombie {
    private boolean running;
    public FootballZombie() {
        super("FootballZombie");
        this.running = true;
    }
    @Override
    public void onReachPlant(Plant plant) {
        if (running && plant != null) {
            plant.receiveInstantKill();
            running = false;
            speed *= 0.5f;
            return;
        }
        super.onReachPlant(plant);
    }
    public boolean collideWithHypnotizedZombie(Zombie zombie) {
        if (!running || zombie == null || !zombie.isHypnotized) {
            return false;
        }
        zombie.forceDie();
        return true;
    }
    public boolean isRunning() {
        return running;
    }
}
