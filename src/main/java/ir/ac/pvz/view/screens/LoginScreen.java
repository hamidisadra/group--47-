package ir.ac.pvz.view.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import ir.ac.pvz.controller.managers.MenuManager;
import ir.ac.pvz.controller.managers.ScreenId;
import ir.ac.pvz.controller.managers.UserManager;
import ir.ac.pvz.controller.online.OnlineAccounts;
import ir.ac.pvz.model.questions.Questions;
import ir.ac.pvz.model.user.User;
import ir.ac.pvz.view.PvzGame;
import ir.ac.pvz.view.ui.Modal;
import ir.ac.pvz.view.ui.Toast;
import ir.ac.pvz.view.ui.Ui;

public class LoginScreen extends BaseMenuScreen {
    private final TextField usernameField;
    private final TextField passwordField;
    private final CheckBox stayLoggedIn;

    public LoginScreen(PvzGame game) {
        super(game, "Sign In", false);

        usernameField = new TextField("", Ui.skin());
        passwordField = new TextField("", Ui.skin());
        passwordField.setPasswordMode(true);
        passwordField.setPasswordCharacter('*');
        stayLoggedIn = new CheckBox(" Stay signed in", Ui.skin());

        Table form = new Table();
        form.setBackground(Ui.panel());
        form.pad(28f);
        form.add(Ui.label("Username", "medium")).left().padBottom(6f).row();
        form.add(usernameField).width(420f).height(52f).padBottom(14f).row();
        form.add(Ui.label("Password", "medium")).left().padBottom(6f).row();
        form.add(passwordField).width(420f).height(52f).padBottom(14f).row();
        form.add(stayLoggedIn).left().padBottom(20f).row();

        Table buttons = new Table();
        buttons.add(action("Sign In", "green", this::attemptLogin)).width(190f).height(64f).pad(6f);
        buttons.add(action("Register", "brown",
            () -> game.navigate(ScreenId.REGISTER))).width(190f).height(64f).pad(6f);
        form.add(buttons).row();
        form.add(action("Forgot password?", "purple", this::showRecovery))
            .width(300f).height(56f).padTop(10f).row();

        content.add(form).center();
    }

    private Actor action(String text, String style, Runnable onClick) {
        com.badlogic.gdx.scenes.scene2d.ui.TextButton button = Ui.button(text, style);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                onClick.run();
            }
        });
        return button;
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        if (username.isEmpty() || password.isEmpty()) {
            Toast.error(stage, "Enter both username and password.");
            return;
        }
        String failure = OnlineAccounts.login(username, password);

        if (failure != null) {
            Toast.error(stage, failure);
            return;
        }

        UserManager users = UserManager.getInstance();
        User user = users.findUserByUsername(username);

        if (user == null) {
            Toast.error(stage, "Signed in, but the profile could not be loaded.");
            return;
        }

        if (stayLoggedIn.isChecked()) {
            users.clearLoggedIn();
            user.setStayLoggedIn(true);
            users.saveAll();
        }
        users.ensurePlayable(user);
        MenuManager.getInstance().loginUser(user);
        game.replace(ScreenId.MAIN);
    }

    private void showRecovery() {
        String username = usernameField.getText().trim();
        UserManager users = UserManager.getInstance();
        User user = users.findUserByUsername(username);

        if (user == null) {
            Toast.error(stage, "Type your username first, then press recover.");
            return;
        }

        TextField emailField = new TextField("", Ui.skin());
        emailField.setMessageText("the email on this account");

        Table body = new Table();
        body.add(Ui.label("Confirm the email of \"" + username + "\"", "medium"))
            .width(480f).left().padBottom(8f).row();
        body.add(emailField).width(480f).height(50f).row();

        Modal modal = new Modal("Password Recovery");
        modal.content(body, 500f);
        modal.action("Cancel", "brown", modal::close);
        modal.action("Continue", "green", () -> {
            String email = emailField.getText().trim();

            if (!users.validateEmail(email)) {
                Toast.error(stage, "That email address is not valid.");
                return;
            }

            if (!user.getEmail().equalsIgnoreCase(email)) {
                Toast.error(stage, "That email does not match this account.");
                return;
            }

            modal.close();
            showRecoveryAnswer(user);
        });

        modal.show(stage);
    }

    private void showRecoveryAnswer(User user) {
        String question = Questions.getQuestionsList().get(user.getSecurityQuestionId() - 1);

        TextField answerField = new TextField("", Ui.skin());
        TextField newPasswordField = new TextField("", Ui.skin());
        newPasswordField.setPasswordMode(true);
        newPasswordField.setPasswordCharacter('*');

        Table body = new Table();
        body.add(Ui.label(question, "medium")).width(480f).left().padBottom(8f).row();
        body.add(answerField).width(480f).height(50f).padBottom(14f).row();
        body.add(Ui.label("New password", "medium")).left().padBottom(8f).row();
        body.add(newPasswordField).width(480f).height(50f).row();

        Modal modal = new Modal("Password Recovery");
        modal.content(body, 500f);
        modal.action("Cancel", "brown", modal::close);
        modal.action("Confirm", "green", () -> {
            if (!user.getSecurityAnswer().equalsIgnoreCase(answerField.getText().trim())) {
                Toast.error(stage, "That answer does not match.");
                return;
            }

            String validation = UserManager.getInstance()
                .validatePassword(newPasswordField.getText());

            if (!validation.equals("Valid")) {
                Toast.error(stage, validation);
                return;
            }

            user.setPasswordHash(UserManager.getInstance()
                .hashPassword(newPasswordField.getText()));
            UserManager.getInstance().saveAll();
            modal.close();
            Toast.info(stage, "Password updated. You can sign in now.");
        });

        modal.show(stage);
    }
}
