package ir.ac.pvz.model.zombies;

import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.support.ZombieDataRepository;

public class FootballZombie extends Zombie {
    private final float runningSpeedScale;
    private boolean running;
    public FootballZombie() {
        super("FootballZombie");
        runningSpeedScale = loadRunningSpeedScale();
        speed /= runningSpeedScale;
        running = true;
    }

    @Override
    public void applyBaseData(float movementSpeed, int baseHealth,
                              int eatDamagePerSecond, int cost,
                              int weight, boolean plantFoodEligible) {
        super.applyBaseData(movementSpeed, baseHealth, eatDamagePerSecond,
                cost, weight, plantFoodEligible);
        if (running && runningSpeedScale > 0f) {
            speed /= runningSpeedScale;
        }
    }

    @Override
    public void onReachPlant(Plant plant) {
        if (running && plant != null) {
            plant.receiveInstantKill();
            running = false;
            speed *= runningSpeedScale;
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

    private static float loadRunningSpeedScale() {
        float scale = (float) ZombieDataRepository.getInstance().getNumber(
                "FootballZombie", "RunningSpeedScale", 0d);
        if (scale <= 0f) {
            throw new IllegalStateException(
                    "RunningSpeedScale must be positive for FootballZombie");
        }
        return scale;
    }

    public boolean isRunning() {
        return running;
    }
}
