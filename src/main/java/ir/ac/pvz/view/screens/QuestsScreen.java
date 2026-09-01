package ir.ac.pvz.view.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import ir.ac.pvz.controller.gui.QuestSeeder;
import ir.ac.pvz.controller.managers.MenuManager;
import ir.ac.pvz.controller.managers.ScreenId;
import ir.ac.pvz.controller.managers.UserManager;
import ir.ac.pvz.model.travel.Quest;
import ir.ac.pvz.model.user.User;
import ir.ac.pvz.view.PvzGame;
import ir.ac.pvz.view.ui.Toast;
import ir.ac.pvz.view.ui.Ui;

public class QuestsScreen extends BaseMenuScreen {
    private final Table list = new Table();
    private final User user;
    private String page = QuestSeeder.PAGE_ADVENTURE;

    public QuestsScreen(PvzGame game) {
        super(game, "Quests", true);
        user = MenuManager.getInstance().getActiveUser();

        Table tabs = new Table();
        tabs.add(tabButton("Adventure", QuestSeeder.PAGE_ADVENTURE)).pad(4f);
        tabs.add(tabButton("Special", QuestSeeder.PAGE_SPECIAL)).pad(4f);
        tabs.add(tabButton("Mini-games", QuestSeeder.PAGE_MINIGAMES)).pad(4f);
        tabs.add(tabButton("Challenge", QuestSeeder.PAGE_CHALLENGE)).pad(4f);
        tabs.add(tabButton("Mystery", QuestSeeder.PAGE_MYSTERY)).pad(4f);
        tabs.add(tabButton("Daily", QuestSeeder.PAGE_DAILY)).pad(4f);
        tabs.add(navButton("Play mini-games")).pad(4f).padLeft(20f);
        content.add(tabs).padBottom(12f).row();

        list.top();
        ScrollPane pane = new ScrollPane(list, Ui.skin());
        pane.setFadeScrollBars(false);
        content.add(pane).width(940f).grow();
        rebuild();
    }

    private Actor tabButton(String text, String target) {
        com.badlogic.gdx.scenes.scene2d.ui.TextButton button =
            Ui.button(text, target.equals(page) ? "green" : "brown");
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                page = target;
                rebuild();
            }
        });
        Table wrapper = new Table();
        wrapper.add(button).width(150f).height(52f);
        return wrapper;
    }

    private Actor navButton(String text) {
        com.badlogic.gdx.scenes.scene2d.ui.TextButton button = Ui.button(text, "purple");
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.navigate(ScreenId.MINIGAMES);
            }
        });
        Table wrapper = new Table();
        wrapper.add(button).width(200f).height(52f);
        return wrapper;
    }

    private void rebuild() {
        list.clear();
        if (user == null) {
            list.add(Ui.label("You are not signed in.", "medium"));
            return;
        }

        java.util.List<Quest> quests = user.getQuestLog().getPage(page);
        if (quests == null || quests.isEmpty()) {
            list.add(Ui.label("No quests on this page yet.", "medium")).pad(24f);
            return;
        }

        quests.sort((first, second) ->
            first.getPriority().compareTo(second.getPriority()));
        for (Quest quest : quests) {
            list.add(questCard(quest)).width(900f).padBottom(12f).row();
        }
    }

    private Table questCard(Quest quest) {
        Table card = new Table();
        card.setBackground(Ui.panel());
        card.pad(16f);

        Table info = new Table();
        info.add(Ui.label(quest.getTitle(), "medium")).left().row();
        Label description = Ui.label(quest.getDescription(), "secondary");
        description.setWrap(true);
        info.add(description).width(560f).left().padBottom(6f).row();

        ProgressBar bar = new ProgressBar(0f, 1f, 0.01f, false, Ui.skin(),
            Ui.skin().has("xp_yellow", ProgressBar.ProgressBarStyle.class)
                ? "xp_yellow" : "default-horizontal");
        bar.setValue(quest.getTargetProgress() <= 0 ? 1f
            : Math.min(1f, quest.getCurrentProgress() / (float) quest.getTargetProgress()));
        info.add(bar).width(560f).left().row();
        info.add(Ui.label(quest.getCurrentProgress() + " / " + quest.getTargetProgress(),
            "secondary")).left().row();
        card.add(info).growX().left();

        Table right = new Table();
        right.add(Ui.label(String.valueOf(quest.getPriority()), "secondary")).row();
        com.badlogic.gdx.scenes.scene2d.ui.TextButton claim =
            Ui.button(quest.isCompleted() ? "Claimed" : "Claim", "green");
        claim.setDisabled(quest.isCompleted() || !quest.checkCompletion());
        claim.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                boolean granted = user.getQuestLog().completeQuest(quest.getId(),
                    user.getWallet(), user.getCollection(), user.getInventory());
                if (granted) {
                    UserManager.getInstance().saveAll();
                    Toast.info(stage, "Reward collected.");
                    rebuild();
                }

                else {
                    Toast.error(stage, "This quest is not finished yet.");
                }
            }
        });
        right.add(claim).width(180f).height(56f).padTop(8f);
        card.add(right).right();
        return card;
    }
}
