package ir.ac.pvz.view.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

import ir.ac.pvz.view.assets.GameAssets;

import java.util.HashMap;
import java.util.Map;

public final class Ui {
    private static final Map<String, Drawable> SOLIDS = new HashMap<>();

    private Ui() {
    }

    public static Skin skin() {
        return GameAssets.get().skin();
    }

    public static Label label(String text, String style) {
        Skin skin = skin();
        String resolved = skin.has(style, Label.LabelStyle.class) ? style : "default";
        return new Label(text, skin, resolved);
    }

    public static Label label(String text) {
        return label(text, "default");
    }

    public static TextButton button(String text, String style) {
        Skin skin = skin();
        String resolved = skin.has(style, TextButton.TextButtonStyle.class) ? style : "default";
        return new TextButton(text, skin, resolved);
    }

    public static TextButton button(String text) {
        return button(text, "default");
    }

    public static Drawable drawable(String name) {
        Skin skin = skin();
        if (skin.has(name, Drawable.class)) {
            return skin.getDrawable(name);
        }
        return null;
    }

    public static Drawable panel() {
        Drawable drawable = drawable("image_ui_dialog_asset_inner_bkgd_10");
        return drawable != null ? drawable : solid(new Color(0.16f, 0.10f, 0.05f, 0.92f));
    }

    public static Drawable solid(Color color) {
        String key = color.toString();
        Drawable cached = SOLIDS.get(key);
        if (cached != null) {
            return cached;
        }
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Drawable drawable = new TextureRegionDrawable(new Texture(pixmap));
        pixmap.dispose();
        SOLIDS.put(key, drawable);
        return drawable;
    }

    public static Drawable dim() {
        return solid(new Color(0f, 0f, 0f, 0.65f));
    }
}
