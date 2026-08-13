package ir.ac.pvz.controller.game_core;

import ir.ac.pvz.model.others.*;

import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.support.ProjectileResolver;

@FunctionalInterface
public interface PlantFoodStrategy {
    void apply(Plant plant, GameSession session, ProjectileResolver resolver);
}
