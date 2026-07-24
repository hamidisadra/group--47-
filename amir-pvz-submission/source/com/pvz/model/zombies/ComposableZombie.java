package com.pvz.model.zombies;

import com.pvz.model.core.Zombie;
import com.pvz.model.support.ArmorDataRepository;
import com.pvz.model.support.ArmorPiece;
import com.pvz.model.support.ZombieAbilityRegistry;
import com.pvz.model.support.ZombieDefinition;

public final class ComposableZombie extends Zombie {
    public ComposableZombie(String type, ZombieDefinition definition) {
        super(definition.speed, definition.health,
                definition.eatDamagePerSecond, definition.waveCost);
        setIdentity(type, type);
        selectionWeight = definition.weight;
        canSpawnPlantFood = definition.canSpawnPlantFood;
        for (String armorAlias : definition.getArmorAliases()) {
            addArmor(armorAlias);
        }
        for (String abilityName : definition.getAbilities()) {
            abilities.add(ZombieAbilityRegistry.create(abilityName, definition));
        }
    }
    private void addArmor(String alias) {
        ArmorDataRepository repository = ArmorDataRepository.getInstance();
        addArmorPiece(new ArmorPiece(alias, repository.getHealth(alias),
                repository.isMetallic(alias)));
    }
}
