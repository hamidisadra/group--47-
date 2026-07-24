package ir.ac.pvz.model.support;

import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.enums.SeasonType;
import ir.ac.pvz.model.enums.TileType;
import java.util.ArrayList;
import java.util.List;

public class Board {
    public int rows;
    public int columns;
    public SeasonType seasonType;
    private final Tile[][] tiles;
    private final List<LawnMower> lawnMowers;
    private final List<Barrel> looseBarrels;
    public Board(int rows, int columns, SeasonType seasonType) {
        this.rows = rows;
        this.columns = columns;
        this.seasonType = seasonType;
        this.tiles = new Tile[rows][columns];
        this.lawnMowers = new ArrayList<>();
        this.looseBarrels = new ArrayList<>();
        TileType defaultType = getDefaultTileType(seasonType);
        for (int row = 0; row < rows; row++) {
            lawnMowers.add(new LawnMower(row));
            for (int column = 0; column < columns; column++) {
                tiles[row][column] = new Tile(new GridPosition(column, row), defaultType, true);
            }
        }
    }
    public static TileType getDefaultTileType(SeasonType type) {
        if (type == SeasonType.FROSTBITE_CAVES) {
            return TileType.FROSTBITE_GROUND;
        }
        if (type == SeasonType.BIG_WAVE_BEACH) {
            return TileType.BEACH_GROUND;
        }
        if (type == SeasonType.DARK_AGES) {
            return TileType.DARK_GROUND;
        }
        return TileType.EGYPT_GROUND;
    }
    public boolean configureTile(GridPosition position, TileType type) {
        Tile tile = getTile(position);
        if (tile == null || type == null) {
            return false;
        }
        if (type != TileType.TOMBSTONE && type != TileType.FROZEN_TILE) {
            tile.setNativeGroundType(type);
        }
        tile.type = type;
        if (type == TileType.SLIPPERY_UP) {
            tile.slipDeltaRow = -1;
        }
        else if (type == TileType.SLIPPERY_DOWN) {
            tile.slipDeltaRow = 1;
        }
        else {
            tile.slipDeltaRow = 0;
        }
        tile.isWater = type == TileType.WATER;
        tile.isLowTideSpawn = type == TileType.LOW_TIDE
                || type == TileType.NECROMANCY;
        tile.canPlant = type != TileType.TOMBSTONE
                && type != TileType.SLIPPERY_UP
                && type != TileType.SLIPPERY_DOWN
                && type != TileType.FROZEN_TILE;
        tile.obstacle = createObstacle(type);
        return true;
    }
    public void clearDestroyedObstacle(Tile tile) {
        if (tile == null || tile.obstacle == null || tile.obstacle.isAlive) {
            return;
        }
        tile.obstacle.destroy();
        tile.obstacle = null;
        tile.restoreNativeGround();
    }
    public boolean configureFrozenPlant(GridPosition position, Plant plant) {
        Tile tile = getTile(position);
        if (tile == null || plant == null) {
            return false;
        }
        tile.type = TileType.FROZEN_TILE;
        tile.canPlant = false;
        tile.addFrozenPlant(plant);
        tile.obstacle = new FrozenBlock(plant);
        return true;
    }
    public boolean configureFrozenZombie(GridPosition position, Zombie zombie) {
        Tile tile = getTile(position);
        if (tile == null || zombie == null) {
            return false;
        }
        tile.type = TileType.FROZEN_TILE;
        tile.canPlant = false;
        tile.addFrozenZombie(zombie);
        tile.obstacle = new FrozenBlock(zombie);
        return true;
    }
    public List<Tile> getTilesByType(TileType type) {
        List<Tile> result = new ArrayList<>();
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                Tile tile = tiles[row][column];
                if (tile.type == type) {
                    result.add(tile);
                }
            }
        }
        return result;
    }
    private TileObstacle createObstacle(TileType type) {
        if (type == TileType.TOMBSTONE) {
            return new Tombstone();
        }
        if (type == TileType.FROZEN_TILE) {
            return new FrozenBlock();
        }
        return null;
    }
    public Tile getTile(GridPosition position) {
        if (!isInside(position)) {
            return null;
        }
        return tiles[position.y][position.x];
    }
    public List<Zombie> getZombiesInLane(int row) {
        List<Zombie> zombies = new ArrayList<>();
        if (row < 0 || row >= rows) {
            return zombies;
        }
        for (int column = 0; column < columns; column++) {
            zombies.addAll(tiles[row][column].getZombies());
        }
        return zombies;
    }
    public List<Plant> getPlantsInLane(int row) {
        List<Plant> plants = new ArrayList<>();
        if (row < 0 || row >= rows) {
            return plants;
        }
        for (int column = 0; column < columns; column++) {
            plants.addAll(tiles[row][column].getPlants());
        }
        return plants;
    }
    public List<Plant> getAllPlants() {
        List<Plant> plants = new ArrayList<>();
        for (int row = 0; row < rows; row++) {
            plants.addAll(getPlantsInLane(row));
        }
        return plants;
    }
    public boolean movePlant(Plant plant, GridPosition target) {
        if (plant == null || target == null || !isInside(target)) {
            return false;
        }
        Tile destination = getTile(target);
        if (!plant.canPlantOn(destination)) {
            return false;
        }
        Tile source = getTile(plant.location);
        if (source == null || !source.getPlants().remove(plant)) {
            return false;
        }
        if (!destination.addPlant(plant)) {
            source.addPlant(plant);
            return false;
        }
        return true;
    }
    public void removeZombieEverywhere(Zombie zombie) {
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                tiles[row][column].removeZombie(zombie);
            }
        }
    }
    public boolean placeZombie(Zombie zombie, ContinuousPosition position) {
        if (zombie == null || position == null) {
            return false;
        }
        GridPosition tilePosition = new GridPosition(
                (int) Math.floor(position.x), position.y);
        if (!isInside(tilePosition)) {
            return false;
        }
        removeZombieEverywhere(zombie);
        zombie.currentPosition = position;
        zombie.positionX = position.x;
        zombie.positionY = position.y;
        zombie.lane = position.y;
        getTile(tilePosition).addZombie(zombie);
        return true;
    }
    public void addLooseBarrel(Barrel barrel) {
        if (barrel != null && barrel.health > 0 && !looseBarrels.contains(barrel)) {
            looseBarrels.add(barrel);
        }
    }
    public void removeLooseBarrel(Barrel barrel) {
        looseBarrels.remove(barrel);
    }
    public List<Barrel> getLooseBarrels() {
        return new ArrayList<>(looseBarrels);
    }
    public List<Zombie> getAllAliveZombies() {
        List<Zombie> zombies = new ArrayList<>();
        for (int row = 0; row < rows; row++) {
            for (Zombie zombie : getZombiesInLane(row)) {
                if (!zombie.isDead() && !zombies.contains(zombie)) {
                    zombies.add(zombie);
                }
            }
        }
        return zombies;
    }
    public List<Zombie> getWaterZombies() {
        List<Zombie> zombies = new ArrayList<>();
        for (Zombie zombie : getAllAliveZombies()) {
            Tile tile = getTile(new GridPosition(
                    (int) zombie.currentPosition.x, zombie.lane));
            if (tile != null && tile.isWater) {
                zombies.add(zombie);
            }
        }
        return zombies;
    }
    public List<Zombie> getZombiesAround(GridPosition center, int radius) {
        List<Zombie> zombies = new ArrayList<>();
        for (int row = center.y - radius; row <= center.y + radius; row++) {
            for (Zombie zombie : getZombiesInLane(row)) {
                if (!zombie.isDead()
                        && Math.abs(zombie.currentPosition.x - center.x) <= radius) {
                    zombies.add(zombie);
                }
            }
        }
        return zombies;
    }
    public Zombie getNearestZombieAhead(int x, int row) {
        Zombie nearest = null;
        for (Zombie zombie : getZombiesInLane(row)) {
            if (!zombie.isDead() && zombie.currentPosition.x >= x
                    && (nearest == null
                    || zombie.currentPosition.x < nearest.currentPosition.x)) {
                nearest = zombie;
            }
        }
        return nearest;
    }
    public Zombie getNearestZombieBehind(int x, int row) {
        Zombie nearest = null;
        for (Zombie zombie : getZombiesInLane(row)) {
            if (!zombie.isDead() && zombie.currentPosition.x < x
                    && (nearest == null
                    || zombie.currentPosition.x > nearest.currentPosition.x)) {
                nearest = zombie;
            }
        }
        return nearest;
    }
    public LawnMower getLawnMower(int row) {
        if (row < 0 || row >= lawnMowers.size()) {
            return null;
        }
        return lawnMowers.get(row);
    }
    public boolean isInside(GridPosition position) {
        return position != null && position.x >= 0 && position.x < columns
                && position.y >= 0 && position.y < rows;
    }
    public String printMap() {
        StringBuilder builder = new StringBuilder();
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                Tile tile = tiles[row][column];
                builder.append(symbolFor(tile));
            }
            builder.append(System.lineSeparator());
        }
        return builder.toString();
    }
    private String symbolFor(Tile tile) {
        if (!tile.getZombies().isEmpty() && !tile.getPlants().isEmpty()) {
            return "[PZ]";
        }
        if (!tile.getZombies().isEmpty()) {
            return "[ Z]";
        }
        if (!tile.getPlants().isEmpty()) {
            return "[ P]";
        }
        if (tile.type == TileType.WATER) {
            return "[ W]";
        }
        if (tile.type == TileType.LOW_TIDE) {
            return "[ L]";
        }
        if (tile.type == TileType.NECROMANCY) {
            return "[ N]";
        }
        if (tile.type == TileType.SLIPPERY_UP) {
            return "[ ^]";
        }
        if (tile.type == TileType.SLIPPERY_DOWN) {
            return "[ v]";
        }
        if (tile.type == TileType.TOMBSTONE || tile.obstacle instanceof Tombstone) {
            return "[ T]";
        }
        if (tile.type == TileType.FROZEN_TILE || tile.obstacle instanceof FrozenBlock) {
            return "[ I]";
        }
        if (tile.type == TileType.FROSTBITE_GROUND) {
            return "[ F]";
        }
        if (tile.type == TileType.BEACH_GROUND) {
            return "[ B]";
        }
        if (tile.type == TileType.DARK_GROUND) {
            return "[ D]";
        }
        return "[ E]";
    }
    public int getRows() {
        return rows;
    }
    public int getColumns() {
        return columns;
    }
    public SeasonType getSeasonType() {
        return seasonType;
    }
}
