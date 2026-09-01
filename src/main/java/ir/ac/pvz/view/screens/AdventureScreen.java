package ir.ac.pvz.view.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import ir.ac.pvz.controller.gui.CampaignController;
import ir.ac.pvz.controller.managers.MenuManager;
import ir.ac.pvz.controller.managers.ScreenId;
import ir.ac.pvz.model.chapter.Chapter;
import ir.ac.pvz.model.user.User;
import ir.ac.pvz.view.PvzGame;
import ir.ac.pvz.view.ui.Toast;
import ir.ac.pvz.view.ui.Ui;

public class AdventureScreen extends BaseMenuScreen {

    public AdventureScreen(PvzGame game) {
        super(game, "Adventure", true);
        CampaignController campaign = CampaignController.getInstance();
        User user = MenuManager.getInstance().getActiveUser();
        int progress = user == null ? 0 : user.getGameProgress();

        Table shortcuts = new Table();
        shortcuts.add(navButton("Quests", ScreenId.QUESTS)).pad(6f);
        shortcuts.add(navButton("Mini-games", ScreenId.MINIGAMES)).pad(6f);
        content.add(shortcuts).padBottom(16f).row();

        Table list = new Table();
        for (Chapter chapter : campaign.getChapters()) {
            list.add(chapterCard(chapter, campaign, progress)).width(280f).pad(8f);
        }
        content.add(list).center();
    }

    private Actor navButton(String text, ScreenId target) {
        com.badlogic.gdx.scenes.scene2d.ui.TextButton button = Ui.button(text, "purple");
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.navigate(target);
            }
        });
        button.getLabel().setFontScale(0.9f);
        Table wrapper = new Table();
        wrapper.add(button).width(230f).height(60f);
        return wrapper;
    }

    private Table chapterCard(Chapter chapter, CampaignController campaign, int progress) {
        boolean unlocked = campaign.isChapterUnlocked(chapter, progress);
        int completed = campaign.completedStages(chapter, progress);

        Table card = new Table();
        card.setBackground(Ui.panel());
        card.pad(18f);

        Label title = Ui.label(chapter.getName(), "medium");
        title.setWrap(true);
        title.setAlignment(com.badlogic.gdx.utils.Align.center);
        card.add(title).width(230f).padBottom(10f).row();

        ProgressBar bar = new ProgressBar(0f, 1f, 0.01f, false, Ui.skin(),
                Ui.skin().has("xp_teal", ProgressBar.ProgressBarStyle.class)
                        ? "xp_teal" : "default-horizontal");
        bar.setValue(completed / (float) CampaignController.STAGES_PER_CHAPTER);
        card.add(bar).width(220f).padBottom(6f).row();
        card.add(Ui.label(completed + " / " + CampaignController.STAGES_PER_CHAPTER
                + " stages", "secondary")).padBottom(12f).row();

        com.badlogic.gdx.scenes.scene2d.ui.TextButton open =
                Ui.button(unlocked ? "Play" : "Locked", unlocked ? "green" : "brown");
        open.setDisabled(!unlocked);
        open.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!unlocked) {
                    Toast.error(stage, "Finish the previous chapter first.");
                    return;
                }
                game.setPendingChapter(chapter);
                game.navigate(ScreenId.CHAPTER);
            }
        });
        card.add(open).width(200f).height(60f).row();
        return card;
    }
}
