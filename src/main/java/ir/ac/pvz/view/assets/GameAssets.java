package ir.ac.pvz.view.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Disposable;

import ir.ac.pvz.model.enums.SeasonType;

import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;
import pvz.skin.PvzSkin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class GameAssets implements Disposable {
    private static GameAssets instance;

    private final TextureBank textures;
    private final PamPlayer player;
    private final AnimationIndex index;
    private final Skin skin;
    private final Map<String, String> resolvedPaths = new HashMap<>();
    private final Set<String> missing = new HashSet<>();
    private final com.badlogic.gdx.math.Matrix4 previousTransform =
            new com.badlogic.gdx.math.Matrix4();
    private final com.badlogic.gdx.math.Matrix4 scaleTransform =
            new com.badlogic.gdx.math.Matrix4();

    private GameAssets() {
        FileHandle root = Gdx.files.internal("assets");
        textures = new TextureBank("768", root);
        player = new PamPlayer(textures, root);
        index = new AnimationIndex(root.child("animations.json"));
        skin = PvzSkin.get();
    }

    public static GameAssets get() {
        if (instance == null) {
            instance = new GameAssets();
        }
        return instance;
    }

    public Skin skin() {
        return skin;
    }

    public AnimationIndex index() {
        return index;
    }

    public void update() {
        textures.update();
    }

    public TextureRegion image(String resourceId) {
        return textures.region(resourceId);
    }

    public String plantPath(String plantName) {
        return resolve(AnimationNames.plant(plantName), "PLANT");
    }

    public String zombiePath(String gameType, SeasonType season) {
        for (String candidate : AnimationNames.zombieCandidates(gameType, season)) {
            String path = resolve(candidate, "ZOMBIE");
            if (path != null) {
                return path;
            }
        }
        return null;
    }

    public String mowerPath(SeasonType season) {
        return resolve(AnimationNames.mower(season), "MOWERS");
    }

    public String gravestonePath(String variant) {
        String name = variant == null ? "DARK_NOOP" : variant;
        return resolve(name, "GRAVESTONES");
    }

    public String effectPath(String name) {
        return resolve(name, "EFFECTS");
    }

    private String resolve(String name, String preferredFolder) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        String cacheKey = preferredFolder + "/" + name;
        if (resolvedPaths.containsKey(cacheKey)) {
            return resolvedPaths.get(cacheKey);
        }
        AnimationIndex.Entry entry = index.find(name, preferredFolder);
        String path = entry == null ? null : entry.path;
        if (path == null && missing.add(cacheKey)) {
            Gdx.app.log("assets", "no animation for " + cacheKey);
        }
        resolvedPaths.put(cacheKey, path);
        return path;
    }

    private final Map<String, Map<String, Boolean>> fullVisibility = new HashMap<>();

    public Map<String, Boolean> allParts(String path) {
        if (path == null) {
            return null;
        }
        Map<String, Boolean> cached = fullVisibility.get(path);
        if (cached != null) {
            return cached;
        }
        Map<String, Boolean> visible = new HashMap<>();
        PamPlayer.AnimationPart root = player.getParts(path);
        if (root != null) {
            collectParts(root, visible);
        }
        fullVisibility.put(path, visible);
        return visible;
    }

    private void collectParts(PamPlayer.AnimationPart part, Map<String, Boolean> visible) {
        if (part.name != null && !part.name.isEmpty()) {
            visible.put(part.name, Boolean.TRUE);
        }
        for (PamPlayer.AnimationPart child : part.children) {
            collectParts(child, visible);
        }
    }

    public boolean hasClip(String path, String clip) {
        AnimationIndex.Entry entry = index.byPath(path);
        return entry != null && entry.hasClip(clip);
    }

    public String pickClip(String path, String... preferred) {
        AnimationIndex.Entry entry = index.byPath(path);
        if (entry == null) {
            return preferred.length > 0 ? preferred[0] : "idle";
        }
        for (String clip : preferred) {
            if (entry.hasClip(clip)) {
                return clip;
            }
        }
        return entry.clips.isEmpty() ? "idle" : entry.clips.iterator().next();
    }

    public void draw(Batch batch, String path, String clip, float time, float x, float y,
                     float scale, boolean loop) {
        if (path == null) {
            return;
        }
        player.draw(batch, path, clip, time, x, y, scale, scale, loop);
    }

    public void draw(Batch batch, String path, String clip, float time, float x, float y,
                     float scale, boolean loop, Map<String, Boolean> parts) {
        if (path == null) {
            return;
        }
        if (parts == null) {
            player.draw(batch, path, clip, time, x, y, scale, scale, loop);
            return;
        }
        previousTransform.set(batch.getTransformMatrix());
        scaleTransform.set(previousTransform)
                .translate(x, y, 0f)
                .scale(scale, scale, 1f)
                .translate(-x, -y, 0f);
        batch.setTransformMatrix(scaleTransform);
        player.draw(batch, path, clip, time, x, y, loop, parts);
        batch.setTransformMatrix(previousTransform);
    }

    public void drawPart(Batch batch, String path, String clip, float time,
                         float x, float y, float scale, float rotation, String part) {
        if (path == null || part == null) {
            return;
        }
        previousTransform.set(batch.getTransformMatrix());
        scaleTransform.set(previousTransform)
                .translate(x, y, 0f)
                .rotate(0f, 0f, 1f, rotation)
                .scale(scale, scale, 1f)
                .translate(-x, -y, 0f);
        batch.setTransformMatrix(scaleTransform);
        player.drawPart(batch, path, clip, time, x, y, part);
        batch.setTransformMatrix(previousTransform);
    }

    public boolean hasPart(String path, String part) {
        return path != null && part != null && allParts(path).containsKey(part);
    }

    public Rectangle bounds(String path, String clip) {
        return path == null ? null : player.bounds(path, clip);
    }

    public java.util.List<String> clips(String path) {
        return path == null ? new java.util.ArrayList<>() : player.clips(path);
    }

    public float clipDuration(String path, String clip) {
        if (path == null || clip == null) {
            return 0f;
        }
        return player.clipDurationSeconds(path, clip);
    }

    public void preload(String path) {
        if (path != null) {
            player.loadAsync(path, null);
        }
    }

    public PamPlayer player() {
        return player;
    }

    @Override
    public void dispose() {
        textures.dispose();
        skin.dispose();
        instance = null;
    }
}
