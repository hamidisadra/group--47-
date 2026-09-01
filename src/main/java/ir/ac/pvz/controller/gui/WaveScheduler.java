package ir.ac.pvz.controller.gui;

import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.others.GameSession;
import ir.ac.pvz.model.others.Wave;
import ir.ac.pvz.model.support.Board;
import ir.ac.pvz.model.support.ContinuousPosition;

import java.util.ArrayList;
import java.util.List;

final class WaveScheduler {
    static final float FIRST_WAVE_DELAY = 22f;
    static final float WAVE_INTERVAL = 40f;
    static final float WAVE_RETRY = 8f;
    static final float WAVE_SPREAD = 62f;
    static final float MIN_SPAWN_GAP = 1.2f;
    static final float MAX_SPAWN_GAP = 6f;
    static final float LAWN_CLEAR_SPEED_UP = 5f;

    private final Board board;
    private final GameSession session;
    private final List<Zombie> incoming = new ArrayList<>();

    private float spawnTimer = FIRST_WAVE_DELAY;
    private float waveTimer = FIRST_WAVE_DELAY + WAVE_INTERVAL;
    private float currentGap = MAX_SPAWN_GAP;
    private int harvestedWave;

    WaveScheduler(Board board, GameSession session) {
        this.board = board;
        this.session = session;
    }

    int getPendingZombieCount() {
        return incoming.size();
    }

    boolean hasPending() {
        return !incoming.isEmpty();
    }

    void resetWaveTimer() {
        waveTimer = WAVE_INTERVAL;
    }

    void harvestNewWave() {
        if (session.currentWaveNumber == harvestedWave) {
            return;
        }

        harvestedWave = session.currentWaveNumber;
        Wave wave = session.getWaveController().getCurrentWave();

        if (wave == null) {
            return;
        }

        int taken = takeZombiesFrom(wave);

        if (taken > 0) {
            currentGap = Math.max(MIN_SPAWN_GAP,
                Math.min(MAX_SPAWN_GAP, WAVE_SPREAD / taken));
        }

        waveTimer = WAVE_INTERVAL;
    }

    private int takeZombiesFrom(Wave wave) {
        int taken = 0;

        for (Zombie zombie : wave.zombies) {
            if (zombie == null || zombie.isDead() || incoming.contains(zombie)) {
                continue;
            }

            board.removeZombieEverywhere(zombie);
            incoming.add(zombie);
            taken++;
        }

        return taken;
    }

    void releaseIncoming(float delta, float entryColumnForLane) {
        if (incoming.isEmpty()) {
            return;
        }

        spawnTimer -= delta;

        if (spawnTimer > 0f) {
            return;
        }

        spawnTimer = currentGap;
        Zombie zombie = incoming.remove(0);

        if (zombie.isDead()) {
            return;
        }

        int lane = Math.max(0, Math.min(board.rows - 1, zombie.lane));
        board.placeZombie(zombie,
            new ContinuousPosition(entryColumnForLane, lane));
    }

    int nextLane() {
        if (incoming.isEmpty()) {
            return 0;
        }

        return Math.max(0, Math.min(board.rows - 1, incoming.get(0).lane));
    }

    void pushWaveOnTimer(float delta) {
        if (!incoming.isEmpty()) {
            waveTimer = WAVE_INTERVAL;
            return;
        }

        boolean lawnClear = board.getAllAliveZombies().isEmpty();
        waveTimer -= lawnClear ? delta * LAWN_CLEAR_SPEED_UP : delta;

        if (waveTimer > 0f) {
            return;
        }

        int before = session.currentWaveNumber;
        session.getWaveController().startNextWaveIfReady();
        harvestNewWave();

        waveTimer = session.currentWaveNumber == before
            ? WAVE_RETRY : WAVE_INTERVAL;
    }
}
