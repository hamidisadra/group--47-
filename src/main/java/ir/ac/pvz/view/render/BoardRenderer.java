package ir.ac.pvz.view.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;

import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.enums.SeasonType;
import ir.ac.pvz.model.support.ArmorPiece;
import ir.ac.pvz.model.support.Board;
import ir.ac.pvz.model.support.LawnMower;
import ir.ac.pvz.model.support.Sun;
import ir.ac.pvz.model.support.Tile;
import ir.ac.pvz.view.assets.AnimationNames;
import ir.ac.pvz.view.assets.GameAssets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BoardRenderer {
    private static final float PLANT_SCALE = 0.40f;
    private static final float ZOMBIE_SCALE = 0.43f;
    private static final float ACTION_ANIMATION_SECONDS = 0.55f;
    private static final float PLANT_GROUND = 0.30f;
    private static final float ZOMBIE_GROUND = 0.24f;

    public static final float SUN_FALL_HEIGHT = 620f;

    private final GameAssets assets;
    private final Lawn lawn;
    private final ShapeRenderer shapes;
    private final Map<ArmorPiece, Integer> armorMaxHealth = new java.util.WeakHashMap<>();
    private final List<Drawable> queue = new ArrayList<>();
    private final Map<Plant, Float> plantActionTimers = new java.util.WeakHashMap<>();
    private final Map<Zombie, Float> zombieHitTimers = new java.util.WeakHashMap<>();
    private final Map<Zombie, Integer> lastHealth = new java.util.WeakHashMap<>();
    private final List<Zombie> seenZombies = new ArrayList<>();
    private static final float DEBRIS_GRAVITY = 900f;
    private static final String HEAD_PART = "particle_head";
    private static final String ARM_PART = "particle_arm";
    private static final float LANE_EASE = 0.18f;

    private final DebrisLayer debrisLayer;
    private final TileLayer tileLayer;
    private final ZombieTracker tracker;
    private final Map<Zombie, Integer> lastArmorCount = new java.util.WeakHashMap<>();
    private final Map<Zombie, Float> visualLane = new java.util.WeakHashMap<>();
    private String bossMove;
    private final Map<Plant, Integer> lastPlantHealth = new java.util.WeakHashMap<>();
    private final Map<Plant, Float> plantHitTimers = new java.util.WeakHashMap<>();
    private final java.util.Set<Zombie> impThrowSeen =
        java.util.Collections.newSetFromMap(new java.util.WeakHashMap<>());
    private final Map<Integer, Float> mowerTravel = new HashMap<>();

    private boolean showGrid;

    private static final class Drawable {
        float sortY;
        String path;
        String clip;
        float time;
        float x;
        float y;
        float scale;
        boolean loop;
        Map<String, Boolean> parts;
        Color tint;
        boolean glow;
    }

    public BoardRenderer(SeasonType season) {
        this.assets = GameAssets.get();
        this.lawn = new Lawn(season);
        this.shapes = new ShapeRenderer();
        this.debrisLayer = new DebrisLayer(assets);
        this.tileLayer = new TileLayer(assets, lawn);
        this.tracker = new ZombieTracker(assets, debrisLayer);
    }

    public void drawDebris(com.badlogic.gdx.graphics.g2d.Batch batch,
                           com.badlogic.gdx.graphics.g2d.TextureRegion particle) {
        debrisLayer.drawDebris(batch, particle);
    }

    public void drawPartDebris(com.badlogic.gdx.graphics.g2d.Batch batch) {
        debrisLayer.drawPartDebris(batch);
    }

    public void setBossMove(String move) {
        this.bossMove = move;
    }

    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
    }

    public void notifyPlantAction(Plant plant) {
        if (plant == null) {
            return;
        }

        plantActionTimers.put(plant, actionSpan(plant));
    }

    private float actionSpan(Plant plant) {
        String path = assets.plantPath(plant.type);
        if (path == null) {
            return ACTION_ANIMATION_SECONDS;
        }

        String clip = assets.pickClip(path, "attack", "special", "idle");
        float span = assets.clipDuration(path, clip);
        if (span <= 0f) {
            return ACTION_ANIMATION_SECONDS;
        }

        float interval = plant.actionInterval > 0f
            ? plant.actionInterval * 0.92f : span;
        return Math.max(0.2f, Math.min(span, interval));
    }

    public void advance(float delta, Board board) {
        tickTimers(delta);
        trackZombies(board);
        advanceDebris(delta);
        debrisLayer.advancePartDebris(delta);
        advanceDeaths(delta);
        mowerTravel.replaceAll((row, travel) -> travel + delta * 0.75f);
    }

    private void tickTimers(float delta) {
        plantActionTimers.replaceAll((plant, remaining) -> remaining - delta);
        plantActionTimers.values().removeIf(remaining -> remaining <= 0f);
        plantHitTimers.replaceAll((plant, remaining) -> remaining - delta);
        plantHitTimers.values().removeIf(remaining -> remaining <= 0f);
        zombieHitTimers.replaceAll((zombie, remaining) -> remaining - delta);
        zombieHitTimers.values().removeIf(remaining -> remaining <= 0f);
    }

    private void trackZombies(Board board) {
        tracker.trackZombies(board);
    }

    private void advanceDebris(float delta) {
        tracker.advanceDebris(delta);
    }

    private void advanceDeaths(float delta) {
        tracker.advanceDeaths(delta);
    }

    private void collectDeaths() {
        for (DebrisLayer.DeathAnimation death : debrisLayer.getDeaths()) {
            Drawable item = new Drawable();
            item.path = death.path;
            item.clip = death.clip;
            item.time = death.total - death.remaining;
            item.x = death.x;
            item.y = death.y;
            item.scale = death.scale;
            item.loop = false;
            item.sortY = death.y;
            item.tint = new Color(1f, 1f, 1f, Math.min(1f, death.remaining / 0.4f));
            queue.add(item);
        }
    }

    public void drawBackground(Batch batch) {
        lawn.draw(batch);
    }

    public float groundY(String path, String clip, float scale, int row, int rowCount) {
        return Lawn.rowBottomY(row, rowCount) + Lawn.CELL_HEIGHT * PLANT_GROUND;
    }

    public void drawGrid(Matrix4 projection, Board board) {
        if (showGrid) {
            tileLayer.drawGrid(shapes, projection, board);
        }
    }

    public void drawCursor(Matrix4 projection, int column, int row,
                           int rowCount) {
        shapes.setProjectionMatrix(projection);
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(1f, 0.85f, 0.2f, 1f);

        float x = Lawn.GRID_LEFT + column * Lawn.CELL_WIDTH;
        float y = Lawn.rowBottomY(row, rowCount);

        shapes.rect(x + 2f, y + 2f, Lawn.CELL_WIDTH - 4f,
            Lawn.CELL_HEIGHT - 4f);
        shapes.end();
    }

    public void drawTiles(Batch batch, Board board, float time) {
        tileLayer.drawTiles(batch, board, time);
    }

    public void drawEntities(Batch batch, Board board, float time) {
        queue.clear();
        collectMowers(board, time);
        collectPlants(board, time);
        collectZombies(board, time);
        collectDeaths();
        queue.sort((first, second) -> Float.compare(second.sortY, first.sortY));
        com.badlogic.gdx.graphics.g2d.TextureRegion pixel =
            ((com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable)
                ir.ac.pvz.view.ui.Ui.solid(Color.WHITE)).getRegion();
        for (Drawable item : queue) {
            if (item.glow) {
                float pulse = 0.35f + 0.2f * (float) Math.sin(time * 8f);
                batch.setColor(1f, 0.95f, 0.35f, pulse);
                batch.draw(pixel, item.x - Lawn.CELL_WIDTH * 0.5f,
                    item.y - Lawn.CELL_HEIGHT * 0.2f,
                    Lawn.CELL_WIDTH, Lawn.CELL_HEIGHT);
                batch.setColor(Color.WHITE);
            }

            if (item.tint != null) {
                batch.setColor(item.tint);
            }

            assets.draw(batch, item.path, item.clip, item.time, item.x, item.y, item.scale,
                item.loop, item.parts);
            if (item.tint != null) {
                batch.setColor(Color.WHITE);
            }
        }
    }

    private void collectMowers(Board board, float time) {
        String path = assets.mowerPath(board.seasonType);
        for (int row = 0; row < board.rows; row++) {
            LawnMower mower = board.getLawnMower(row);
            if (mower == null) {
                continue;
            }

            Float travel = mowerTravel.get(row);
            if (mower.isActivated() && travel == null) {
                mowerTravel.put(row, 0f);
                travel = 0f;
            }

            if (mower.isActivated() && travel > 1f) {
                continue;
            }

            Drawable item = new Drawable();
            item.path = path;
            item.clip = assets.pickClip(path, mower.isActivated() ? "attack" : "idle", "idle");
            item.time = time;
            item.x = Lawn.GRID_LEFT - Lawn.CELL_WIDTH * 0.55f
                + (travel == null ? 0f : travel)
                * (board.columns + 1) * Lawn.CELL_WIDTH;
            item.y = Lawn.rowBottomY(row, board.rows) + Lawn.CELL_HEIGHT * ZOMBIE_GROUND;
            item.scale = ZOMBIE_SCALE;
            item.loop = true;
            item.sortY = item.y;
            queue.add(item);
        }
    }

    private void collectPlants(Board board, float time) {
        for (int row = 0; row < board.rows; row++) {
            for (int column = 0; column < board.columns; column++) {
                collectPlantsOnTile(board, time, row, column);
            }
        }
    }

    private void collectPlantsOnTile(Board board, float time, int row, int column) {
        Tile tile = board.getTile(
            new ir.ac.pvz.model.support.GridPosition(column, row));
        if (tile == null) {
            return;
        }

        for (Plant plant : tile.getPlants()) {
            if (!plant.isAlive) {
                continue;
            }

            String path = assets.plantPath(plant.type);
            if (path == null) {
                continue;
            }

            queue.add(buildPlantDrawable(board, plant, path, time, row, column));
        }
    }

    private Drawable buildPlantDrawable(Board board, Plant plant, String path,
                                        float time, int row, int column) {
        Drawable item = new Drawable();
        item.path = path;
        applyPlantClip(item, plant, path, time);

        item.x = Lawn.columnCenterX(column);
        item.y = Lawn.rowBottomY(row, board.rows)
            + Lawn.CELL_HEIGHT * PLANT_GROUND;
        item.scale = PLANT_SCALE;
        item.sortY = Lawn.rowCenterY(row, board.rows);

        if (plant.isBoostedByPlantFood) {
            item.tint = new Color(1f, 1f, 0.55f, 1f);
            item.glow = true;
        }

        applyPlantTint(item, plant);

        if (!plant.canAct()) {
            item.clip = assets.pickClip(path, "idle");
            item.loop = false;
            item.time = 0f;
        }

        return item;
    }

    private void applyPlantClip(Drawable item, Plant plant, String path, float time) {
        Float action = plantActionTimers.get(plant);

        if (action == null) {
            item.clip = assets.pickClip(path, "idle");
            item.time = time + plant.id * 0.37f;
            item.loop = true;
            return;
        }

        item.clip = assets.pickClip(path, actionClips(plant));
        float span = assets.clipDuration(path, item.clip);
        float elapsed = actionSpan(plant) - action;
        item.time = span > 0f ? Math.min(elapsed, span - 0.001f) : elapsed;
        item.loop = false;
    }

    private void applyPlantTint(Drawable item, Plant plant) {
        Integer previousHp = lastPlantHealth.get(plant);

        if (previousHp != null && plant.currentHp < previousHp) {
            plantHitTimers.put(plant, 0.14f);
        }

        lastPlantHealth.put(plant, plant.currentHp);

        if (plantHitTimers.containsKey(plant)) {
            item.tint = new Color(1.7f, 1.2f, 1.2f, 1f);
        }

        Color stateTint = plantStateTint(plant);

        if (stateTint != null) {
            item.tint = stateTint;
        }
    }

    private void collectZombies(Board board, float time) {
        for (Zombie zombie : board.getAllAliveZombies()) {
            boolean boss = zombie.isBoss;
            String path = boss
                ? assets.zombiePath("Zomboss", board.seasonType)
                : assets.zombiePath(zombie.getType(), board.seasonType);
            if (path == null) {
                continue;
            }

            float scale = boss ? bossScale(path) : ZOMBIE_SCALE;
            int anchorRow = boss ? Math.min(board.rows - 1, zombie.lane + 1) : zombie.lane;
            Drawable item = new Drawable();
            item.path = path;
            if (boss) {
                item.clip = assets.pickClip(path, bossClips(zombie));
            }

            else {
                item.clip = isEating(zombie, board)
                    ? assets.pickClip(path, eatClips(zombie))
                    : assets.pickClip(path, moveClips(zombie));
            }

            item.time = time + zombie.hashCode() % 100 * 0.011f;
            item.x = Lawn.columnCenterX(zombie.currentPosition.x);
            float smoothRow = smoothLane(zombie, anchorRow);
            item.y = Lawn.rowBottomY(0, board.rows)
                - smoothRow * Lawn.CELL_HEIGHT
                + Lawn.CELL_HEIGHT * ZOMBIE_GROUND;
            item.scale = scale;
            item.loop = true;
            item.sortY = Lawn.rowCenterY(Math.round(smoothRow), board.rows);
            item.parts = boss ? assets.allParts(path) : armorVisibility(zombie, path);
            item.tint = effectTint(zombie);
            queue.add(item);
        }
    }

    private float bossScale(String path) {
        com.badlogic.gdx.math.Rectangle bounds = assets.bounds(path, "idle");
        if (bounds == null || bounds.height <= 0f) {
            return ZOMBIE_SCALE * 1.5f;
        }

        return (Lawn.CELL_HEIGHT * 3.1f) / bounds.height;
    }

    private boolean isEating(Zombie zombie, Board board) {
        int column = (int) Math.floor(zombie.currentPosition.x);
        Tile tile = board.getTile(new ir.ac.pvz.model.support.GridPosition(column, zombie.lane));
        if (tile == null) {
            return false;
        }

        for (Plant plant : tile.getPlants()) {
            if (plant.isAlive && !plant.isCatTransformed) {
                return true;
            }
        }

        return false;
    }

    private float smoothLane(Zombie zombie, int targetRow) {
        Float current = visualLane.get(zombie);

        if (current == null) {
            visualLane.put(zombie, (float) targetRow);
            return targetRow;
        }

        float eased = current + (targetRow - current) * LANE_EASE;

        if (Math.abs(targetRow - eased) < 0.01f) {
            eased = targetRow;
        }

        visualLane.put(zombie, eased);
        return eased;
    }

    private String[] bossClips(Zombie zombie) {
        if (zombie.isStunned()) {
            return new String[] { "stun_loop", "stun", "idle" };
        }

        if (bossMove == null) {
            return new String[] { "idle", "walk_forward" };
        }

        switch (bossMove) {
            case "summon":
                return new String[] { "summon", "call", "special", "idle" };

            case "missile":
            case "iceMissile":
                return new String[] { "missile", "launch", "attack", "idle" };

            case "fireballs":
                return new String[] { "fireball", "spit", "attack", "idle" };

            case "burnRows":
                return new String[] { "flame", "breath", "attack", "idle" };

            case "charge":
                return new String[] { "charge", "run", "walk_forward", "idle" };

            case "coldWind":
                return new String[] { "wind", "breath", "freeze", "attack", "idle" };

            case "freezeColumn":
                return new String[] { "freeze", "ice", "breath", "attack", "idle" };

            case "babySharks":
                return new String[] { "shark", "summon_shark", "special", "attack", "idle" };

            case "turbine":
                return new String[] { "turbine", "suck", "vacuum", "special", "attack", "idle" };

            case "switchRow":
                return new String[] { "walk_forward", "walk", "idle" };

            default:
                return new String[] { "idle", "walk_forward" };
        }
    }

    private String[] moveClips(Zombie zombie) {
        if (zombie instanceof ir.ac.pvz.model.zombies.FootballZombie
            && ((ir.ac.pvz.model.zombies.FootballZombie) zombie).isRunning()) {
            return new String[] { "run", "charge", "walk", "idle" };
        }

        if (zombie instanceof ir.ac.pvz.model.zombies.Gargantuar) {
            return new String[] { "walk", "walk_forward", "idle" };
        }

        if (zombie instanceof ir.ac.pvz.model.zombies.FishermanZombie
            || zombie instanceof ir.ac.pvz.model.zombies.KingZombie) {
            return new String[] { "idle", "walk" };
        }

        if (zombie instanceof ir.ac.pvz.model.zombies.OctopusZombie
            || zombie instanceof ir.ac.pvz.model.zombies.HunterZombie
            || zombie instanceof ir.ac.pvz.model.zombies.TombRaiserZombie) {
            return new String[] { "throw", "attack", "walk", "idle" };
        }

        if (zombie instanceof ir.ac.pvz.model.zombies.RaZombie) {
            return new String[] { "steal", "attack", "walk", "idle" };
        }

        if (zombie instanceof ir.ac.pvz.model.zombies.JesterZombie) {
            return new String[] { "spin", "juggle", "walk", "idle" };
        }

        if (zombie instanceof ir.ac.pvz.model.zombies.BarrelRollerZombie
            || zombie instanceof ir.ac.pvz.model.zombies.Troglobite
            || zombie instanceof ir.ac.pvz.model.zombies.ArcadeZombie) {
            return new String[] { "push", "walk", "idle" };
        }

        return new String[] { "walk", "idle" };
    }

    private String[] eatClips(Zombie zombie) {
        if (zombie instanceof ir.ac.pvz.model.zombies.Gargantuar) {
            return new String[] { "smash", "attack", "eat", "walk", "idle" };
        }

        if (zombie instanceof ir.ac.pvz.model.zombies.WizardZombie) {
            return new String[] { "cast", "special", "attack", "eat", "idle" };
        }

        if (zombie instanceof ir.ac.pvz.model.zombies.OctopusZombie
            || zombie instanceof ir.ac.pvz.model.zombies.HunterZombie) {
            return new String[] { "throw", "attack", "eat", "idle" };
        }

        return new String[] { "eat", "attack", "walk", "idle" };
    }

    private String[] actionClips(Plant plant) {
        if (plant.category == null) {
            return new String[] { "attack", "special", "idle" };
        }

        switch (plant.category) {
            case SUN_PRODUCER:
                return new String[] { "produce", "sun", "special", "attack", "idle" };

            case SHOOTER:
                return new String[] { "shoot", "shooting", "attack", "special", "idle" };

            case LOBBER:
                return new String[] { "lob", "throw", "attack", "special", "idle" };

            case EXPLOSIVE:
                return new String[] { "explode", "boom", "special", "attack", "idle" };

            case MELEE:
                return new String[] { "chomp", "bite", "eat", "attack", "special", "idle" };

            case WALL:
                return new String[] { "hit", "damage", "idle" };

            case MODIFIER:
                return new String[] { "special", "activate", "attack", "idle" };

            case STRIKE_THROUGH:
                return new String[] { "shoot", "fume", "attack", "special", "idle" };

            case HOMING:
                return new String[] { "shoot", "launch", "attack", "special", "idle" };

            case MINT:
                return new String[] { "special", "burst", "attack", "idle" };

            default:
                return new String[] { "attack", "special", "idle" };
        }
    }

    private Color plantStateTint(Plant plant) {
        if (plant.isCatTransformed) {
            return new Color(0.85f, 0.7f, 1f, 1f);
        }

        if (plant.isOctopusBlocked) {
            return new Color(0.75f, 0.55f, 0.85f, 1f);
        }

        int iceLevel = plant.getIceLevel();

        if (iceLevel >= 3) {
            return new Color(0.55f, 0.8f, 1f, 1f);
        }

        if (iceLevel == 2) {
            return new Color(0.72f, 0.88f, 1f, 1f);
        }

        if (iceLevel == 1) {
            return new Color(0.86f, 0.94f, 1f, 1f);
        }

        return null;
    }

    private Map<String, Boolean> armorVisibility(Zombie zombie, String path) {
        List<ArmorPiece> pieces = zombie.armorPieces;
        if (pieces == null || pieces.isEmpty() || path == null) {
            return null;
        }

        Map<String, Boolean> visibility = new HashMap<>();
        for (ArmorPiece piece : pieces) {
            if (piece == null || piece.health <= 0) {
                continue;
            }

            int stage = debrisLayer.damageStage(piece);
            String part = AnimationNames.armorPart(piece.name, stage);
            String slot = AnimationNames.armorSlot(piece.name);
            if (part == null || slot == null) {
                continue;
            }

            visibility.put(part, Boolean.TRUE);
            String group = armorGroup(path, slot);
            if (group != null) {
                visibility.put(group, Boolean.TRUE);
            }
        }

        return visibility.isEmpty() ? null : visibility;
    }

    private String armorGroup(String path, String slot) {
        Map<String, Boolean> parts = assets.allParts(path);
        if (parts == null || parts.isEmpty()) {
            return null;
        }

        String wanted = "bucket".equals(slot) ? "armor2_states" : "armor1_states";
        String fallback = null;
        for (String key : parts.keySet()) {
            if (key == null || !key.startsWith("_")) {
                continue;
            }

            if (key.endsWith(wanted)) {
                return key;
            }

            if (key.contains("armor") && key.endsWith("_states")) {
                fallback = key;
            }
        }

        return fallback;
    }

    private int damageStage(ArmorPiece piece) {
        Integer known = armorMaxHealth.get(piece);
        int max = known == null ? piece.health : Math.max(known, piece.health);
        armorMaxHealth.put(piece, max);
        if (max <= 0) {
            return 0;
        }

        float ratio = piece.health / (float) max;
        if (ratio > 0.66f) {
            return 0;
        }

        return ratio > 0.33f ? 1 : 2;
    }

    private Color effectTint(Zombie zombie) {
        if (zombieHitTimers.containsKey(zombie)) {
            return new Color(1.6f, 1.6f, 1.6f, 1f);
        }

        if (zombie.currentPosition.x <= 1.2f) {
            return new Color(1f, 0.55f, 0.5f, 1f);
        }

        if (zombie.isStunned()) {
            return new Color(1f, 0.95f, 0.4f, 1f);
        }

        if (zombie.isFrozen()) {
            return new Color(0.35f, 0.65f, 1f, 1f);
        }

        if (zombie.getChillSlowFactor() < 1f) {
            return new Color(0.6f, 0.8f, 1f, 1f);
        }

        if (zombie.isHypnotized()) {
            return new Color(0.8f, 0.5f, 1f, 1f);
        }

        return null;
    }

    public void drawSuns(Batch batch, List<Sun> suns, int rowCount, float time) {
        String path = assets.effectPath("SUN");
        if (path == null) {
            return;
        }

        for (Sun sun : suns) {
            if (sun.isCollected() || sun.groundPosition == null) {
                continue;
            }

            float x = Lawn.columnCenterX(sun.groundPosition.x);
            float y = Lawn.rowCenterY(sun.groundPosition.y, rowCount)
                + sun.height * SUN_FALL_HEIGHT;
            float scale = sun.type == ir.ac.pvz.model.enums.FallingSunType.SPECIAL ? 0.7f : 0.5f;
            batch.setColor(sun.type == ir.ac.pvz.model.enums.FallingSunType.RADIOACTIVE
                ? new Color(0.75f, 0.4f, 1f, 1f) : Color.WHITE);
            assets.draw(batch, path, assets.pickClip(path, "idle", "animation"), time, x, y,
                scale, true);
            batch.setColor(Color.WHITE);
        }
    }

    public void dispose() {
        shapes.dispose();
    }
}
