package ir.ac.pvz.model.support;

import ir.ac.pvz.model.core.Zombie;

import java.util.List;

public interface ZombieDefinitionRepository {

    ZombieDefinition getByZombieType(String zombieType);

    ZombieDefinition getByAlias(String alias);

    List<ZombieDefinition> getAll();

    void applyTo(Zombie zombie, String zombieType);
}
