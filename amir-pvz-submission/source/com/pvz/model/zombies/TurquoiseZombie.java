package com.pvz.model.zombies;

import com.pvz.game.GameSession;
import com.pvz.model.core.Zombie;
import com.pvz.model.support.GridPosition;

public class TurquoiseZombie extends Zombie {
    private int stolenSuns;
    private float stealingSeconds;
    private final float chargingTimeSeconds;
    private final float laserCooldownSeconds;
    public TurquoiseZombie() {
        super("TurquoiseZombie");
        this.stolenSuns = 0;
        this.stealingSeconds = 0f;
        this.chargingTimeSeconds = (float) com.pvz.model.support
                .ZombieDataRepository.getInstance().getNumber(
                        "TurquoiseZombie", "ChargingTime", 5d);
        this.laserCooldownSeconds = (float) com.pvz.model.support
                .ZombieDataRepository.getInstance().getNumber(
                        "TurquoiseZombie", "LaserCooldownTime", 5d);
    }
    public void stealSunForOneSecond(GameSession session) {
        if (session == null || stealingSeconds >= chargingTimeSeconds) {
            return;
        }
        int stolen = session.stealSuns(25);
        stolenSuns += stolen;
        stealingSeconds += 1f;
        if (stealingSeconds >= chargingTimeSeconds) {
            fireLaser(session);
        }
    }
    public boolean isStealingSun() {
        return stealingSeconds > 0f && stealingSeconds < chargingTimeSeconds;
    }
    public boolean hasFinishedStealing() {
        return stealingSeconds >= chargingTimeSeconds;
    }
    public float getLaserCooldownSeconds() {
        return laserCooldownSeconds;
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
