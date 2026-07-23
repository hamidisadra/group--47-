package com.pvz.model.zombies;

import com.pvz.model.core.GameObject;
import com.pvz.model.core.Zombie;
import com.pvz.model.support.FrozenBlock;

public class Troglobite extends Zombie {
    public Troglobite() {
        super("Troglobite");
    }
    public void pushIce(FrozenBlock block, GameObject target) {
        if (block == null || target == null || !target.isAlive) {
            return;
        }
        if (target instanceof Zombie) {
            ((Zombie) target).forceDie();
        }
        else {
            target.die();
        }
    }

}
