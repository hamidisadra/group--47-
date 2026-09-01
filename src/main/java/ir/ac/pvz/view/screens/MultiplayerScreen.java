package ir.ac.pvz.view.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import ir.ac.pvz.controller.managers.ScreenId;
import ir.ac.pvz.controller.online.InviteHub;
import ir.ac.pvz.view.PvzGame;
import ir.ac.pvz.view.ui.Toast;
import ir.ac.pvz.view.ui.Ui;

import network.client.GameClient;
import network.protocol.MessageType;
import network.protocol.NetworkMessage;

public class MultiplayerScreen extends BaseMenuScreen {
    private final TextField opponentField;
    private final Label statusLabel;

    private boolean queued;

    public MultiplayerScreen(PvzGame game) {
        super(game, "I, Zombie - Versus", true);

        this.opponentField = new TextField("", Ui.skin());
        this.opponentField.setMessageText("Opponent username");
        this.statusLabel = Ui.label("Choose how you want to find an opponent.");
        this.statusLabel.setAlignment(Align.center);

        Table panel = new Table();
        panel.setBackground(Ui.panel());
        panel.pad(24f);

        panel.add(Ui.label("Play a specific player")).left().row();
        panel.add(opponentField).width(420f).height(46f).padTop(6f).row();
        panel.add(challengeButton()).width(420f).height(56f).padTop(10f).row();
        panel.add(Ui.label("or")).padTop(18f).row();
        panel.add(randomButton()).width(420f).height(56f).padTop(10f).row();
        panel.add(Ui.label("or")).padTop(18f).row();
        panel.add(couchButton()).width(420f).height(56f).padTop(10f).row();
        panel.add(Ui.label("Couch play: plants use the mouse, zombies use "
            + "WASD to move, 1-5 to pick, Space to place.")).width(420f)
            .padTop(6f).row();
        panel.add(statusLabel).width(420f).padTop(18f).row();

        content.add(panel).center();

        connectIfNeeded();
    }

    private void connectIfNeeded() {
        if (!GameClient.getInstance().isSignedIn()) {
            statusLabel.setText("You are not signed in to the server.");
        }
    }

    private Actor challengeButton() {
        TextButton button = Ui.button("Send invite", "green");

        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                sendChallenge();
            }
        });

        return button;
    }

    private Actor randomButton() {
        TextButton button = Ui.button("Find a random opponent");

        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                toggleQueue();
            }
        });

        return button;
    }

    private Actor couchButton() {
        TextButton button = Ui.button("Two players on this device");

        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.startCouchMatch();
            }
        });

        return button;
    }

    private void sendChallenge() {
        String target = opponentField.getText().trim();

        if (target.isEmpty()) {
            Toast.error(stage, "Enter a username first.");
            return;
        }

        GameClient.getInstance().challenge(target);
        statusLabel.setText("Invite sent to " + target + ". Waiting...");
    }

    private void toggleQueue() {
        if (queued) {
            GameClient.getInstance().leaveQueue();
            queued = false;
            statusLabel.setText("You left the queue.");
            return;
        }

        GameClient.getInstance().joinRandomQueue();
        queued = true;
        statusLabel.setText("Waiting for another player...");
    }

    @Override
    public void render(float delta) {
        NetworkMessage declined = InviteHub.pollDeclined();

        if (declined != null) {
            statusLabel.setText(declined.get("from", "The player")
                + " declined your invite.");
        }

        super.render(delta);
    }

}
