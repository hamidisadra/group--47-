package ir.ac.pvz.model.plants;

import ir.ac.pvz.model.core.GameObject;
import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.enums.ProjectileTrajectory;

public class Chomper extends MeleePlant {

    public Chomper(int id) {
        super(id, "Chomper", 150, 300, 5f, 40f, 0, false);
    }

    @Override
    public void attack(GameObject target) {
        if (!isReady() || target == null || !target.isAlive) {
            return;
        }
        if (target instanceof Zombie) {
            ((Zombie) target).receiveInstantKill(ProjectileTrajectory.STRAIGHT);
        } else {
            target.die();
        }
        ready = false;
    }
}
