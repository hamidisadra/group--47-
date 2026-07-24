package com.pvz.model.support;

import com.pvz.model.core.Plant;
import com.pvz.model.core.Zombie;

public interface ZombieAttackStrategy {
    void attack(Zombie zombie, Plant plant);
}
