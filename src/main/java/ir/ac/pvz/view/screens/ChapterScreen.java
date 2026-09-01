package ir.ac.pvz.view.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import ir.ac.pvz.controller.gui.CampaignController;
import ir.ac.pvz.controller.managers.MenuManager;
import ir.ac.pvz.controller.managers.ScreenId;
import ir.ac.pvz.model.chapter.Chapter;
import ir.ac.pvz.model.user.User;
import ir.ac.pvz.view.PvzGame;
import ir.ac.pvz.view.ui.Toast;
import ir.ac.pvz.view.ui.Ui;

public class ChapterScreen extends BaseMenuScreen {

    public ChapterScreen(PvzGame game, Chapter chapter) {
        super(game, chapter == null ? "Chapter" : chapter.getName(), true);
        if (chapter == null) {
            content.add(Ui.label("No chapter selected.", "medium"));
            return;
        }
        CampaignController campaign = CampaignController.getInstance();
        User user = MenuManager.getInstance().getActiveUser();
        int progress = user == null ? 0 : user.getGameProgress();

        Table grid = new Table();
        int column = 0;
        for (ir.ac.pvz.model.stage.Stage levelStage : chapter.getStages()) {
            grid.add(stageCard(chapter, levelStage, campaign, progress)).width(210f).pad(8f);
            column++;
            if (column % 4 == 0) {
                grid.row();
            }
        }
        content.add(grid).center();
    }

    private Table stageCard(Chapter chapter, ir.ac.pvz.model.stage.Stage levelStage, CampaignController campaign,
                            int progress) {
        boolean unlocked = campaign.isStageUnlocked(chapter, levelStage, progress);
        boolean cleared = campaign.globalIndex(chapter, levelStage) <= progress;

        Table card = new Table();
        card.setBackground(Ui.panel());
        card.pad(14f);

        card.add(Ui.label("Stage " + levelStage.getNumber(), "medium")).padBottom(4f).row();
        Label kind = Ui.label(campaign.describe(levelStage), "secondary");
        kind.setWrap(true);
        kind.setAlignment(Align.center);
        kind.setFontScale(0.85f);
        card.add(kind).width(180f).padBottom(8f).row();
        card.add(Ui.label(cleared ? "cleared" : unlocked ? "available" : "locked",
                "secondary")).padBottom(10f).row();

        com.badlogic.gdx.scenes.scene2d.ui.TextButton play =
                Ui.button(unlocked ? "Play" : "Locked", unlocked ? "green_small" : "brown");
        play.setDisabled(!unlocked);
        play.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!unlocked) {
                    Toast.error(stage, "Clear the previous stage first.");
                    return;
                }
                game.setPendingChapter(chapter);
                game.setPendingStage(levelStage);
                if (levelStage instanceof ir.ac.pvz.model.stage.BossStage
                        || levelStage instanceof ir.ac.pvz.model.stage.ConveyorBeltStage) {
                    game.startBattle(new java.util.ArrayList<>());
                }

                else {
                    game.navigate(ScreenId.PLANT_SELECTION);
                }
            }
        });
        card.add(play).width(170f).height(54f).row();
        return card;
    }

}
