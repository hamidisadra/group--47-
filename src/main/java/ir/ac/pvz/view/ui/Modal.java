package ir.ac.pvz.view.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

public class Modal extends Table {
    private final Table panel;
    private final Table buttons;
    private boolean closing;

    public Modal(String title) {
        setFillParent(true);
        setBackground(Ui.dim());
        setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled);
        addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                event.stop();
            }
        });

        panel = new Table();
        panel.setBackground(Ui.panel());
        panel.pad(30f);

        Label heading = Ui.label(title, "big");
        heading.setAlignment(Align.center);
        panel.add(heading).padBottom(18f).row();

        buttons = new Table();
        add(panel);
        getColor().a = 0f;
        addAction(Actions.fadeIn(0.16f));
    }

    public Modal content(Actor actor, float width) {
        panel.add(actor).width(width).padBottom(18f).row();
        return this;
    }

    public Modal message(String text) {
        Label label = Ui.label(text, "medium");
        label.setWrap(true);
        label.setAlignment(Align.center);
        return content(label, 520f);
    }

    public Modal action(String text, String style, Runnable onClick) {
        com.badlogic.gdx.scenes.scene2d.ui.TextButton button = Ui.button(text, style);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                onClick.run();
            }
        });
        buttons.add(button).minWidth(160f).height(62f).pad(6f);
        return this;
    }

    public Modal action(String text, Runnable onClick) {
        return action(text, "default", onClick);
    }

    public void close() {
        if (closing) {
            return;
        }
        closing = true;
        setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
        addAction(Actions.sequence(Actions.fadeOut(0.14f), Actions.removeActor()));
    }

    public void show(Stage stage) {
        panel.add(buttons).row();
        panel.pack();
        stage.addActor(this);
    }
}
