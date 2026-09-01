package ir.ac.pvz.controller.gui;

import ir.ac.pvz.model.chapter.AncientEgypt;
import ir.ac.pvz.model.chapter.BigWaveBeach;
import ir.ac.pvz.model.chapter.Chapter;
import ir.ac.pvz.model.chapter.DarkAges;
import ir.ac.pvz.model.chapter.FrostbiteCaves;
import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.enums.PlantTag;
import ir.ac.pvz.model.enums.TileType;
import ir.ac.pvz.model.others.GameSession;
import ir.ac.pvz.model.support.Board;
import ir.ac.pvz.model.support.ContinuousPosition;
import ir.ac.pvz.model.support.FrozenBlock;
import ir.ac.pvz.model.support.GridPosition;
import ir.ac.pvz.model.support.Tile;
import ir.ac.pvz.model.support.Tombstone;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ChapterEffectsController {
    public static final int PLANT_ICE_LEVELS = 3;
    private static final int LOW_TIDE_TILES = 3;
    private static final int NECROMANCY_TILES = 4;
    private static final int FROZEN_START_ZOMBIES = 2;

    private final Random random = new Random();
    private final List<Marker> markers = new ArrayList<>();
    private String announcement;
    private float announcementRemaining;

    public static final class Marker {
        public final String kind;
        public final int column;
        public final int row;
        public float remaining;

        Marker(String kind, int column, int row, float remaining) {
            this.kind = kind;
            this.column = column;
            this.row = row;
            this.remaining = remaining;
        }
    }

    public List<Marker> getMarkers() {
        return markers;
    }

    public String getAnnouncement() {
        return announcementRemaining > 0f ? announcement : null;
    }

    public void announce(String text, float seconds) {
        announcement = text;
        announcementRemaining = seconds;
    }

    public void update(float delta) {
        if (announcementRemaining > 0f) {
            announcementRemaining -= delta;
        }

        for (int index = markers.size() - 1; index >= 0; index--) {
            Marker marker = markers.get(index);
            marker.remaining -= delta;
            if (marker.remaining <= 0f) {
                markers.remove(index);
            }
        }
    }

    public void prepareChapterTiles(Chapter chapter, Board board) {
        if (chapter instanceof DarkAges) {
            markSpecialTiles(board, TileType.NECROMANCY, NECROMANCY_TILES);
        }

        if (chapter instanceof BigWaveBeach) {
            markSpecialTiles(board, TileType.LOW_TIDE, LOW_TIDE_TILES);
        }
    }

    private void markSpecialTiles(Board board, TileType type, int count) {
        for (int index = 0; index < count; index++) {
            int column = board.columns - 1
                - random.nextInt(Math.max(1, board.columns / 3));
            GridPosition position = new GridPosition(column,
                random.nextInt(board.rows));
            Tile tile = board.getTile(position);
            if (tile == null || tile.type == type) {
                continue;
            }

            board.configureTile(position, type);
        }
    }

    public void freezeStartingZombies(Board board, GameSession session) {
        for (int index = 0; index < FROZEN_START_ZOMBIES; index++) {
            GridPosition position = new GridPosition(
                board.columns - 2 - random.nextInt(2), random.nextInt(board.rows));
            Tile tile = board.getTile(position);
            if (tile == null || tile.hasObstacle() || !tile.getPlants().isEmpty()) {
                continue;
            }

            Zombie zombie = session.spawnConfiguredZombie("BasicZombie",
                new ContinuousPosition(position.x, position.y));
            if (zombie != null) {
                board.configureFrozenZombie(position, zombie);
            }
        }
    }

    public void onWaveStarted(Chapter chapter, Board board, GameSession session,
                              int waveNumber) {
        if (chapter == null) {
            return;
        }

        if (chapter instanceof FrostbiteCaves) {
            blowColdWind(board);
        }

        else if (chapter instanceof BigWaveBeach) {
            updateTide(chapter, board, session);
        }

        else if (chapter instanceof DarkAges) {
            raiseGraves(chapter, board);
            runNecromancy(board, session);
        }

        else if (chapter instanceof AncientEgypt && waveNumber > 1) {
            announce("The desert wind is picking up.", 2f);
        }
    }

    private void blowColdWind(Board board) {
        int first = random.nextInt(board.rows);
        int second = (first + 1 + random.nextInt(Math.max(1, board.rows - 1)))
            % board.rows;

        for (int row : new int[] { first, second }) {
            for (int column = 0; column < board.columns; column++) {
                markers.add(new Marker("coldwind", column, row, 2.2f));
                chillPlant(board, new GridPosition(column, row));
            }
        }

        announce("An icy wind sweeps across the lawn!", 2.4f);
    }

    private void chillPlant(Board board, GridPosition position) {
        Tile tile = board.getTile(position);
        if (tile == null || tile.obstacle instanceof FrozenBlock) {
            return;
        }

        Plant plant = tile.getPlant();
        if (plant == null || !plant.isAlive
            || plant.plantTags.contains(PlantTag.FIRE)) {
            return;
        }

        plant.receiveHunterIceHit(PLANT_ICE_LEVELS);
        if (!plant.isPermanentlyFrozen()) {
            return;
        }

        tile.obstacle = new FrozenBlock(plant);
        tile.type = TileType.FROZEN_TILE;
        tile.canPlant = false;
    }

    private void updateTide(Chapter chapter, Board board, GameSession session) {
        BigWaveBeach beach = (BigWaveBeach) chapter;
        beach.shiftWaterLevel(board, random);
        int surfaced = spawnFromSpecialTiles(board, session, TileType.LOW_TIDE);

        if (surfaced > 0) {
            announce("Zombies are surfacing from the low tide!", 2.6f);
        }

        else {
            announce("The tide is changing!", 2.4f);
        }
    }

    private void raiseGraves(Chapter chapter, Board board) {
        List<GridPosition> raised =
            ((DarkAges) chapter).spawnGravesPerWave(board, random);

        for (GridPosition position : raised) {
            markers.add(new Marker("grave", position.x, position.y, 2f));
        }

        if (!raised.isEmpty()) {
            announce("Graves are rising from the ground!", 2.4f);
        }
    }

    private void runNecromancy(Board board, GameSession session) {
        int raised = 0;

        for (Tile tile : necromancyTilesWithGrave(board)) {
            if (random.nextInt(2) != 0) {
                continue;
            }

            Zombie zombie = session.spawnZombieFromSpecialTile("BasicZombie",
                tile.getPosition());
            if (zombie == null) {
                continue;
            }

            tile.obstacle.destroy();
            board.clearDestroyedObstacle(tile);
            markers.add(new Marker("surface", tile.getPosition().x,
                tile.getPosition().y, 2f));
            raised++;
        }

        if (raised > 0) {
            announce("Necromancy is raising the dead!", 2.6f);
        }
    }

    private List<Tile> necromancyTilesWithGrave(Board board) {
        List<Tile> result = new ArrayList<>();

        for (int row = 0; row < board.rows; row++) {
            for (int column = 0; column < board.columns; column++) {
                Tile tile = board.getTile(new GridPosition(column, row));
                if (tile != null
                    && tile.nativeGroundType == TileType.NECROMANCY
                    && tile.obstacle instanceof Tombstone
                    && tile.obstacle.isAlive) {
                    result.add(tile);
                }
            }
        }

        return result;
    }

    private int spawnFromSpecialTiles(Board board, GameSession session,
                                      TileType type) {
        int spawned = 0;

        for (Tile tile : board.getTilesByType(type)) {
            if (random.nextInt(2) != 0) {
                continue;
            }

            Zombie zombie = session.spawnZombieFromSpecialTile("BasicZombie",
                tile.getPosition());
            if (zombie != null) {
                markers.add(new Marker("surface", tile.getPosition().x,
                    tile.getPosition().y, 2f));
                spawned++;
            }
        }

        return spawned;
    }

    public int tornadoEntryColumn(Chapter chapter, Board board, boolean finalWave,
                                  int lane) {
        if (!(chapter instanceof AncientEgypt) || !finalWave) {
            return -1;
        }

        int offset = 1 + random.nextInt(4);
        int column = Math.max(1, board.columns - 1 - offset);
        markers.add(new Marker("tornado", column, lane, 1.8f));
        announce("A tornado drops a zombie deep into your lawn!", 2.2f);
        return column;
    }

    public boolean isGraveWithContents(Tile tile) {
        return tile != null && tile.obstacle instanceof Tombstone
            && !Tombstone.PLAIN.equals(((Tombstone) tile.obstacle).getVariant());
    }
}
