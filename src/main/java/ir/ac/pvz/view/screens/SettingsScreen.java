package ir.ac.pvz.view.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;

import ir.ac.pvz.controller.managers.GameSettings;
import ir.ac.pvz.controller.managers.MenuManager;
import ir.ac.pvz.controller.managers.UserManager;
import ir.ac.pvz.model.user.User;
import ir.ac.pvz.view.PvzGame;
import ir.ac.pvz.view.ui.Toast;
import ir.ac.pvz.view.ui.Ui;

public class SettingsScreen extends BaseMenuScreen {

    public SettingsScreen(PvzGame game) {
        super(game, "Settings", true);

        Table panel = new Table();
        panel.setBackground(Ui.panel());
        panel.pad(28f);

        panel.add(row("Difficulty", difficultyBox())).growX().row();
        panel.add(row("Game speed", speedBox())).growX().row();
        panel.add(gridToggle()).left().padTop(14f).row();
        panel.add(debugToggle()).left().padTop(8f).padBottom(14f).row();
        panel.add(row("Music", musicSlider())).growX().row();
        panel.add(row("Sound effects", soundSlider())).growX().row();

        content.add(panel).width(660f).center();
    }

    private SelectBox<String> difficultyBox() {
        User activeUser = MenuManager.getInstance().getActiveUser();
        int current = activeUser == null
            ? GameSettings.getInstance().getDifficulty() : activeUser.getDifficultyLevel();

        SelectBox<String> difficulty = new SelectBox<>(Ui.skin());
        difficulty.setItems(new Array<>(new String[] { "1 - Relaxed", "2 - Easy",
            "3 - Normal", "4 - Hard", "5 - Brutal" }));
        difficulty.setSelectedIndex(Math.max(0, Math.min(4, current - 1)));
        difficulty.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                applyDifficulty(difficulty.getSelectedIndex() + 1);
            }
        });

        return difficulty;
    }

    private void applyDifficulty(int level) {
        GameSettings.getInstance().setDifficulty(level);
        User user = MenuManager.getInstance().getActiveUser();

        if (user != null && user.setDifficultyLevel(level)) {
            UserManager.getInstance().saveAll();
        }
    }

    private SelectBox<String> speedBox() {
        SelectBox<String> speed = new SelectBox<>(Ui.skin());
        speed.setItems(new Array<>(new String[] { "1", "2", "3" }));
        speed.setSelectedIndex(Math.max(0, GameSettings.getInstance().getGameSpeed() - 1));
        speed.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameSettings.getInstance().setGameSpeed(speed.getSelectedIndex() + 1);
            }
        });

        return speed;
    }

    private CheckBox gridToggle() {
        CheckBox grid = new CheckBox(" Show the lawn grid", Ui.skin());
        grid.setChecked(GameSettings.getInstance().isShowGrid());
        grid.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameSettings.getInstance().setShowGrid(grid.isChecked());
            }
        });

        return grid;
    }

    private CheckBox debugToggle() {
        CheckBox debug = new CheckBox(" Debug mode", Ui.skin());
        debug.setChecked(GameSettings.getInstance().isDebugMode());
        debug.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameSettings.getInstance().setDebugMode(debug.isChecked());
                Toast.info(stage, "Debug mode "
                    + (debug.isChecked() ? "enabled" : "disabled")
                    + ". Reopen a menu to refresh the cheat buttons.");
            }
        });

        return debug;
    }

    private Slider musicSlider() {
        Slider music = new Slider(0f, 1f, 0.05f, false, Ui.skin());
        music.setValue(GameSettings.getInstance().getMusicVolume());
        music.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameSettings.getInstance().setMusicVolume(music.getValue());
                game.jukebox.updateVolume();
            }
        });

        return music;
    }

    private Slider soundSlider() {
        Slider sound = new Slider(0f, 1f, 0.05f, false, Ui.skin());
        sound.setValue(GameSettings.getInstance().getSoundVolume());
        sound.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameSettings.getInstance().setSoundVolume(sound.getValue());
            }
        });

        return sound;
    }

    private Table row(String title, Actor control) {
        Table row = new Table();
        Label label = Ui.label(title, "medium");
        row.add(label).width(230f).left();
        row.add(control).width(330f).height(46f).right();
        row.pad(8f, 0f, 8f, 0f);
        return row;
    }
}
