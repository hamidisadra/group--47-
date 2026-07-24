package com.pvz.model.interfaces;

import com.pvz.model.core.Zombie;

public interface IFireEffect {
    void burn(Zombie target);
    int getBurnDamage();
}
