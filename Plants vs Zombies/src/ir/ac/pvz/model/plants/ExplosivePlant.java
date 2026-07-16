package com.pvz.model.plants;

import com.pvz.game.GameSession;
import com.pvz.model.core.Plant;
import com.pvz.model.core.Zombie;
import com.pvz.model.enums.PlantCategory;
import com.pvz.model.enums.PlantTag;
import com.pvz.model.enums.ProjectileTrajectory;
import com.pvz.model.enums.TileType;
import com.pvz.model.support.BalanceDefaults;
import com.pvz.model.support.Board;
import com.pvz.model.support.FrozenBlock;
import com.pvz.model.support.GridPosition;
import com.pvz.model.support.ProjectileResolver;
import com.pvz.model.support.Tile;
import com.pvz.model.support.Tombstone;

import java.util.ArrayList;
import java.util.List;

public class ExplosivePlant extends Plant {

    public int explosionDamage;
    public float explosionRadius;
    public boolean instantUse;

    private boolean exploded;

    public ExplosivePlant(int id, String name, int cost, int baseHp, float rechargeTime,
                          float actionInterval, int explosionDamage, float explosionRadius,
                          boolean instantUse, PlantTag... tags) {
        super(id, name, cost, baseHp, rechargeTime, actionInterval, explosionDamage,
                PlantCategory.EXPLOSIVE, tags);
        this.explosionDamage = explosionDamage;
        this.explosionRadius = explosionRadius;
        this.instantUse = instantUse;
        this.exploded = false;
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

    public static void resolveInstantPlant(Plant plant, GameSession session,
                                           ProjectileResolver resolver) {
        String type = plant.getNormalizedType();
        if (type.equals("iceshroom")) {
            resolver.freezeAllZombies(session.getBoard(), plant.level >= 2 ? 50 : 30);
            if (plant.level >= 4) {
                session.getBoard().getAllAliveZombies()
                        .forEach(zombie -> zombie.takeDamage(plant.attackPower));
            }
            plant.die();
        } else if (type.equals("hotpotato")) {
            meltFrozenTile(plant, session.getBoard());
        } else if (type.equals("gravebuster")) {
            destroyTombstone(plant, session.getBoard());
        } else if (type.equals("doomshroom")) {
            explodeWholeBoard(plant, session.getBoard(), resolver);
        } else if (type.equals("jalapeno")) {
            explodeLane(plant, session.getBoard());
        } else if (type.equals("grapeshot") && plant instanceof ExplosivePlant) {
            ((ExplosivePlant) plant).explode(session.getBoard(), plant.location);
            session.launchGrapeshot(plant);
        } else if (plant instanceof ExplosivePlant) {
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
            } else if (type.equals("squash")) {
                crushWithSquash(plant, session.getBoard());
            } else if (type.equals("tanglekelp") && tile.isWater) {
                zombie.lastDamageSource = plant;
                zombie.receiveInstantKill(ProjectileTrajectory.STRAIGHT);
                plant.die();
            } else if (type.equals("iceberglettuce")) {
                zombie.freeze(plant.level >= 3 ? 50 : 30);
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
        } else if (type.equals("iceberglettuce")) {
            nearby.get(0).freeze(plant.level >= 3 ? 50 : 30);
            plant.die();
        } else if (type.equals("tanglekelp")) {
            drownFirstWaterZombie(plant, nearby, board);
        } else if (type.equals("potatomine") || type.equals("primalpotatomine")) {
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
        int crushCount = plant.level >= 4 ? 2 : 1;
        for (int i = 0; i < crushCount && i < targets.size(); i++) {
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
        int radius = plant.getNormalizedType().equals("primalpotatomine") ? 1 : 0;
        explodeArea(plant, plant.location, plant.attackPower, radius, board);
        plant.die();
    }

    private static float mineArmTime(Plant plant) {
        boolean primal = plant.getNormalizedType().equals("primalpotatomine");
        float base = primal ? 5f : 15f;
        if (plant.level >= 2) {
            base -= primal ? 1f : 3f;
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
        int radius = plant.level >= 3 ? 1 : 0;
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
