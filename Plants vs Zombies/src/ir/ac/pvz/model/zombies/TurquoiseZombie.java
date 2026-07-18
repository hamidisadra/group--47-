package ir.ac.pvz.model.zombies;

import ir.ac.pvz.model.others.GameSession;
import ir.ac.pvz.model.support.GridPosition;
import ir.ac.pvz.model.core.Zombie;

public class TurquoiseZombie extends Zombie {

    private int stolenSuns;
    private float stealingSeconds;

    public TurquoiseZombie() {
        super(0.185f, 250, 100, 500);
        this.stolenSuns = 0;
        this.stealingSeconds = 0f;
    }

    public void stealSunForOneSecond(GameSession session) {
        if (session == null || stealingSeconds >= 5f) {
            return;
        }
        int stolen = session.stealSuns(25);
        stolenSuns += stolen;
        stealingSeconds += 1f;
        if (stealingSeconds >= 5f) {
            fireLaser(session);
        }
    }

    public boolean isStealingSun() {
        return stealingSeconds > 0f && stealingSeconds < 5f;
    }

    public boolean hasFinishedStealing() {
        return stealingSeconds >= 5f;
    }

    public void fireLaser(GameSession session) {
        for (int offset = 1; offset <= 4; offset++) {
            int x = (int) currentPosition.x - offset;
            GridPosition position = new GridPosition(x, lane);
            if (session.getBoard().isInside(position)) {
                session.getBoard().getTile(position).getPlants()
                        .forEach(plant -> plant.receiveInstantKill());
            }
        }
    }

    @Override
    public void onDeath() {
        super.onDeath();
    }

    public int releaseStolenSuns() {
        int released = stolenSuns / 2;
        stolenSuns = 0;
        return released;
    }
}
