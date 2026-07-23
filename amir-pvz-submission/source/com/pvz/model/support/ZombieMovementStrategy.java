package com.pvz.model.support;

import com.pvz.model.core.Zombie;

public interface ZombieMovementStrategy {

    void move(Zombie zombie, float distance);
}
