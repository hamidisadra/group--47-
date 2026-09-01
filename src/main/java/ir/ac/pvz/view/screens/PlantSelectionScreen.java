package ir.ac.pvz.view.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import ir.ac.pvz.controller.gui.PlantCatalogController;
import ir.ac.pvz.controller.managers.ScreenId;
import ir.ac.pvz.model.chapter.Chapter;
import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.stage.LockedPlantsStage;
import ir.ac.pvz.view.PvzGame;
import ir.ac.pvz.view.ui.PlantCard;
import ir.ac.pvz.view.ui.Toast;
import ir.ac.pvz.view.ui.Ui;

import java.util.ArrayList;
import java.util.List;

public class PlantSelectionScreen extends BaseMenuScreen {
    private final int maxSelected;

    private final PlantCatalogController catalog = PlantCatalogController.getInstance();
    private final List<String> chosen = new ArrayList<>();
    private String activeType;
    private final List<String> lockedPlants = new ArrayList<>();
    private final Table grid = new Table();
    private final Table chosenRow = new Table();
    private final Table toolRow = new Table();
    private final Chapter chapter;
    private final ir.ac.pvz.model.stage.Stage levelStage;
    private final String minigame;

    public PlantSelectionScreen(PvzGame game, Chapter chapter,
                                ir.ac.pvz.model.stage.Stage levelStage,
                                String minigame) {
        super(game, "Choose Your Plants", true);
        this.chapter = chapter;
        this.levelStage = levelStage;
        this.minigame = minigame;

        maxSelected = levelStage == null
            ? ir.ac.pvz.model.stage.Stage.DEFAULT_PLANT_SLOTS : levelStage.getPlantSlots();

        if (levelStage instanceof LockedPlantsStage) {
            lockedPlants.addAll(((LockedPlantsStage) levelStage).getLockedPlants());
        }

        buildLayout();

        rebuild();
    }

    private void buildLayout() {
        chosenRow.left();

        Table chosenPanel = new Table();
        chosenPanel.setBackground(Ui.panel());
        chosenPanel.pad(10f);
        chosenPanel.add(chosenRow).height(132f).growX().row();
        chosenPanel.add(toolRow).padTop(2f);
        content.add(chosenPanel).width(1180f).padBottom(12f).row();

        grid.top();

        ScrollPane pane = new ScrollPane(grid, Ui.skin());
        pane.setFadeScrollBars(false);
        content.add(pane).width(1180f).height(268f).row();

        content.add(buildStartButton()).width(260f).height(62f).padTop(8f).row();
    }

    private com.badlogic.gdx.scenes.scene2d.ui.TextButton buildStartButton() {
        com.badlogic.gdx.scenes.scene2d.ui.TextButton rock =
            Ui.button("Let's Rock!", "green");

        rock.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                startBattle();
            }
        });

        return rock;
    }

    private void startBattle() {
        if (chosen.isEmpty()) {
            Toast.error(stage, "Pick at least one plant.");
            return;
        }

        int level = game.getPendingMinigameLevel();

        game.setPendingChapter(chapter);
        game.setPendingStage(levelStage);

        if (minigame != null) {
            game.setPendingMinigame(minigame, level);
        }

        game.startBattle(chosen);
    }

    private void rebuild() {
        grid.clear();
        chosenRow.clear();
        buildCatalogGrid();
        buildChosenRow();
        buildToolRow();
    }

    private void buildCatalogGrid() {
        int column = 0;

        for (String type : catalog.allTypes()) {
            if (!catalog.isUnlocked(type)) {
                continue;
            }

            grid.add(catalogCard(type)).pad(6f);
            column++;
            if (column % 8 == 0) {
                grid.row();
            }
        }

        if (column == 0) {
            grid.add(Ui.label("Unlock plants in the collection first.", "medium"))
                .pad(24f);
        }
    }

    private PlantCard catalogCard(String type) {
        Plant prototype = catalog.prototype(type);
        boolean stageLocked = isStageLocked(type);
        PlantCard card = new PlantCard(type, 108f);
        card.cost(prototype == null ? 0 : prototype.sunCost)
            .level(catalog.level(type))
            .boosted(catalog.isBoosted(type))
            .locked(stageLocked)
            .selected(chosen.contains(type));
        card.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (stageLocked) {
                    Toast.error(stage, type + " is locked in this level.");
                    return;
                }

                activeType = type;
                toggle(type);
            }
        });

        return card;
    }

    private void buildChosenRow() {
        for (String type : chosen) {
            chosenRow.add(chosenCard(type)).pad(4f);
        }

        for (int slot = chosen.size(); slot < maxSelected; slot++) {
            chosenRow.add(slotPlaceholder("+", 0.35f)).size(92f, 116f).pad(4f);
        }

        for (int slot = maxSelected;
             slot < ir.ac.pvz.model.stage.Stage.DEFAULT_PLANT_SLOTS; slot++) {
            chosenRow.add(slotPlaceholder("locked", 0.6f)).size(92f, 116f).pad(4f);
        }
    }

    private PlantCard chosenCard(String type) {
        Plant prototype = catalog.prototype(type);
        PlantCard card = new PlantCard(type, 92f);
        card.cost(prototype == null ? 0 : prototype.sunCost)
            .boosted(catalog.isBoosted(type));
        card.selected(type.equals(activeType));
        card.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (type.equals(activeType)) {
                    toggle(type);
                    return;
                }

                activeType = type;
                rebuild();
            }
        });

        return card;
    }

    private Table slotPlaceholder(String text, float alpha) {
        Table slot = new Table();
        slot.setBackground(Ui.solid(new com.badlogic.gdx.graphics.Color(
            0f, 0f, 0f, alpha)));
        slot.add(Ui.label(text, alpha > 0.5f ? "secondary" : "medium"));
        return slot;
    }

    private void buildToolRow() {
        Table tools = new Table();
        tools.add(toolButton("Boost " + focusLabel() + " ("
            + PlantCatalogController.BOOST_COST_GEMS + " gems)",
            () -> applyToFocus(catalog::toggleBoost, null))).pad(4f);
        tools.add(toolButton("Upgrade " + focusLabel(),
            () -> applyToFocus(catalog::upgrade, "Plant upgraded."))).pad(4f);
        toolRow.clear();
        toolRow.add(tools);
    }

    private void applyToFocus(java.util.function.Function<String, String> action,
                              String successMessage) {
        if (activePlant() == null) {
            Toast.error(stage, "Select a plant first.");
            return;
        }

        String error = action.apply(activePlant());
        if (error != null) {
            Toast.error(stage, error);
            return;
        }

        if (successMessage != null) {
            Toast.info(stage, successMessage);
        }

        rebuild();
    }

    private boolean isStageLocked(String type) {
        for (String locked : lockedPlants) {
            if (locked.equalsIgnoreCase(type)) {
                return true;
            }
        }

        return false;
    }

    private String activePlant() {
        if (activeType != null && chosen.contains(activeType)) {
            return activeType;
        }

        if (chosen.isEmpty()) {
            return null;
        }

        return chosen.get(chosen.size() - 1);
    }

    private String focusLabel() {
        String plant = activePlant();
        return plant == null ? "plant" : plant;
    }

    private Actor toolButton(String text, Runnable onClick) {
        com.badlogic.gdx.scenes.scene2d.ui.TextButton button = Ui.button(text, "purple");
        button.getLabel().setFontScale(0.75f);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                onClick.run();
            }
        });
        Table wrapper = new Table();
        wrapper.add(button).width(230f).height(52f);
        return wrapper;
    }

    private void toggle(String type) {
        if (isStageLocked(type)) {
            Toast.error(stage, type + " is locked in this level.");
            return;
        }

        if (chosen.contains(type)) {
            chosen.remove(type);

            if (type.equals(activeType)) {
                activeType = chosen.isEmpty() ? null : chosen.get(chosen.size() - 1);
            }
        }

        else if (chosen.size() >= maxSelected) {
            Toast.error(stage, "This level only allows " + maxSelected + " plants.");
            return;
        }

        else {
            chosen.add(type);
        }

        rebuild();
    }
}
