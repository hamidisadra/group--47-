package ir.ac.pvz.controller.game_core;

import ir.ac.pvz.model.others.*;

import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.support.ArmorPiece;
import ir.ac.pvz.model.support.Board;
import ir.ac.pvz.model.support.GridPosition;
import ir.ac.pvz.model.support.LawnMower;
import ir.ac.pvz.model.support.PlantStatusView;
import ir.ac.pvz.model.support.Tile;
import ir.ac.pvz.model.support.ZombieEffect;
import java.util.ArrayList;
import java.util.List;

public class GamePrinter {
    public String showMap(GameSession session) {
        StringBuilder builder = new StringBuilder();
        builder.append("wave: ").append(session.getCurrentWaveNumber())
                .append(System.lineSeparator());
        builder.append("plant foods: ").append(session.getPlantFoodCount())
                .append(System.lineSeparator());
        builder.append("suns: ").append(session.getCurrentSunAmount())
                .append(System.lineSeparator());
        for (LawnMower mower : session.getLawnMowers()) {
            builder.append("lawn mower row ").append(mower.relatedRow + 1)
                    .append(": ").append(mowerStatus(mower))
                    .append(System.lineSeparator());
        }
        Board board = session.getBoard();
        builder.append(board.printMap());
        appendTerrainDetails(builder, board);
        appendZombiePositions(builder, board);
        return builder.toString();
    }
    private void appendTerrainDetails(StringBuilder builder, Board board) {
        builder.append("terrain details:").append(System.lineSeparator());
        for (int row = 0; row < board.rows; row++) {
            for (int column = 0; column < board.columns; column++) {
                Tile tile = board.getTile(new GridPosition(column, row));
                builder.append("    ").append(tile.position.toUserString())
                        .append(": ").append(tile.type)
                        .append(System.lineSeparator());
            }
        }
    }
    private void appendZombiePositions(StringBuilder builder, Board board) {
        builder.append("zombie positions:").append(System.lineSeparator());
        for (Zombie zombie : board.getAllAliveZombies()) {
            builder.append("    ").append(zombie.getType()).append(": (")
                    .append(zombie.currentPosition.x + 1f).append(", ")
                    .append(zombie.lane + 1).append(')')
                    .append(System.lineSeparator());
        }
    }
    private String mowerStatus(LawnMower mower) {
        if (mower.activated) {
            return "used";
        }
        return "available";
    }
    public List<PlantStatusView> showPlantsStatus(GameSession session) {
        List<PlantStatusView> statusViews = new ArrayList<>();
        for (Plant plant : session.getPlantCatalog()) {
            float cooldown = session.getCooldown(plant.type);
            boolean canPlant = cooldown <= 0f
                    && session.getCurrentSunAmount() >= plant.sunCost
                    && hasPlantableTile(session.getBoard(), plant);
            statusViews.add(new PlantStatusView(plant.type, plant.sunCost,
                    canPlant, cooldown));
        }
        return statusViews;
    }
    private boolean hasPlantableTile(Board board, Plant plant) {
        for (int row = 0; row < board.rows; row++) {
            for (int column = 0; column < board.columns; column++) {
                Tile tile = board.getTile(new GridPosition(column, row));
                if (plant.canPlantOn(tile)) {
                    return true;
                }
            }
        }
        return false;
    }
    public String showTileStatus(Board board, GridPosition position) {
        if (board == null || position == null || !board.isInside(position)) {
            return "";
        }
        return board.getTile(position).getStatus();
    }
    public String zombiesInfo(Board board) {
        if (board == null) {
            return "No zombies on the board.";
        }
        StringBuilder builder = new StringBuilder();
        for (int row = 0; row < board.rows; row++) {
            for (Zombie zombie : board.getZombiesInLane(row)) {
                appendZombie(builder, zombie);
            }
        }
        if (builder.length() == 0) {
            return "No zombies on the board.";
        }
        return builder.toString();
    }
    private void appendZombie(StringBuilder builder, Zombie zombie) {
        builder.append(zombie.getType()).append(':')
                .append(System.lineSeparator());
        builder.append("    position: ").append(zombie.currentPosition.x + 1f)
                .append(", ").append(zombie.lane + 1)
                .append(System.lineSeparator());
        builder.append("    health: ").append(zombie.currentHealth)
                .append(System.lineSeparator());
        builder.append("    armor:").append(System.lineSeparator());
        for (ArmorPiece piece : zombie.armorPieces) {
            builder.append("        ").append(piece.name).append(": ")
                    .append(piece.health).append(System.lineSeparator());
        }
        builder.append("    effects:").append(System.lineSeparator());
        for (ZombieEffect effect : zombie.effects) {
            builder.append("        ").append(effect.type.name().toLowerCase())
                    .append(": ").append(effect.remainingSeconds).append('s')
                    .append(System.lineSeparator());
        }
    }
}
