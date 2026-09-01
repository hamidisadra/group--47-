package ir.ac.pvz.view.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import ir.ac.pvz.controller.managers.MenuManager;
import ir.ac.pvz.controller.managers.ScreenId;
import ir.ac.pvz.controller.managers.UserManager;
import ir.ac.pvz.model.user.News;
import ir.ac.pvz.model.user.User;
import ir.ac.pvz.view.PvzGame;
import ir.ac.pvz.view.ui.Ui;

public class MainMenuScreen extends BaseMenuScreen {

    public MainMenuScreen(PvzGame game) {
        super(game, "Plants vs. Zombies 2", false);
        game.jukebox.playMusic("menu");

        Table menu = new Table();
        menu.setBackground(Ui.panel());
        menu.pad(26f);

        menu.add(entry("Adventure", "green", ScreenId.ADVENTURE)).row();
        menu.add(entry("Scored Game", "green", ScreenId.SCORE_GAME)).row();
        menu.add(entry("Mini-games", "default", ScreenId.MINIGAMES)).row();
        menu.add(entry("Travel Log", "default", ScreenId.QUESTS)).row();
        menu.add(entry("Collection", "default", ScreenId.COLLECTION)).row();
        menu.add(entry("Greenhouse", "default", ScreenId.GREENHOUSE)).row();
        menu.add(entry("Shop", "default", ScreenId.SHOP)).row();

        Table second = new Table();
        second.setBackground(Ui.panel());
        second.pad(26f);
        second.add(newsEntry()).row();
        second.add(entry("Leaderboard", "default", ScreenId.LEADERBOARD)).row();
        second.add(entry("Profile", "default", ScreenId.PROFILE)).row();
        second.add(entry("Settings", "default", ScreenId.SETTINGS)).row();
        second.add(logout()).row();

        Table columns = new Table();
        columns.add(menu).padRight(28f).top();
        columns.add(second).top();
        content.add(columns).center();
    }

    private Actor entry(String text, String style, ScreenId target) {
        com.badlogic.gdx.scenes.scene2d.ui.TextButton button = Ui.button(text, style);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.navigate(target);
            }
        });
        Table wrapper = new Table();
        wrapper.add(button).width(300f).height(66f).pad(5f);
        return wrapper;
    }

    private Actor newsEntry() {
        int unread = countUnreadNews();
        com.badlogic.gdx.scenes.scene2d.ui.TextButton button =
            Ui.button("News", unread > 0 ? "purple" : "default");
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.navigate(ScreenId.NEWS);
            }
        });
        Table wrapper = new Table();
        wrapper.add(button).width(unread > 0 ? 240f : 300f).height(66f).pad(5f);
        if (unread > 0) {
            Label badge = Ui.label("! " + unread, "medium");
            Table badgeBox = new Table();
            badgeBox.setBackground(Ui.solid(new com.badlogic.gdx.graphics.Color(
                0.75f, 0.15f, 0.12f, 0.95f)));
            badgeBox.add(badge).pad(6f, 12f, 6f, 12f);
            wrapper.add(badgeBox).height(52f).pad(5f);
        }

        return wrapper;
    }

    private int countUnreadNews() {
        User user = MenuManager.getInstance().getActiveUser();
        if (user == null) {
            return 0;
        }

        int unread = 0;
        for (News news : user.getNewsList()) {
            if (!news.isRead()) {
                unread++;
            }
        }

        return unread;
    }

    private Actor logout() {
        com.badlogic.gdx.scenes.scene2d.ui.TextButton button = Ui.button("Log Out", "brown");
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                UserManager.getInstance().clearLoggedIn();
                MenuManager.getInstance().logoutUser();
                game.replace(ScreenId.LOGIN);
            }
        });
        Table wrapper = new Table();
        wrapper.add(button).width(300f).height(66f).pad(5f);
        return wrapper;
    }
}
