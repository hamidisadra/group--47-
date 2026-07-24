package ir.ac.pvz.model.support;

import ir.ac.pvz.model.core.Zombie;

public interface ZombieMovementStrategy {

    void move(Zombie zombie, float distance);
}
