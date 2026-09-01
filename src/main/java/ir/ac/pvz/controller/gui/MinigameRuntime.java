package ir.ac.pvz.controller.gui;

import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.minigame.BowlingNut;
import ir.ac.pvz.model.minigame.BowlingNutType;
import ir.ac.pvz.model.minigame.IZombie;
import ir.ac.pvz.model.others.GameSession;
import ir.ac.pvz.model.support.Board;
import ir.ac.pvz.model.support.ContinuousPosition;
import ir.ac.pvz.model.support.StationaryMovementStrategy;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

final class MinigameRuntime {
    private static final float NUT_HIT_RANGE = 0.6f;
    private static final int BOWLING_NUT_DAMAGE = 190;
    private static final float BRAIN_COLUMN = 0.4f;

    private final Board board;
    private final GameSession session;
    private final MinigameController minigameController;
    private final Map<Zombie, Integer> sunProducers = new LinkedHashMap<>();

    private float sunProducerTimer;

    MinigameRuntime(Board board, GameSession session,
                    MinigameController minigameController) {
        this.board = board;
        this.session = session;
        this.minigameController = minigameController;
    }

    private IZombie iZombieGame() {
        if (minigameController == null
            || !(minigameController.getGame() instanceof IZombie)) {
            return null;
        }

        return (IZombie) minigameController.getGame();
    }

    void resolveNutCollisions() {
        if (minigameController == null || !minigameController.isBowling()) {
            return;
        }

        for (BowlingNut nut : minigameController.getNuts()) {
            if (!nut.isAlive() || !isOnBoard(nut)) {
                continue;
            }

            crushFirstZombie(nut);
        }
    }

    private boolean isOnBoard(BowlingNut nut) {
        return nut.getRow() >= 0 && nut.getRow() < board.rows
            && nut.getCol() < board.columns;
    }

    private void crushFirstZombie(BowlingNut nut) {
        for (Zombie zombie : board.getZombiesInLane(nut.getRow())) {
            if (zombie.isDead()) {
                continue;
            }

            if (Math.abs(zombie.currentPosition.x - nut.getCol()) < NUT_HIT_RANGE) {
                applyNutDamage(nut, zombie);
                nut.onHitZombie(zombie.getType());

                if (nut.hasExploded()) {
                    explodeAround(nut, zombie);
                }

                return;
            }
        }
    }

    private void applyNutDamage(BowlingNut nut, Zombie zombie) {
        if (nut.getType() == BowlingNutType.GIANT) {
            zombie.forceDie();
            return;
        }

        zombie.takeDamage(BOWLING_NUT_DAMAGE);
    }

    private void explodeAround(BowlingNut nut, Zombie centre) {
        for (int lane = nut.getRow() - 1; lane <= nut.getRow() + 1; lane++) {
            if (lane < 0 || lane >= board.rows) {
                continue;
            }

            for (Zombie other : board.getZombiesInLane(lane)) {
                if (other == centre || other.isDead()) {
                    continue;
                }

                if (Math.abs(other.currentPosition.x - nut.getCol()) <= 1f) {
                    other.takeDamage(BOWLING_NUT_DAMAGE);
                }
            }
        }
    }

    void placeSunProducerZombies(int column) {
        IZombie game = iZombieGame();

        if (game == null) {
            return;
        }

        for (int lane = 0; lane < board.rows; lane++) {
            Zombie producer = session.spawnConfiguredZombie("BucketheadZombie",
                new ContinuousPosition(column, lane));

            if (producer == null) {
                continue;
            }

            producer.speed = 0f;
            producer.movementStrategy = new StationaryMovementStrategy();
            sunProducers.put(producer, lane);
        }
    }

    void updateSunProducers(float delta) {
        IZombie game = iZombieGame();

        if (game == null) {
            return;
        }

        for (Map.Entry<Zombie, Integer> entry
            : new ArrayList<>(sunProducers.entrySet())) {
            if (entry.getKey().isDead()) {
                game.onSunProducerKilled(entry.getValue() + 1);
                sunProducers.remove(entry.getKey());
            }
        }

        sunProducerTimer += delta;

        while (sunProducerTimer >= 1f) {
            sunProducerTimer -= 1f;
            game.collectSunFromProducers();
        }
    }

    void resolveBrains() {
        IZombie game = iZombieGame();

        if (game == null) {
            return;
        }

        for (Zombie zombie : board.getAllAliveZombies()) {
            if (zombie.currentPosition.x <= BRAIN_COLUMN) {
                game.eatBrain(zombie.lane + 1);
            }
        }

        if (game.checkWinCondition()) {
            session.win();
            return;
        }

        if (game.checkLoseCondition(!board.getAllAliveZombies().isEmpty())) {
            session.lose();
        }
    }
}
