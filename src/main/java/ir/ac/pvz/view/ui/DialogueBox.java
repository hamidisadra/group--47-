package ir.ac.pvz.view.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import ir.ac.pvz.view.assets.GameAssets;

import java.util.List;

public class DialogueBox extends Table {
    public static final class Line {
        public final String speaker;
        public final String animationName;
        public final String clip;
        public final String text;

        public Line(String speaker, String animationName, String clip, String text) {
            this.speaker = speaker;
            this.animationName = animationName;
            this.clip = clip;
            this.text = text;
        }
    }

    private final List<Line> lines;
    private final Runnable onFinished;
    private final PamActor portrait;
    private final Label speakerLabel;
    private final Label textLabel;
    private int index;
    private boolean finished;

    public DialogueBox(List<Line> lines, Runnable onFinished) {
        this.lines = lines;
        this.onFinished = onFinished;

        setFillParent(true);
        setBackground(Ui.solid(new Color(0f, 0f, 0f, 0.55f)));
        setTouchable(Touchable.enabled);
        bottom();

        portrait = new PamActor(190f);
        speakerLabel = Ui.label("", "medium");
        textLabel = Ui.label("", "secondary");
        textLabel.setWrap(true);
        textLabel.setAlignment(Align.topLeft);

        Table panel = new Table();
        panel.setBackground(Ui.panel());
        panel.pad(18f);
        panel.add(portrait).size(190f).padRight(18f);

        Table body = new Table();
        body.add(speakerLabel).left().padBottom(8f).row();
        body.add(textLabel).width(660f).height(96f).left().row();
        panel.add(body).top();

        com.badlogic.gdx.scenes.scene2d.ui.TextButton next = Ui.button("Next", "green");
        next.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                advance();
            }
        });
        panel.add(next).width(150f).height(60f).padLeft(18f);

        add(panel).padBottom(60f);
        getColor().a = 0f;
        addAction(Actions.fadeIn(0.2f));
        show(0);
    }

    private void show(int position) {
        if (position >= lines.size()) {
            return;
        }
        Line line = lines.get(position);
        speakerLabel.setText(line.speaker);
        textLabel.setText(line.text);
        GameAssets assets = GameAssets.get();
        ir.ac.pvz.view.assets.AnimationIndex.Entry entry =
                assets.index().find(line.animationName, null);
        if (entry != null) {
            portrait.setAnimation(entry.path, line.clip, "anim_mediumtalk", "anim_idle",
                    "idle");
        }
    }

    private void advance() {
        if (finished) {
            return;
        }
        index++;
        if (index >= lines.size()) {
            finished = true;
            setTouchable(Touchable.disabled);
            addAction(Actions.sequence(Actions.fadeOut(0.2f), Actions.run(onFinished),
                    Actions.removeActor()));
            return;
        }
        show(index);
    }

    public void present(Stage stage) {
        stage.addActor(this);
    }
}
