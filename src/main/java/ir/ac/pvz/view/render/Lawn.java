package ir.ac.pvz.view.render;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import ir.ac.pvz.model.enums.SeasonType;
import ir.ac.pvz.view.assets.AnimationNames;
import ir.ac.pvz.view.assets.GameAssets;

public final class Lawn {
    public static final float WIDTH = 1975f;
    public static final float HEIGHT = 768f;

    public static final float GRID_LEFT = 531f;
    public static final float GRID_BOTTOM = 75f;
    public static final float CELL_WIDTH = 82.7f;
    public static final float CELL_HEIGHT = 99.2f;

    private static final float LEFT_STRIP_WIDTH = 278f;
    private static final float CENTER_STRIP_WIDTH = 1024f;

    private final TextureRegion left;
    private final TextureRegion center;
    private final TextureRegion right;

    public Lawn(SeasonType season) {
        GameAssets assets = GameAssets.get();
        String base = AnimationNames.lawnImage(season);
        center = assets.image(base);
        left = assets.image(base + "_LEFT");
        right = assets.image(base + "_RIGHT");
    }

    public void draw(Batch batch) {
        if (left != null) {
            batch.draw(left, 0f, 0f, LEFT_STRIP_WIDTH, HEIGHT);
        }
        if (center != null) {
            batch.draw(center, LEFT_STRIP_WIDTH, 0f, CENTER_STRIP_WIDTH, HEIGHT);
        }
        if (right != null) {
            batch.draw(right, LEFT_STRIP_WIDTH + CENTER_STRIP_WIDTH, 0f,
                    WIDTH - LEFT_STRIP_WIDTH - CENTER_STRIP_WIDTH, HEIGHT);
        }
    }

    public static float columnCenterX(float column) {
        return GRID_LEFT + (column + 0.5f) * CELL_WIDTH;
    }

    public static float rowBottomY(int row, int rowCount) {
        return GRID_BOTTOM + (rowCount - 1 - row) * CELL_HEIGHT;
    }

    public static float rowCenterY(int row, int rowCount) {
        return rowBottomY(row, rowCount) + CELL_HEIGHT * 0.5f;
    }

    public static int columnAt(float worldX) {
        return (int) Math.floor((worldX - GRID_LEFT) / CELL_WIDTH);
    }

    public static int rowAt(float worldY, int rowCount) {
        int fromBottom = (int) Math.floor((worldY - GRID_BOTTOM) / CELL_HEIGHT);
        return rowCount - 1 - fromBottom;
    }
}
