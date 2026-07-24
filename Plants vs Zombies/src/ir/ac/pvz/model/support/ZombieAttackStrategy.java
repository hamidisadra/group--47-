package ir.ac.pvz.model.support;

import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.core.Zombie;

public interface ZombieAttackStrategy {
    void attack(Zombie zombie, Plant plant);
}
