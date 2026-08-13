package io.github.some_example_name.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

import io.github.some_example_name.Main;

import ir.ac.pvz.controller.managers.MenuManager;
import ir.ac.pvz.controller.managers.UserManager;

public class MainScreen extends BaseScreen {

    private final MenuManager menuManager = MenuManager.getInstance();
    private final UserManager userManager = UserManager.getInstance();

    private Texture bgTexture;
    private Texture logoTexture;

    private Skin handMadeSkin;

    public MainScreen(Main game) {
        super(game);
    }

    @Override
    protected void build() {
        stage.clear();

        bgTexture = new Texture(Gdx.files.internal("main menu/MAINMENU_BACKGROUND_768_00.png"));

        Image backdrop = new Image(new TextureRegionDrawable(new TextureRegion(bgTexture)));
        backdrop.setFillParent(true);
        backdrop.setScaling(Scaling.fill);
        backdrop.setAlign(Align.top);

        stage.addActor(backdrop);

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        status = statusLabel();

        logoTexture = new Texture(Gdx.files.internal("main menu/UI_MAINMENULOGO_768_00.png"));
        Image logo = new Image(logoTexture);

        TextButton play = menuButton("PLAY", "green", "Game menu is coming soon.");
        TextButton profile = menuButton("Profile", "brown", "Profile menu is coming soon.");
        TextButton score = menuButton("Score Game", "green", "Score game is coming soon.");
        TextButton logout = button("Logout", "purple", new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                logout();
            }
        });

        handMadeSkin = new Skin(Gdx.files.internal("main menu/news.json"));

        ImageButton news;

        if (menuManager.getActiveUser().getUnreadNews().isEmpty()) {
            news = new ImageButton(handMadeSkin, "default");
        }
        else {
            news = new ImageButton(handMadeSkin, "unreadNews");
        }

        news.addListener(info("News menu is coming soon."));

        ImageButton settings = new ImageButton(skin, "settings");
        settings.addListener(info("Settings menu is coming soon."));

        root.add(logo).expandX().center().padTop(40f);
        root.row();
        root.add(status).width(760f).expand().center();
        root.row();

        Table left = new Table();
        left.add(profile).width(220f).height(66f).pad(10f);
        left.add(news).size(88f).pad(10f);

        Table right = new Table();
        right.add(score).width(220f).height(66f).pad(10f);
        right.add(settings).size(88f).pad(10f);
        right.add(logout).width(200f).height(66f).pad(10f);

        Table bottom = new Table();
        bottom.add(left).left().expandX();
        bottom.add(play).center().width(320f).height(96f);
        bottom.add(right).right().expandX();
        root.add(bottom).expandX().fillX().bottom().pad(30f);
    }

    private TextButton menuButton(String text, String style, final String message) {
        return button(text, style, info(message));
    }

    private ChangeListener info(final String message) {
        return new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                status.setColor(TEXT_COLOR);
                status.setText(message);
            }
        };
    }

    private void logout() {
        userManager.clearLoggedIn();
        menuManager.logoutUser();
        game.setScreen(new RegisterScreen(game));
    }

    @Override
    public void dispose() {
        super.dispose();
        if (bgTexture != null) {
            bgTexture.dispose();
        }

        if (logoTexture != null) {
            logoTexture.dispose();
        }
    }
}
