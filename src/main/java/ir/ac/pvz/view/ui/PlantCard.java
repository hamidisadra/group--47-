package ir.ac.pvz.view.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

import ir.ac.pvz.view.assets.GameAssets;

public class PlantCard extends Table {
    private static final Color BOOSTED = new Color(1f, 0.84f, 0.32f, 1f);
    private static final Color LOCKED = new Color(0.45f, 0.45f, 0.45f, 1f);
    private static final Color UNAVAILABLE = new Color(0.55f, 0.55f, 0.55f, 1f);

    private final String plantName;
    private final PamActor art;
    private final Label nameLabel;
    private final Label costLabel;
    private final Label levelLabel;
    private final Table frame;
    private ProgressBar seedBar;

    private boolean locked;
    private boolean boosted;
    private boolean selected;
    private float cooldownFraction;

    public PlantCard(String plantName, float width) {
        this.plantName = plantName;
        this.art = new PamActor(width * 0.62f);
        this.nameLabel = Ui.label(plantName, "secondary");
        this.costLabel = Ui.label("", "medium");
        this.levelLabel = Ui.label("", "secondary");

        nameLabel.setAlignment(Align.center);
        nameLabel.setFontScale(0.72f);
        costLabel.setAlignment(Align.center);
        levelLabel.setFontScale(0.7f);

        frame = new Table();
        frame.setBackground(cardBackground());
        frame.pad(6f);
        frame.add(levelLabel).left().row();
        frame.add(art).size(width * 0.62f).row();
        frame.add(nameLabel).width(width - 12f).row();
        frame.add(costLabel).row();

        add(frame).width(width);

        String path = GameAssets.get().plantPath(plantName);
        art.setAnimation(path, "idle");
    }

    private com.badlogic.gdx.scenes.scene2d.utils.Drawable cardBackground() {
        com.badlogic.gdx.scenes.scene2d.utils.Drawable drawable =
                Ui.drawable("image_ui_cards_chooser_chooser_plant_card");
        if (drawable == null) {
            drawable = Ui.drawable("image_ui_cards_almanac_plant_card");
        }
        return drawable != null ? drawable : Ui.solid(new Color(0.20f, 0.14f, 0.07f, 0.95f));
    }

    public PlantCard cost(int sunCost) {
        costLabel.setText(sunCost > 0 ? String.valueOf(sunCost) : "");
        return this;
    }

    public PlantCard level(int level) {
        levelLabel.setText(level > 0 ? "Lv " + level : "");
        return this;
    }

    public PlantCard seedProgress(int collected, int required) {
        if (seedBar == null) {
            String style = Ui.skin().has("xp_green", ProgressBar.ProgressBarStyle.class)
                    ? "xp_green" : "default-horizontal";
            seedBar = new ProgressBar(0f, 1f, 0.01f, false, Ui.skin(), style);
            frame.add(seedBar).width(60f).padTop(2f).row();
        }
        seedBar.setValue(required <= 0 ? 1f : Math.min(1f, collected / (float) required));
        return this;
    }

    public PlantCard locked(boolean locked) {
        this.locked = locked;
        return this;
    }

    public PlantCard boosted(boolean boosted) {
        this.boosted = boosted;
        return this;
    }

    public PlantCard selected(boolean selected) {
        this.selected = selected;
        return this;
    }

    public PlantCard cooldown(float fraction) {
        this.cooldownFraction = Math.max(0f, Math.min(1f, fraction));
        return this;
    }

    public boolean isLocked() {
        return locked;
    }

    public String getPlantName() {
        return plantName;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Color tint = Color.WHITE;
        if (locked) {
            tint = LOCKED;
        }

        else if (boosted) {
            tint = BOOSTED;
        }

        else if (cooldownFraction > 0f) {
            tint = UNAVAILABLE;
        }
        frame.setColor(tint);
        super.draw(batch, parentAlpha);

        if (selected) {
            batch.setColor(1f, 1f, 0.4f, 0.35f * parentAlpha);
            batch.draw(((com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable)
                    Ui.solid(Color.WHITE)).getRegion(), getX(), getY(), getWidth(), getHeight());
            batch.setColor(Color.WHITE);
        }
        if (cooldownFraction > 0f) {
            batch.setColor(0f, 0f, 0f, 0.55f * parentAlpha);
            batch.draw(((com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable)
                    Ui.solid(Color.WHITE)).getRegion(), getX(), getY(), getWidth(),
                    getHeight() * cooldownFraction);
            batch.setColor(Color.WHITE);
        }
    }
}
