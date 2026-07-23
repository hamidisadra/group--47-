package com.pvz.model.interfaces;

import com.pvz.model.core.GameObject;

public interface IAttacker {
    void attack(GameObject target);
    int getDamage();
    int getRange();
}
