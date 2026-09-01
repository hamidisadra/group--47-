package ir.ac.pvz.model.others;

import ir.ac.pvz.controller.game_core.*;

import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.enums.PlantCategory;
import ir.ac.pvz.model.enums.ProjectileType;
import ir.ac.pvz.model.plants.ShooterPlant;
import ir.ac.pvz.model.plants.SunProducerPlant;
import ir.ac.pvz.model.zombies.Gargantuar;
import ir.ac.pvz.model.support.ContinuousPosition;
import ir.ac.pvz.model.support.BalanceDefaults;
import ir.ac.pvz.model.support.Board;
import ir.ac.pvz.model.support.ProjectileResolver;
import java.util.List;

public class PlantFoodInventory {
    public interface Listener {
        void onPlantFoodEarned(int total);
    }

    private Listener listener;
    public int count;
    public int maxCapacity;
    private final PlantFoodStrategyRegistry strategyRegistry;
    public PlantFoodInventory(int maxCapacity) {
        if (maxCapacity < 1) {
            throw new IllegalArgumentException("Plant food capacity must be positive.");
        }

        this.count = 0;
        this.maxCapacity = maxCapacity;
        this.strategyRegistry = createStrategyRegistry();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public boolean addFromGlowingZombie(Zombie zombie) {
        if (zombie == null || !zombie.isGlowing
            || !zombie.canSpawnPlantFood || count >= maxCapacity) {
            return false;
        }

        count++;
        String unit = count == 1 ? "plant food" : "plant foods";
        System.out.println("The glowing zombie dropeed a plant food; you have "
            + count + " " + unit + " now.");

        if (listener != null) {
            listener.onPlantFoodEarned(count);
        }

        return true;
    }

    public boolean feedPlant(Plant plant) {
        if (plant == null || count <= 0) {
            return false;
        }

        PlantFoodStrategy strategy = strategyRegistry.resolve(plant.plantFoodType);
        if (strategy == null || "NONE".equalsIgnoreCase(plant.plantFoodType)) {
            return false;
        }

        count--;
        plant.applyPlantFoodEffect();
        return true;
    }

    public boolean feedPlant(Plant plant, GameSession session,
                             ProjectileResolver resolver) {
        if (plant == null || count <= 0 || session == null || resolver == null) {
            return false;
        }

        PlantFoodStrategy strategy = strategyRegistry.resolve(plant.plantFoodType);
        if (strategy == null) {
            return false;
        }

        count--;
        applyPlantFood(plant, session, resolver, strategy);
        return true;
    }

    public boolean boostPlant(Plant plant, GameSession session,
                              ProjectileResolver resolver) {
        if (plant == null || session == null || resolver == null) {
            return false;
        }

        PlantFoodStrategy strategy = strategyRegistry.resolve(plant.plantFoodType);
        if (strategy == null) {
            return false;
        }

        applyPlantFood(plant, session, resolver, strategy);
        return true;
    }

    public boolean cheatAddPlantFood() {
        if (count >= maxCapacity) {
            return false;
        }

        count++;
        return true;
    }

    public int getCount() {
        return count;
    }

    public boolean supportsPlantFoodType(String effectType) {
        return strategyRegistry.contains(effectType);
    }

    private void applyPlantFood(Plant plant, GameSession session,
                                ProjectileResolver resolver) {
        PlantFoodStrategy strategy = strategyRegistry.resolve(plant.plantFoodType);
        if (strategy == null) {
            throw new IllegalStateException("Unknown Plant Food strategy: "
                + plant.plantFoodType + " for " + plant.type + ".");
        }

        applyPlantFood(plant, session, resolver, strategy);
    }

    private void applyPlantFood(Plant plant, GameSession session,
                                ProjectileResolver resolver,
                                PlantFoodStrategy strategy) {
        plant.applyPlantFoodEffect();
        strategy.apply(plant, session, resolver);
    }

    private PlantFoodStrategyRegistry createStrategyRegistry() {
        PlantFoodStrategyRegistry registry = new PlantFoodStrategyRegistry();
        registry.register("NONE", (plant, session, resolver) -> { });
        registry.register("SPAWN_SUN_ITEMS", this::applySunPlantFood);
        registry.register("PROJECTILE_BURST", this::applyProjectileBurst);
        registry.register("RANDOM_HYPNOTIZE", this::applyHomingByData);
        registry.register("RANDOM_INSTANT_KILL", this::applyHomingByData);
        registry.register("METAL_DISARM", this::applyHomingByData);
        registry.register("BUTTER_BARRAGE", this::applyLobberByData);
        registry.register("KNOCKBACK_BLAST", this::applyStrikeByData);
        registry.register("SPAWN_CLONES", this::applyCloneByData);
        registry.register("LOCAL_AOE_ATTACK", this::applyLocalAttackByData);
        registry.register("PULL_UNDERWATER", this::applyExplosiveByData);
        registry.register("MAP_WIDE_FREEZE", this::applyExplosiveByData);
        registry.register("REMOTE_SWALLOW", this::applyMeleeByData);
        registry.register("GRANT_PERMANENT_ARMOR", this::applyWallByData);
        registry.register("FORCE_LANE_CHANGE", this::applyWallByData);
        registry.register("PULL_AND_FULL_HEAL", this::applyWallByData);
        return registry;
    }

    private void applySunPlantFood(Plant plant, GameSession session,
                                   ProjectileResolver resolver) {
        if (plant instanceof SunProducerPlant) {
            ((SunProducerPlant) plant).consumeQueuedPlantFoodSun();
        }

        int amount = Math.max(0, (int) Math.round(plant.plantFoodValue));
        session.getSunManager().produceBonusPlantSun(plant, amount);
    }

    private void applyProjectileBurst(Plant plant, GameSession session,
                                      ProjectileResolver resolver) {
        String type = plant.getNormalizedType();
        if (plant.category == PlantCategory.SHOOTER) {
            PlantFoodCategoryEffects.applyShooterPlantFood(plant, type, session, resolver);
        }

        else if (plant.category == PlantCategory.LOBBER) {
            PlantFoodCategoryEffects.applyLobberPlantFood(plant, type, session, resolver);
        }

        else if (plant.category == PlantCategory.STRIKE_THROUGH) {
            PlantFoodCategoryEffects.applyStrikePlantFood(plant, type, session, resolver);
        }

        else if (plant.category == PlantCategory.HOMING) {
            PlantFoodCategoryEffects.applyHomingPlantFood(plant, type, session, resolver);
        }

        else if (plant.category == PlantCategory.MODIFIER) {
            PlantFoodCategoryEffects.applyModifierPlantFood(plant, type, session, resolver);
        }
    }

    private void applyHomingByData(Plant plant, GameSession session,
                                   ProjectileResolver resolver) {
        PlantFoodCategoryEffects.applyHomingPlantFood(plant, plant.getNormalizedType(), session, resolver);
    }

    private void applyLobberByData(Plant plant, GameSession session,
                                   ProjectileResolver resolver) {
        PlantFoodCategoryEffects.applyLobberPlantFood(plant, plant.getNormalizedType(), session, resolver);
    }

    private void applyStrikeByData(Plant plant, GameSession session,
                                   ProjectileResolver resolver) {
        PlantFoodCategoryEffects.applyStrikePlantFood(plant, plant.getNormalizedType(), session, resolver);
    }

    private void applyCloneByData(Plant plant, GameSession session,
                                  ProjectileResolver resolver) {
        if (plant.category == PlantCategory.MODIFIER) {
            PlantFoodCategoryEffects.applyModifierPlantFood(plant, plant.getNormalizedType(), session, resolver);
        } else {
            PlantFoodCategoryEffects.applyExplosivePlantFood(plant, plant.getNormalizedType(), session, resolver);
        }
    }

    private void applyLocalAttackByData(Plant plant, GameSession session,
                                        ProjectileResolver resolver) {
        if (plant.category == PlantCategory.EXPLOSIVE) {
            PlantFoodCategoryEffects.applyExplosivePlantFood(plant, plant.getNormalizedType(), session, resolver);
        } else {
            PlantFoodCategoryEffects.applyMeleePlantFood(plant, plant.getNormalizedType(), session, resolver);
        }
    }

    private void applyExplosiveByData(Plant plant, GameSession session,
                                      ProjectileResolver resolver) {
        PlantFoodCategoryEffects.applyExplosivePlantFood(plant, plant.getNormalizedType(), session, resolver);
    }

    private void applyMeleeByData(Plant plant, GameSession session,
                                  ProjectileResolver resolver) {
        PlantFoodCategoryEffects.applyMeleePlantFood(plant, plant.getNormalizedType(), session, resolver);
    }

    private void applyWallByData(Plant plant, GameSession session,
                                 ProjectileResolver resolver) {
        PlantFoodCategoryEffects.applyWallPlantFood(plant, plant.getNormalizedType(), session, resolver);
    }

    public void applyMint(Plant mint, GameSession session,
                          ProjectileResolver resolver) {
        PlantFoodBoardSupport.applyMint(
            mint, session, resolver, this::applyPlantFood);
    }

}
