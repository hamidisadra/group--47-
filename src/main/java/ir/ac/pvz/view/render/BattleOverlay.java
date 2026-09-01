package ir.ac.pvz.view.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import ir.ac.pvz.controller.gui.ChapterEffectsController;
import ir.ac.pvz.controller.gui.SessionController;
import ir.ac.pvz.controller.gui.ZombossController;
import ir.ac.pvz.model.support.GridPosition;
import ir.ac.pvz.view.assets.GameAssets;
import ir.ac.pvz.view.ui.Ui;

public class BattleOverlay {
    private final SpriteBatch batch;
    private final SessionController controller;
    private final GameAssets assets;
    private final BoardRenderer renderer;

    private float animationTime;
    private float pointerX;
    private float pointerY;
    private String armedPlant;
    private boolean shovelMode;
    private boolean plantFoodMode;

    public BattleOverlay(SpriteBatch batch, SessionController controller,
                         GameAssets assets, BoardRenderer renderer) {
        this.batch = batch;
        this.controller = controller;
        this.assets = assets;
        this.renderer = renderer;
    }

    public void sync(float animationTime, float pointerX, float pointerY,
                     String armedPlant, boolean shovelMode, boolean plantFoodMode) {
        this.animationTime = animationTime;
        this.pointerX = pointerX;
        this.pointerY = pointerY;
        this.armedPlant = armedPlant;
        this.shovelMode = shovelMode;
        this.plantFoodMode = plantFoodMode;
    }

    public void drawVases() {
        ir.ac.pvz.controller.gui.MinigameController minigames =
            controller.getMinigameController();
        if (minigames == null || !minigames.isVasebreaker()) {
            return;
        }

        int rows = controller.getBoard().rows;
        for (ir.ac.pvz.model.minigame.Vase vase : minigames.getVases()) {
            if (vase.isBroken()) {
                continue;
            }

            ir.ac.pvz.view.assets.AnimationIndex.Entry entry =
                assets.index().find(minigames.vaseAnimation(vase), "VASEBREAKER");
            if (entry == null) {
                continue;
            }

            assets.draw(batch, entry.path, "idle", animationTime,
                Lawn.columnCenterX(vase.getX() - 1),
                renderer.groundY(entry.path, "idle", 0.30f, vase.getY() - 1, rows),
                0.30f, true);
        }
    }

    public void drawMinigameExtras() {
        ir.ac.pvz.controller.gui.MinigameController minigames =
            controller.getMinigameController();
        if (minigames == null) {
            return;
        }

        if (minigames.isBowling()) {
            drawBowlingNuts(minigames);
        }

        if (minigames.isIZombie()) {
            drawBrains((ir.ac.pvz.model.minigame.IZombie) minigames.getGame());
        }
    }

    private com.badlogic.gdx.graphics.g2d.TextureRegion whitePixel() {
        return ((com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable)
            Ui.solid(Color.WHITE)).getRegion();
    }

    private void drawVerticalLine(float x, float alpha, Color color) {
        batch.setColor(color.r, color.g, color.b, alpha);
        batch.draw(whitePixel(), x - 3f, Lawn.GRID_BOTTOM, 6f,
            controller.getBoard().rows * Lawn.CELL_HEIGHT);
        batch.setColor(Color.WHITE);
    }

    private void drawBowlingNuts(
        ir.ac.pvz.controller.gui.MinigameController minigames) {
        int rows = controller.getBoard().rows;
        String path = assets.plantPath("Wall-nut");

        for (ir.ac.pvz.model.minigame.BowlingNut nut : minigames.getNuts()) {
            if (!nut.isAlive() || nut.getRow() < 0 || nut.getRow() >= rows) {
                continue;
            }

            assets.draw(batch, path, "idle", animationTime,
                Lawn.columnCenterX((float) nut.getCol()),
                Lawn.rowCenterY(nut.getRow(), rows), 0.32f, true);
        }

        drawVerticalLine(Lawn.GRID_LEFT
            + (minigames.getRedLineColumn() + 1) * Lawn.CELL_WIDTH, 0.7f,
            new Color(1f, 0.2f, 0.2f, 1f));
    }

    private void drawBrains(ir.ac.pvz.model.minigame.IZombie izombie) {
        int rows = controller.getBoard().rows;
        drawVerticalLine(Lawn.GRID_LEFT
            + SessionController.IZOMBIE_PLANT_COLUMNS * Lawn.CELL_WIDTH, 0.7f,
            new Color(1f, 0.2f, 0.2f, 1f));

        for (ir.ac.pvz.model.minigame.Brain brain : izombie.getBrains()) {
            drawBrain(brain, rows);
        }

        batch.setColor(Color.WHITE);
    }

    private void drawBrain(ir.ac.pvz.model.minigame.Brain brain, int rows) {
        int row = brain.getRow() - 1;
        if (row < 0 || row >= rows) {
            return;
        }

        com.badlogic.gdx.graphics.g2d.TextureRegion pixel = whitePixel();
        float brainX = Lawn.GRID_LEFT - Lawn.CELL_WIDTH * 0.9f;
        float brainY = Lawn.rowBottomY(row, rows) + Lawn.CELL_HEIGHT * 0.25f;
        float brainWidth = Lawn.CELL_WIDTH * 0.6f;
        float brainHeight = Lawn.CELL_HEIGHT * 0.5f;

        if (brain.isEaten()) {
            batch.setColor(0.28f, 0.28f, 0.28f, 0.45f);
            batch.draw(pixel, brainX, brainY, brainWidth, brainHeight);
            return;
        }

        batch.setColor(0.35f, 0.12f, 0.18f, 0.9f);
        batch.draw(pixel, brainX - 3f, brainY - 3f,
            brainWidth + 6f, brainHeight + 6f);
        batch.setColor(0.95f, 0.55f, 0.65f, 1f);
        batch.draw(pixel, brainX, brainY, brainWidth, brainHeight);
        batch.setColor(1f, 0.78f, 0.84f, 1f);
        batch.draw(pixel, brainX + brainWidth * 0.15f,
            brainY + brainHeight * 0.55f,
            brainWidth * 0.7f, brainHeight * 0.25f);
    }

    public void drawStageMarkers() {
        drawDeadline();
        drawWaterBoundary();
        drawProtectedTiles();
        drawChapterMarkers();
        drawBurningTiles();
        batch.setColor(Color.WHITE);
    }

    private void drawDeadline() {
        int deadline = controller.deadlineColumn();
        if (deadline >= 0) {
            drawVerticalLine(Lawn.GRID_LEFT + (deadline + 1) * Lawn.CELL_WIDTH,
                0.75f, new Color(1f, 0.2f, 0.2f, 1f));
        }
    }

    private void drawWaterBoundary() {
        int waterEdge = controller.waterBoundaryColumn();
        if (waterEdge < 0 || waterEdge > controller.getBoard().columns) {
            return;
        }

        batch.setColor(0.35f, 0.8f, 1f, 0.8f);
        batch.draw(whitePixel(),
            Lawn.GRID_LEFT + waterEdge * Lawn.CELL_WIDTH - 2f,
            Lawn.GRID_BOTTOM, 4f, controller.getBoard().rows * Lawn.CELL_HEIGHT);
        batch.setColor(Color.WHITE);
    }

    private void drawProtectedTiles() {
        com.badlogic.gdx.graphics.g2d.TextureRegion pixel = whitePixel();

        for (GridPosition position : controller.getProtectedTiles()) {
            batch.setColor(1f, 0.35f, 0.25f, 0.4f);
            batch.draw(pixel, Lawn.GRID_LEFT,
                Lawn.rowBottomY(position.y, controller.getBoard().rows),
                controller.getBoard().columns * Lawn.CELL_WIDTH, 4f);
            batch.setColor(1f, 0.45f, 0.3f, 0.45f);
            batch.draw(pixel, Lawn.GRID_LEFT + position.x * Lawn.CELL_WIDTH,
                Lawn.rowBottomY(position.y, controller.getBoard().rows),
                Lawn.CELL_WIDTH, Lawn.CELL_HEIGHT);
        }
    }

    private static final String TORNADO_REAR = "SANDSTORM_REAR";
    private static final String TORNADO_TOP = "SANDSTORM_TOP";

    private void drawChapterMarkers() {
        com.badlogic.gdx.graphics.g2d.TextureRegion pixel = whitePixel();

        for (ChapterEffectsController.Marker marker
            : controller.getChapterEffects().getMarkers()) {
            if ("tornado".equals(marker.kind) && drawTornado(marker)) {
                continue;
            }

            batch.setColor(markerColor(marker));
            batch.draw(pixel, Lawn.GRID_LEFT + marker.column * Lawn.CELL_WIDTH,
                Lawn.rowBottomY(marker.row, controller.getBoard().rows),
                Lawn.CELL_WIDTH, Lawn.CELL_HEIGHT);
        }

        batch.setColor(Color.WHITE);
    }

    private boolean drawTornado(ChapterEffectsController.Marker marker) {
        String rear = assets.effectPath(TORNADO_REAR);
        String top = assets.effectPath(TORNADO_TOP);
        if (rear == null && top == null) {
            return false;
        }

        int rows = controller.getBoard().rows;
        float x = Lawn.columnCenterX(marker.column);
        float y = Lawn.rowBottomY(marker.row, rows)
            + Lawn.CELL_HEIGHT * 0.15f;
        float fade = Math.min(1f, Math.max(0.15f, marker.remaining));
        float swirl = animationTime * 2.4f;
        float sway = (float) Math.sin(swirl) * Lawn.CELL_WIDTH * 0.12f;
        float scale = 0.55f + 0.05f * (float) Math.sin(swirl * 1.7f);

        batch.setColor(1f, 1f, 1f, fade);

        if (rear != null) {
            String clip = assets.pickClip(rear, "idle", "anim_idle");
            assets.draw(batch, rear, clip, animationTime, x + sway, y,
                scale, true);
        }

        if (top != null) {
            String clip = assets.pickClip(top, "idle", "anim_idle");
            assets.draw(batch, top, clip, animationTime, x - sway * 0.6f,
                y + Lawn.CELL_HEIGHT * 0.55f, scale * 0.9f, true);
        }

        batch.setColor(Color.WHITE);
        return true;
    }

    private Color markerColor(ChapterEffectsController.Marker marker) {
        if (marker.kind.equals("tornado")) {
            return new Color(0.85f, 0.85f, 0.95f, Math.min(0.6f, marker.remaining));
        }

        if (marker.kind.equals("grave")) {
            return new Color(0.55f, 0.5f, 0.5f, Math.min(0.5f, marker.remaining * 0.3f));
        }

        if (marker.kind.equals("surface")) {
            return new Color(0.7f, 0.35f, 0.75f, Math.min(0.55f, marker.remaining * 0.4f));
        }

        return new Color(0.65f, 0.9f, 1f, Math.min(0.45f, marker.remaining * 0.3f));
    }

    private void drawBurningTiles() {
        ZombossController zomboss = controller.getZomboss();
        if (zomboss == null) {
            return;
        }

        for (ZombossController.FireTile fire : zomboss.getBurningTiles()) {
            batch.setColor(1f, 0.4f, 0.1f, 0.5f);
            batch.draw(whitePixel(),
                Lawn.GRID_LEFT + fire.position.x * Lawn.CELL_WIDTH,
                Lawn.rowBottomY(fire.position.y, controller.getBoard().rows),
                Lawn.CELL_WIDTH, Lawn.CELL_HEIGHT);
        }
    }

    public void drawCursorPreview(GridPosition cell) {
        if (cell == null) {
            return;
        }

        com.badlogic.gdx.graphics.g2d.TextureRegion pixel =
            ((com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable)
                Ui.solid(Color.WHITE)).getRegion();
        if (armedPlant != null || shovelMode || plantFoodMode) {
            Color tint = cursorTint(cell);
            batch.setColor(tint.r, tint.g, tint.b, tint.a);
            batch.draw(pixel, Lawn.GRID_LEFT + cell.x * Lawn.CELL_WIDTH,
                Lawn.rowBottomY(cell.y, controller.getBoard().rows),
                Lawn.CELL_WIDTH, Lawn.CELL_HEIGHT);
            batch.setColor(Color.WHITE);
        }

        if (armedPlant != null) {
            String path = assets.plantPath(armedPlant);

            if (path != null) {
                batch.setColor(1f, 1f, 1f, 0.75f);
                assets.draw(batch, path, assets.pickClip(path, "idle"), animationTime,
                    pointerX, pointerY, 0.5f, true);
                batch.setColor(Color.WHITE);
            }
        }

        drawToolCursor(pixel);
    }

    private void drawToolCursor(com.badlogic.gdx.graphics.g2d.TextureRegion pixel) {
        if (!shovelMode && !plantFoodMode) {
            return;
        }

        float size = Lawn.CELL_WIDTH * 0.32f;
        if (shovelMode) {
            batch.setColor(0.55f, 0.38f, 0.2f, 0.95f);
            batch.draw(pixel, pointerX - size * 0.12f, pointerY,
                size * 0.24f, size);
            batch.setColor(0.78f, 0.78f, 0.82f, 0.95f);
            batch.draw(pixel, pointerX - size * 0.3f, pointerY - size * 0.45f,
                size * 0.6f, size * 0.5f);
        }

        else {
            batch.setColor(0.3f, 0.9f, 0.35f, 0.95f);
            batch.draw(pixel, pointerX - size * 0.35f, pointerY - size * 0.2f,
                size * 0.7f, size * 0.45f);
            batch.setColor(0.18f, 0.6f, 0.22f, 0.95f);
            batch.draw(pixel, pointerX - size * 0.05f, pointerY - size * 0.2f,
                size * 0.1f, size * 0.6f);
        }

        batch.setColor(Color.WHITE);
    }

    private Color cursorTint(GridPosition cell) {
        ir.ac.pvz.model.support.Tile tile = controller.getBoard().getTile(cell);

        if (tile == null) {
            return new Color(1f, 0.3f, 0.3f, 0.30f);
        }

        if (shovelMode) {
            return hasPlant(tile)
                ? new Color(1f, 0.85f, 0.35f, 0.34f)
                : new Color(1f, 0.3f, 0.3f, 0.30f);
        }

        if (plantFoodMode) {
            return hasPlant(tile)
                ? new Color(0.55f, 1f, 0.5f, 0.36f)
                : new Color(1f, 0.3f, 0.3f, 0.30f);
        }

        return tile.canPlant && !hasPlant(tile)
            ? new Color(0.5f, 1f, 0.5f, 0.30f)
            : new Color(1f, 0.3f, 0.3f, 0.30f);
    }

    private boolean hasPlant(ir.ac.pvz.model.support.Tile tile) {
        for (ir.ac.pvz.model.core.Plant plant : tile.getPlants()) {
            if (plant.isAlive) {
                return true;
            }
        }

        return false;
    }

}
