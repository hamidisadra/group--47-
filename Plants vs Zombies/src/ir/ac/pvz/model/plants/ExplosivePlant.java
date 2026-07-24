package ir.ac.pvz.model.plants;

import ir.ac.pvz.model.others.GameSession;
import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.enums.PlantCategory;
import ir.ac.pvz.model.enums.PlantTag;
import ir.ac.pvz.model.enums.ProjectileTrajectory;
import ir.ac.pvz.model.enums.TileType;
import ir.ac.pvz.model.support.BalanceDefaults;
import ir.ac.pvz.model.support.Board;
import ir.ac.pvz.model.support.FrozenBlock;
import ir.ac.pvz.model.support.GridPosition;
import ir.ac.pvz.model.support.ProjectileResolver;
import ir.ac.pvz.model.support.Tile;
import ir.ac.pvz.model.support.Tombstone;
import java.util.ArrayList;
import java.util.List;

public class ExplosivePlant extends Plant {
    public int explosionDamage;
    public float explosionRadius;
    public boolean instantUse;
    private boolean exploded;
    private float graveEatingSeconds;
    private float graveEatingElapsedSeconds;
    public ExplosivePlant(int id, String name, int cost, int baseHp, float rechargeTime,
                          float actionInterval, int explosionDamage, float explosionRadius,
                          boolean instantUse, PlantTag... tags) {
        super(id, name, cost, baseHp, rechargeTime, actionInterval, explosionDamage,
                PlantCategory.EXPLOSIVE, tags);
        this.explosionDamage = explosionDamage;
        this.explosionRadius = explosionRadius;
        this.instantUse = instantUse;
        this.exploded = false;
        this.graveEatingSeconds = 0f;
        if (name.replace(" ", "").equalsIgnoreCase("GraveBuster")) {
            this.graveEatingSeconds = 4f;
        }
        this.graveEatingElapsedSeconds = 0f;
    }
    @Override
    public void onTick() {
        super.onTick();
        if (getNormalizedType().equals("gravebuster")) {
            graveEatingElapsedSeconds += 0.1f;
        }
    }
    public boolean isGraveEatingFinished() {
        return getNormalizedType().equals("gravebuster")
                && graveEatingElapsedSeconds + 0.0001f >= graveEatingSeconds;
    }
    public void reduceGraveEatingTime(float seconds) {
        graveEatingSeconds = Math.max(0f, graveEatingSeconds - seconds);
    }
    public float getGraveEatingSeconds() {
        return graveEatingSeconds;
    }
    public void explode(Board board, GridPosition center) {
        if (exploded || board == null || center == null) {
            return;
        }
        exploded = true;
        int radius = Math.max(0, Math.round(explosionRadius));
        explodeArea(this, center, explosionDamage, radius, board);
        die();
    }
    public static void resolveInstantPlant(Plant plant, GameSession session, ProjectileResolver resolver) {
        String type = plant.getNormalizedType();
        if (type.equals("iceshroom")) {
            int freezeTicks = 30;
            if (plant.level >= 2) {
                freezeTicks = 50;
            }
            resolver.freezeAllZombies(session.getBoard(), freezeTicks);
            if (plant.level >= 4) {
                session.getBoard().getAllAliveZombies()
                        .forEach(zombie -> zombie.takeDamage(plant.attackPower));
            }
            plant.die();
        }
        else if (type.equals("hotpotato")) {
            meltFrozenTile(plant, session.getBoard());
        }
        else if (type.equals("gravebuster")) {
            destroyTombstone(plant, session.getBoard());
        }
        else if (type.equals("doomshroom")) {
            explodeWholeBoard(plant, session.getBoard(), resolver);
        }
        else if (type.equals("jalapeno")) {
            explodeLane(plant, session.getBoard());
        }
        else if (type.equals("grapeshot") && plant instanceof ExplosivePlant) {
            ((ExplosivePlant) plant).explode(session.getBoard(), plant.location);
            session.launchGrapeshot(plant);
        }
        else if (plant instanceof ExplosivePlant) {
            ((ExplosivePlant) plant).explode(session.getBoard(), plant.location);
        }
    }
    public static void resolveTrapContact(Tile tile, Zombie zombie,
                                          GameSession session,
                                          ProjectileResolver resolver) {
        if (tile == null || zombie == null || zombie.isDead()) {
            return;
        }
        for (Plant plant : new ArrayList<>(tile.getPlants())) {
            String type = plant.getNormalizedType();
            if (type.equals("potatomine") || type.equals("primalpotatomine")) {
                triggerMine(plant, session.getBoard());
            }
            else if (type.equals("squash")) {
                crushWithSquash(plant, session.getBoard());
            }
            else if (type.equals("tanglekelp") && tile.isWater) {
                zombie.lastDamageSource = plant;
                zombie.receiveInstantKill(ProjectileTrajectory.STRAIGHT);
                plant.die();
            }
            else if (type.equals("iceberglettuce")) {
                int freezeTicks = 30;
                if (plant.level >= 3) {
                    freezeTicks = 50;
                }
                zombie.freeze(freezeTicks);
                plant.die();
            }
        }
    }
    public static void resolveNearbyTrap(Plant plant, GameSession session,
                                         ProjectileResolver resolver) {
        Board board = session.getBoard();
        String type = plant.getNormalizedType();
        List<Zombie> nearby = board.getZombiesAround(plant.location, 1);
        if (nearby.isEmpty()) {
            return;
        }
        if (type.equals("squash")) {
            crushWithSquash(plant, board);
        }
        else if (type.equals("iceberglettuce")) {
            int freezeTicks = 30;
            if (plant.level >= 3) {
                freezeTicks = 50;
            }
            nearby.get(0).freeze(freezeTicks);
            plant.die();
        }
        else if (type.equals("tanglekelp")) {
            drownFirstWaterZombie(plant, nearby, board);
        }
        else if (type.equals("potatomine") || type.equals("primalpotatomine")) {
            triggerMine(plant, board);
        }
    }
    public static void explodeArea(GridPosition center, int damage,
                                   int radius, Board board) {
        explodeArea(null, center, damage, radius, board);
    }
    public static void explodeArea(Plant source, GridPosition center, int damage,
                                   int radius, Board board) {
        for (int y = center.y - radius; y <= center.y + radius; y++) {
            for (int x = center.x - radius; x <= center.x + radius; x++) {
                Tile tile = board.getTile(new GridPosition(x, y));
                if (tile != null) {
                    for (Zombie zombie : new ArrayList<>(tile.getZombies())) {
                        zombie.lastDamageSource = source;
                        zombie.takeDamage(damage);
                    }
                }
            }
        }
    }
    private static void drownFirstWaterZombie(Plant plant,
                                              List<Zombie> nearby,
                                              Board board) {
        for (Zombie zombie : nearby) {
            Tile tile = board.getTile(new GridPosition(
                    (int) zombie.currentPosition.x, zombie.lane));
            if (tile != null && tile.isWater) {
                zombie.lastDamageSource = plant;
                zombie.receiveInstantKill(ProjectileTrajectory.STRAIGHT);
                plant.die();
                return;
            }
        }
    }
    private static void crushWithSquash(Plant plant, Board board) {
        List<Zombie> targets = board.getZombiesAround(plant.location, 1);
        int crushCount = 1;
        if (plant.level >= 4) {
            crushCount = 2;
        }
        int targetCount = Math.min(Math.max(0, crushCount), targets.size());
        for (int i = 0; i < targetCount; i++) {
            targets.get(i).lastDamageSource = plant;
            targets.get(i).receiveInstantKill(ProjectileTrajectory.STRAIGHT);
        }
        plant.die();
    }
    private static void triggerMine(Plant plant, Board board) {
        if (!plant.isBoostedByPlantFood
                && plant.getAgeSeconds() + 0.0001f < mineArmTime(plant)) {
            return;
        }
        int radius = 0;
        if (plant.getNormalizedType().equals("primalpotatomine")) {
            radius = 1;
        }
        explodeArea(plant, plant.location, plant.attackPower, radius, board);
        plant.die();
    }
    private static float mineArmTime(Plant plant) {
        boolean primal = plant.getNormalizedType().equals("primalpotatomine");
        float base = 15f;
        if (primal) {
            base = 5f;
        }
        if (plant.level >= 2) {
            if (primal) {
                base -= 1f;
            }
            else {
                base -= 3f;
            }
        }
        return Math.max(0f, base);
    }
    private static void explodeWholeBoard(Plant plant, Board board,
                                          ProjectileResolver resolver) {
        for (Zombie zombie : board.getAllAliveZombies()) {
            zombie.lastDamageSource = plant;
            zombie.takeDamage(plant.attackPower);
        }
        Tile tile = board.getTile(plant.location);
        if (tile != null) {
            tile.canPlant = false;
        }
        plant.die();
    }
    private static void explodeLane(Plant plant, Board board) {
        for (Zombie zombie : board.getZombiesInLane(plant.location.y)) {
            zombie.lastDamageSource = plant;
            zombie.takeDamage(plant.attackPower);
            zombie.melt();
        }
        meltLaneIce(plant.location.y, board);
        plant.die();
    }
    private static void meltLaneIce(int row, Board board) {
        for (int x = 0; x < board.columns; x++) {
            Tile tile = board.getTile(new GridPosition(x, row));
            if (tile != null && tile.obstacle instanceof FrozenBlock) {
                tile.obstacle.destroy();
                board.clearDestroyedObstacle(tile);
            }
        }
    }
    private static void meltFrozenTile(Plant plant, Board board) {
        int radius = 0;
        if (plant.level >= 3) {
            radius = 1;
        }
        for (int y = plant.location.y - radius; y <= plant.location.y + radius; y++) {
            for (int x = plant.location.x - radius; x <= plant.location.x + radius; x++) {
                meltTile(board.getTile(new GridPosition(x, y)));
            }
        }
        if (plant.level >= 4) {
            explodeArea(plant, plant.location,
                    BalanceDefaults.HOT_POTATO_FINISH_DAMAGE, 1, board);
        }
        plant.die();
    }
    private static void meltTile(Tile tile) {
        if (tile != null && tile.obstacle instanceof FrozenBlock) {
            tile.obstacle.destroy();
            tile.obstacle = null;
            tile.restoreNativeGround();
        }
    }
    private static void destroyTombstone(Plant plant, Board board) {
        Tile tile = board.getTile(plant.location);
        if (tile != null && tile.obstacle instanceof Tombstone) {
            tile.obstacle.destroy();
            ((Tombstone) tile.obstacle).turnIntoNormalGround(tile);
        }
        if (plant.level >= 4) {
            explodeArea(plant, plant.location,
                    BalanceDefaults.GRAVE_BUSTER_FINISH_DAMAGE, 1, board);
        }
        plant.die();
    }
}
