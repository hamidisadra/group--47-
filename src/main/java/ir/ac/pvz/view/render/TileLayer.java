package ir.ac.pvz.view.render;

import ir.ac.pvz.model.support.Board;
import ir.ac.pvz.model.enums.SeasonType;
import ir.ac.pvz.model.support.Tile;
import ir.ac.pvz.view.assets.GameAssets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;

final class TileLayer {
    private static final float PLANT_SCALE = 0.40f;
    private static final float PLANT_GROUND = 0.30f;

    private final GameAssets assets;
    private final Lawn lawn;

    TileLayer(GameAssets assets, Lawn lawn) {
        this.assets = assets;
        this.lawn = lawn;
    }

    void drawGrid(ShapeRenderer shapes, Matrix4 projection, Board board) {
        shapes.setProjectionMatrix(projection);
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(Color.RED);
        for (int column = 0; column <= board.columns; column++) {
            float x = Lawn.GRID_LEFT + column * Lawn.CELL_WIDTH;
            shapes.line(x, Lawn.GRID_BOTTOM, x, Lawn.GRID_BOTTOM + board.rows * Lawn.CELL_HEIGHT);
        }

        for (int row = 0; row <= board.rows; row++) {
            float y = Lawn.GRID_BOTTOM + row * Lawn.CELL_HEIGHT;
            shapes.line(Lawn.GRID_LEFT, y, Lawn.GRID_LEFT + board.columns * Lawn.CELL_WIDTH, y);
        }

        shapes.end();
    }

    void drawTiles(Batch batch, Board board, float time) {
        com.badlogic.gdx.graphics.g2d.TextureRegion pixel =
            ((com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable)
                ir.ac.pvz.view.ui.Ui.solid(Color.WHITE)).getRegion();
        for (int row = 0; row < board.rows; row++) {
            for (int column = 0; column < board.columns; column++) {
                Tile tile = board.getTile(new ir.ac.pvz.model.support.GridPosition(column, row));
                if (tile == null || tile.type == null) {
                    continue;
                }

                float x = Lawn.GRID_LEFT + column * Lawn.CELL_WIDTH;
                float y = Lawn.rowBottomY(row, board.rows);
                Color overlay = tileOverlay(tile.type);
                if (overlay != null) {
                    batch.setColor(overlay);
                    batch.draw(pixel, x, y, Lawn.CELL_WIDTH, Lawn.CELL_HEIGHT);
                    batch.setColor(Color.WHITE);
                }

                drawSlipperyArrow(batch, pixel, tile, x, y);

                if (tile.obstacle instanceof ir.ac.pvz.model.support.Tombstone) {
                    String grave = assets.gravestonePath(board.seasonType
                        == SeasonType.DARK_AGES ? "DARK_NOOP" : "EGYPT_HIEROGLYPH");
                    if (grave != null) {
                        String clip = assets.pickClip(grave,
                            gravestoneClip(
                                (ir.ac.pvz.model.support.Tombstone) tile.obstacle),
                            "undamaged");
                        assets.draw(batch, grave, clip, time,
                            Lawn.columnCenterX(column),
                            y + Lawn.CELL_HEIGHT * PLANT_GROUND,
                            PLANT_SCALE, true);
                    }
                }
            }
        }
    }

    void drawSlipperyArrow(Batch batch,
                                   com.badlogic.gdx.graphics.g2d.TextureRegion pixel,
                                   Tile tile, float x, float y) {
        boolean up = tile.type == ir.ac.pvz.model.enums.TileType.SLIPPERY_UP;
        boolean down = tile.type == ir.ac.pvz.model.enums.TileType.SLIPPERY_DOWN;

        if (!up && !down) {
            return;
        }

        float centerX = x + Lawn.CELL_WIDTH * 0.5f;
        float baseY = y + Lawn.CELL_HEIGHT * (up ? 0.30f : 0.62f);
        float step = Lawn.CELL_HEIGHT * (up ? 0.09f : -0.09f);
        batch.setColor(0.15f, 0.45f, 0.7f, 0.75f);

        for (int band = 0; band < 4; band++) {
            float width = Lawn.CELL_WIDTH * (0.34f - band * 0.07f);
            batch.draw(pixel, centerX - width * 0.5f, baseY + band * step,
                width, Lawn.CELL_HEIGHT * 0.055f);
        }

        batch.setColor(Color.WHITE);
    }

    String gravestoneClip(ir.ac.pvz.model.support.Tombstone tombstone) {
        if (tombstone.initialHealth <= 0) {
            return "undamaged";
        }

        float ratio = tombstone.getHealth() / (float) tombstone.initialHealth;

        if (ratio > 0.66f) {
            return "undamaged";
        }

        if (ratio > 0.33f) {
            return "damaged1";
        }

        return "damaged2";
    }

    Color tileOverlay(ir.ac.pvz.model.enums.TileType type) {
        switch (type.name()) {
            case "WATER":
                return new Color(0.25f, 0.55f, 0.95f, 0.45f);
            case "LOW_TIDE":
                return new Color(0.35f, 0.75f, 0.95f, 0.30f);
            case "FROZEN_TILE":
                return new Color(0.75f, 0.92f, 1f, 0.50f);
            case "SLIPPERY_UP":
                return new Color(0.62f, 0.94f, 0.85f, 0.38f);

            case "SLIPPERY_DOWN":
                return new Color(0.70f, 0.82f, 1f, 0.38f);
            case "NECROMANCY":
                return new Color(0.55f, 0.25f, 0.75f, 0.35f);
            default:
                return null;
        }
    }

}
