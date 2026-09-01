package ir.ac.pvz.controller.gui;

import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.enums.SeasonType;
import ir.ac.pvz.model.enums.TileType;
import ir.ac.pvz.model.others.GameSession;
import ir.ac.pvz.model.support.Board;
import ir.ac.pvz.model.support.GridPosition;
import ir.ac.pvz.model.support.Tile;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ZombossController {
    public static final int SEGMENTS = 3;

    private static final float MOVE_ANIMATION_SECONDS = 1.1f;
    private static final int FROZEN_SUMMON_TICKS = 40;

    public interface ImpactListener {
        void onImpact(GridPosition position, float radius, float shake);
    }

    private ImpactListener impactListener;
    private String activeMove;
    private float moveAnimationRemaining;
    private static final float MOVE_INTERVAL = 6.5f;
    private static final float STUN_DURATION = 5f;

    private final SeasonType season;
    private final Board board;
    private final GameSession session;
    private final Random random = new Random();

    private final int segmentHealth;
    private Zombie boss;
    private int health;
    private int topRow;
    private float actionTimer;
    private float stunRemaining;
    private int clearedSegments;
    private String lastAction = "";
    private float lastActionShown;
    private final List<FireTile> burningTiles = new ArrayList<>();

    public static final class FireTile {
        public final GridPosition position;
        public float remaining;

        FireTile(GridPosition position, float remaining) {
            this.position = position;
            this.remaining = remaining;
        }
    }

    public ZombossController(SeasonType season, Board board, GameSession session,
                             int difficulty) {
        this.season = season;
        this.board = board;
        this.session = session;
        this.segmentHealth = 2200 + difficulty * 700;
        this.health = segmentHealth * SEGMENTS;
        this.topRow = Math.max(1, board.rows / 2 - 1);
        this.actionTimer = 4f;
    }

    public void spawn() {
        boss = session.cheatSpawnZombie("Gargantuar", board.columns - 1, topRow);
        if (boss != null) {
            boss.isBoss = true;
            boss.currentHealth = segmentHealth * SEGMENTS;
            boss.initialWaveCost = segmentHealth * SEGMENTS;
            boss.speed = 0f;
            boss.setIdentity("Zomboss", ZombossSupport.title(season));
        }
    }

    public Zombie getBoss() {
        return boss;
    }

    public int getTopRow() {
        return topRow;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return segmentHealth * SEGMENTS;
    }

    public boolean isStunned() {
        return stunRemaining > 0f;
    }

    public boolean isDefeated() {
        return boss != null && (boss.isDead() || health <= 0);
    }

    public void setImpactListener(ImpactListener listener) {
        this.impactListener = listener;
    }

    private void reportImpact(GridPosition position, float radius, float shake) {
        if (impactListener != null && position != null) {
            impactListener.onImpact(position, radius, shake);
        }
    }

    public String getActiveMove() {
        return moveAnimationRemaining > 0f ? activeMove : null;
    }

    public String getLastAction() {
        return lastActionShown > 0f ? lastAction : null;
    }

    public List<FireTile> getBurningTiles() {
        return burningTiles;
    }

    public boolean occupies(int row) {
        return row == topRow || row == topRow + 1;
    }

    public float columnPosition() {
        return board.columns - 1.4f;
    }

    private void syncHealth() {
        if (boss == null) {
            return;
        }

        health = Math.max(0, boss.currentHealth);
        int remainingSegments = (int) Math.ceil(health / (float) segmentHealth);
        int cleared = SEGMENTS - remainingSegments;
        if (cleared > clearedSegments && health > 0) {
            clearedSegments = cleared;
            stunRemaining = STUN_DURATION;
            announce("Zomboss is dazed!");
        }
    }

    public void update(float delta) {
        if (moveAnimationRemaining > 0f) {
            moveAnimationRemaining -= delta;
        }

        syncHealth();
        if (lastActionShown > 0f) {
            lastActionShown -= delta;
        }

        for (int index = burningTiles.size() - 1; index >= 0; index--) {
            FireTile fire = burningTiles.get(index);
            fire.remaining -= delta;
            if (fire.remaining <= 0f) {
                Tile tile = board.getTile(fire.position);
                if (tile != null) {
                    tile.canPlant = true;
                }

                burningTiles.remove(index);
            }
        }

        if (isDefeated()) {
            return;
        }

        if (stunRemaining > 0f) {
            stunRemaining -= delta;
            return;
        }

        keepBossInPlace();
        actionTimer -= delta;
        if (actionTimer <= 0f) {
            actionTimer = MOVE_INTERVAL;
            performAction();
        }
    }

    private void keepBossInPlace() {
        if (boss == null || boss.isDead()) {
            return;
        }

        boss.speed = 0f;
        boss.currentPosition.x = columnPosition();
        if (boss.lane != topRow) {
            board.placeZombie(boss, new ir.ac.pvz.model.support.ContinuousPosition(
                columnPosition(), topRow));
        }
    }

    private void performAction() {
        List<String> moves = new ArrayList<>();
        moves.add("summon");
        moves.add("switchRow");
        switch (season) {
            case DARK_AGES:
                moves.add("fireballs");
                moves.add("burnRows");
                break;
            case ANCIENT_EGYPT:
                moves.add("missile");
                moves.add("charge");
                break;
            case FROSTBITE_CAVES:
                moves.add("iceMissile");
                moves.add("coldWind");
                moves.add("freezeColumn");
                break;
            case BIG_WAVE_BEACH:
            default:
                moves.add("babySharks");
                moves.add("turbine");
                break;
        }

        String move = moves.get(random.nextInt(moves.size()));
        if (season == SeasonType.FROSTBITE_CAVES && move.equals("switchRow")) {
            move = "iceMissile";
        }

        execute(move);
    }

    private void execute(String move) {
        activeMove = move;
        moveAnimationRemaining = MOVE_ANIMATION_SECONDS;

        switch (move) {
            case "summon":
                summonZombies();
                break;
            case "switchRow":
                switchRow();
                break;
            case "fireballs":
                castFireballs();
                break;
            case "burnRows":
                burnRows();
                break;
            case "missile":
            case "iceMissile":
                launchMissile(move.equals("missile"));
                break;
            case "charge":
                chargeForward();
                break;
            case "coldWind":
                blowColdWind();
                break;
            case "freezeColumn":
                freezeColumn();
                break;
            case "babySharks":
                releaseBabySharks();
                break;
            case "turbine":
                runTurbine();
                break;
            default:
                break;
        }
    }

    private void switchRow() {
        topRow = random.nextInt(Math.max(1, board.rows - 2)) + 1;
        announce("Zomboss moves to another lane");
    }

    private void castFireballs() {
        for (int shot = 0; shot < 3; shot++) {
            GridPosition target = randomCell();
            destroyPlantAt(target);
            ignite(target, 4f);
            reportImpact(target, 0.8f, 3.5f);
        }

        announce("The dragon spits fireballs!");
    }

    private void burnRows() {
        forEachOccupiedRow(target -> {
            destroyPlantAt(target);
            ignite(target, 4f);
        });

        announce("The dragon sets two rows on fire!");
    }

    private void launchMissile(boolean plantsGraves) {
        GridPosition target = randomCell();
        destroyPlantAt(target);
        reportImpact(target, 1.2f, 6f);

        if (!plantsGraves) {
            announce("An icy missile lands!");
            return;
        }

        for (int grave = 0; grave < 2; grave++) {
            GridPosition gravePosition = randomCell();
            Tile tile = board.getTile(gravePosition);
            if (tile != null && tile.getPlants().isEmpty()) {
                board.configureTile(gravePosition, TileType.TOMBSTONE);
            }
        }

        announce("A missile lands and graves appear!");
    }

    private void chargeForward() {
        forEachOccupiedRow(this::destroyPlantAt);
        reportImpact(new GridPosition(1, topRow), 1.5f, 8f);
        announce("Zomboss charges forward!");
    }

    private void blowColdWind() {
        for (int attempt = 0; attempt < 2; attempt++) {
            int row = random.nextInt(board.rows);
            for (Zombie zombie : board.getZombiesInLane(row)) {
                zombie.chill(0.5f, 6f);
            }
        }

        announce("An icy wind sweeps the lawn!");
    }

    private void freezeColumn() {
        int column = 2 + random.nextInt(Math.max(1, board.columns - 3));

        for (int row = 0; row < board.rows; row++) {
            GridPosition position = new GridPosition(column, row);
            Tile tile = board.getTile(position);
            if (tile != null && tile.getPlants().isEmpty()) {
                board.configureTile(position, TileType.FROZEN_TILE);
            }
        }

        announce("Zomboss freezes a whole column!");
    }

    private void releaseBabySharks() {
        for (int shot = 0; shot < 2; shot++) {
            GridPosition bite = randomCell();
            destroyPlantAt(bite);
            reportImpact(bite, 0.7f, 3f);
        }

        announce("Baby sharks swallow your plants!");
    }

    private void runTurbine() {
        for (int row = topRow; row <= topRow + 1 && row < board.rows; row++) {
            for (Zombie zombie : board.getZombiesInLane(row)) {
                zombie.currentPosition.x = Math.min(board.columns - 1,
                    zombie.currentPosition.x + 1.5f);
            }

            GridPosition pulled = new GridPosition(board.columns - 2, row);
            destroyPlantAt(pulled);
            reportImpact(pulled, 0.9f, 4f);
        }

        announce("The turbine drags everything in!");
    }

    private void forEachOccupiedRow(java.util.function.Consumer<GridPosition> action) {
        for (int row = topRow; row <= topRow + 1 && row < board.rows; row++) {
            for (int column = 0; column < board.columns; column++) {
                action.accept(new GridPosition(column, row));
            }
        }
    }

    private void summonZombies() {
        String[] pool = ZombossSupport.summonPool(season);
        boolean icy = season == SeasonType.FROSTBITE_CAVES;

        for (int count = 0; count < 2; count++) {
            String type = pool[random.nextInt(pool.length)];
            Zombie summoned = session.cheatSpawnZombie(type, board.columns - 1,
                random.nextInt(board.rows));

            if (icy && summoned != null) {
                summoned.freeze(FROZEN_SUMMON_TICKS);
            }
        }

        announce(icy
            ? "Zomboss summons frozen zombies!"
            : "Zomboss summons more zombies!");
    }

    private GridPosition randomCell() {
        return new GridPosition(random.nextInt(board.columns), random.nextInt(board.rows));
    }

    private void destroyPlantAt(GridPosition position) {
        Tile tile = board.getTile(position);
        if (tile == null) {
            return;
        }

        for (Plant plant : new ArrayList<>(tile.getPlants())) {
            plant.die();
            tile.getPlants().remove(plant);
        }
    }

    private void ignite(GridPosition position, float seconds) {
        Tile tile = board.getTile(position);
        if (tile == null) {
            return;
        }

        tile.canPlant = false;
        burningTiles.add(new FireTile(position, seconds));
    }

    private void announce(String message) {
        lastAction = message;
        lastActionShown = 2.4f;
    }
}
