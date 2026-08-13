package io.github.some_example_name.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import io.github.some_example_name.Main;

import ir.ac.pvz.controller.managers.MenuManager;
import ir.ac.pvz.controller.managers.UserManager;
import ir.ac.pvz.model.questions.Questions;
import ir.ac.pvz.model.user.User;

public class LoginScreen extends BaseScreen {

    private final UserManager userManager = UserManager.getInstance();
    private final MenuManager menuManager = MenuManager.getInstance();

    public LoginScreen(Main game) {
        super(game);
    }

    @Override
    protected void build() {
        buildLogin();
    }

    private void buildLogin() {
        final TextField usernameField = textField("your username");
        final TextField passwordField = passwordField();
        final CheckBox stayLoggedIn = new CheckBox(" Stay logged in", skin);

        status = statusLabel();

        TextButton backButton = button("Back", "brown", new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new RegisterScreen(game));
            }
        });

        TextButton loginButton = button("Login", "green", new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                submitLogin(usernameField.getText(), passwordField.getText(), stayLoggedIn.isChecked());
            }
        });

        TextButton forgotButton = button("Forgot Password", "purple", new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                buildForgot();
            }
        });

        Table dialog = dialog("Login");

        addRow(dialog, "Username", usernameField);
        addRow(dialog, "Password", passwordField);

        dialog.add();
        dialog.add(stayLoggedIn).left();
        dialog.row();

        Table buttons = new Table();
        buttons.add(backButton).width(200f).height(64f).padRight(20f);
        buttons.add(loginButton).width(260f).height(64f).padRight(20f);
        buttons.add(forgotButton).width(300f).height(64f);

        dialog.add(buttons).colspan(2).padTop(20f);
        dialog.row();

        dialog.add(status).colspan(2).width(620f).padTop(12f);

        showDialog(dialog);
    }

    private void submitLogin(String username, String password, boolean stayLoggedIn) {
        username = username.trim();

        if (username.isEmpty() || password.isEmpty()) {
            error("Username and password are required.");
            return;
        }

        if (!userManager.validateUsername(username)) {
            error("Invalid username format!");
            return;
        }

        User user = userManager.findUserByUsername(username);
        if (user == null) {
            error("There is no user with this username.");
            return;
        }

        String passwordStatus = userManager.validatePassword(password);
        if (!passwordStatus.equals("Valid")) {
            error("Invalid password format!");
            return;
        }

        if (!user.getPasswordHash().equals(userManager.hashPassword(password))) {
            error("Incorrect password!");
            return;
        }

        userManager.clearLoggedIn();

        if (stayLoggedIn) {
            user.setStayLoggedIn(true);
        }

        menuManager.loginUser(user);
        userManager.saveAll();

        success("Logged in successfully!");
    }

    private void buildForgot() {
        final TextField usernameField = textField("your username");
        final TextField emailField = textField("your email");

        status = statusLabel();

        TextButton continueButton = button("Continue", "green", new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                submitForgot(usernameField.getText(), emailField.getText());
            }
        });

        TextButton backButton = button("Back", "brown", new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                buildLogin();
            }
        });

        Table dialog = dialog("Reset Password");
        addRow(dialog, "Username", usernameField);
        addRow(dialog, "Email", emailField);

        Table buttons = new Table();
        buttons.add(backButton).width(170f).height(60f).padRight(20f);
        buttons.add(continueButton).width(260f).height(60f);

        dialog.add(buttons).colspan(2).padTop(20f);
        dialog.row();

        dialog.add(status).colspan(2).width(620f).padTop(12f);

        showDialog(dialog);
    }

    private void submitForgot(String username, String email) {
        username = username.trim();
        email = email.trim();

        if (username.isEmpty() || email.isEmpty()) {
            error("Username and email are required.");
            return;
        }

        if (!userManager.validateUsername(username)) {
            error("Invalid username format!");
            return;
        }

        User user = userManager.findUserByUsername(username);
        if (user == null) {
            error("There is no user with this username.");
            return;
        }

        if (!userManager.validateEmail(email)) {
            error("Invalid email format!");
            return;
        }

        if (!user.getEmail().equals(email)) {
            error("Incorrect email!");
            return;
        }

        buildReset(user);
    }

    private void buildReset(final User user) {
        String questionText = Questions.getQuestionsList().get(user.getSecurityQuestionId() - 1);

        final TextField answerField = textField("your answer");
        final TextField newPasswordField = passwordField();
        final TextField confirmField = passwordField();

        status = statusLabel();

        TextButton resetButton = button("Reset Password", "green", new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                submitReset(user, answerField.getText(), newPasswordField.getText(), confirmField.getText());
            }
        });

        TextButton backButton = button("Back", "brown", new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                buildLogin();
            }
        });

        Table dialog = dialog("Security Question");

        dialog.add(styledLabel("Question")).right().padRight(14f);

        Label questionLabel = new Label(questionText, skin);
        questionLabel.setColor(TEXT_COLOR);
        questionLabel.setWrap(true);
        dialog.add(questionLabel).left().width(560f);
        dialog.row();

        addRow(dialog, "Answer", answerField);
        addRow(dialog, "New Password", newPasswordField);
        addRow(dialog, "Confirm Password", confirmField);

        Table buttons = new Table();
        buttons.add(backButton).width(170f).height(60f).padRight(20f);
        buttons.add(resetButton).width(320f).height(60f);

        dialog.add(buttons).colspan(2).padTop(16f);
        dialog.row();

        dialog.add(status).colspan(2).width(700f).padTop(12f);

        showDialog(dialog);
    }

    private void submitReset(User user, String answer, String newPassword, String confirm) {
        answer = answer.trim();

        if (!answer.equals(user.getSecurityAnswer())) {
            error("Answer is not correct!");
            return;
        }

        String passwordStatus = userManager.validatePassword(newPassword);
        if (!passwordStatus.equals("Valid")) {
            error(passwordStatus);
            return;
        }

        if (!newPassword.equals(confirm)) {
            error("Passwords do not match!");
            return;
        }

        user.setPasswordHash(userManager.hashPassword(newPassword));
        userManager.saveAll();
        success("Password changed successfully. You can log in now.");
    }
}
