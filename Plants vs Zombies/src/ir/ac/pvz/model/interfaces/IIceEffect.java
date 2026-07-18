package ir.ac.pvz.model.interfaces;

import ir.ac.pvz.model.core.Zombie;

public interface IIceEffect {
    void chill(Zombie target);
    float getSlowFactor();
}
