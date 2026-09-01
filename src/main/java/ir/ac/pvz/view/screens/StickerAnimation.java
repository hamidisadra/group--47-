package ir.ac.pvz.view.screens;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

import ir.ac.pvz.view.ui.Ui;

final class StickerAnimation {
    private static final String[] CAPTIONS = {
        "Sunflower is dancing!",
        "Chomp chomp chomp",
        "Boom!"
    };

    private static final float SHOW_SECONDS = 2.4f;

    private StickerAnimation() {
    }

    static void play(Stage ui, int index, String sender) {
        Table card = buildCard(index, sender);

        card.setPosition(940f, 120f);
        card.getColor().a = 0f;
        ui.addActor(card);
        card.addAction(animationFor(index));
    }

    private static Table buildCard(int index, String sender) {
        Table card = new Table();
        card.setBackground(Ui.panel());
        card.pad(14f);

        Label caption = Ui.label(captionFor(index));
        caption.setAlignment(Align.center);

        card.add(Ui.label(sender)).row();
        card.add(caption).width(240f);
        card.pack();

        return card;
    }

    private static String captionFor(int index) {
        if (index < 0 || index >= CAPTIONS.length) {
            return CAPTIONS[0];
        }

        return CAPTIONS[index];
    }

    private static Action animationFor(int index) {
        if (index == 1) {
            return Actions.sequence(Actions.fadeIn(0.15f), shake(),
                Actions.fadeOut(0.35f), Actions.removeActor());
        }

        if (index == 2) {
            return Actions.sequence(Actions.parallel(Actions.fadeIn(0.12f),
                Actions.scaleTo(1.35f, 1.35f, 0.25f)),
                Actions.scaleTo(1f, 1f, 0.18f),
                Actions.delay(SHOW_SECONDS * 0.4f),
                Actions.fadeOut(0.4f), Actions.removeActor());
        }

        return Actions.sequence(Actions.fadeIn(0.15f), bob(),
            Actions.fadeOut(0.35f), Actions.removeActor());
    }

    private static Action bob() {
        return Actions.repeat(4, Actions.sequence(
            Actions.moveBy(0f, 18f, 0.16f),
            Actions.moveBy(0f, -18f, 0.16f)));
    }

    private static Action shake() {
        return Actions.repeat(6, Actions.sequence(
            Actions.moveBy(14f, 0f, 0.07f),
            Actions.moveBy(-28f, 0f, 0.14f),
            Actions.moveBy(14f, 0f, 0.07f)));
    }
}
