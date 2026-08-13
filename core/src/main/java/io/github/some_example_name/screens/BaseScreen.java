package io.github.some_example_name.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.some_example_name.Main;

import pvz.skin.BorderedTable;
import pvz.skin.PvzSkin;

public abstract class BaseScreen extends ScreenAdapter {

    protected static final Color TEXT_COLOR = new Color(0.20f, 0.12f, 0.05f, 1f);

    protected final Main game;
    protected final Viewport viewport = new FitViewport(1920f, 1080f);
    protected final Stage stage = new Stage(viewport);
    protected final Skin skin = PvzSkin.get();

    protected Label status;

    private Texture backgroundTexture;
    private Drawable background;

    protected BaseScreen(Main game) {
        this.game = game;
    }

    protected abstract void build();

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        backgroundTexture = new Texture(Gdx.files.internal("extra/menu_bg.png"));
        background = new TextureRegionDrawable(new TextureRegion(backgroundTexture));
        build();
    }

    protected BorderedTable dialog(String title) {
        BorderedTable dialog = new BorderedTable();
        dialog.pad(70f);
        dialog.defaults().pad(8f);

        dialog.add(new Label(title, skin, "big")).colspan(2).padBottom(28f);
        dialog.row();

        return dialog;
    }

    protected void showDialog(Table dialog) {
        stage.clear();
        Table root = new Table();
        root.setFillParent(true);
        root.setBackground(background);
        root.add(dialog);
        stage.addActor(root);
    }

    protected TextField textField(String hint) {
        TextField field = new TextField("", skin);
        field.setMessageText(hint);
        return field;
    }

    protected TextField passwordField() {
        TextField field = textField("");
        field.setPasswordMode(true);
        field.setPasswordCharacter('*');
        return field;
    }

    protected TextButton button(String text, String style, ChangeListener listener) {
        TextButton textButton = new TextButton(text, skin, style);
        textButton.addListener(listener);
        return textButton;
    }

    protected void addRow(Table table, String label, Actor input) {
        table.add(styledLabel(label)).right().padRight(14f);
        table.add(input).left().width(440f).height(52f);
        table.row();
    }

    protected Label styledLabel(String text) {
        Label label = new Label(text, skin);
        label.setColor(TEXT_COLOR);
        label.setFontScale(1.3f);
        return label;
    }

    protected Label statusLabel() {
        Label label = new Label("", skin);
        label.setWrap(true);
        return label;
    }

    protected void error(String message) {
        status.setColor(1f, 0.5f, 0.45f, 1f);
        status.setText(message);
    }

    protected void success(String message) {
        status.setColor(0.6f, 0.95f, 0.5f, 1f);
        status.setText(message);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.05f, 0.12f, 0.07f, 1f);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        stage.dispose();
        if (backgroundTexture != null) {
            backgroundTexture.dispose();
        }
    }
}
