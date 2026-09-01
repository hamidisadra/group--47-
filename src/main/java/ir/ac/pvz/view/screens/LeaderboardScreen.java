package ir.ac.pvz.view.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import ir.ac.pvz.model.travel.Leaderboard;
import ir.ac.pvz.controller.online.RemoteLeaderboard;
import ir.ac.pvz.model.travel.LeaderboardEntry;
import ir.ac.pvz.view.PvzGame;
import ir.ac.pvz.view.ui.Ui;

public class LeaderboardScreen extends BaseMenuScreen {
    private final Leaderboard leaderboard;
    private final Table rows = new Table();
    private String sortColumn = "score";
    private boolean ascending;

    public LeaderboardScreen(PvzGame game) {
        super(game, "Leaderboard", true);
        leaderboard = RemoteLeaderboard.load();

        Table sorters = new Table();
        sorters.add(sorter("High score", "score")).pad(4f);
        sorters.add(sorter("Stage", "stage")).pad(4f);
        sorters.add(sorter("Minigames", "minigames")).pad(4f);
        sorters.add(sorter("Daily quests", "daily")).pad(4f);
        sorters.add(sorter("Other quests", "nondaily")).pad(4f);

        Table panel = new Table();
        panel.setBackground(Ui.panel());
        panel.pad(20f);
        panel.add(sorters).padBottom(12f).row();

        rows.top();
        ScrollPane pane = new ScrollPane(rows, Ui.skin());
        pane.setFadeScrollBars(false);
        panel.add(pane).width(880f).height(420f).row();

        content.add(panel).center();
        rebuild();
    }

    private String scoreText(LeaderboardEntry entry) {
        if (!entry.hasRankedScore()) {
            return "\u2014";
        }

        return String.valueOf(entry.getHighScore());
    }

    private Actor sorter(String text, String column) {
        com.badlogic.gdx.scenes.scene2d.ui.TextButton button = Ui.button(text, "brown");
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ascending = column.equals(sortColumn) && !ascending;
                sortColumn = column;
                rebuild();
            }
        });
        return button;
    }

    private void rebuild() {
        rows.clear();
        leaderboard.sortBy(sortColumn, ascending);

        Table header = new Table();
        header.add(Ui.label("#", "medium")).width(50f);
        header.add(Ui.label("Player", "medium")).width(230f).left();
        header.add(Ui.label("Score", "medium")).width(130f);
        header.add(Ui.label("Chapter", "medium")).width(200f).left();
        header.add(Ui.label("Stage", "medium")).width(100f);
        header.add(Ui.label("Mini", "medium")).width(90f);
        header.add(Ui.label("Daily", "medium")).width(90f);
        header.add(Ui.label("Other", "medium")).width(90f);
        rows.add(header).padBottom(10f).row();

        int position = 1;
        for (LeaderboardEntry entry : leaderboard.getEntries()) {
            Table row = new Table();
            row.add(Ui.label(String.valueOf(position), "secondary")).width(50f);
            row.add(Ui.label(entry.getUsername(), "secondary")).width(230f).left();
            row.add(Ui.label(scoreText(entry), "secondary")).width(130f);
            row.add(Ui.label(entry.getLastChapter() == null ? "-" : entry.getLastChapter(),
                "secondary")).width(200f).left();
            row.add(Ui.label(String.valueOf(entry.getLastStage()), "secondary")).width(100f);
            row.add(Ui.label(String.valueOf(entry.getMinigamesCompleted()), "secondary"))
                .width(90f);
            row.add(Ui.label(String.valueOf(entry.getDailyQuestsCompleted()), "secondary"))
                .width(90f);
            row.add(Ui.label(String.valueOf(entry.getNonDailyQuestsCompleted()), "secondary"))
                .width(90f);
            rows.add(row).padBottom(6f).row();
            position++;
        }

        if (position == 1) {
            rows.add(Ui.label("No scores recorded yet.", "medium")).pad(20f);
        }
    }
}
