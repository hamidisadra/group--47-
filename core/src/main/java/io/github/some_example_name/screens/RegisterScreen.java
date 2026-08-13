package io.github.some_example_name.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.some_example_name.Main;

import ir.ac.pvz.controller.managers.UserManager;
import ir.ac.pvz.model.questions.Questions;
import ir.ac.pvz.model.user.Gender;

import pvz.skin.BorderedTable;
import pvz.skin.PvzSkin;

import java.util.List;

public class RegisterScreen extends ScreenAdapter  implements Screen {

    private static final String GENDER_PROMPT = "Select...";
    private static final Color TEXT_COLOR = new Color(0.20f, 0.12f, 0.05f, 1f);

    private final Main game;
    private final UserManager userManager = UserManager.getInstance();

    private final Viewport viewport = new FitViewport(1920f, 1080f);
    private final Stage stage = new Stage(viewport);
    private final Skin skin = PvzSkin.get();

    private Label status;

    private String username;
    private String password;
    private String nickname;
    private String email;
    private Gender gender;

    public RegisterScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        buildForm();
        //buildQuestionStep();
    }

    private void buildForm() {
        stage.clear();

        final TextField usernameField = textField("letters, digits, '-'");
        final TextField passwordField = passwordField();
        final TextField confirmField = passwordField();
        final TextField nicknameField = textField("3-30 characters");
        final TextField emailField = textField("name@example.com");

        final SelectBox<String> genderBox = new SelectBox<>(skin);
        genderBox.setItems(GENDER_PROMPT, "Male", "Female");

        TextButton registerButton = new TextButton("Register", skin, "green");
        registerButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                submitForm(usernameField.getText(), passwordField.getText(), confirmField.getText(),
                    nicknameField.getText(), emailField.getText(), genderBox.getSelected());
            }
        });

        status = new Label("", skin);
        status.setWrap(true);

        BorderedTable dialog = new BorderedTable();
        dialog.pad(70f);
        dialog.defaults().pad(8f);
        dialog.add(new Label("Create Account", skin, "big")).colspan(2).padBottom(28f);
        dialog.row();

        addRow(dialog, "Username", usernameField);
        addRow(dialog, "Password", passwordField);
        addRow(dialog, "Confirm Password", confirmField);
        addRow(dialog, "Nickname", nicknameField);
        addRow(dialog, "Email", emailField);
        addRow(dialog, "Gender", genderBox);

        dialog.add(registerButton).colspan(2).width(340f).height(64f).padTop(20f);
        dialog.row();

        dialog.add(status).colspan(2).width(600f).padTop(12f);

        showDialog(dialog);
    }

    private void submitForm(String username, String password, String confirmPassword, String nickName, String email, String genderChoice) {
        username = username.trim();
        nickName = nickName.trim();
        email = email.trim();

        if (username.isEmpty() || password.isEmpty() || nickName.isEmpty() || email.isEmpty()) {
            error("All fields are required.");
            return;
        }

        if (!userManager.validateUsername(username)) {
            error("Invalid username. Use only letters, digits and '-'.");
            return;
        }

        if (userManager.isUsernameTaken(username)) {
            error("Username is already taken!");
            return;
        }

        if (!password.equals(confirmPassword)) {
            error("Passwords do not match!");
            return;
        }

        String passwordStatus = userManager.validatePassword(password);
        if (!passwordStatus.equals("Valid")) {
            error(passwordStatus);
            return;
        }

        if (!userManager.validateNickname(nickName)) {
            error("Nickname length is not valid (3-30 characters).");
            return;
        }

        if (!userManager.validateEmail(email)) {
            error("Invalid email format!");
            return;
        }

        if (GENDER_PROMPT.equals(genderChoice)) {
            error("Please select a gender.");
            return;
        }

        this.username = username;
        this.password = password;
        this.nickname = nickName;
        this.email = email;
        this.gender = "Male".equals(genderChoice) ? Gender.MALE : Gender.FEMALE;
        buildQuestionStep();
    }

    private void buildQuestionStep() {
        stage.clear();

        List<String> questions = Questions.getQuestionsList();
        final SelectBox<String> questionBox = new SelectBox<>(skin);
        questionBox.setItems(questions.toArray(new String[0]));

        final TextField answerField = textField("your answer");
        final TextField confirmField = textField("repeat your answer");

        TextButton createButton = new TextButton("Create Account", skin, "green");
        createButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                int questionId = questionBox.getSelectedIndex() + 1;
                submitQuestion(questionId, answerField.getText(), confirmField.getText());
            }
        });

        TextButton backButton = new TextButton("Back", skin, "brown");
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                buildForm();
            }
        });

        status = new Label("", skin);
        status.setWrap(true);

        BorderedTable dialog = new BorderedTable();
        dialog.pad(70f);
        dialog.defaults().pad(8f);
        dialog.add(new Label("Security Question", skin, "big")).colspan(2).padBottom(24f);
        dialog.row();

        Label question = new Label("Question", skin);
        question.setFontScale(1.30f);
        question.setColor(TEXT_COLOR);

        dialog.add(question).right().padRight(14f);
        dialog.add(questionBox).left().width(600f).height(52f);
        dialog.row();

        addRow(dialog, "Answer", answerField);
        addRow(dialog, "Confirm Answer", confirmField);

        Table buttons = new Table();
        buttons.add(backButton).width(170f).height(60f).padRight(20f);
        buttons.add(createButton).width(320f).height(60f);

        dialog.add(buttons).colspan(2).padTop(8f);
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

        boolean success = userManager.register(username, password, nickname, email, gender, questionId, answer);

        if (success) {
            status.setColor(0.6f, 0.95f, 0.5f, 1f);
            status.setText("Account created!");
            // TODO: game.setScreen(new LoginScreen(game)); once LoginScreen exists.
        } else {
            error("An unexpected error occurred.");
        }
    }

    private void showDialog(Table dialog) {
        Table root = new Table();
        root.setFillParent(true);
        root.setBackground(new TextureRegionDrawable(new Texture(Gdx.files.internal("extra/menu_bg.png"))));
        root.add(dialog);
        stage.addActor(root);
    }

    private TextField textField(String hint) {
        TextField field = new TextField("", skin);
        field.setMessageText(hint);
        return field;
    }

    private TextField passwordField() {
        TextField field = textField("");
        field.setPasswordMode(true);
        field.setPasswordCharacter('*');
        return field;
    }

    private void addRow(Table table, String label, Actor input) {
        table.add(styledLabel(label)).right().padRight(14f);
        table.add(input).left().width(440f).height(52f);
        table.row();
    }

    private Label styledLabel(String text) {
        Label label = new Label(text, skin);
        label.setColor(TEXT_COLOR);
        label.setFontScale(1.3f);
        return label;
    }

    private void error(String message) {
        status.setColor(1f, 0.5f, 0.45f, 1f);
        status.setText(message);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.05f, 0.12f, 0.07f, 1f);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
