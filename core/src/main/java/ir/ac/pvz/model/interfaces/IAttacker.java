package ir.ac.pvz.model.interfaces;

import ir.ac.pvz.model.core.GameObject;

public interface IAttacker {
    void attack(GameObject target);
    int getDamage();
    int getRange();
}
