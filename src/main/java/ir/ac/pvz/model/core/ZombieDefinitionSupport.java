package ir.ac.pvz.model.core;

import ir.ac.pvz.model.support.ZombieAbilityRegistry;
import ir.ac.pvz.model.support.ZombieDataRepository;
import ir.ac.pvz.model.support.ZombieDefinition;

final class ZombieDefinitionSupport {
    private ZombieDefinitionSupport() {
    }

    static void initialize(Zombie zombie, String zombieType) {
        ZombieDefinition definition = ZombieDataRepository.getInstance()
                .getByZombieType(zombieType);

        if (definition == null) {
            throw new IllegalArgumentException(
                    "Unknown zombie type: " + zombieType);
        }

        zombie.selectionWeight = definition.weight;
        zombie.canSpawnPlantFood = definition.canSpawnPlantFood;
        zombie.type = definition.runtimeType;
        zombie.displayName = definition.gameType;

        for (String abilityName : definition.abilities) {
            zombie.abilities.add(
                    ZombieAbilityRegistry.create(abilityName, definition));
        }
    }

    static double requiredNumber(String type, String key) {
        ZombieDefinition definition =
                ZombieDataRepository.getInstance().getByZombieType(type);

        if (definition == null
                || !definition.numericProperties.containsKey(key)) {
            throw new IllegalStateException(
                    "Missing zombie value " + key + " for " + type);
        }

        return definition.numericProperties.get(key);
    }
}
