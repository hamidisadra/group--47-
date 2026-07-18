package ir.ac.pvz.model.interfaces;

import ir.ac.pvz.model.core.Zombie;

public interface IWall {
    void block(Zombie zombie);
    int getHealth();
}
