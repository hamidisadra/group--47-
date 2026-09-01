package ir.ac.pvz.view.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;

import ir.ac.pvz.controller.managers.ScreenId;
import ir.ac.pvz.controller.managers.UserManager;
import ir.ac.pvz.controller.online.OnlineAccounts;
import ir.ac.pvz.model.questions.Questions;
import ir.ac.pvz.model.user.Gender;
import ir.ac.pvz.view.PvzGame;
import ir.ac.pvz.view.ui.Toast;
import ir.ac.pvz.view.ui.Ui;

public class RegisterScreen extends BaseMenuScreen {
    private final TextField usernameField = new TextField("", Ui.skin());
    private final TextField passwordField = new TextField("", Ui.skin());
    private final TextField passwordConfirmField = new TextField("", Ui.skin());
    private final TextField nicknameField = new TextField("", Ui.skin());
    private final TextField emailField = new TextField("", Ui.skin());
    private final SelectBox<String> genderBox = new SelectBox<>(Ui.skin());
    private final SelectBox<String> questionBox = new SelectBox<>(Ui.skin());
    private final TextField answerField = new TextField("", Ui.skin());
    private final TextField answerConfirmField = new TextField("", Ui.skin());

    public RegisterScreen(PvzGame game) {
        super(game, "Create Account", true);

        preparePasswordFields();
        prepareChoiceFields();

        Table form = new Table();
        form.setBackground(Ui.panel());
        form.pad(26f);
        form.add(leftColumn()).padRight(30f).top();
        form.add(rightColumn()).top().row();
        form.add(buttonRow()).colspan(2).padTop(20f).row();

        content.add(form).center();
    }

    private void preparePasswordFields() {
        passwordField.setPasswordMode(true);
        passwordField.setPasswordCharacter('*');
        passwordConfirmField.setPasswordMode(true);
        passwordConfirmField.setPasswordCharacter('*');
    }

    private void prepareChoiceFields() {
        genderBox.setItems(new Array<>(new String[] { "Male", "Female" }));

        Array<String> questions = new Array<>();
        for (String question : Questions.getQuestionsList()) {
            questions.add(question);
        }

        questionBox.setItems(questions);
        answerField.setMessageText("your answer");
        answerConfirmField.setMessageText("repeat your answer");
    }

    private Table leftColumn() {
        Table left = new Table();
        left.add(Ui.label("Username", "medium")).left().padBottom(4f).row();
        left.add(usernameField).width(380f).height(48f).padBottom(12f).row();
        left.add(Ui.label("Password", "medium")).left().padBottom(4f).row();
        left.add(passwordField).width(380f).height(48f).padBottom(12f).row();
        left.add(Ui.label("Confirm password", "medium")).left().padBottom(4f).row();
        left.add(passwordConfirmField).width(380f).height(48f).padBottom(12f).row();
        left.add(Ui.label("Nickname", "medium")).left().padBottom(4f).row();
        left.add(nicknameField).width(380f).height(48f).padBottom(12f).row();
        return left;
    }

    private Table rightColumn() {
        Table right = new Table();
        right.add(Ui.label("Email", "medium")).left().padBottom(4f).row();
        right.add(emailField).width(380f).height(48f).padBottom(12f).row();
        right.add(Ui.label("Gender", "medium")).left().padBottom(4f).row();
        right.add(genderBox).width(380f).height(48f).padBottom(12f).row();
        right.add(Ui.label("Security question", "medium")).left().padBottom(4f).row();
        right.add(questionBox).width(380f).height(48f).padBottom(12f).row();
        right.add(answerField).width(380f).height(48f).padBottom(12f).row();
        right.add(answerConfirmField).width(380f).height(48f).row();
        return right;
    }

    private Table buttonRow() {
        TextButton submit = Ui.button("Create Account", "green");
        submit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                attemptRegister();
            }
        });

        TextButton toLogin = Ui.button("Login Menu", "brown");
        toLogin.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.replace(ScreenId.LOGIN);
            }
        });

        Table buttons = new Table();
        buttons.add(submit).width(260f).height(64f).padRight(16f);
        buttons.add(toLogin).width(200f).height(64f);
        return buttons;
    }

    private void attemptRegister() {
        String username = usernameField.getText().trim();
        String nickname = nicknameField.getText().trim();
        String email = emailField.getText().trim();

        String error = validateCredentials(username, nickname, email);
        if (error == null) {
            error = validateSecurityAnswer();
        }

        if (error != null) {
            Toast.error(stage, error);
            return;
        }

        Gender gender = Gender.valueOf(genderBox.getSelected().toUpperCase());
        String failure = OnlineAccounts.register(username,
            passwordField.getText(), nickname, email, gender.name(),
            questionBox.getSelectedIndex() + 1, answerField.getText().trim());

        if (failure != null) {
            Toast.error(stage, failure);
            return;
        }

        game.replace(ScreenId.LOGIN);
    }

    private String validateCredentials(String username, String nickname, String email) {
        UserManager users = UserManager.getInstance();

        if (!users.validateUsername(username)) {
            return "Username may only contain letters, digits and dashes.";
        }

        if (users.isUsernameTaken(username)) {
            return "That username is already taken.";
        }

        String passwordCheck = users.validatePassword(passwordField.getText());
        if (!passwordCheck.equals("Valid")) {
            return passwordCheck;
        }

        if (!passwordField.getText().equals(passwordConfirmField.getText())) {
            return "Password and its confirmation do not match.";
        }

        if (!users.validateNickname(nickname)) {
            return "Nickname must be between 3 and 30 characters.";
        }

        if (!users.validateEmail(email)) {
            return "That email address is not valid.";
        }

        return null;
    }

    private String validateSecurityAnswer() {
        String answer = answerField.getText().trim();

        if (answer.isEmpty()) {
            return "Please answer the security question.";
        }

        if (!answer.equals(answerConfirmField.getText().trim())) {
            return "Answer and its confirmation do not match.";
        }

        return null;
    }
}
