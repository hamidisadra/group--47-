package ir.ac.pvz.view.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import ir.ac.pvz.controller.gui.SessionController;
import ir.ac.pvz.controller.managers.ScreenId;
import ir.ac.pvz.controller.online.OnlineMatch;
import ir.ac.pvz.view.PvzGame;
import ir.ac.pvz.view.ui.Ui;

import network.service.ReactionCatalog;

final class VersusOverlay {
    private static final float REACTION_SECONDS = 2.5f;

    private final PvzGame game;
    private final OnlineMatch match;

    private Label incoming;
    private float reactionTimer;
    private Stage overlayStage;

    VersusOverlay(PvzGame game, SessionController controller) {
        this.game = game;
        this.match = game.getVersusMatch();

        if (match != null) {
            match.attach(controller);
            prepareRole(controller);
        }
    }

    private void prepareRole(SessionController controller) {
        if (!match.isHost()) {
            return;
        }

        controller.getSession().getSunManager().setSkyDropEnabled(true);
    }

    boolean isPlantSide() {
        return match != null && match.isHost();
    }

    boolean isOnline() {
        return match != null;
    }

    boolean isGuest() {
        return match != null && !match.isHost();
    }

    boolean isFinished() {
        return match != null && match.isFinished();
    }

    void requestZombie(String cardType,
                       ir.ac.pvz.model.support.GridPosition cell) {
        if (match != null) {
            match.requestZombie(cardType, cell);
        }
    }

    void update(float delta) {
        if (match == null) {
            return;
        }

        match.update(delta);
        showIncomingReaction(match.pollReaction());
        fadeReaction(delta);
    }

    private void showIncomingReaction(OnlineMatch.Reaction reaction) {
        if (reaction == null) {
            return;
        }

        if ("sticker".equals(reaction.kind) && overlayStage != null) {
            StickerAnimation.play(overlayStage, reaction.index,
                match.getOpponent());
            return;
        }

        if (incoming == null) {
            return;
        }

        incoming.setText(match.getOpponent() + ": " + reaction.value);
        incoming.setVisible(true);
        incoming.invalidateHierarchy();
        reactionTimer = REACTION_SECONDS;
    }

    private void fadeReaction(float delta) {
        if (reactionTimer <= 0f) {
            return;
        }

        reactionTimer -= delta;

        if (reactionTimer <= 0f && incoming != null) {
            incoming.setVisible(false);
        }
    }

    void installReactionBar(Stage ui) {
        if (match == null) {
            return;
        }

        overlayStage = ui;

        Table root = new Table();
        root.setFillParent(true);
        root.bottom().right().pad(14f);

        Table bar = new Table();
        bar.setBackground(Ui.panel());
        bar.pad(8f);

        addRow(bar, "text", ReactionCatalog.TEXTS.size());
        addRow(bar, "emoji", ReactionCatalog.EMOJIS.size());
        addRow(bar, "sticker", ReactionCatalog.STICKERS.size());

        incoming = Ui.label("");
        incoming.setAlignment(Align.right);
        incoming.setVisible(false);

        Table banner = new Table();
        banner.setBackground(Ui.panel());
        banner.pad(8f);
        banner.add(incoming).width(300f);

        root.add(banner).right().padBottom(6f).row();
        root.add(bar).right();
        ui.addActor(root);
    }

    private void addRow(Table bar, String kind, int count) {
        for (int index = 0; index < count; index++) {
            bar.add(reactionButton(kind, index)).width(96f).height(38f).pad(3f);
        }

        bar.row();
    }

    private Actor reactionButton(String kind, int index) {
        TextButton button = Ui.button(shortCaption(kind, index));
        button.getLabel().setFontScale(0.7f);

        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                network.client.GameClient.getInstance()
                    .sendReaction(kind, index);
            }
        });

        return button;
    }

    private String shortCaption(String kind, int index) {
        if ("emoji".equals(kind)) {
            return ReactionCatalog.valueOf(kind, index);
        }

        if ("sticker".equals(kind)) {
            return "Sticker " + (index + 1);
        }

        String text = ReactionCatalog.valueOf(kind, index);

        if (text != null && text.length() > 14) {
            return text.substring(0, 13) + "\u2026";
        }

        return text;
    }

    void showOutcome(Stage ui, PvzGame owner) {
        Table root = new Table();
        root.setFillParent(true);
        root.setBackground(Ui.dim());

        Table box = new Table();
        box.setBackground(Ui.panel());
        box.pad(28f);
        box.add(Ui.label(headline())).padBottom(18f).row();
        box.add(backButton(owner)).width(220f).height(56f);

        root.add(box);
        ui.addActor(root);
    }

    private String headline() {
        if ("win".equals(match.getResult())) {
            return "You win!";
        }

        return "You lose. " + match.getOpponent() + " takes it.";
    }

    private Actor backButton(PvzGame owner) {
        TextButton button = Ui.button("Back to menu", "green");

        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                owner.clearVersusMatch();
                owner.replace(ScreenId.MINIGAMES);
            }
        });

        return button;
    }
}
