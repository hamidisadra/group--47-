package com.pvz.game;

import com.pvz.model.core.Plant;
import com.pvz.model.core.Zombie;
import com.pvz.model.support.ArmorPiece;
import com.pvz.model.support.Board;
import com.pvz.model.support.GridPosition;
import com.pvz.model.support.LawnMower;
import com.pvz.model.support.PlantStatusView;
import com.pvz.model.support.ZombieEffect;

import java.util.ArrayList;
import java.util.List;

public class GamePrinter {

    public String showMap(GameSession session) {
        StringBuilder builder = new StringBuilder();
        builder.append("wave: ").append(session.currentWaveNumber)
                .append(System.lineSeparator());
        builder.append("plant foods: ").append(session.plantFoodCount)
                .append(System.lineSeparator());
        builder.append("suns: ").append(session.currentSunAmount)
                .append(System.lineSeparator());
        for (LawnMower mower : session.getLawnMowers()) {
            builder.append("lawn mower row ").append(mower.relatedRow + 1)
                    .append(": ").append(mower.activated ? "used" : "available")
                    .append(System.lineSeparator());
        }
        builder.append(session.getBoard().printMap());
        return builder.toString();
    }

    public List<PlantStatusView> showPlantsStatus(GameSession session) {
        List<PlantStatusView> statusViews = new ArrayList<>();
        for (Plant plant : session.getPlantCatalog()) {
            float cooldown = session.getCooldown(plant.type);
            statusViews.add(new PlantStatusView(plant.type, plant.sunCost,
                    cooldown <= 0f && session.currentSunAmount >= plant.sunCost,
                    cooldown));
        }
        return statusViews;
    }

    public String showTileStatus(Board board, GridPosition position) {
        if (board == null || !board.isInside(position)) {
            return "";
        }
        return board.getTile(position).getStatus();
    }

    public String zombiesInfo(Board board) {
        StringBuilder builder = new StringBuilder();
        for (int row = 0; row < board.rows; row++) {
            for (Zombie zombie : board.getZombiesInLane(row)) {
                appendZombie(builder, zombie);
            }
        }
        return builder.toString();
    }

    private void appendZombie(StringBuilder builder, Zombie zombie) {
        builder.append(zombie.getClass().getSimpleName()).append(':')
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
