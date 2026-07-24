package ir.ac.pvz.model.zombies;

import ir.ac.pvz.model.core.GameObject;
import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.support.FrozenBlock;

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
