package com.pvz.model.interfaces;

import com.pvz.model.core.Zombie;

public interface IIceEffect {
    void chill(Zombie target);
    float getSlowFactor();
}
