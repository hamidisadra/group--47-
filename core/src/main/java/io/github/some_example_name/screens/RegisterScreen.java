package io.github.some_example_name.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import io.github.some_example_name.Main;

import ir.ac.pvz.controller.managers.UserManager;
import ir.ac.pvz.model.questions.Questions;
import ir.ac.pvz.model.user.Gender;

import java.util.List;

public class RegisterScreen extends BaseScreen {

    private static final String GENDER_PROMPT = "Select...";

    private final UserManager userManager = UserManager.getInstance();

    private String username;
    private String password;
    private String nickname;
    private String email;
    private Gender gender;

    public RegisterScreen(Main game) {
        super(game);
    }

    @Override
    protected void build() {
        buildForm();
    }

    private void buildForm() {
        final TextField usernameField = textField("letters, digits, '-'");
        final TextField passwordField = passwordField();
        final TextField confirmField = passwordField();
        final TextField nicknameField = textField("3-30 characters");
        final TextField emailField = textField("name@example.com");

        final SelectBox<String> genderBox = new SelectBox<>(skin);
        genderBox.setItems(GENDER_PROMPT, "Male", "Female");

        status = statusLabel();

        TextButton registerButton = button("Register", "green", new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                submitForm(usernameField.getText(), passwordField.getText(), confirmField.getText(),
                    nicknameField.getText(), emailField.getText(), genderBox.getSelected());
            }
        });

        TextButton loginButton = button("Login", "purple", new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new LoginScreen(game));
            }
        });

        Table dialog = dialog("Create Account");

        addRow(dialog, "Username", usernameField);
        addRow(dialog, "Password", passwordField);
        addRow(dialog, "Confirm Password", confirmField);
        addRow(dialog, "Nickname", nicknameField);
        addRow(dialog, "Email", emailField);
        addRow(dialog, "Gender", genderBox);

        Table buttons = new Table();
        buttons.add(registerButton).width(200f).height(64f).padRight(20f);
        buttons.add(loginButton).width(260f).height(64f).padRight(20f);

        dialog.add(buttons).colspan(2).padTop(20f);
        dialog.row();

        dialog.add(status).colspan(2).width(600f).padTop(12f);

        showDialog(dialog);
    }

    private void submitForm(String user, String pass, String confirm, String nick, String mail, String genderChoice) {
        user = user.trim();
        nick = nick.trim();
        mail = mail.trim();

        if (user.isEmpty() || pass.isEmpty() || nick.isEmpty() || mail.isEmpty()) {
            error("All fields are required.");
            return;
        }

        if (!userManager.validateUsername(user)) {
            error("Invalid username. Use only letters, digits and '-'.");
            return;
        }

        if (userManager.isUsernameTaken(user)) {
            error("Username is already taken!");
            return;
        }

        if (!pass.equals(confirm)) {
            error("Passwords do not match!");
            return;
        }

        String passwordStatus = userManager.validatePassword(pass);
        if (!passwordStatus.equals("Valid")) {
            error(passwordStatus);
            return;
        }

        if (!userManager.validateNickname(nick)) {
            error("Nickname length is not valid (3-30 characters).");
            return;
        }

        if (!userManager.validateEmail(mail)) {
            error("Invalid email format!");
            return;
        }

        if (GENDER_PROMPT.equals(genderChoice)) {
            error("Please select a gender.");
            return;
        }

        this.username = user;
        this.password = pass;
        this.nickname = nick;
        this.email = mail;
        this.gender = "Male".equals(genderChoice) ? Gender.MALE : Gender.FEMALE;

        buildQuestionStep();
    }

    private void buildQuestionStep() {
        List<String> questions = Questions.getQuestionsList();
        final SelectBox<String> questionBox = new SelectBox<>(skin);
        questionBox.setItems(questions.toArray(new String[0]));

        final TextField answerField = textField("your answer");
        final TextField confirmField = textField("repeat your answer");

        status = statusLabel();

        TextButton createButton = button("Create Account", "green", new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                submitQuestion(questionBox.getSelectedIndex() + 1, answerField.getText(), confirmField.getText());
            }
        });

        TextButton backButton = button("Back", "brown", new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                buildForm();
            }
        });

        Table dialog = dialog("Security Question");
        dialog.add(styledLabel("Question")).right().padRight(14f);
        dialog.add(questionBox).left().width(600f).height(52f);
        dialog.row();

        addRow(dialog, "Answer", answerField);
        addRow(dialog, "Confirm Answer", confirmField);

        Table buttons = new Table();
        buttons.add(backButton).width(170f).height(60f).padRight(20f);
        buttons.add(createButton).width(320f).height(60f);
        dialog.add(buttons).colspan(2).padTop(16f);
        dialog.row();
        dialog.add(status).colspan(2).width(760f).padTop(12f);

        showDialog(dialog);
    }

    private void submitQuestion(int questionId, String answer, String confirm) {
        answer = answer.trim();
        confirm = confirm.trim();

        if (answer.isEmpty()) {
            error("Answer cannot be empty.");
            return;
        }
        if (!answer.equals(confirm)) {
            error("Answers do not match!");
            return;
        }

        boolean created = userManager.register(username, password, nickname, email, gender, questionId, answer);
        if (created) {
            game.setScreen(new LoginScreen(game));
        } else {
            error("An unexpected error occurred.");
        }
    }
}
