package ir.ac.pvz.view.render;

import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.support.ArmorPiece;
import ir.ac.pvz.model.support.Board;
import ir.ac.pvz.view.assets.GameAssets;

import com.badlogic.gdx.graphics.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

final class ZombieTracker {
    private static final float DEBRIS_GRAVITY = 900f;
    private static final float ZOMBIE_SCALE = 0.43f;
    private static final float ZOMBIE_GROUND = 0.24f;

    private final GameAssets assets;
    private final DebrisLayer debrisLayer;
    private final List<Zombie> seenZombies = new ArrayList<>();
    private final Map<Zombie, Integer> lastHealth = new WeakHashMap<>();
    private final Map<Zombie, Float> zombieHitTimers = new WeakHashMap<>();
    private final Map<Zombie, Integer> lastArmorCount = new WeakHashMap<>();
    private final java.util.Set<Zombie> impThrowSeen =
        java.util.Collections.newSetFromMap(new WeakHashMap<>());

    ZombieTracker(GameAssets assets, DebrisLayer debrisLayer) {
        this.assets = assets;
        this.debrisLayer = debrisLayer;
    }

    Map<Zombie, Float> getHitTimers() {
        return zombieHitTimers;
    }

    void trackZombies(Board board) {
        List<Zombie> alive = board.getAllAliveZombies();

        for (Zombie zombie : alive) {
            Integer previous = lastHealth.get(zombie);
            if (previous != null && zombie.currentHealth < previous) {
                zombieHitTimers.put(zombie, 0.14f);
            }

            lastHealth.put(zombie, zombie.currentHealth);
            trackArmorLoss(zombie, board);
            trackImpThrow(zombie, board);
        }

        for (Zombie zombie : seenZombies) {
            if (!alive.contains(zombie)) {
                addDeath(zombie, board);
            }
        }

        seenZombies.clear();
        seenZombies.addAll(alive);
    }

    void advanceDebris(float delta) {
        for (int index = debrisLayer.getDebris().size() - 1; index >= 0; index--) {
            DebrisLayer.Debris piece = debrisLayer.getDebris().get(index);
            piece.remaining -= delta;

            if (piece.remaining <= 0f) {
                debrisLayer.getDebris().remove(index);
                continue;
            }

            piece.velocityY -= DEBRIS_GRAVITY * delta;
            piece.x += piece.velocityX * delta;
            piece.y += piece.velocityY * delta;
        }
    }

    void advanceDeaths(float delta) {
        for (int index = debrisLayer.getDeaths().size() - 1; index >= 0; index--) {
            DebrisLayer.DeathAnimation death = debrisLayer.getDeaths().get(index);
            death.remaining -= delta;
            if (death.remaining <= 0f) {
                debrisLayer.getDeaths().remove(index);
            }
        }
    }

    void trackArmorLoss(Zombie zombie, Board board) {
        int current = countArmor(zombie);
        Integer previous = lastArmorCount.get(zombie);
        lastArmorCount.put(zombie, current);

        if (previous == null || current >= previous) {
            return;
        }

        float x = Lawn.columnCenterX(zombie.currentPosition.x);
        float y = Lawn.rowBottomY(zombie.lane, board.rows)
            + Lawn.CELL_HEIGHT * 0.75f;
        String path = assets.zombiePath(zombie.getType(), board.seasonType);

        for (int piece = previous; piece > current; piece--) {
            debrisLayer.spawnArmorPart(zombie, path, x, y);
        }
    }

    void trackImpThrow(Zombie zombie, Board board) {
        if (!(zombie instanceof ir.ac.pvz.model.zombies.Gargantuar)) {
            return;
        }

        ir.ac.pvz.model.zombies.Gargantuar gargantuar =
            (ir.ac.pvz.model.zombies.Gargantuar) zombie;

        if (!gargantuar.hasThrownImp() || impThrowSeen.contains(zombie)) {
            return;
        }

        impThrowSeen.add(zombie);
        float startX = Lawn.columnCenterX(zombie.currentPosition.x);
        float startY = Lawn.rowBottomY(zombie.lane, board.rows)
            + Lawn.CELL_HEIGHT * 0.9f;

        for (int trail = 0; trail < 6; trail++) {
            DebrisLayer.Debris piece = new DebrisLayer.Debris();
            piece.x = startX;
            piece.y = startY;
            piece.velocityX = -260f - trail * 18f;
            piece.velocityY = 330f - trail * 20f;
            piece.total = 1.1f;
            piece.remaining = piece.total;
            piece.size = 16f;
            piece.color = new Color(0.55f, 0.35f, 0.65f, 1f);
            debrisLayer.getDebris().add(piece);
        }
    }

    int countArmor(Zombie zombie) {
        if (zombie.armorPieces == null) {
            return 0;
        }

        int count = 0;

        for (ArmorPiece piece : zombie.armorPieces) {
            if (piece != null && piece.health > 0) {
                count++;
            }
        }

        return count;
    }

    void addDeath(Zombie zombie, Board board) {
        String path = assets.zombiePath(zombie.getType(), board.seasonType);
        if (path == null || !assets.hasClip(path, "die")) {
            return;
        }

        DebrisLayer.DeathAnimation death = new DebrisLayer.DeathAnimation();
        death.path = path;
        death.clip = "die";
        death.scale = ZOMBIE_SCALE;
        death.x = Lawn.columnCenterX(zombie.currentPosition.x);
        death.y = Lawn.rowBottomY(zombie.lane, board.rows)
            + Lawn.CELL_HEIGHT * ZOMBIE_GROUND;
        death.total = 1.2f;
        death.remaining = death.total;
        debrisLayer.getDeaths().add(death);
        debrisLayer.spawnDeathParts(zombie, board, death.x, death.y);
    }
}
