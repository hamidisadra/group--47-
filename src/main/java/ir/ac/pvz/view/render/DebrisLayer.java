package ir.ac.pvz.view.render;

import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.support.ArmorPiece;
import ir.ac.pvz.model.support.Board;
import ir.ac.pvz.view.assets.AnimationNames;
import ir.ac.pvz.view.assets.GameAssets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;

import java.util.ArrayList;
import java.util.List;

final class DebrisLayer {
    private static final float DEBRIS_GRAVITY = 900f;
    private static final String HEAD_PART = "particle_head";
    private static final String ARM_PART = "particle_arm";
    private static final float ZOMBIE_SCALE = 0.43f;

    private final GameAssets assets;
    private final java.util.Map<ArmorPiece, Integer> armorMaxHealth =
        new java.util.WeakHashMap<>();
    private final List<Debris> debris = new ArrayList<>();
    private final List<PartDebris> partDebris = new ArrayList<>();
    private final List<DeathAnimation> deaths = new ArrayList<>();

    DebrisLayer(GameAssets assets) {
        this.assets = assets;
    }

    List<Debris> getDebris() {
        return debris;
    }

    List<PartDebris> getPartDebris() {
        return partDebris;
    }

    List<DeathAnimation> getDeaths() {
        return deaths;
    }

    static final class PartDebris {
        public String path;
        public String clip;
        public String part;
        public float x;
        public float y;
        public float velocityX;
        public float velocityY;
        public float rotation;
        public float spin;
        public float remaining;
        public float total;
        public float floor;
    }

    static final class Debris {
        public float x;
        public float y;
        public float velocityX;
        public float velocityY;
        public float remaining;
        public float total;
        public float size;
        public Color color;
    }

    static final class DeathAnimation {
        public String path;
        public String clip;
        public float x;
        public float y;
        public float scale;
        public float remaining;
        public float total;
    }

    void spawnDebris(float x, float y, Color color, float size) {
        Debris piece = new Debris();
        piece.x = x;
        piece.y = y;
        piece.velocityX = -60f + (float) Math.random() * 120f;
        piece.velocityY = 180f + (float) Math.random() * 160f;
        piece.total = 0.9f;
        piece.remaining = piece.total;
        piece.size = size;
        piece.color = color;
        debris.add(piece);
    }

    void spawnDeathParts(Zombie zombie, Board board, float x, float y) {
        String path = assets.zombiePath(zombie.getType(), board.seasonType);
        boolean head = spawnPart(path, HEAD_PART, x,
            y + Lawn.CELL_HEIGHT * 0.62f, 250f);
        boolean arm = spawnPart(path, ARM_PART, x,
            y + Lawn.CELL_HEIGHT * 0.42f, 170f);

        if (!head) {
            spawnDebris(x, y + Lawn.CELL_HEIGHT * 0.35f,
                new Color(0.55f, 0.72f, 0.45f, 1f), 26f);
        }

        if (!arm) {
            spawnDebris(x, y + Lawn.CELL_HEIGHT * 0.15f,
                new Color(0.5f, 0.66f, 0.42f, 1f), 18f);
        }

        for (int piece = countArmor(zombie); piece > 0; piece--) {
            spawnArmorPart(zombie, path, x, y + Lawn.CELL_HEIGHT * 0.5f);
        }
    }

    void spawnArmorPart(Zombie zombie, String path, float x, float y) {
        String part = topArmorPart(zombie);
        if (!spawnPart(path, part, x, y, 210f, "idle")) {
            spawnDebris(x, y, new Color(0.72f, 0.74f, 0.78f, 1f), 22f);
        }
    }

    String topArmorPart(Zombie zombie) {
        List<ArmorPiece> pieces = zombie.armorPieces;
        if (pieces == null) {
            return null;
        }

        for (ArmorPiece piece : pieces) {
            if (piece == null) {
                continue;
            }

            String part = AnimationNames.armorPart(piece.name, damageStage(piece));
            if (part != null) {
                return part;
            }
        }

        return null;
    }

    boolean spawnPart(String path, String part, float x, float y, float lift) {
        return spawnPart(path, part, x, y, lift, "particles");
    }

    boolean spawnPart(String path, String part, float x, float y, float lift,
                              String preferredClip) {
        if (path == null || part == null || !assets.hasPart(path, part)) {
            return false;
        }

        PartDebris piece = new PartDebris();
        piece.path = path;
        piece.clip = assets.pickClip(path, preferredClip, "idle");
        piece.part = part;
        piece.x = x;
        piece.y = y;
        piece.velocityX = -140f + (float) Math.random() * 280f;
        piece.velocityY = lift + (float) Math.random() * 90f;
        piece.rotation = (float) Math.random() * 360f;
        piece.spin = -420f + (float) Math.random() * 840f;
        piece.total = 1.15f;
        piece.remaining = piece.total;
        piece.floor = y - Lawn.CELL_HEIGHT * 0.55f;
        partDebris.add(piece);
        return true;
    }

    void advancePartDebris(float delta) {
        for (int index = partDebris.size() - 1; index >= 0; index--) {
            PartDebris piece = partDebris.get(index);
            piece.remaining -= delta;

            if (piece.remaining <= 0f) {
                partDebris.remove(index);
                continue;
            }

            piece.velocityY -= DEBRIS_GRAVITY * delta;
            piece.x += piece.velocityX * delta;
            piece.y += piece.velocityY * delta;

            if (piece.y <= piece.floor) {
                piece.y = piece.floor;
                piece.velocityY = 0f;
                piece.velocityX *= 0.6f;
                piece.spin *= 0.4f;
            }

            else {
                piece.rotation += piece.spin * delta;
            }
        }
    }

    void drawPartDebris(Batch batch) {
        for (PartDebris piece : partDebris) {
            float fade = Math.min(1f, piece.remaining / (piece.total * 0.35f));
            batch.setColor(1f, 1f, 1f, Math.max(0f, fade));
            assets.drawPart(batch, piece.path, piece.clip, 0f, piece.x, piece.y,
                ZOMBIE_SCALE, piece.rotation, piece.part);
        }

        batch.setColor(Color.WHITE);
    }

    void drawDebris(Batch batch,
                           com.badlogic.gdx.graphics.g2d.TextureRegion pixel) {
        for (Debris piece : debris) {
            float fade = Math.max(0f, piece.remaining / piece.total);
            batch.setColor(piece.color.r, piece.color.g, piece.color.b, fade);
            batch.draw(pixel, piece.x - piece.size * 0.5f, piece.y,
                piece.size, piece.size * 0.7f);
        }

        batch.setColor(Color.WHITE);
    }


    private int countArmor(Zombie zombie) {
        if (zombie.armorPieces == null) {
            return 0;
        }

        int count = 0;

        for (ArmorPiece piece : zombie.armorPieces) {
            if (piece != null && piece.health > 0) {
                count++;
            }
        }

        return count;
    }

    int damageStage(ArmorPiece piece) {
        Integer known = armorMaxHealth.get(piece);
        int max = known == null ? piece.health : Math.max(known, piece.health);
        armorMaxHealth.put(piece, max);

        if (max <= 0) {
            return 0;
        }

        float ratio = piece.health / (float) max;

        if (ratio > 0.66f) {
            return 0;
        }

        return ratio > 0.33f ? 1 : 2;
    }
}
