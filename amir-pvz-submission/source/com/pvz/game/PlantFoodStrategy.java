package com.pvz.game;

import com.pvz.model.core.Plant;
import com.pvz.model.support.ProjectileResolver;

@FunctionalInterface
public interface PlantFoodStrategy {
    void apply(Plant plant, GameSession session, ProjectileResolver resolver);
}
