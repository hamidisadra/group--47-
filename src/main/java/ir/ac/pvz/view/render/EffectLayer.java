package ir.ac.pvz.view.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;

import ir.ac.pvz.model.enums.ProjectileTrajectory;
import ir.ac.pvz.model.enums.ProjectileType;
import ir.ac.pvz.model.support.ShotEvent;
import ir.ac.pvz.view.assets.GameAssets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class EffectLayer {
    private static final float SHOT_SPEED = 620f;

    private final GameAssets assets = GameAssets.get();
    private final List<Shot> shots = new ArrayList<>();
    private final List<Burst> bursts = new ArrayList<>();
    private final int rowCount;

    private float shakeTime;
    private float shakeStrength;

    private static final class Shot {
        float delay;
        float startX;
        float startY;
        float endX;
        float endY;
        float progress;
        float duration;
        boolean arc;
        Color color;
    }

    private static final class Burst {
        float x;
        float y;
        float life;
        float maxLife;
        Color color;
        float size;
    }

    public EffectLayer(int rowCount) {
        this.rowCount = rowCount;
    }

    public void onShot(ShotEvent event) {
        Shot shot = new Shot();
        shot.startX = Lawn.columnCenterX(event.sourceColumn) + Lawn.CELL_WIDTH * 0.25f;
        shot.startY = Lawn.rowCenterY(event.sourceRow, rowCount) + Lawn.CELL_HEIGHT * 0.12f;
        shot.endX = Lawn.columnCenterX(event.targetColumn);
        shot.endY = Lawn.rowCenterY(event.targetRow, rowCount);
        shot.arc = event.trajectory == ProjectileTrajectory.ARC;
        shot.duration = Math.max(0.12f,
                Math.abs(shot.endX - shot.startX) / SHOT_SPEED);
        shot.color = colorFor(event.type);
        shot.delay = launchDelay(event);
        shots.add(shot);
    }

    private static final float LAUNCH_FRACTION = 0.3f;
    private static final float MAX_LAUNCH_DELAY = 0.32f;

    private float launchDelay(ShotEvent event) {
        if (event == null || event.source == null) {
            return 0f;
        }

        String path = assets.plantPath(event.source.type);
        if (path == null) {
            return 0f;
        }

        String clip = assets.pickClip(path, "attack", "special", "idle");
        float span = assets.clipDuration(path, clip);
        if (span <= 0f) {
            return 0f;
        }

        return Math.min(MAX_LAUNCH_DELAY, span * LAUNCH_FRACTION);
    }

    private Color colorFor(ProjectileType type) {
        if (type == null) {
            return new Color(0.55f, 0.9f, 0.35f, 1f);
        }
        switch (type.name()) {
            case "FIRE":
                return new Color(1f, 0.45f, 0.12f, 1f);
            case "ICE":
                return new Color(0.55f, 0.85f, 1f, 1f);
            case "POISON":
                return new Color(0.6f, 0.35f, 0.85f, 1f);
            case "LOBBED":
                return new Color(0.95f, 0.75f, 0.25f, 1f);
            default:
                return new Color(0.55f, 0.9f, 0.35f, 1f);
        }
    }

    public void explosion(float column, int row, float radius) {
        Burst burst = new Burst();
        burst.x = Lawn.columnCenterX(column);
        burst.y = Lawn.rowCenterY(row, rowCount);
        burst.maxLife = 0.45f;
        burst.life = burst.maxLife;
        burst.color = new Color(1f, 0.6f, 0.15f, 1f);
        burst.size = Math.max(70f, radius * Lawn.CELL_WIDTH);
        bursts.add(burst);
        shake(0.3f, 9f);
    }

    public void shake(float duration, float strength) {
        shakeTime = Math.max(shakeTime, duration);
        shakeStrength = Math.max(shakeStrength, strength);
    }

    public float shakeOffsetX() {
        if (shakeTime <= 0f) {
            return 0f;
        }
        return (float) Math.sin(shakeTime * 55f) * shakeStrength;
    }

    public float shakeOffsetY() {
        if (shakeTime <= 0f) {
            return 0f;
        }
        return (float) Math.cos(shakeTime * 47f) * shakeStrength * 0.6f;
    }

    public void update(float delta) {
        if (shakeTime > 0f) {
            shakeTime -= delta;
            if (shakeTime <= 0f) {
                shakeStrength = 0f;
            }
        }
        Iterator<Shot> shotIterator = shots.iterator();
        while (shotIterator.hasNext()) {
            Shot shot = shotIterator.next();
            if (shot.delay > 0f) {
                shot.delay -= delta;
                continue;
            }

            shot.progress += delta / shot.duration;
            if (shot.progress >= 1f) {
                Burst burst = new Burst();
                burst.x = shot.endX;
                burst.y = shot.endY;
                burst.maxLife = 0.2f;
                burst.life = burst.maxLife;
                burst.color = shot.color;
                burst.size = 42f;
                bursts.add(burst);
                shotIterator.remove();
            }
        }
        Iterator<Burst> burstIterator = bursts.iterator();
        while (burstIterator.hasNext()) {
            Burst burst = burstIterator.next();
            burst.life -= delta;
            if (burst.life <= 0f) {
                burstIterator.remove();
            }
        }
    }

    public void draw(Batch batch) {
        com.badlogic.gdx.graphics.g2d.TextureRegion pixel =
                ((com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable)
                        ir.ac.pvz.view.ui.Ui.solid(Color.WHITE)).getRegion();

        for (Shot shot : shots) {
            if (shot.delay > 0f) {
                continue;
            }

            float progress = Math.min(1f, shot.progress);
            float x = shot.startX + (shot.endX - shot.startX) * progress;
            float y = shot.startY + (shot.endY - shot.startY) * progress;
            if (shot.arc) {
                y += (float) Math.sin(progress * Math.PI) * 120f;
            }
            batch.setColor(shot.color);
            batch.draw(pixel, x - 9f, y - 9f, 18f, 18f);
        }
        for (Burst burst : bursts) {
            float alpha = burst.life / burst.maxLife;
            float size = burst.size * (1.4f - alpha * 0.4f);
            batch.setColor(burst.color.r, burst.color.g, burst.color.b, alpha * 0.8f);
            batch.draw(pixel, burst.x - size / 2f, burst.y - size / 2f, size, size);
        }
        batch.setColor(Color.WHITE);
    }
}
