package com.pvz.model.support;

import com.pvz.game.GameSession;
import com.pvz.model.core.Plant;
import com.pvz.model.core.Zombie;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class TransformPlantAbility extends ZombieAbility {
    private final Random random;
    private CatCurse curse;
    public TransformPlantAbility(float cooldownSeconds) {
        this(cooldownSeconds, new Random());
    }
    public TransformPlantAbility(float cooldownSeconds, Random random) {
        super("transform-plant", "Transforms plants into cats.",
                cooldownSeconds);
        if (random == null) {
            this.random = new Random();
        }
        else {
            this.random = random;
        }
    }
    @Override
    public void onTick(Zombie zombie, GameSession session, float elapsed) {
        super.onTick(zombie, session, elapsed);
        if (!isReady() || session == null) {
            return;
        }
        List<Plant> targets = new ArrayList<>();
        for (Plant plant : session.getBoard().getAllPlants()) {
            if (plant.isAlive && !plant.isCatTransformed) {
                targets.add(plant);
            }
        }
        if (!targets.isEmpty()) {
            getCurse(zombie).transform(targets.get(random.nextInt(targets.size())));
            resetCooldown();
        }
    }
    @Override
    public boolean onPlantContact(Zombie zombie, Plant plant,
                                  GameSession session) {
        if (plant == null) {
            return false;
        }
        getCurse(zombie).transform(plant);
        return true;
    }
    @Override
    public void onDeath(Zombie zombie, GameSession session) {
        if (curse != null) {
            curse.restoreWhenCasterDies();
        }
    }
    private CatCurse getCurse(Zombie zombie) {
        if (curse == null) {
            curse = new CatCurse(zombie);
        }
        return curse;
    }
}
