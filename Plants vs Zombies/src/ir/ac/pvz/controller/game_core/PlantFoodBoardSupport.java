package ir.ac.pvz.controller.game_core;

import ir.ac.pvz.model.others.*;

import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.enums.PlantCategory;
import ir.ac.pvz.model.plants.MintPlant;
import ir.ac.pvz.model.support.Board;
import ir.ac.pvz.model.support.GridPosition;
import ir.ac.pvz.model.support.ProjectileResolver;
import ir.ac.pvz.model.support.Tile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PlantFoodBoardSupport {
    private PlantFoodBoardSupport() {
    }
    public static void applyRepeatedAttacks(
            Plant plant, GameSession session,
            ProjectileResolver resolver, int attacks) {
        for (int index = 0; index < attacks; index++) {
            if (!resolver.resolvePlantAttack(plant, session)) {
                return;
            }
        }
    }
    public static void applySnowPeaFood(
            Plant plant, GameSession session, ProjectileResolver resolver) {
        int freezeTicks = 30;
        if (plant.level >= 3) {
            freezeTicks = 50;
        }
        for (Zombie zombie
                : session.getBoard().getZombiesInLane(plant.location.y)) {
            zombie.freeze(freezeTicks);
        }
        applyRepeatedAttacks(plant, session, resolver, 5);
    }
    public static void cloneMineToEmptyTiles(Plant source, Board board, int count) {
        List<Tile> emptyTiles = collectEmptyTiles(board, false);
        Collections.shuffle(emptyTiles);
        int cloneCount = Math.min(Math.max(0, count), emptyTiles.size());
        for (int index = 0; index < cloneCount; index++) {
            Plant clone = Plant.createSpreadsheetPlant(0, source.type);
            if (clone != null) {
                clone.isBoostedByPlantFood = true;
                emptyTiles.get(index).addPlant(clone);
            }
        }
    }
    public static void cloneLilyPads(Board board, int requestedCount) {
        List<Tile> waterTiles = collectEmptyTiles(board, true);
        Collections.shuffle(waterTiles);
        int cloneCount = Math.min(
                Math.max(0, requestedCount), waterTiles.size());
        for (int index = 0; index < cloneCount; index++) {
            Plant lilyPad = Plant.createSpreadsheetPlant(0, "Lily Pad");
            if (lilyPad != null) {
                waterTiles.get(index).addPlant(lilyPad);
            }
        }
    }
    private static List<Tile> collectEmptyTiles(
            Board board, boolean waterOnly) {
        List<Tile> tiles = new ArrayList<>();
        for (int row = 0; row < board.rows; row++) {
            for (int column = 0; column < board.columns; column++) {
                Tile tile = board.getTile(new GridPosition(column, row));
                boolean eligibleTerrain = tile != null && tile.canPlant;
                if (waterOnly) {
                    eligibleTerrain = tile != null && tile.isWater;
                }
                if (eligibleTerrain && tile.getPlants().isEmpty()) {
                    tiles.add(tile);
                }
            }
        }
        return tiles;
    }
    public static void pullAdjacentZombies(Plant plant, Board board) {
        int firstLane = Math.max(0, plant.location.y - 1);
        int lastLane = Math.min(board.rows - 1, plant.location.y + 1);
        for (int lane = firstLane; lane <= lastLane; lane++) {
            if (lane == plant.location.y) {
                continue;
            }
            for (Zombie zombie
                    : new ArrayList<>(board.getZombiesInLane(lane))) {
                moveToLane(zombie, plant.location.y);
            }
        }
    }
    public static void moveZombieToAdjacentLane(Zombie zombie, Board board) {
        int targetLane = zombie.lane + 1;
        if (zombie.lane > 0) {
            targetLane = zombie.lane - 1;
        }
        if (targetLane >= 0 && targetLane < board.rows) {
            moveToLane(zombie, targetLane);
        }
    }
    private static void moveToLane(Zombie zombie, int lane) {
        zombie.lane = lane;
        zombie.positionY = lane;
        zombie.currentPosition.y = lane;
    }
    public static void addHealth(Plant plant, int amount) {
        plant.baseHp += amount;
        plant.health += amount;
        plant.currentHp += amount;
    }
    public static int dataAmount(Plant plant, int fallback) {
        int value = (int) Math.round(plant.plantFoodValue);
        if (value > 0) {
            return value;
        }
        return fallback;
    }
    public static void applyMint(
            Plant mint, GameSession session, ProjectileResolver resolver,
            PlantFoodStrategy effect) {
        if (!(mint instanceof MintPlant)) {
            return;
        }
        PlantCategory family = ((MintPlant) mint).familyCategory;
        for (int row = 0; row < session.getBoard().rows; row++) {
            for (Plant plant : session.getBoard().getPlantsInLane(row)) {
                if (plant != mint && plant.category == family) {
                    effect.apply(plant, session, resolver);
                }
            }
        }
        if (mint.level >= 4) {
            session.resetFamilyCooldown(family);
        }
        MintPlant mintPlant = (MintPlant) mint;
        mint.setLifeSpanSeconds(mintPlant.durationSeconds);
    }
}
