package ir.ac.pvz.view.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

public final class Toast {
    private Toast() {
    }

    public static void error(Stage stage, String message) {
        show(stage, message, new Color(0.65f, 0.12f, 0.10f, 0.94f));
    }

    public static void info(Stage stage, String message) {
        show(stage, message, new Color(0.13f, 0.38f, 0.16f, 0.94f));
    }

    public static void show(Stage stage, String message, Color background) {
        Label label = Ui.label(message, "medium");
        label.setAlignment(Align.center);
        label.setWrap(true);

        Table container = new Table();
        container.setBackground(Ui.solid(background));
        container.pad(14f, 26f, 14f, 26f);
        container.add(label).width(460f);
        container.pack();
        container.setPosition((stage.getWidth() - container.getWidth()) / 2f,
                stage.getHeight() - container.getHeight() - 90f);
        container.getColor().a = 0f;
        container.addAction(Actions.sequence(
                Actions.fadeIn(0.18f),
                Actions.delay(2.1f),
                Actions.fadeOut(0.35f),
                Actions.removeActor()));
        stage.addActor(container);
    }
}
