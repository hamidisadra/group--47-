package ir.ac.pvz.view.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import ir.ac.pvz.controller.gui.MinigameController;
import ir.ac.pvz.controller.managers.MenuManager;
import ir.ac.pvz.controller.managers.ScreenId;
import ir.ac.pvz.model.user.User;
import ir.ac.pvz.view.PvzGame;
import ir.ac.pvz.view.ui.Ui;

public class MinigamesScreen extends BaseMenuScreen {
    private static final String[][] GAMES = {
        { "Vasebreaker", "Break the vases to find plants and zombies." },
        { "Wall-nut Bowling", "Roll nuts down the lawn to flatten the zombies." },
        { "I, Zombie", "Place zombies and eat every brain." },
        { "Beghouled", "Swap plants to make matches of three." },
        { "Zombotany", "Normal rules, but the zombies are part plant." }
    };

    public MinigamesScreen(PvzGame game) {
        super(game, "Mini-games", true);

        Table grid = new Table();
        int column = 0;
        for (String[] entry : GAMES) {
            grid.add(card(entry[0], entry[1])).width(330f).pad(10f);
            column++;
            if (column % 3 == 0) {
                grid.row();
            }
        }

        content.add(grid).center();
    }

    private Table card(String name, String description) {
        Table card = new Table();
        card.setBackground(Ui.panel());
        card.pad(18f);

        card.add(Ui.label(name, "medium")).padBottom(8f).row();
        Label text = Ui.label(description, "secondary");
        text.setWrap(true);
        text.setAlignment(Align.center);
        text.setFontScale(0.85f);
        card.add(text).width(280f).padBottom(12f).row();

        Table levels = new Table();
        for (int level = 1; level <= MinigameController.LEVELS_PER_MINIGAME; level++) {
            levels.add(levelButton(name, level)).width(88f).height(52f).pad(3f);
        }

        card.add(levels).row();

        if (name.equals("I, Zombie")) {
            card.add(versusButton()).width(280f).height(48f).padTop(8f).row();
        }

        return card;
    }

    private Actor versusButton() {
        com.badlogic.gdx.scenes.scene2d.ui.TextButton button =
            Ui.button("Play online versus", "green");
        button.getLabel().setFontScale(0.72f);

        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.navigate(ScreenId.MULTIPLAYER);
            }
        });

        return button;
    }

    private Actor levelButton(String name, int level) {
        boolean unlocked = isLevelUnlocked(name, level);
        com.badlogic.gdx.scenes.scene2d.ui.TextButton button =
            Ui.button(unlocked ? "Level " + level : "Locked",
                unlocked ? "green" : "brown");
        button.getLabel().setFontScale(0.72f);
        button.setDisabled(!unlocked);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!unlocked) {
                    return;
                }

                launch(name, level);
            }
        });

        return button;
    }

    private boolean isLevelUnlocked(String name, int level) {
        if (level <= 1) {
            return true;
        }

        User user = MenuManager.getInstance().getActiveUser();
        if (user == null) {
            return false;
        }

        return user.getCompletedMinigames().contains(levelKey(name, level - 1));
    }

    public static String levelKey(String name, int level) {
        return name + " #" + level;
    }

    private void launch(String name, int level) {
        game.setPendingMinigame(name, level);

        if (name.equals("Beghouled")) {
            game.setScreen(new BeghouledScreen(game));
            return;
        }

        if (name.equals("Zombotany")) {
            game.navigate(ScreenId.PLANT_SELECTION);
            return;
        }

        game.startBattle(java.util.Collections.emptyList());
    }
}
