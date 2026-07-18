package ir.ac.pvz.model.support;

import ir.ac.pvz.model.others.GameSession;
import ir.ac.pvz.model.core.Zombie;

import java.util.ArrayList;
import java.util.List;

public class LawnMower {

    public int relatedRow;
    public boolean activated;
    public List<Zombie> destroyedZombies;
    public boolean secondReachCausesLoss;

    public LawnMower(int relatedRow) {
        this.relatedRow = relatedRow;
        this.activated = false;
        this.destroyedZombies = new ArrayList<>();
        this.secondReachCausesLoss = true;
    }

    public void trigger(List<Zombie> zombies) {
        if (activated) {
            return;
        }
        activated = true;
        destroyedZombies.clear();
        System.out.println("The lawn mower in the row " + (relatedRow + 1)
                + "is triggered and killed these zombies:");
        for (Zombie zombie : new ArrayList<>(zombies)) {
            if (canKill(zombie)) {
                zombie.forceDie();
                destroyedZombies.add(zombie);
                System.out.println(zombie.getClass().getSimpleName());
            }
        }
    }

    public boolean canKill(Zombie zombie) {
        return zombie != null && zombie.isAlive && !zombie.isBoss;
    }

    public void handleZombieAtEnd(Zombie zombie, GameSession session) {
        if (!activated) {
            List<Zombie> zombies = new ArrayList<>(
                    session.getBoard().getZombiesInLane(relatedRow));
            if (zombie != null && !zombies.contains(zombie)) {
                zombies.add(zombie);
            }
            trigger(zombies);
            return;
        }
        if (secondReachCausesLoss) {
            System.out.println("The zombie ate your brain; LOSER!!!");
            session.lose();
        }
    }

    public int getRelatedRow() {
        return relatedRow;
    }

    public boolean isActivated() {
        return activated;
    }

    public List<Zombie> getDestroyedZombies() {
        return destroyedZombies;
    }
}
