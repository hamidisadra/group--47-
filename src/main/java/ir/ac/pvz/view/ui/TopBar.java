package ir.ac.pvz.view.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import ir.ac.pvz.controller.managers.GameSettings;
import ir.ac.pvz.controller.managers.MenuManager;
import ir.ac.pvz.controller.managers.UserManager;
import ir.ac.pvz.model.user.User;

public class TopBar extends Table {
    private final Label coinsLabel;
    private final Label gemsLabel;

    public TopBar(Stage stage) {
        setBackground(Ui.solid(new Color(0f, 0f, 0f, 0.45f)));
        pad(8f, 18f, 8f, 18f);

        coinsLabel = Ui.label("0", "medium");
        gemsLabel = Ui.label("0", "medium");

        add(Ui.label("Coins", "secondary")).padRight(6f);
        add(coinsLabel).padRight(24f);
        add(Ui.label("Gems", "secondary")).padRight(6f);
        add(gemsLabel).padRight(18f);

        if (GameSettings.getInstance().isDebugMode()) {
            add(cheatButton("+1000", stage, true)).padRight(8f);
            add(cheatButton("+100", stage, false));
        }
        refresh();
    }

    private Actor cheatButton(String text, Stage stage, boolean coins) {
        com.badlogic.gdx.scenes.scene2d.ui.TextButton button =
                Ui.button(text, coins ? "brown" : "purple");
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                User user = MenuManager.getInstance().getActiveUser();
                if (user == null) {
                    return;
                }
                if (coins) {
                    user.getWallet().addCoins(1000);
                }

                else {
                    user.getWallet().addGems(100);
                }
                UserManager.getInstance().saveAll();
                refresh();
                Toast.info(stage, coins ? "1000 coins added" : "100 gems added");
            }
        });
        return button;
    }

    public final void refresh() {
        User user = MenuManager.getInstance().getActiveUser();
        if (user == null) {
            coinsLabel.setText("0");
            gemsLabel.setText("0");
            return;
        }
        coinsLabel.setText(String.valueOf(user.getWallet().getCoins()));
        gemsLabel.setText(String.valueOf(user.getWallet().getGems()));
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        refresh();
    }
}
