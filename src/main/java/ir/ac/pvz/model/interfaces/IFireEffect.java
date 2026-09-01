package ir.ac.pvz.model.interfaces;

import ir.ac.pvz.model.core.Zombie;

public interface IFireEffect {
    void burn(Zombie target);
    int getBurnDamage();
}
