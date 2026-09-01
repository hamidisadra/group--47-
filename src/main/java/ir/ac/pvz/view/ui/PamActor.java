package ir.ac.pvz.view.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;

import ir.ac.pvz.view.assets.GameAssets;

public class PamActor extends Actor {
    private final GameAssets assets;
    private String path;
    private String clip;
    private float time;
    private float fitSize;
    private float scale = 1f;
    private boolean scaleResolved;

    public PamActor(float fitSize) {
        this.assets = GameAssets.get();
        this.fitSize = fitSize;
        setSize(fitSize, fitSize);
    }

    public void setAnimation(String path, String... preferredClips) {
        if (path == null) {
            this.path = null;
            return;
        }
        if (!path.equals(this.path)) {
            this.path = path;
            this.scaleResolved = false;
            assets.preload(path);
        }
        this.clip = assets.pickClip(path, preferredClips.length > 0 ? preferredClips
                : new String[] { "idle" });
    }

    public void setFitSize(float fitSize) {
        this.fitSize = fitSize;
        this.scaleResolved = false;
        setSize(fitSize, fitSize);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        time += delta;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (path == null) {
            return;
        }
        if (!scaleResolved) {
            Rectangle bounds = assets.bounds(path, clip);
            if (bounds != null && bounds.height > 0f && bounds.width > 0f) {
                scale = fitSize / Math.max(bounds.width, bounds.height);
                scaleResolved = true;
            }
        }
        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
        assets.draw(batch, path, clip, time, getX() + getWidth() / 2f,
                getY() + getHeight() / 2f, scale, true);
        batch.setColor(Color.WHITE);
    }
}
