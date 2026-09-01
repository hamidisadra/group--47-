package ir.ac.pvz.view.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import ir.ac.pvz.controller.managers.MenuManager;
import ir.ac.pvz.view.PvzGame;
import ir.ac.pvz.controller.online.InviteHub;
import ir.ac.pvz.view.assets.GameAssets;
import ir.ac.pvz.view.ui.Ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import network.client.GameClient;
import network.protocol.NetworkMessage;
import ir.ac.pvz.view.ui.TopBar;
import ir.ac.pvz.view.ui.Ui;

public abstract class BaseMenuScreen extends ScreenAdapter {
    protected static final float UI_WIDTH = 1280f;
    protected static final float UI_HEIGHT = 720f;

    protected final PvzGame game;
    protected final Stage stage;
    protected final Table root;
    protected final Table content;
    private Table inviteDialog;
    protected TopBar topBar;

    private final TextureRegion background;

    private boolean navigating;

    protected BaseMenuScreen(PvzGame game, String title, boolean showBack) {
        this(game, title, showBack, "IMAGE_MAINMENU_BACKGROUND");
    }

    protected BaseMenuScreen(PvzGame game, String title, boolean showBack,
                             String backgroundKey) {
        this.game = game;
        this.stage = new Stage(new FitViewport(UI_WIDTH, UI_HEIGHT), game.batch);
        this.background = resolveBackground(backgroundKey);

        root = new Table();
        root.setFillParent(true);
        root.getColor().a = 0f;
        root.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn(0.25f));
        stage.addActor(root);

        Table bar = new Table();
        if (showBack) {
            com.badlogic.gdx.scenes.scene2d.ui.TextButton back = Ui.button("Back", "brown");
            back.getLabel().setFontScale(0.8f);
            back.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    onBack();
                }
            });
            bar.add(back).width(130f).height(46f).padRight(14f).left();
        }
        topBar = new TopBar(stage);
        bar.add(topBar).growX();
        root.add(bar).growX().top().row();

        Label heading = Ui.label(title, "big_outline");
        root.add(heading).padTop(6f).row();

        content = new Table();
        ScrollPane scroller = new ScrollPane(content, Ui.skin());
        scroller.setFadeScrollBars(false);
        scroller.setScrollingDisabled(true, false);
        scroller.setOverscroll(false, false);
        root.add(scroller).grow().pad(8f, 16f, 12f, 16f).row();
    }

    private static TextureRegion resolveBackground(String backgroundKey) {
        TextureRegion picked = safeImage(backgroundKey);
        return picked == null ? safeImage("IMAGE_MAINMENU_BACKGROUND") : picked;
    }

    private static TextureRegion safeImage(String resourceId) {
        try {
            return GameAssets.get().image(resourceId);
        }

        catch (RuntimeException missing) {
            return null;
        }
    }

    protected void onBack() {
        if (navigating) {
            return;
        }

        navigating = true;
        game.back();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        GameAssets.get().update();
        ScreenUtils.clear(0.05f, 0.06f, 0.08f, 1f);
        stage.getViewport().apply();
        game.batch.setProjectionMatrix(stage.getCamera().combined);
        if (background != null) {
            game.batch.begin();
            game.batch.draw(background, 0f, 0f, UI_WIDTH, UI_HEIGHT);
            game.batch.end();
        }
        pumpInvites();
        stage.act(delta);
        stage.draw();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            onBack();
        }
    }

    private void pumpInvites() {
        NetworkMessage start = InviteHub.pollStart();

        if (start != null) {
            game.startVersusMatch(start);
            return;
        }

        NetworkMessage invite = InviteHub.pollInvite();

        if (invite != null && inviteDialog == null) {
            showInviteDialog(invite.get("from", "A player"));
        }
    }

    private void showInviteDialog(String from) {
        inviteDialog = new Table();
        inviteDialog.setFillParent(true);
        inviteDialog.setBackground(Ui.dim());

        Table box = new Table();
        box.setBackground(Ui.panel());
        box.pad(24f);
        box.add(Ui.label(from + " wants to play I, Zombie against you."))
            .colspan(2).padBottom(16f).row();
        box.add(inviteChoice("Accept", true)).width(160f).height(52f).pad(6f);
        box.add(inviteChoice("Decline", false)).width(160f).height(52f).pad(6f);

        inviteDialog.add(box);
        stage.addActor(inviteDialog);
    }

    private Actor inviteChoice(String text, boolean accept) {
        TextButton button = Ui.button(text, accept ? "green" : "red");

        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                answerInvite(accept);
            }
        });

        return button;
    }

    private void answerInvite(boolean accept) {
        if (accept) {
            GameClient.getInstance().acceptInvite();
        }

        else {
            GameClient.getInstance().rejectInvite();
        }

        inviteDialog.remove();
        inviteDialog = null;
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
