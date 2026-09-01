package ir.ac.pvz.view.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import ir.ac.pvz.controller.managers.MenuManager;
import ir.ac.pvz.controller.managers.UserManager;
import ir.ac.pvz.model.user.News;
import ir.ac.pvz.model.user.User;
import ir.ac.pvz.view.PvzGame;
import ir.ac.pvz.view.ui.Ui;

public class NewsScreen extends BaseMenuScreen {

    public NewsScreen(PvzGame game) {
        super(game, "News", true);

        Table list = new Table();
        list.top();
        User user = MenuManager.getInstance().getActiveUser();

        if (user == null || user.getNewsList().isEmpty()) {
            list.add(Ui.label("There is no news yet.", "medium")).pad(30f);
        }

        else {
            for (News news : user.getNewsList()) {
                list.add(entry(news)).width(860f).padBottom(12f).row();
                news.markAsRead();
            }
            UserManager.getInstance().saveAll();
        }

        ScrollPane pane = new ScrollPane(list, Ui.skin());
        pane.setFadeScrollBars(false);
        content.add(pane).grow();
    }

    private Table entry(News news) {
        Table card = new Table();
        card.setBackground(Ui.panel());
        card.pad(16f);

        Table header = new Table();
        Label type = Ui.label(String.valueOf(news.getType()), "medium");
        Label date = Ui.label(news.getFormattedDate(), "secondary");
        header.add(type).left().expandX();
        header.add(date).right();
        card.add(header).growX().padBottom(8f).row();

        Label body = Ui.label(news.getMessage(), "secondary");
        body.setWrap(true);
        card.add(body).width(820f).left().row();

        if (!news.isRead()) {
            card.add(Ui.label("new", "medium")).left().padTop(6f).row();
        }
        return card;
    }
}
