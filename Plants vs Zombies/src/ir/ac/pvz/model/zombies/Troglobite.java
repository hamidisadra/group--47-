package ir.ac.pvz.model.zombies;

import ir.ac.pvz.model.support.FrozenBlock;
import ir.ac.pvz.model.core.GameObject;
import ir.ac.pvz.model.core.Zombie;

public class Troglobite extends Zombie {

    public Troglobite() {
        super(0.185f, 470, 100, 600);
    }

    public void pushIce(FrozenBlock block, GameObject target) {
        if (block == null || target == null || !target.isAlive) {
            return;
        }
        if (target instanceof Zombie) {
            ((Zombie) target).forceDie();
        } else {
            target.die();
        }
    }

    @Override
    public void freeze(int duration) {
    }
}
