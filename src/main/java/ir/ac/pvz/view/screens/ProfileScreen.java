package ir.ac.pvz.view.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import ir.ac.pvz.controller.managers.MenuManager;
import ir.ac.pvz.controller.managers.UserManager;
import ir.ac.pvz.model.user.User;
import ir.ac.pvz.view.PvzGame;
import ir.ac.pvz.view.ui.Modal;
import ir.ac.pvz.view.ui.Toast;
import ir.ac.pvz.view.ui.Ui;

public class ProfileScreen extends BaseMenuScreen {
    private final User user;
    private final Table stats = new Table();

    public ProfileScreen(PvzGame game) {
        super(game, "Profile", true);
        user = MenuManager.getInstance().getActiveUser();
        if (user == null) {
            content.add(Ui.label("You are not signed in.", "medium"));
            return;
        }

        Table panel = new Table();
        panel.setBackground(Ui.panel());
        panel.pad(20f);
        stats.top();
        panel.add(stats).width(470f).top().padRight(24f);

        Table actions = new Table();
        actions.top();
        actions.add(action("Change username", this::changeUsername)).pad(5f).row();
        actions.add(action("Change nickname", this::changeNickname)).pad(5f).row();
        actions.add(action("Change email", this::changeEmail)).pad(5f).row();
        actions.add(action("Change password", this::changePassword)).pad(5f).row();
        panel.add(actions).top().row();

        content.add(panel).center();
        refreshStats();
    }

    private void refreshStats() {
        stats.clear();
        addRow("Username", user.getUsername());
        addRow("Nickname", user.getNickName());
        addRow("Email", user.getEmail());
        addRow("Games played", String.valueOf(user.getGamesCount()));
        addRow("Stages completed", String.valueOf(user.getGameProgress()));
        addRow("Highest MU points", String.valueOf(user.getMaxMuPoint()));
        addRow("Coins", String.valueOf(user.getWallet().getCoins()));
        addRow("Gems", String.valueOf(user.getWallet().getGems()));
    }

    private void addRow(String title, String value) {
        Table row = new Table();
        row.add(Ui.label(title, "medium")).width(240f).left();
        row.add(Ui.label(value == null ? "-" : value, "secondary")).width(220f).left();
        stats.add(row).growX().pad(2f, 0f, 2f, 0f).row();
    }

    private Actor action(String text, Runnable onClick) {
        com.badlogic.gdx.scenes.scene2d.ui.TextButton button = Ui.button(text, "brown");
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                onClick.run();
            }
        });
        Table wrapper = new Table();
        wrapper.add(button).width(320f).height(58f);
        return wrapper;
    }

    private interface Validator {
        boolean accept(String value);
    }

    private void editField(String title, String current, Validator validator) {
        TextField field = new TextField(current == null ? "" : current, Ui.skin());

        Modal modal = new Modal("Edit " + title);
        modal.content(field, 420f);
        modal.action("Cancel", "brown", modal::close);

        modal.action("Save", "green", () -> {
            String value = field.getText().trim();
            if (value.isEmpty()) {
                Toast.error(stage, title + " cannot be empty.");
                return;
            }
            if (!validator.accept(value)) {
                return;
            }
            UserManager.getInstance().saveAll();
            refreshStats();
            modal.close();
            Toast.info(stage, title + " updated.");
        });

        modal.show(stage);
    }

    private void changeUsername() {
        editField("Username", user.getUsername(), value -> {
            UserManager users = UserManager.getInstance();

            if (!users.validateUsername(value)) {
                Toast.error(stage, "Username may only contain letters, digits, dashes.");
                return false;
            }

            if (!value.equals(user.getUsername()) && users.isUsernameTaken(value)) {
                Toast.error(stage, "That username is already taken.");
                return false;
            }

            users.updateUsernameKeyValue(user.getUsername(), value, user);
            return true;
        });
    }

    private void changeNickname() {
        editField("Nickname", user.getNickName(), value -> {
            if (!UserManager.getInstance().validateNickname(value)) {
                Toast.error(stage, "Nickname must be 3 to 30 characters.");
                return false;
            }

            user.setNickName(value);
            return true;
        });
    }

    private void changeEmail() {
        editField("Email", user.getEmail(), value -> {
            if (!UserManager.getInstance().validateEmail(value)) {
                Toast.error(stage, "That email address is not valid.");
                return false;
            }

            user.setEmail(value);
            return true;
        });
    }

    private void changePassword() {
        TextField oldField = new TextField("", Ui.skin());
        TextField newField = new TextField("", Ui.skin());
        oldField.setPasswordMode(true);
        oldField.setPasswordCharacter('*');
        newField.setPasswordMode(true);
        newField.setPasswordCharacter('*');

        Table body = new Table();
        body.add(Ui.label("Current password", "medium")).left().padBottom(6f).row();
        body.add(oldField).width(420f).height(48f).padBottom(14f).row();
        body.add(Ui.label("New password", "medium")).left().padBottom(6f).row();
        body.add(newField).width(420f).height(48f).row();

        Modal modal = new Modal("Change Password");
        modal.content(body, 440f);
        modal.action("Cancel", "brown", modal::close);

        modal.action("Save", "green", () -> {
            UserManager users = UserManager.getInstance();
            if (!user.getPasswordHash().equals(users.hashPassword(oldField.getText()))) {
                Toast.error(stage, "Your current password is not correct.");
                return;
            }
            String check = users.validatePassword(newField.getText());
            if (!check.equals("Valid")) {
                Toast.error(stage, check);
                return;
            }
            user.setPasswordHash(users.hashPassword(newField.getText()));
            users.saveAll();
            modal.close();
            Toast.info(stage, "Password updated.");
        });

        modal.show(stage);
    }
}
