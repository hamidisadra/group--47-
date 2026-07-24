package ir.ac.pvz.model.support;

import ir.ac.pvz.model.others.GameSession;
import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.enums.PlantTag;
import ir.ac.pvz.model.enums.ProjectileType;

public final class TorchAbility extends ZombieAbility {
    private boolean lit;
    public TorchAbility() {
        super("torch", "Instantly burns nearby plants while lit.", 0f);
        lit = true;
    }
    @Override
    public void onTick(Zombie zombie, GameSession session, float elapsed) {
        super.onTick(zombie, session, elapsed);
        if (!lit || zombie == null || session == null) {
            return;
        }
        Plant nearest = null;
        float nearestDistance = Float.MAX_VALUE;
        for (Plant candidate : session.getBoard().getPlantsInLane(zombie.lane)) {
            float distance = zombie.currentPosition.x - candidate.location.x;
            if (candidate.isAlive && distance >= 0f && distance < 1f
                    && distance < nearestDistance) {
                nearest = candidate;
                nearestDistance = distance;
            }
        }
        if (nearest != null) {
            onPlantContact(zombie, nearest, session);
        }
    }
    @Override
    public boolean onPlantContact(Zombie zombie, Plant plant,
                                  GameSession session) {
        if (plant == null) {
            return false;
        }
        if (plant.plantTags.contains(PlantTag.ICE)) {
            lit = false;
        }
        if (plant.plantTags.contains(PlantTag.FIRE)) {
            lit = true;
        }
        if (!lit) {
            return false;
        }
        plant.receiveInstantKill();
        return true;
    }
    @Override
    public void onProjectileReceived(Zombie zombie, Projectile projectile,
                                     GameSession session) {
        if (projectile.type == ProjectileType.ICE) {
            lit = false;
        }
        if (projectile.type == ProjectileType.FIRE) {
            lit = true;
        }
    }
}
