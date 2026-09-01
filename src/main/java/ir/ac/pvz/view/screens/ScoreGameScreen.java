package ir.ac.pvz.view.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import ir.ac.pvz.controller.gui.CampaignController;
import ir.ac.pvz.controller.managers.MenuManager;
import ir.ac.pvz.model.chapter.Chapter;
import ir.ac.pvz.model.stage.Stage;
import ir.ac.pvz.model.user.User;
import ir.ac.pvz.view.PvzGame;
import ir.ac.pvz.view.ui.Ui;

import java.time.LocalDate;

public class ScoreGameScreen extends BaseMenuScreen {
    private static final String[][] PATTERNS = {
        { "Multi hit", "One shot that damages several zombies." },
        { "Speed kill", "Take a zombie down moments after it appears." },
        { "Simultaneous kills", "Several zombies fall at the same moment." },
        { "Chain kill", "Keep a streak of kills going without a pause." },
        { "Flawless defense", "Finish waves without losing a single plant." }
    };

    public ScoreGameScreen(PvzGame game) {
        super(game, "Scored Game", true);

        User user = MenuManager.getInstance().getActiveUser();
        int best = user == null ? 0 : user.getMaxMuPoint();

        Table panel = new Table();
        panel.setBackground(Ui.panel());
        panel.pad(26f);

        panel.add(Ui.label("Today's challenge", "medium")).padBottom(6f).row();
        panel.add(Ui.label(LocalDate.now().toString() + "   seed " + dailySeed(),
                "secondary")).padBottom(4f).row();

        Label note = Ui.label("Every player faces the same zombies today. "
                + "Score comes from how stylishly you defend, not just from winning.",
                "secondary");
        note.setWrap(true);
        note.setAlignment(Align.center);
        panel.add(note).width(520f).padBottom(16f).row();

        panel.add(Ui.label("Your best: " + best + " MU points", "medium")).padBottom(18f).row();

        Table patterns = new Table();
        for (String[] pattern : PATTERNS) {
            patterns.add(Ui.label(pattern[0], "medium")).left().padRight(16f);
            Label detail = Ui.label(pattern[1], "secondary");
            detail.setFontScale(0.85f);
            patterns.add(detail).left().row();
        }

        panel.add(patterns).padBottom(20f).row();

        TextButton play = Ui.button("Start today's run", "green");
        play.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                startRun();
            }
        });

        panel.add(play).width(300f).height(64f);
        content.add(panel).center();
    }

    private void startRun() {
        Chapter chapter = CampaignController.getInstance().getChapters().isEmpty()
                ? null : CampaignController.getInstance().getChapters().get(0);

        if (chapter == null || chapter.getStages().isEmpty()) {
            return;
        }

        Stage stage = chapter.getStages().get(0);
        game.setPendingChapter(chapter);
        game.setPendingStage(stage);
        game.setScoredSeed(dailySeed());
        game.navigate(ir.ac.pvz.controller.managers.ScreenId.PLANT_SELECTION);
    }

    public static long dailySeed() {
        return LocalDate.now().toEpochDay();
    }
}
