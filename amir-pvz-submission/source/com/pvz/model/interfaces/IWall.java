package com.pvz.model.interfaces;

import com.pvz.model.core.Zombie;

public interface IWall {
    void block(Zombie zombie);
    int getHealth();
}
